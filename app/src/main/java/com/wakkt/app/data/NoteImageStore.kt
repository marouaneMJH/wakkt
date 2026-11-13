package com.wakkt.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Copies images picked for a note into private app storage, downscaled so the overlay can
 * decode them quickly and cheaply. The original picked content may disappear later; our copy stays.
 */
@Singleton
class NoteImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir: File get() = File(context.filesDir, "note_images").apply { mkdirs() }

    /** Imports [uri] for [packageName]. Returns the stored file path, or null if it can't be read. */
    suspend fun import(uri: Uri, packageName: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE_PX) sample *= 2
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null

            val rotation = resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val bitmap = if (rotation == 0f) decoded else {
                val m = android.graphics.Matrix().apply { postRotate(rotation) }
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, m, true)
                    .also { if (it !== decoded) decoded.recycle() }
            }

            val safeName = packageName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val file = File(dir, "${safeName}_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
            bitmap.recycle()
            file.absolutePath
        }.getOrNull()
    }

    /** Deletes a stored image. Ignores paths outside our folder. */
    suspend fun delete(path: String?) = withContext(Dispatchers.IO) { deleteNow(path) }

    /** Blocking variant for small cleanups where suspending is not possible. */
    fun deleteNow(path: String?) {
        if (path == null) return
        val file = File(path)
        if (file.parentFile?.canonicalPath == dir.canonicalPath) file.delete()
    }

    companion object {
        const val MAX_EDGE_PX = 1080

        /** Decodes a stored note image. Call off the main thread. */
        fun load(path: String): Bitmap? = runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
    }
}

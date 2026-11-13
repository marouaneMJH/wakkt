package com.wakkt.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.wakkt.app.alerts.NoteAlerter
import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.ui.theme.WakktTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Minimal owner so a ComposeView can live in a window not attached to an Activity. */
private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val registry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    override val viewModelStore = ViewModelStore()
    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    fun create() {
        savedStateController.performRestore(Bundle())
        registry.currentState = Lifecycle.State.CREATED
    }

    fun resume() {
        registry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        registry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}

/**
 * Two window modes:
 * - COMPACT: small draggable note, the app underneath stays usable.
 * - BACKDROP: full-screen layer that blurs/dims (or fully covers) the app, note centered.
 *   It goes away when the note is closed; for STICKY labels it stays until the user leaves the app.
 */
@Singleton
class WindowOverlayController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alerter: NoteAlerter,
) : OverlayController {

    private enum class Mode { COMPACT, BACKDROP }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var view: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null
    private var params: WindowManager.LayoutParams? = null
    private var mode: Mode? = null

    private var label by mutableStateOf<LabelEntity?>(null)
    private var onClose: () -> Unit = {}
    /** Package of the current visit. */
    private var visitPackage: String? = null

    override val isShowing: Boolean get() = view != null

    override fun show(label: LabelEntity, onClose: () -> Unit) {
        if (!Settings.canDrawOverlays(context)) return
        val newVisit = label.packageName != visitPackage
        if (newVisit) visitPackage = label.packageName
        this.label = label
        this.onClose = onClose
        render()
        // Sounds/vibration play once per visit (and repeat if configured), not on every label update.
        if (newVisit && view != null) alerter.start(label)
    }

    override fun hide() {
        alerter.stop()
        removeView()
        visitPackage = null
        label = null
    }

    private fun render() {
        val l = label ?: return
        val desired = if (l.backdrop != Backdrop.NONE) Mode.BACKDROP else Mode.COMPACT
        if (view != null && mode == desired) return // state change recomposes in place
        removeView()
        try {
            addView(desired)
        } catch (e: Exception) {
            // Permission revoked, bad window state, etc.: fail silently.
            cleanup()
        }
    }

    private fun removeView() {
        val v = view ?: return
        try {
            windowManager.removeView(v)
        } catch (_: IllegalArgumentException) {
            // Already removed.
        }
        cleanup()
    }

    private fun cleanup() {
        owner?.destroy()
        owner = null
        view = null
        params = null
        mode = null
    }

    private fun addView(target: Mode) {
        val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && windowManager.isCrossWindowBlurEnabled
        val lp = when (target) {
            Mode.COMPACT -> compactParams()
            Mode.BACKDROP -> backdropParams(label?.backdrop ?: Backdrop.HALF, blurSupported)
        }
        params = lp

        val o = OverlayLifecycleOwner().also { it.create() }
        owner = o
        val cv = ComposeView(context).apply {
            setViewTreeLifecycleOwner(o)
            setViewTreeSavedStateRegistryOwner(o)
            setViewTreeViewModelStoreOwner(o)
            setContent {
                WakktTheme {
                    val l = label ?: return@WakktTheme
                    when (target) {
                        Mode.COMPACT -> CompactNote(l)
                        Mode.BACKDROP -> BackdropNote(l, blurSupported)
                    }
                }
            }
        }
        view = cv
        mode = target
        windowManager.addView(cv, lp)
        o.resume()
    }

    @androidx.compose.runtime.Composable
    private fun CompactNote(l: LabelEntity) {
        StickyNote(
            text = l.text,
            closable = l.behavior != LabelBehavior.STICKY,
            onClose = { onClose() },
            color = l.color,
            style = l.style,
            imagePath = l.imagePath,
            effects = l.effects,
            modifier = Modifier.pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    moveBy(drag.x.toInt(), drag.y.toInt())
                }
            },
        )
    }

    @androidx.compose.runtime.Composable
    private fun BackdropNote(l: LabelEntity, blurSupported: Boolean) {
        val alpha = scrimAlpha(l.backdrop, blurSupported)
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = alpha))
                // Swallow taps so they don't reach the hidden app.
                .pointerInput(Unit) { detectTapGestures { } },
        ) {
            if (l.grain) TvStaticLayer(alpha = 0.22f, modifier = Modifier.matchParentSize())
            Column(
                Modifier.fillMaxSize().systemBarsPadding().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                StickyNote(
                    text = l.text,
                    closable = l.behavior != LabelBehavior.STICKY,
                    onClose = { onClose() },
                    color = l.color,
                    style = l.style,
                    imagePath = l.imagePath,
                    maxWidth = 320.dp,
                    effects = l.effects,
                )
            }
        }
    }

    private fun compactParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        windowAnimations = android.R.style.Animation_Toast
        val pos = lastPosition ?: defaultPosition()
        x = pos.first
        y = pos.second
    }

    private fun backdropParams(backdrop: Backdrop, blurSupported: Boolean) = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.CENTER
        windowAnimations = android.R.style.Animation_Dialog
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        if (blurSupported && backdrop != Backdrop.FULL && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags = flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            blurBehindRadius = (backdrop.strength * MAX_BLUR_DP * context.resources.displayMetrics.density).toInt()
        }
    }

    private fun moveBy(dx: Int, dy: Int) {
        if (mode != Mode.COMPACT) return
        val v = view ?: return
        val lp = params ?: return
        val dm = context.resources.displayMetrics
        val maxX = (dm.widthPixels - v.width).coerceAtLeast(0)
        val maxY = (dm.heightPixels - v.height).coerceAtLeast(0)
        lp.x = (lp.x + dx).coerceIn(0, maxX)
        lp.y = (lp.y + dy).coerceIn(0, maxY)
        lastPosition = lp.x to lp.y
        try {
            windowManager.updateViewLayout(v, lp)
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun defaultPosition(): Pair<Int, Int> {
        val res = context.resources
        val density = res.displayMetrics.density
        val screenW = res.displayMetrics.widthPixels
        val noteW = (280 * density).toInt().coerceAtMost(screenW)
        val resId = res.getIdentifier("status_bar_height", "dimen", "android")
        val statusBar = if (resId > 0) res.getDimensionPixelSize(resId) else (24 * density).toInt()
        return ((screenW - noteW) / 2).coerceAtLeast(0) to statusBar + (8 * density).toInt()
    }

    companion object {
        private const val MAX_BLUR_DP = 40f

        /** Last dragged position, kept in memory for the process lifetime. */
        @Volatile
        private var lastPosition: Pair<Int, Int>? = null

        /**
         * Opacity of the backdrop tint. With real blur the tint can be lighter, since blur already
         * hides detail; without blur (older phones, battery saver) the tint does all the hiding.
         */
        fun scrimAlpha(backdrop: Backdrop, blurSupported: Boolean): Float = when {
            backdrop == Backdrop.NONE -> 0f
            backdrop == Backdrop.FULL -> 1f
            blurSupported -> backdrop.strength * 0.6f
            else -> backdrop.strength
        }
    }
}

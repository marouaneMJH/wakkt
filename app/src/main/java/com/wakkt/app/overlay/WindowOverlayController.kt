package com.wakkt.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
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

@Singleton
class WindowOverlayController @Inject constructor(
    @ApplicationContext private val context: Context,
) : OverlayController {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var view: ComposeView? = null
    private var owner: OverlayLifecycleOwner? = null
    private var params: WindowManager.LayoutParams? = null

    private var text by mutableStateOf("")
    private var closable by mutableStateOf(false)
    private var onClose: () -> Unit = {}

    override val isShowing: Boolean get() = view != null

    override fun show(label: LabelEntity, onClose: () -> Unit) {
        if (!Settings.canDrawOverlays(context)) return
        text = label.text
        closable = label.behavior != LabelBehavior.STICKY
        this.onClose = onClose
        if (view != null) return
        try {
            addView()
        } catch (e: Exception) {
            // Permission revoked, bad window state, etc.: fail silently.
            cleanup()
        }
    }

    override fun hide() {
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
    }

    private fun addView() {
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            val pos = lastPosition ?: defaultPosition()
            x = pos.first
            y = pos.second
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
                    StickyNote(
                        text = text,
                        closable = closable,
                        onClose = { onClose() },
                        modifier = Modifier.pointerInput(Unit) {
                            detectDragGestures { change, drag ->
                                change.consume()
                                moveBy(drag.x.toInt(), drag.y.toInt())
                            }
                        },
                    )
                }
            }
        }
        view = cv
        windowManager.addView(cv, lp)
        o.resume()
    }

    private fun moveBy(dx: Int, dy: Int) {
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

    private companion object {
        /** Last dragged position, kept in memory for the process lifetime. */
        @Volatile
        var lastPosition: Pair<Int, Int>? = null
    }
}

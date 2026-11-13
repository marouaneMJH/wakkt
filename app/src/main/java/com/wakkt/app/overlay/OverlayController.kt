package com.wakkt.app.overlay

import com.wakkt.app.data.LabelEntity

/**
 * Draws the sticky note over other apps. CONTRACT; implemented by the overlay agent.
 * All calls happen on the main thread. At most one note is visible at a time.
 */
interface OverlayController {
    /** Show (or replace) the note for [label]. [onClose] is invoked when the user taps close (non-sticky only). */
    fun show(label: LabelEntity, onClose: () -> Unit)
    fun hide()
    val isShowing: Boolean
}

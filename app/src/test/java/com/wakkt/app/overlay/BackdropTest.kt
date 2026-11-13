package com.wakkt.app.overlay

import com.wakkt.app.data.Backdrop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackdropTest {
    @Test fun `none is fully transparent`() {
        assertEquals(0f, WindowOverlayController.scrimAlpha(Backdrop.NONE, blurSupported = true))
        assertEquals(0f, WindowOverlayController.scrimAlpha(Backdrop.NONE, blurSupported = false))
    }

    @Test fun `full always covers the app completely`() {
        assertEquals(1f, WindowOverlayController.scrimAlpha(Backdrop.FULL, blurSupported = true))
        assertEquals(1f, WindowOverlayController.scrimAlpha(Backdrop.FULL, blurSupported = false))
    }

    @Test fun `without blur the tint matches the chosen strength`() {
        assertEquals(0.5f, WindowOverlayController.scrimAlpha(Backdrop.HALF, blurSupported = false))
        assertEquals(0.8f, WindowOverlayController.scrimAlpha(Backdrop.STRONG, blurSupported = false))
    }

    @Test fun `with blur the tint is lighter but still ordered`() {
        val half = WindowOverlayController.scrimAlpha(Backdrop.HALF, blurSupported = true)
        val strong = WindowOverlayController.scrimAlpha(Backdrop.STRONG, blurSupported = true)
        assertTrue(half < 0.5f && strong < 0.8f && half < strong)
    }
}

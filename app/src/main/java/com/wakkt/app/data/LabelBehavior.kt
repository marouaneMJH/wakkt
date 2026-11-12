package com.wakkt.app.data

/** How a label behaves once shown over its app. */
enum class LabelBehavior {
    STICKY,             // pinned for the whole session, cannot be closed
    CLOSABLE_PERIOD,    // can be closed, comes back after a chosen period
    CLOSABLE_FOREVER,   // can be closed, never shows again unless re-enabled
}

package com.wakkt.app.ui.navigation

/** Navigation routes. CONTRACT; implemented by the UI agent. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val APP_LIST = "apps"
    const val EDITOR = "editor/{packageName}"
    const val SETTINGS = "settings"
    fun editor(packageName: String) = "editor/$packageName"
}

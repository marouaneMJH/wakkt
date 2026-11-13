package com.wakkt.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakkt.app.data.LabelRepository
import com.wakkt.app.ui.applist.AppRow
import com.wakkt.app.ui.applist.InstalledApp
import com.wakkt.app.ui.applist.InstalledAppsSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeState(
    val loading: Boolean = true,
    val rows: List<AppRow> = emptyList(),
)

/** Home: only the apps that have a note. Loads just those apps, not the whole app list. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val source: InstalledAppsSource,
    private val labels: LabelRepository,
) : ViewModel() {

    val state: StateFlow<HomeState> = labels.observeAll()
        .map { list ->
            val rows = list.map { label ->
                // An app that was uninstalled keeps its note; show the package name instead.
                val app = source.get(label.packageName)
                    ?: InstalledApp(label.packageName, label.packageName, icon = null, installed = false)
                AppRow(app, label)
            }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.app.name })
            HomeState(loading = false, rows = rows)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    fun setEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch { labels.setEnabled(packageName, enabled) }
    }
}

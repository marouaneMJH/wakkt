package com.wakkt.app.ui.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.LabelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppRow(val app: InstalledApp, val label: LabelEntity?)

data class AppListState(
    val loading: Boolean = true,
    val query: String = "",
    val rows: List<AppRow> = emptyList(),
)

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val source: InstalledAppsSource,
    labels: LabelRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val apps = MutableStateFlow<List<InstalledApp>?>(null)

    val state: StateFlow<AppListState> = combine(apps, labels.observeAll(), query) { apps, labels, q ->
        if (apps == null) return@combine AppListState(loading = true, query = q)
        val byPkg = labels.associateBy { it.packageName }
        val rows = apps
            .filter { q.isBlank() || it.name.contains(q.trim(), ignoreCase = true) }
            .map { AppRow(it, byPkg[it.packageName]) }
            .sortedWith(
                compareBy<AppRow> { it.label == null }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.app.name },
            )
        AppListState(loading = false, query = q, rows = rows)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppListState())

    init {
        viewModelScope.launch { apps.value = source.load() }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }
}

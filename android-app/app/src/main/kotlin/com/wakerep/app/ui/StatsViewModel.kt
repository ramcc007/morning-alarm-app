package com.wakerep.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wakerep.app.data.CompletionRepository
import com.wakerep.app.model.CompletionRecord
import com.wakerep.app.model.CompletionStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Backs both the Streaks/Stats screen and the Achievements screen off the same history. */
class StatsViewModel(repository: CompletionRepository) : ViewModel() {

    val records: StateFlow<List<CompletionRecord>> = repository.completions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val stats: StateFlow<CompletionStats> = repository.completions
        .map { CompletionStats.from(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompletionStats.EMPTY)

    class Factory(private val repository: CompletionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StatsViewModel(repository) as T
    }
}

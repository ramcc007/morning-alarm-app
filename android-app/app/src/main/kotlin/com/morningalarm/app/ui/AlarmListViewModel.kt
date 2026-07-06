package com.morningalarm.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.morningalarm.app.alarm.AlarmScheduler
import com.morningalarm.app.data.AlarmRepository
import com.morningalarm.app.model.Alarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmListViewModel(
    private val repository: AlarmRepository,
    context: Context,
) : ViewModel() {

    private val scheduler = AlarmScheduler(context.applicationContext)

    val alarms: StateFlow<List<Alarm>> = repository.alarms.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun add(alarm: Alarm) = viewModelScope.launch {
        repository.upsert(alarm)
        scheduler.schedule(alarm)
    }

    fun update(alarm: Alarm) = viewModelScope.launch {
        repository.upsert(alarm)
        scheduler.schedule(alarm)
    }

    fun delete(alarm: Alarm) = viewModelScope.launch {
        scheduler.cancel(alarm)
        repository.delete(alarm.id)
    }

    fun setEnabled(alarm: Alarm, enabled: Boolean) = viewModelScope.launch {
        repository.setEnabled(alarm.id, enabled)
        val updated = alarm.copy(isEnabled = enabled)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
    }

    class Factory(private val repository: AlarmRepository, private val context: Context) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlarmListViewModel(repository, context) as T
    }
}

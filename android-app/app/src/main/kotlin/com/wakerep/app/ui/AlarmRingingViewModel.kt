package com.wakerep.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wakerep.app.WakerepApplication
import com.wakerep.app.camera.JumpingJackRepDetector
import com.wakerep.app.camera.PoseCameraController
import com.wakerep.app.camera.PushupRepDetector
import com.wakerep.app.camera.RepDetector
import com.wakerep.app.camera.SquatRepDetector
import com.wakerep.app.model.Alarm
import com.wakerep.app.model.ExerciseType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmRingingViewModel(val alarm: Alarm, context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val application = appContext as WakerepApplication
    private val settingsRepository = application.settingsRepository
    private val completionRepository = application.completionRepository

    val cameraController = PoseCameraController(appContext)

    private val detector: RepDetector = when (alarm.exercise) {
        ExerciseType.PUSHUPS -> PushupRepDetector()
        ExerciseType.SQUATS -> SquatRepDetector()
        ExerciseType.JUMPING_JACKS -> JumpingJackRepDetector()
    }

    private val _repCount = MutableStateFlow(0)
    val repCount: StateFlow<Int> = _repCount

    private val _liveExtension = MutableStateFlow(0f)

    /**
     * 0f..1f - how much of the horizon should be lit right now. Combines
     * banked (fully completed) reps with the live in-progress extension of
     * the current rep, so the sun's height tracks the user's real-time body
     * position rather than only jumping once per completed rep. Optionally
     * reduced by [momentumDecay] while stalled, if Momentum mode is on.
     */
    private val _displayedFraction = MutableStateFlow(0f)
    val displayedFraction: StateFlow<Float> = _displayedFraction

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete

    private val _guidance = MutableStateFlow<String?>(null)
    val guidance: StateFlow<String?> = _guidance

    private val _isBodyDetected = MutableStateFlow(false)
    val isBodyDetected: StateFlow<Boolean> = _isBodyDetected

    /** Emits once per confirmed rep - the ringing screen fires a haptic tick off this. */
    private val _repTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val repTick: SharedFlow<Unit> = _repTick

    val targetReps: Int get() = alarm.repTarget

    private var momentumModeEnabled = false
    private var lastProgressAt = System.currentTimeMillis()
    private var momentumDecay = 0f
    private var previousLiveExtension = 0f

    init {
        cameraController.activeDetector = detector
        cameraController.onBodyDetectedChanged = { detected -> _isBodyDetected.value = detected }

        detector.onRepCompleted = { count ->
            _repCount.value = count
            registerProgress()
            viewModelScope.launch { _repTick.emit(Unit) }
            if (count >= targetReps && !_isComplete.value) {
                _isComplete.value = true
                val usedSnooze = application.snoozedAlarmIds.remove(alarm.id)
                viewModelScope.launch {
                    completionRepository.recordCompletion(alarm.exercise, count, usedSnooze)
                }
            }
        }

        detector.onLiveUpdate = {
            val live = detector.liveExtension
            if (live > previousLiveExtension + 0.05f) registerProgress()
            previousLiveExtension = live
            _liveExtension.value = live
            _guidance.value = detector.guidance
            recomputeDisplayedFraction()
        }

        viewModelScope.launch {
            momentumModeEnabled = settingsRepository.momentumModeEnabled.first()
        }

        // Momentum mode (off by default): earned light drains slowly if the
        // user stalls, purely as a visual/motivational pressure - it never
        // touches repCount or completion logic.
        viewModelScope.launch {
            while (true) {
                delay(200)
                if (momentumModeEnabled && !_isComplete.value) {
                    val stalledMs = System.currentTimeMillis() - lastProgressAt
                    if (stalledMs > 5000) {
                        momentumDecay = (momentumDecay + 0.004f).coerceAtMost(1f)
                        recomputeDisplayedFraction()
                    }
                }
            }
        }
    }

    private fun registerProgress() {
        lastProgressAt = System.currentTimeMillis()
        momentumDecay = 0f
    }

    private fun recomputeDisplayedFraction() {
        val target = targetReps.coerceAtLeast(1)
        val banked = _repCount.value.toFloat() / target
        val live = _liveExtension.value / target
        _displayedFraction.value = (banked + live - momentumDecay).coerceIn(0f, 1f)
    }

    override fun onCleared() {
        cameraController.shutdown()
        super.onCleared()
    }

    class Factory(private val alarm: Alarm, private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AlarmRingingViewModel(alarm, context) as T
    }
}

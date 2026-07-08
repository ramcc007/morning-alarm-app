package com.wakerep.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wakerep.app.camera.PoseCameraController
import com.wakerep.app.camera.PushupRepDetector
import com.wakerep.app.camera.RepDetector
import com.wakerep.app.model.Alarm
import com.wakerep.app.model.ExerciseType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmRingingViewModel(val alarm: Alarm, context: Context) : ViewModel() {

    val cameraController = PoseCameraController(context.applicationContext)

    private val detector: RepDetector = when (alarm.exercise) {
        ExerciseType.PUSHUPS -> PushupRepDetector()
    }

    private val _repCount = MutableStateFlow(0)
    val repCount: StateFlow<Int> = _repCount

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete

    private val _guidance = MutableStateFlow<String?>(null)
    val guidance: StateFlow<String?> = _guidance

    private val _isBodyDetected = MutableStateFlow(false)
    val isBodyDetected: StateFlow<Boolean> = _isBodyDetected

    val targetReps: Int get() = alarm.repTarget

    init {
        cameraController.activeDetector = detector
        cameraController.onBodyDetectedChanged = { detected -> _isBodyDetected.value = detected }
        detector.onRepCompleted = { count ->
            _repCount.value = count
            _guidance.value = detector.guidance
            if (count >= targetReps) _isComplete.value = true
        }
        // Poll guidance on a light cadence too, in case it changes without a rep completing
        // (e.g. the user steps out of frame between reps).
        viewModelScope.launch {
            while (true) {
                _guidance.value = detector.guidance
                kotlinx.coroutines.delay(400)
            }
        }
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

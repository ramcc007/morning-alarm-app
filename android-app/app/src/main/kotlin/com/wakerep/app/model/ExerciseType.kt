package com.wakerep.app.model

import kotlinx.serialization.Serializable

/**
 * A physical exercise the app can validate via the camera before an alarm can be dismissed.
 *
 * New exercises plug in by adding a case here and a matching `RepDetector`
 * implementation (see `PushupRepDetector` for the reference implementation).
 */
@Serializable
enum class ExerciseType {
    PUSHUPS;

    val displayName: String
        get() = when (this) {
            PUSHUPS -> "Push-ups"
        }

    val instructions: String
        get() = when (this) {
            PUSHUPS -> "Prop your phone up so the front camera can see your full upper body, " +
                "then start doing push-ups. The alarm stops once you've completed your target " +
                "reps with good form."
        }

    val defaultTarget: Int
        get() = when (this) {
            PUSHUPS -> 3
        }

    val targetRange: IntRange
        get() = when (this) {
            PUSHUPS -> 1..50
        }
}

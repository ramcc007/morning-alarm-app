package com.wakerep.app.model

import kotlinx.serialization.Serializable

/**
 * A physical exercise the app can validate via the camera before an alarm can be dismissed.
 *
 * New exercises plug in by adding a case here and a matching `RepDetector`
 * implementation (see `PushupRepDetector`/`SquatRepDetector` for the
 * joint-angle reference, or `JumpingJackRepDetector` for a motion that
 * isn't a single-joint angle).
 */
@Serializable
enum class ExerciseType {
    PUSHUPS, SQUATS, JUMPING_JACKS;

    val displayName: String
        get() = when (this) {
            PUSHUPS -> "Push-ups"
            SQUATS -> "Squats"
            JUMPING_JACKS -> "Jumping jacks"
        }

    val instructions: String
        get() = when (this) {
            PUSHUPS -> "Prop your phone up so the front camera can see your full upper body, " +
                "then start doing push-ups. The alarm stops once you've completed your target " +
                "reps with good form."
            SQUATS -> "Prop your phone up so the front camera can see your full body from the " +
                "waist down, then start doing squats. The alarm stops once you've completed " +
                "your target reps with full depth."
            JUMPING_JACKS -> "Prop your phone up so the front camera can see your whole body, " +
                "arms and legs included, then start doing jumping jacks. The alarm stops once " +
                "you've completed your target reps."
        }

    val defaultTarget: Int
        get() = when (this) {
            PUSHUPS -> 3
            SQUATS -> 10
            JUMPING_JACKS -> 15
        }

    val targetRange: IntRange
        get() = when (this) {
            PUSHUPS -> 1..50
            SQUATS -> 1..100
            JUMPING_JACKS -> 1..100
        }
}

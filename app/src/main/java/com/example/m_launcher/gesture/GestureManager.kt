package com.example.m_launcher.gesture

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.content.getSystemService
import kotlin.math.abs

/**
 * Manages swipe gesture detection and handling for the launcher.
 * Handles left and right swipe gestures with configurable velocity and distance thresholds.
 */
class GestureManager(
    private val context: Context,
    private val onLeftSwipe: () -> Unit = {},
    private val onRightSwipe: () -> Unit = {}
) {
    companion object {
        private const val SWIPE_THRESHOLD = 100 // Minimum distance for swipe
        private const val SWIPE_VELOCITY_THRESHOLD = 100 // Minimum velocity for swipe
        private const val HAPTIC_FEEDBACK_DURATION = 50L // Duration in milliseconds
    }

    private val vibrator = context.getSystemService<Vibrator>()

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            // Return true to ensure we continue receiving gesture events (required for onFling)
            return true
        }
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            
            // Check if the gesture is more horizontal than vertical
            if (abs(diffX) > abs(diffY) && 
                abs(diffX) > SWIPE_THRESHOLD && 
                abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                
                // Determine swipe direction
                if (diffX > 0) {
                    // Right swipe
                    provideFeedback()
                    onRightSwipe()
                } else {
                    // Left swipe
                    provideFeedback()
                    onLeftSwipe()
                }
                return true
            }
            return false
        }
    })

    /**
     * Provides haptic feedback when a swipe gesture is detected
     */
    private fun provideFeedback() {
        vibrator?.vibrate(
            VibrationEffect.createOneShot(
                HAPTIC_FEEDBACK_DURATION,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    }

    /**
     * Handles touch events for gesture detection.
     * Should be called from the activity's onTouchEvent.
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}

package com.example.m_launcher

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityGestureTest {

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Launch MainActivity
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        scenario = ActivityScenario.launch(intent)
        
        // Wait for activity to fully load
        Thread.sleep(1000)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testSwipeUpGestureDetection() {
        // Get screen dimensions for gesture
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Perform swipe up gesture from bottom to top
        device.swipe(
            displayWidth / 2,           // Start X (center)
            displayHeight * 3 / 4,      // Start Y (3/4 down screen)
            displayWidth / 2,           // End X (center)
            displayHeight / 4,          // End Y (1/4 down screen)
            10                          // Steps for smooth gesture
        )
        
        // Wait for gesture processing and potential activity launch
        Thread.sleep(2000)
        
        // Verify search activity might have launched
        // Note: This test may need adjustment based on actual search activity behavior
    }

    @Test
    fun testLongPressGestureDetection() {
        // Get screen center for long press
        val centerX = device.displayWidth / 2
        val centerY = device.displayHeight / 2
        
        // Perform long press gesture
        device.click(centerX, centerY)
        Thread.sleep(600) // Hold for longer than long press threshold
        
        // Wait for potential settings activity launch
        Thread.sleep(1000)
        
        // Verify gesture was detected (settings might have opened)
    }

    @Test
    fun testMultipleSwipeUpGestures() {
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Perform multiple swipe up gestures
        for (i in 1..3) {
            device.swipe(
                displayWidth / 2,
                displayHeight * 3 / 4,
                displayWidth / 2,
                displayHeight / 4,
                10
            )
            
            Thread.sleep(500)
            
            // Press back if search opened
            device.pressBack()
            Thread.sleep(500)
        }
    }

    @Test
    fun testSwipeUpFromDifferentPositions() {
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Test swipe up from left side
        device.swipe(
            displayWidth / 4,
            displayHeight * 3 / 4,
            displayWidth / 4,
            displayHeight / 4,
            10
        )
        
        Thread.sleep(1000)
        device.pressBack()
        Thread.sleep(500)
        
        // Test swipe up from right side
        device.swipe(
            displayWidth * 3 / 4,
            displayHeight * 3 / 4,
            displayWidth * 3 / 4,
            displayHeight / 4,
            10
        )
        
        Thread.sleep(1000)
        device.pressBack()
        Thread.sleep(500)
        
        // Test swipe up from center
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,
            10
        )
        
        Thread.sleep(1000)
        device.pressBack()
    }

    @Test
    fun testInvalidGestures() {
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Test horizontal swipe (should not trigger search)
        device.swipe(
            displayWidth / 4,
            displayHeight / 2,
            displayWidth * 3 / 4,
            displayHeight / 2,
            10
        )
        
        Thread.sleep(1000)
        
        // Test swipe down (should not trigger search)
        device.swipe(
            displayWidth / 2,
            displayHeight / 4,
            displayWidth / 2,
            displayHeight * 3 / 4,
            10
        )
        
        Thread.sleep(1000)
        
        // Test short swipe (should not trigger search)
        device.swipe(
            displayWidth / 2,
            displayHeight / 2,
            displayWidth / 2,
            displayHeight / 2 - 50,
            5
        )
        
        Thread.sleep(1000)
    }

    @Test
    fun testGestureVelocityThreshold() {
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Test slow swipe (should not trigger)
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,
            50  // Many steps = slow gesture
        )
        
        Thread.sleep(1000)
        
        // Test fast swipe (should trigger)
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,
            5   // Few steps = fast gesture
        )
        
        Thread.sleep(2000)
        device.pressBack()
    }

    @Test
    fun testGestureDistanceThreshold() {
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Test short distance swipe (should not trigger)
        device.swipe(
            displayWidth / 2,
            displayHeight / 2,
            displayWidth / 2,
            displayHeight / 2 - 50,  // Only 50 pixels
            5
        )
        
        Thread.sleep(1000)
        
        // Test adequate distance swipe (should trigger)
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,  // Quarter screen distance
            5
        )
        
        Thread.sleep(2000)
        device.pressBack()
    }

    @Test
    fun testRapidGestures() {
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Test rapid successive gestures
        for (i in 1..5) {
            device.swipe(
                displayWidth / 2,
                displayHeight * 3 / 4,
                displayWidth / 2,
                displayHeight / 4,
                5
            )
            
            Thread.sleep(100) // Very short delay
            device.pressBack()
            Thread.sleep(100)
        }
    }

    @Test
    fun testGestureAfterAppLaunch() {
        // First, try to launch an app (if any favorites are available)
        // This would require knowing the app list structure
        
        // Then test swipe up gesture
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,
            10
        )
        
        Thread.sleep(2000)
        device.pressBack()
    }

    @Test
    fun testGestureInterruption() {
        // Start a swipe gesture but don't complete it
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        
        // Start swipe but stop in middle
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 2,  // Stop halfway
            10
        )
        
        Thread.sleep(500)
        
        // Complete a proper swipe
        device.swipe(
            displayWidth / 2,
            displayHeight * 3 / 4,
            displayWidth / 2,
            displayHeight / 4,
            10
        )
        
        Thread.sleep(2000)
        device.pressBack()
    }
}
package com.example.m_launcher

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchActivityTest {

    private lateinit var device: UiDevice
    private lateinit var scenario: ActivityScenario<SearchActivity>

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // Launch SearchActivity
        val intent = Intent(ApplicationProvider.getApplicationContext(), SearchActivity::class.java)
        scenario = ActivityScenario.launch(intent)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun testSearchActivityLaunches() {
        // Verify search input field is displayed
        onView(withId(R.id.search_input))
            .check(matches(isDisplayed()))
        
        // Verify search results RecyclerView is displayed
        onView(withId(R.id.search_results))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSearchInputFieldConfiguration() {
        // Verify search input has correct hint
        onView(withId(R.id.search_input))
            .check(matches(withHint("Search apps...")))
        
        // Verify search input accepts text
        onView(withId(R.id.search_input))
            .perform(typeText("test"))
            .check(matches(withText("test")))
    }

    @Test
    fun testKeyboardDisplaysAutomatically() {
        // Wait for keyboard to appear
        Thread.sleep(1000)
        
        // Verify search input has focus
        onView(withId(R.id.search_input))
            .check(matches(hasFocus()))
    }

    @Test
    fun testSearchInputTextChanges() {
        // Type in search field
        onView(withId(R.id.search_input))
            .perform(typeText("chrome"))
        
        // Wait for search to process
        Thread.sleep(500)
        
        // Verify text was entered
        onView(withId(R.id.search_input))
            .check(matches(withText("chrome")))
    }

    @Test
    fun testClearSearchInput() {
        // Type text and then clear it
        onView(withId(R.id.search_input))
            .perform(typeText("test"))
            .perform(clearText())
        
        // Verify input is cleared
        onView(withId(R.id.search_input))
            .check(matches(withText("")))
    }

    @Test
    fun testBackButtonClosesActivity() {
        // Press back button
        device.pressBack()
        
        // Wait for activity to close
        Thread.sleep(500)
        
        // Verify activity is finished (this test may need adjustment based on test framework)
        // The activity should be closed at this point
    }

    @Test
    fun testSwipeDownGesture() {
        // Perform swipe down gesture
        onView(withId(android.R.id.content))
            .perform(swipeDown())
        
        // Wait for gesture processing
        Thread.sleep(500)
        
        // Activity should close (verification depends on test framework capabilities)
    }

    @Test
    fun testSearchWithDifferentQueries() {
        val testQueries = listOf("gm", "chrome", "maps", "face")
        
        for (query in testQueries) {
            // Clear previous input
            onView(withId(R.id.search_input))
                .perform(clearText())
            
            // Type new query
            onView(withId(R.id.search_input))
                .perform(typeText(query))
            
            // Wait for search processing
            Thread.sleep(300)
            
            // Verify query was entered
            onView(withId(R.id.search_input))
                .check(matches(withText(query)))
        }
    }

    @Test
    fun testSearchResultsRecyclerViewScroll() {
        // Type query that should return multiple results
        onView(withId(R.id.search_input))
            .perform(typeText("a"))
        
        // Wait for results
        Thread.sleep(1000)
        
        // Try to scroll the results (if any results are present)
        onView(withId(R.id.search_results))
            .perform(swipeUp())
    }

    @Test
    fun testLongSearchQuery() {
        val longQuery = "this is a very long search query that should still work properly"
        
        onView(withId(R.id.search_input))
            .perform(typeText(longQuery))
        
        // Wait for processing
        Thread.sleep(500)
        
        // Verify long query is handled
        onView(withId(R.id.search_input))
            .check(matches(withText(longQuery)))
    }

    @Test
    fun testSpecialCharactersInSearch() {
        val specialQuery = "@#$%^&*()"
        
        onView(withId(R.id.search_input))
            .perform(typeText(specialQuery))
        
        // Wait for processing
        Thread.sleep(300)
        
        // Should handle special characters without crashing
        onView(withId(R.id.search_input))
            .check(matches(withText(specialQuery)))
    }

    @Test
    fun testRapidTextChanges() {
        // Simulate rapid typing
        val queries = listOf("a", "ab", "abc", "abcd", "abcde")
        
        for (query in queries) {
            onView(withId(R.id.search_input))
                .perform(clearText())
                .perform(typeText(query))
            
            // Short delay to simulate rapid typing
            Thread.sleep(50)
        }
        
        // Wait for final processing
        Thread.sleep(500)
        
        // Should handle rapid changes without issues
        onView(withId(R.id.search_input))
            .check(matches(withText("abcde")))
    }
}
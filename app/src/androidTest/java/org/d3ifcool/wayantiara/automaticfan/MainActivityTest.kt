package org.d3ifcool.wayantiara.automaticfan

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testBluetooth() {

        // Example: Click a button to navigate to the page
        onView(withId(R.id.buttonConnect)).perform(click())

        // Check if the page is displayed
        onView(withId(R.id.layout_select_devices)).check(matches(isDisplayed()))

        onView(withText("BTFan")).perform(click())

        Thread.sleep(5000)

        onView(withId(R.id.buttonToggle)).perform(click())

        Thread.sleep(2000)

        // Test auto
        onView(withId(R.id.switch_otomatis)).perform(click())

        // Check if the page is displayed
        onView(withId(R.id.buttonToggle)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testPageHistoryDisplayed() {

        // Example: Click a button to navigate to the page
        onView(withId(R.id.buttonHistori)).perform(click())

        // Check if the page is displayed
        onView(withId(R.id.layout_history)).check(matches(isDisplayed()))
    }
}
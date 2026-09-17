package com.kmptemplate.app

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestorationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun queryAndDestinationSurviveActivityRecreation() {
        compose.onNodeWithTag("example_search").performTextInput("Sample 2")
        Espresso.closeSoftKeyboard()
        compose.onNodeWithTag("example_item_2").performClick()
        compose.onNodeWithTag("example_detail").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("example_detail").assertIsDisplayed()
        compose.onNodeWithTag("detail_back").performClick()
        compose.onNodeWithTag("example_search").assertTextContains("Sample 2")
        compose.onNodeWithTag("example_item_2").assertIsDisplayed()
        compose.onNodeWithTag("example_item_1").assertDoesNotExist()
    }

    @Test fun phoneRotationPreservesQuery() {
        compose.onNodeWithTag("example_search").performTextInput("Sample 1")
        Espresso.closeSoftKeyboard()
        val target = if (compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val expected = if (target == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
        compose.activityRule.scenario.onActivity { it.requestedOrientation = target }
        try {
            compose.waitUntil(10_000) { compose.activity.resources.configuration.orientation == expected }
            compose.onNodeWithTag("example_search").assertTextContains("Sample 1")
        } finally {
            compose.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
        }
    }

    @Test fun searchAndFirstRowRemainInsideSafeDrawingArea() {
        val insets = androidx.core.view.ViewCompat.getRootWindowInsets(compose.activity.window.decorView)!!
            .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars() or androidx.core.view.WindowInsetsCompat.Type.displayCutout())
        val search = compose.onNodeWithTag("example_search").fetchSemanticsNode().boundsInWindow
        org.junit.Assert.assertTrue(search.top >= insets.top)
        org.junit.Assert.assertTrue(search.left >= insets.left)
        val width = compose.activity.window.decorView.width
        org.junit.Assert.assertTrue(search.right <= width - insets.right)
        compose.onNodeWithTag("example_item_1").assertIsDisplayed()
    }
}

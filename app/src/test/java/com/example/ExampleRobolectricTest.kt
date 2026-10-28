package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.DigitalSaathiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Digital Saathi", appName)
  }

  @Test
  fun unconfigured_app_does_not_bypass_authentication() {
    // Never use a developer's real Firebase project from a unit test.
    com.google.firebase.FirebaseApp.getApps(ApplicationProvider.getApplicationContext<Context>()).forEach { it.delete() }
    composeTestRule.setContent {
      DigitalSaathiTheme {
        DigitalSaathiApp(voiceHelper = null)
      }
    }

    // On fresh app launch / install, user sees the AuthScreen with Sign-in & Google options
    composeTestRule.onNodeWithTag("login_username_input").assertExists()
    composeTestRule.onNodeWithTag("login_google_button").assertExists()
    composeTestRule.onNodeWithTag("demo_access_button").assertExists()

    // Perform Login
    composeTestRule.onNodeWithTag("demo_access_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Without Firebase configuration a guest click must not grant dashboard access.
    composeTestRule.onNodeWithTag("login_username_input").assertExists()
    composeTestRule.onNodeWithTag("digital_sathi_bottom_nav", useUnmergedTree = true).assertDoesNotExist()
  }
}

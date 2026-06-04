package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingNavigationControlsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shouldDisplayBothButtons_andInvokeCallbacksOnClick() {
        var backClicks = 0
        var nextClicks = 0

        composeRule.setContent {
            OnboardingNavigationControls(
                onBackClicked = { backClicks++ },
                onNextClicked = { nextClicks++ },
                backText = "Back",
                nextText = "Next"
            )
        }

        composeRule.onNodeWithText("Back").assertIsDisplayed().assertHasClickAction()
        composeRule.onNodeWithText("Next").assertIsDisplayed().assertHasClickAction()

        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Back").performClick()

        assertEquals(1, backClicks)
        assertEquals(1, nextClicks)
    }

    @Test
    fun shouldHideNextButton_whenHideNextButtonIsTrue() {
        composeRule.setContent {
            OnboardingNavigationControls(
                hideNextButton = true,
                backText = "Back",
                nextText = "Next"
            )
        }

        composeRule.onNodeWithText("Back").assertIsDisplayed()
        composeRule.onNodeWithText("Next").assertDoesNotExist()
    }
}

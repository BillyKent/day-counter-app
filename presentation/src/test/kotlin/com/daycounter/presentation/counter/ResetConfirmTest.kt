package com.daycounter.presentation.counter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.daycounter.presentation.theme.DayCounterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class ResetConfirmTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `shows the archive warning and both actions`() {
        compose.setContent { DayCounterTheme { ResetConfirmContent(onConfirm = {}, onCancel = {}) } }
        compose.onNodeWithTag("reset_confirm_sheet").assertIsDisplayed()
        compose.onNodeWithTag("reset_confirm").assertIsDisplayed()
        compose.onNodeWithTag("reset_cancel").assertIsDisplayed()
    }

    @Test
    fun `confirm and cancel invoke their callbacks`() {
        var confirmed = false
        var cancelled = false
        compose.setContent {
            DayCounterTheme { ResetConfirmContent(onConfirm = { confirmed = true }, onCancel = { cancelled = true }) }
        }
        compose.onNodeWithTag("reset_confirm").performClick()
        compose.onNodeWithTag("reset_cancel").performClick()
        assertEquals(true, confirmed)
        assertEquals(true, cancelled)
    }
}

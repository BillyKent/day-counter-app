package com.daycounter.presentation.celebration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.daycounter.presentation.R
import com.daycounter.presentation.theme.DayCounterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class MilestoneCelebrationTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `shows milestone number, name and message`() {
        compose.setContent {
            DayCounterTheme {
                CelebrationContent(
                    state = CelebrationUiState(milestone = 7, counterName = "Read daily", messageRes = R.string.celebration_message_7),
                    onClose = {},
                    onShare = {},
                )
            }
        }
        compose.onNodeWithTag("celebration_milestone").assertIsDisplayed()
        compose.onNodeWithText("7").assertIsDisplayed()
        compose.onNodeWithText("Read daily").assertIsDisplayed()
        compose.onNodeWithTag("celebration_message").assertIsDisplayed()
    }

    @Test
    fun `shows share action and invokes onShare`() {
        var shared = false
        compose.setContent {
            DayCounterTheme {
                CelebrationContent(
                    state = CelebrationUiState(milestone = 100, counterName = "Run", messageRes = R.string.celebration_message_100),
                    onClose = {},
                    onShare = { shared = true },
                )
            }
        }
        compose.onNodeWithTag("celebration_share").assertIsDisplayed()
        compose.onNodeWithTag("celebration_share").performClick()
        assertEquals(true, shared)
    }

    @Test
    fun `close and keep-going both invoke onClose`() {
        var closeCount = 0
        compose.setContent {
            DayCounterTheme {
                CelebrationContent(
                    state = CelebrationUiState(milestone = 30, counterName = "Run", messageRes = R.string.celebration_message_30),
                    onClose = { closeCount++ },
                    onShare = {},
                )
            }
        }
        compose.onNodeWithTag("celebration_close").performClick()
        compose.onNodeWithTag("celebration_keep_going").performClick()
        assertEquals(2, closeCount)
    }
}

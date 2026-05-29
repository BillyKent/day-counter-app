package com.daycounter.presentation.counter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.daycounter.presentation.theme.DayCounterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class CreateEditSheetTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `create form exposes the four fields and goal-target choices`() {
        compose.setContent {
            DayCounterTheme {
                CounterFormContent(
                    title = "New counter",
                    name = "Dejar de fumar",
                    category = "Salud",
                    startDateText = "May 27, 2026",
                    goalTarget = 30,
                    canSave = true,
                    isSaving = false,
                    nameError = null,
                    categoryError = null,
                    onNameChange = {},
                    onCategoryChange = {},
                    onGoalTargetChange = {},
                    onSave = {},
                    onCancel = {},
                    onStartDateClick = {},
                )
            }
        }
        compose.onNodeWithTag("form_name").assertIsDisplayed()
        compose.onNodeWithTag("form_category").assertIsDisplayed()
        compose.onNodeWithTag("form_date").assertIsDisplayed()
        // The four allowed goal targets are offered as chips.
        listOf(7, 30, 100, 365).forEach { compose.onNodeWithTag("form_goal_chip_$it").performScrollTo().assertIsDisplayed() }
        compose.onNodeWithTag("form_save").performScrollTo().assertIsEnabled()
    }

    @Test
    fun `goal-target chip selection invokes callback`() {
        var selected: Int? = null
        compose.setContent {
            DayCounterTheme {
                CounterFormContent(
                    title = "New counter", name = "Run", category = "", startDateText = "today",
                    goalTarget = 30, canSave = true, isSaving = false, nameError = null, categoryError = null,
                    onNameChange = {}, onCategoryChange = {}, onGoalTargetChange = { selected = it },
                    onSave = {}, onCancel = {}, onStartDateClick = {},
                )
            }
        }
        compose.onNodeWithTag("form_goal_chip_100").performScrollTo().performClick()
        assertEquals(100, selected)
    }

    @Test
    fun `edit form shows read-only date with the reiniciar hint and no picker`() {
        compose.setContent {
            DayCounterTheme {
                CounterFormContent(
                    title = "Edit counter", name = "Read", category = "", startDateText = "May 1, 2026",
                    goalTarget = 30, canSave = true, isSaving = false, nameError = null, categoryError = null,
                    onNameChange = {}, onCategoryChange = {}, onGoalTargetChange = {},
                    onSave = {}, onCancel = {},
                    onStartDateClick = null,
                )
            }
        }
        compose.onNodeWithTag("form_date_hint").assertIsDisplayed()
    }

    @Test
    fun `save disabled when canSave is false`() {
        compose.setContent {
            DayCounterTheme {
                CounterFormContent(
                    title = "New counter", name = "", category = "", startDateText = "today",
                    goalTarget = 30, canSave = false, isSaving = false, nameError = null, categoryError = null,
                    onNameChange = {}, onCategoryChange = {}, onGoalTargetChange = {},
                    onSave = {}, onCancel = {}, onStartDateClick = {},
                )
            }
        }
        compose.onNodeWithTag("form_save").performScrollTo().assertIsNotEnabled()
    }
}

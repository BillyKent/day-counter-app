package com.daycounter.presentation.theme

import com.daycounter.domain.model.AppearanceMode
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceTest {

    @Test
    fun `system follows the device flag`() {
        assertEquals(true, AppearanceMode.SYSTEM.resolveDarkTheme(systemInDark = true))
        assertEquals(false, AppearanceMode.SYSTEM.resolveDarkTheme(systemInDark = false))
    }

    @Test
    fun `light and dark override the device flag`() {
        assertEquals(false, AppearanceMode.LIGHT.resolveDarkTheme(systemInDark = true))
        assertEquals(true, AppearanceMode.DARK.resolveDarkTheme(systemInDark = false))
    }
}

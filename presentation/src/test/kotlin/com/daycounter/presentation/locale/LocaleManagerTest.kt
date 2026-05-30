package com.daycounter.presentation.locale

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.daycounter.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class LocaleManagerTest {

    private val base: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `wrap applies spanish locale to the configuration`() {
        val wrapped = LocaleManager.wrap(base, AppLanguage.SPANISH)
        assertEquals("es", wrapped.resources.configuration.locales[0].language)
    }

    @Test
    fun `wrap applies english locale to the configuration`() {
        val wrapped = LocaleManager.wrap(base, AppLanguage.ENGLISH)
        assertEquals("en", wrapped.resources.configuration.locales[0].language)
    }
}

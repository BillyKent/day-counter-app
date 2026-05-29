package com.daycounter.presentation.navigation

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the deep-link synthetic back stack (navigation-contract, Principle VI). Uses Robolectric
 * for a real [Uri] parser.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class DeepLinkResolverTest {

    @Test
    fun `valid counter uri yields Contadores then Detail`() {
        val stack = DeepLinkResolver.resolve(Uri.parse("daycounter://counter/42"))
        assertEquals(listOf(Contadores, Detail(42)), stack)
    }

    @Test
    fun `null uri yields just Contadores`() {
        assertEquals(listOf(Contadores), DeepLinkResolver.resolve(null))
    }

    @Test
    fun `malformed id yields just Contadores`() {
        assertEquals(listOf(Contadores), DeepLinkResolver.resolve(Uri.parse("daycounter://counter/abc")))
    }

    @Test
    fun `wrong scheme yields just Contadores`() {
        assertEquals(listOf(Contadores), DeepLinkResolver.resolve(Uri.parse("https://counter/42")))
    }

    @Test
    fun `non-positive id yields just Contadores`() {
        assertEquals(listOf(Contadores), DeepLinkResolver.resolve(Uri.parse("daycounter://counter/0")))
    }
}

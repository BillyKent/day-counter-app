package com.daycounter.presentation.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit coverage for the US1 multi-back-stack navigation contract (tab switching, push/pop, the
 * delete-from-Detail and onboarding-handoff helpers). Pure JVM — exercises the Compose snapshot
 * state outside composition.
 */
class TopLevelBackStackTest {

    @Test
    fun `starts on the given tab`() {
        val sut = TopLevelBackStack<NavKey>(Contadores)
        assertEquals(Contadores, sut.topLevelKey)
        assertEquals(listOf(Contadores), sut.backStack)
    }

    @Test
    fun `add pushes a child onto the current tab and flattens`() {
        val sut = TopLevelBackStack<NavKey>(Contadores)
        sut.add(Detail(5))
        assertEquals(listOf(Contadores, Detail(5)), sut.backStack)
        assertEquals(Contadores, sut.topLevelKey)
    }

    @Test
    fun `removeLast pops the child back to the tab base`() {
        val sut = TopLevelBackStack<NavKey>(Contadores)
        sut.add(Detail(5))
        sut.add(History(5))
        sut.removeLast()
        assertEquals(listOf(Contadores, Detail(5)), sut.backStack)
        sut.removeLast()
        assertEquals(listOf(Contadores), sut.backStack)
    }

    @Test
    fun `switching tab preserves each tab's own history`() {
        val sut = TopLevelBackStack<NavKey>(Contadores)
        sut.add(Detail(1)) // Contadores -> Detail
        sut.addTopLevel(Estadisticas)
        assertEquals(Estadisticas, sut.topLevelKey)
        // Returning to Contadores surfaces its preserved stack.
        sut.addTopLevel(Contadores)
        assertEquals(listOf(Estadisticas, Contadores, Detail(1)), sut.backStack)
        assertEquals(Contadores, sut.topLevelKey)
    }

    @Test
    fun `popToBase trims the current tab to its base, leaving other tabs intact`() {
        val sut = TopLevelBackStack<NavKey>(Contadores)
        sut.addTopLevel(Ajustes)
        sut.addTopLevel(Contadores)
        sut.add(Detail(9))
        sut.add(History(9))

        sut.popToBase()

        assertEquals(listOf(Ajustes, Contadores), sut.backStack)
        assertEquals(Contadores, sut.topLevelKey)
    }

    @Test
    fun `resetTo wipes everything to a single tab (onboarding handoff)`() {
        val sut = TopLevelBackStack<NavKey>(Contadores)
        sut.add(Onboarding)

        sut.resetTo(Contadores)

        assertEquals(listOf(Contadores), sut.backStack)
        assertEquals(Contadores, sut.topLevelKey)
    }
}

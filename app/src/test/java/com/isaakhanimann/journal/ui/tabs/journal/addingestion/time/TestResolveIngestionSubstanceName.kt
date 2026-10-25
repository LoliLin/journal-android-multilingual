package com.isaakhanimann.journal.ui.tabs.journal.addingestion.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TestResolveIngestionSubstanceName {
    @Test
    fun customSubstanceIdResolvesNameWithoutRouteName() {
        assertEquals("Custom test substance", resolveIngestionSubstanceName(null, 42, "Custom test substance"))
    }

    @Test
    fun customSubstanceNameTakesPrecedenceOverRouteName() {
        assertEquals("Custom", resolveIngestionSubstanceName("Wrong name", 42, "Custom"))
    }

    @Test
    fun missingCustomSubstanceDoesNotFallBackToRouteName() {
        assertNull(resolveIngestionSubstanceName("Wrong name", 42, null))
    }

    @Test
    fun blankCustomSubstanceNameIsRejected() {
        assertNull(resolveIngestionSubstanceName(null, 42, "  "))
    }

    @Test
    fun catalogSubstanceNameIsPreservedWithoutCustomId() {
        assertEquals("LSD", resolveIngestionSubstanceName("LSD", null, null))
    }

    @Test
    fun blankCatalogSubstanceNameIsRejected() {
        assertNull(resolveIngestionSubstanceName("", null, null))
    }
}

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveIngestionSubstanceNameTest {
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
    @Test
    fun unchangedNameRemainsValidOnSave() {
        org.junit.Assert.assertTrue(isIngestionSubstanceIdentityValid("Custom", "Custom"))
    }

    @Test
    fun renameBetweenLoadAndSaveIsRejected() {
        org.junit.Assert.assertFalse(isIngestionSubstanceIdentityValid("Old custom", "Renamed custom"))
    }

    @Test
    fun deletedSubstanceBetweenLoadAndSaveIsRejected() {
        org.junit.Assert.assertFalse(isIngestionSubstanceIdentityValid("Custom", null))
    }

    @Test
    fun emptyDisplayedNameCannotBeSaved() {
        org.junit.Assert.assertFalse(isIngestionSubstanceIdentityValid("", ""))
    }
}

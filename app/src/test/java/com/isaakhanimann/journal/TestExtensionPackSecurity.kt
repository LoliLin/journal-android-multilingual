/*
 * Copyright (c) 2026.
 * This file is part of the Journal Android Multilingual project.
 */

package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.tabs.settings.ExtensionPackLoader
import com.isaakhanimann.journal.ui.tabs.settings.isValidRegisterName
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the security-critical extension pack helpers:
 * [isValidRegisterName] and [ExtensionPackLoader.extractZipSafely].
 */
class TestExtensionPackSecurity {

    private fun createZip(entries: Map<String, String>): File {
        val tempFile = File.createTempFile("ext_test", ".zip")
        tempFile.deleteOnExit()
        ZipOutputStream(tempFile.outputStream()).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return tempFile
    }

    private fun assertRejected(entries: Map<String, String>) {
        val zip = createZip(entries)
        val target = File.createTempFile("ext_target", "").apply {
            delete()
            mkdirs()
        }
        try {
            ZipFile(zip).use { zf ->
                try {
                    ExtensionPackLoader.extractZipSafely(zf, target)
                    assertTrue("expected extraction of $entries to be rejected", false)
                } catch (e: Exception) {
                    // expected: malicious entries must be refused
                }
            }
        } finally {
            target.deleteRecursively()
            zip.delete()
        }
    }

    @Test
    fun registerNameWhitelist() {
        assertTrue(isValidRegisterName("my_pack-1"))
        assertTrue(isValidRegisterName("A1_b"))
        assertFalse(isValidRegisterName("../evil"))
        assertFalse(isValidRegisterName("a/b"))
        assertFalse(isValidRegisterName("a\\b"))
        assertFalse(isValidRegisterName("a b"))
        assertFalse(isValidRegisterName("a:b"))
        assertFalse(isValidRegisterName(""))
    }

    @Test
    fun extractAcceptsWellFormedPack() {
        val zip = createZip(
            mapOf(
                "manifest.json" to "{\"registerName\":\"demo\"}",
                "substances/en_us/Demo.json" to "{\"name\":\"Demo\"}"
            )
        )
        val target = File.createTempFile("ext_target", "").apply {
            delete()
            mkdirs()
        }
        try {
            ZipFile(zip).use { zf ->
                val count = ExtensionPackLoader.extractZipSafely(zf, target)
                assertEquals(2, count)
                assertTrue(File(target, "manifest.json").isFile)
                assertTrue(File(target, "substances/en_us/Demo.json").isFile)
            }
        } finally {
            target.deleteRecursively()
            zip.delete()
        }
    }

    @Test
    fun extractRejectsParentTraversal() {
        assertRejected(mapOf("manifest.json" to "{}", "../evil.txt" to "boom"))
        assertRejected(mapOf("manifest.json" to "{}", "a/../../evil.txt" to "boom"))
    }

    @Test
    fun extractRejectsAbsoluteAndWindowsPaths() {
        assertRejected(mapOf("manifest.json" to "{}", "/etc/evil.txt" to "boom"))
        assertRejected(mapOf("manifest.json" to "{}", "sub\\..\\evil.txt" to "boom"))
    }

    @Test
    fun extractRejectsNameWithColon() {
        assertRejected(mapOf("manifest.json" to "{}", "C:evil.txt" to "boom"))
    }
}

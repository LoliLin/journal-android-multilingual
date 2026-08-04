/*
 * Copyright (c) 2026.
 * This file is part of the Journal Android Multilingual project.
 */

package com.isaakhanimann.journal

import com.isaakhanimann.journal.ui.tabs.settings.ExportEncryption
import javax.crypto.AEADBadTagException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Round-trip and failure tests for the password-encrypted export format.
 */
class TestExportEncryption {

    @Test
    fun roundTrip() {
        val plain = "{\"experiences\":[]}"
        val encrypted = ExportEncryption.encryptExport(plain, "correct horse")
        assertTrue(ExportEncryption.isEncryptedExport(encrypted))
        assertEquals(plain, ExportEncryption.decryptExport(encrypted, "correct horse"))
    }

    @Test
    fun wrongPasswordFailsGcmTag() {
        val encrypted = ExportEncryption.encryptExport("secret journal", "right-password")
        try {
            ExportEncryption.decryptExport(encrypted, "wrong-password")
            assertTrue("expected AEADBadTagException", false)
        } catch (e: AEADBadTagException) {
            // expected
        }
    }

    @Test
    fun encryptionIsRandomized() {
        val a = ExportEncryption.encryptExport("same text", "pw")
        val b = ExportEncryption.encryptExport("same text", "pw")
        assertNotEquals(a.toList(), b.toList())
    }

    @Test
    fun unicodeRoundTrip() {
        val plain = "日记内容 日本語 \uD83D\uDE00 \n 洛铃"
        val encrypted = ExportEncryption.encryptExport(plain, "pässwörd")
        assertEquals(plain, ExportEncryption.decryptExport(encrypted, "pässwörd"))
    }

    @Test
    fun magicDetection() {
        assertFalse(ExportEncryption.isEncryptedExport("{}".toByteArray()))
        assertFalse(ExportEncryption.isEncryptedExport("JENC".toByteArray()))
        assertFalse(ExportEncryption.isEncryptedExport(ByteArray(0)))
        // Plain JSON that happens to start with the magic prefix is not a real export header.
        assertTrue(ExportEncryption.isEncryptedExport("JENC1...".toByteArray()))
    }

    @Test
    fun emptyPasswordRejected() {
        try {
            ExportEncryption.encryptExport("data", "")
            assertTrue("expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            // expected
        }
        val encrypted = ExportEncryption.encryptExport("data", "pw")
        try {
            ExportEncryption.decryptExport(encrypted, "")
            assertTrue("expected IllegalArgumentException", false)
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun corruptedDataRejected() {
        val encrypted = ExportEncryption.encryptExport("data", "pw")
        val corrupted = encrypted.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 0x01).toByte() }
        try {
            ExportEncryption.decryptExport(corrupted, "pw")
            assertTrue("expected AEADBadTagException", false)
        } catch (e: AEADBadTagException) {
            // expected
        }
    }
}

package com.isaakhanimann.journal.ui.tabs.settings

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-based AES-GCM encryption for journal exports.
 *
 * File format (all big-endian):
 *   magic (5 bytes "JENC1") | saltLength (1 byte) | salt (saltLength bytes) |
 *   iv (12 bytes) | GCM ciphertext (plaintext + 16-byte tag)
 *
 * Key derivation: PBKDF2-HMAC-SHA256, 100_000 iterations, 256-bit key.
 * A wrong password fails GCM tag verification (AEADBadTagException) — no
 * padding-oracle concerns, and corrupted files are rejected outright.
 */
object ExportEncryption {

    private const val MAGIC = "JENC1"
    private const val MAGIC_BYTES_LENGTH = 5
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val KEY_LENGTH_BITS = 256
    private const val PBKDF2_ITERATIONS = 100_000

    private val secureRandom = SecureRandom()

    fun isEncryptedExport(data: ByteArray): Boolean {
        if (data.size < MAGIC_BYTES_LENGTH) return false
        val prefix = data.copyOf(MAGIC_BYTES_LENGTH).toString(Charsets.US_ASCII)
        return prefix == MAGIC
    }

    fun encryptExport(plainText: String, password: String): ByteArray {
        require(password.isNotEmpty()) { "Password must not be empty" }
        val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { secureRandom.nextBytes(it) }
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return MAGIC.toByteArray(Charsets.US_ASCII) +
            byteArrayOf(SALT_LENGTH.toByte()) +
            salt + iv + ciphertext
    }

    /**
     * @throws javax.crypto.AEADBadTagException if the password is wrong or the data is corrupt.
     */
    fun decryptExport(data: ByteArray, password: String): String {
        require(password.isNotEmpty()) { "Password must not be empty" }
        require(isEncryptedExport(data)) { "Not an encrypted export" }
        var offset = MAGIC_BYTES_LENGTH
        val saltLength = data[offset].toInt() and 0xFF
        offset += 1
        require(offset + saltLength + IV_LENGTH <= data.size) { "Truncated encrypted export" }
        val salt = data.copyOfRange(offset, offset + saltLength)
        offset += saltLength
        val iv = data.copyOfRange(offset, offset + IV_LENGTH)
        offset += IV_LENGTH
        val ciphertext = data.copyOfRange(offset, data.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val plainBytes = cipher.doFinal(ciphertext)
        return plainBytes.toString(Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}

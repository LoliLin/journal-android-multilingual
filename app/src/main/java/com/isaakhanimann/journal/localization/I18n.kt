package com.isaakhanimann.journal.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import org.json.JSONObject

object I18n {
    private var strings: Map<String, String> = emptyMap()
    private val extraStrings = mutableMapOf<String, String>()
    private var loadedLangKey: String? = null

    fun setOverride(key: String, value: String) {
        extraStrings[key] = value
    }
    private const val FALLBACK_LANG_KEY = "en_us"
    private var preferredLangKey: String? = null

    fun getCurrentLanguageKey(): String {
        val locale = Locale.getDefault()
        val language = locale.language.lowercase()
        val country = locale.country.lowercase()
        return if (country.isNullOrBlank()) language else "${language}_${country}"
    }

    fun setPreferredLanguageKey(languageKey: String?) {
        preferredLangKey = languageKey?.lowercase()
        loadedLangKey = null
    }

    fun getPreferredLanguageKey(): String? = preferredLangKey

    fun translate(
        context: Context,
        key: String,
        replacements: Map<String, String> = emptyMap(),
    ): String {
        ensureLoaded(context)
        val raw = extraStrings[key] ?: strings[key] ?: strings["missing_key"] ?: key
        return replacements.entries.fold(raw) { acc, entry ->
            acc.replace("{${entry.key}}", entry.value)
        }
    }

    fun translateOrDefault(
        context: Context,
        key: String,
        fallback: String,
        replacements: Map<String, String> = emptyMap(),
    ): String {
        ensureLoaded(context)
        val raw = strings[key] ?: fallback
        return replacements.entries.fold(raw) { acc, entry ->
            acc.replace("{${entry.key}}", entry.value)
        }
    }

    fun getSupportedLanguages(context: Context): Map<String, String> {
        return loadStringsFile(context, "lang/supported.json")
    }

    private fun ensureLoaded(context: Context) {
        val currentKey = (preferredLangKey ?: getCurrentLanguageKey()).lowercase()
        if (currentKey == loadedLangKey && strings.isNotEmpty()) return

        val fallbackStrings = loadLanguageFile(context, FALLBACK_LANG_KEY)
        val localizedStrings = if (currentKey != FALLBACK_LANG_KEY) {
            loadLanguageFile(context, currentKey)
        } else {
            emptyMap()
        }
        strings = fallbackStrings + localizedStrings
        loadedLangKey = currentKey
    }

    private fun loadLanguageFile(context: Context, langKey: String): Map<String, String> {
        val filePath = "lang/$langKey.json"
        return loadStringsFile(context, filePath)
    }

    private fun loadStringsFile(context: Context, filePath: String): Map<String, String> {
        return try {
            context.assets.open(filePath).use { inputStream ->
                val jsonText = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val jsonObject = JSONObject(jsonText)
                val map = mutableMapOf<String, String>()
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = jsonObject.optString(key, "")
                }
                map
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

@Composable
fun i18n(key: String, replacements: Map<String, String> = emptyMap()): String {
    val context = LocalContext.current
    return I18n.translate(context, key, replacements)
}

@Composable
fun i18nOrDefault(
    key: String,
    fallback: String,
    replacements: Map<String, String> = emptyMap(),
): String {
    val context = LocalContext.current
    return I18n.translateOrDefault(context, key, fallback, replacements)
}

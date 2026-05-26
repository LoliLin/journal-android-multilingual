package com.isaakhanimann.journal.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import org.json.JSONObject

object I18n {
    private const val FALLBACK_LANG_KEY = "en_us"

    private val _preferredLangKey = mutableStateOf<String?>(null)
    val preferredLangKey: State<String?> get() = _preferredLangKey

    @Volatile
    private var cachedStrings: Map<String, String> = emptyMap()
    private var loadedLangKey: String? = null

    fun getCurrentLanguageKey(): String {
        val locale = Locale.getDefault()
        val language = locale.language.lowercase()
        val country = locale.country.lowercase()
        return if (country.isBlank()) language else "${language}_${country}"
    }

    fun setPreferredLanguageKey(languageKey: String?) {
        _preferredLangKey.value = languageKey?.lowercase()
        synchronized(this) {
            loadedLangKey = null
        }
    }

    fun translate(
        context: Context,
        key: String,
        replacements: Map<String, String> = emptyMap(),
    ): String {
        ensureLoaded(context)
        val raw = cachedStrings[key] ?: cachedStrings["missing_key"] ?: key
        return raw.applyReplacements(replacements)
    }

    fun translateOrDefault(
        context: Context,
        key: String,
        fallback: String,
        replacements: Map<String, String> = emptyMap(),
    ): String {
        ensureLoaded(context)
        val raw = cachedStrings[key] ?: fallback
        return raw.applyReplacements(replacements)
    }

    fun getSupportedLanguages(context: Context): Map<String, String> =
        loadStringsFile(context, "lang/supported.json")

    @Synchronized
    private fun ensureLoaded(context: Context) {
        val currentKey = (_preferredLangKey.value ?: getCurrentLanguageKey()).lowercase()
        if (currentKey == loadedLangKey && cachedStrings.isNotEmpty()) return

        val fallbackStrings = loadLanguageFile(context, FALLBACK_LANG_KEY)
        val localizedStrings = if (currentKey != FALLBACK_LANG_KEY) {
            loadLanguageFile(context, currentKey)
        } else {
            emptyMap()
        }
        
        cachedStrings = fallbackStrings + localizedStrings
        loadedLangKey = currentKey
    }

    private fun loadLanguageFile(context: Context, langKey: String): Map<String, String> =
        loadStringsFile(context, "lang/$langKey.json")

    private fun loadStringsFile(context: Context, filePath: String): Map<String, String> {
        return runCatching {
            context.assets.open(filePath).use { inputStream ->
                val jsonText = inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(jsonText)
                val map = mutableMapOf<String, String>()
                jsonObject.keys().forEach { key ->
                    map[key] = jsonObject.optString(key, "")
                }
                map
            }
        }.getOrDefault(emptyMap())
    }

    private fun String.applyReplacements(replacements: Map<String, String>): String {
        if (replacements.isEmpty()) return this
        return replacements.entries.fold(this) { acc, (key, value) ->
            acc.replace("{$key}", value)
        }
    }
}

@Composable
@ReadOnlyComposable
fun i18n(key: String, replacements: Map<String, String> = emptyMap()): String {
    val context = LocalContext.current
    I18n.preferredLangKey.value
    return I18n.translate(context, key, replacements)
}

@Composable
@ReadOnlyComposable
fun i18nOrDefault(
    key: String,
    fallback: String,
    replacements: Map<String, String> = emptyMap(),
): String {
    val context = LocalContext.current
    I18n.preferredLangKey.value
    return I18n.translateOrDefault(context, key, fallback, replacements)
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

package com.isaakhanimann.journal.localization

import android.content.Context
import android.util.Xml
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import org.xmlpull.v1.XmlPullParser

object I18n {
    private var strings: Map<String, String> = emptyMap()
    private var loadedLangKey: String? = null
    private const val FALLBACK_LANG_KEY = "en_US"
    private var preferredLangKey: String? = null

    fun getCurrentLanguageKey(): String {
        val locale = Locale.getDefault()
        val language = locale.language
        val country = locale.country
        return if (country.isNullOrBlank()) language else "${language}_${country}"
    }

    fun setPreferredLanguageKey(languageKey: String?) {
        preferredLangKey = languageKey
        loadedLangKey = null
    }

    fun getPreferredLanguageKey(): String? = preferredLangKey

    fun translate(
        context: Context,
        key: String,
        replacements: Map<String, String> = emptyMap(),
    ): String {
        ensureLoaded(context)
        val raw = strings[key] ?: strings["missing_key"] ?: key
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
        return loadStringsFile(context, "lang/supported.xml")
    }

    private fun ensureLoaded(context: Context) {
        val currentKey = preferredLangKey ?: getCurrentLanguageKey()
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
        val filePath = "lang/$langKey.xml"
        return loadStringsFile(context, filePath)
    }

    private fun loadStringsFile(context: Context, filePath: String): Map<String, String> {
        return try {
            context.assets.open(filePath).use { inputStream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setInput(inputStream, "UTF-8")
                val map = mutableMapOf<String, String>()
                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name == "string") {
                        val name = parser.getAttributeValue(null, "name")
                        if (name != null) {
                            map[name] = parser.nextText()
                        }
                    }
                    eventType = parser.next()
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

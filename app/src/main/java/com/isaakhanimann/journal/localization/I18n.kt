package com.isaakhanimann.journal.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

object I18n {
    private const val FALLBACK_LANG_KEY = "en_us"

    // translate() runs on the hot path: `i18n()` is called from composition, so it
    // must never block. The loaded table is published as one immutable snapshot
    // through @Volatile fields, which makes the common case a lock-free read; only
    // the (re)load path takes [loadLock]. Holding a lock across the asset I/O of a
    // reload is what used to stall recomposition whenever another thread — typically
    // a stats-widget render — touched I18n at the same time.
    //
    // Write order matters: `strings` is published before `loadedLangKey`, and
    // `needsReload` is cleared last. A reader that observes the new key (or the
    // cleared flag) is therefore guaranteed to see the matching table.
    @Volatile
    private var strings: Map<String, String> = emptyMap()

    @Volatile
    private var loadedLangKey: String? = null

    @Volatile
    private var preferredLangKey: String? = null

    @Volatile
    private var needsReload = false

    /** Only taken while (re)loading; readers do not need it on the common path. */
    private val loadLock = Any()

    // Emitted when the string table may have changed (language switch, extension-pack
    // install). The stats-widget sync re-renders on it so widget labels do not stay
    // stale after a pack adds or overrides them. replay = 1 so a markDirty() that lands
    // before the consumer has subscribed is not lost.
    private val _stringsChanged = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
    val stringsChanged: SharedFlow<Unit> = _stringsChanged.asSharedFlow()

    fun markDirty() {
        needsReload = true
        _stringsChanged.tryEmit(Unit)
    }

    fun getCurrentLanguageKey(): String {
        val locale = Locale.getDefault()
        val language = locale.language.lowercase()
        val country = locale.country.lowercase()
        return if (country.isNullOrBlank()) language else "${language}_$country"
    }

    fun setPreferredLanguageKey(languageKey: String?) {
        preferredLangKey = languageKey?.lowercase()
        loadedLangKey = null
        _stringsChanged.tryEmit(Unit)
        com.isaakhanimann.journal.ui.utils.DateFormat.notifyLanguageChanged()
    }

    fun getPreferredLanguageKey(): String? = preferredLangKey

    fun translate(
        context: Context,
        key: String,
        replacements: Map<String, String> = emptyMap()
    ): String {
        val current = stringsFor(context)
        val raw = current[key] ?: current["missing_key"] ?: key
        return applyReplacements(raw, replacements)
    }

    fun translateOrDefault(
        context: Context,
        key: String,
        fallback: String,
        replacements: Map<String, String> = emptyMap()
    ): String = applyReplacements(stringsFor(context)[key] ?: fallback, replacements)

    fun getSupportedLanguages(context: Context): Map<String, String> =
        loadStringsFile(context, "lang/supported.json")

    private fun applyReplacements(raw: String, replacements: Map<String, String>): String =
        replacements.entries.fold(raw) { acc, entry ->
            acc.replace("{" + entry.key + "}", entry.value)
        }

    /**
     * The string table for the current language, reloading it when the language
     * changed or [markDirty] was called.
     *
     * Fast path: a lock-free read of the volatile snapshot. Slow path: double-checked
     * under [loadLock], so concurrent callers load the assets once and every other
     * reader keeps being served the previous snapshot instead of waiting for the I/O.
     */
    private fun stringsFor(context: Context): Map<String, String> {
        val currentKey = (preferredLangKey ?: getCurrentLanguageKey()).lowercase()
        val snapshot = strings
        if (currentKey == loadedLangKey && snapshot.isNotEmpty() && !needsReload) {
            return snapshot
        }
        return synchronized(loadLock) {
            val recheckedKey = (preferredLangKey ?: getCurrentLanguageKey()).lowercase()
            if (recheckedKey == loadedLangKey && strings.isNotEmpty() && !needsReload) {
                strings
            } else {
                val fallbackStrings = loadLanguageFile(context, FALLBACK_LANG_KEY)
                val localizedStrings = if (recheckedKey != FALLBACK_LANG_KEY) {
                    loadLanguageFile(context, recheckedKey)
                } else {
                    emptyMap()
                }
                val loaded = fallbackStrings + localizedStrings
                strings = loaded
                loadedLangKey = recheckedKey
                needsReload = false
                loaded
            }
        }
    }

    private fun loadLanguageFile(context: Context, langKey: String): Map<String, String> {
        val filePath = "lang/$langKey.json"
        return loadStringsFile(context, filePath)
    }

    private fun loadStringsFile(context: Context, filePath: String): Map<String, String> {
        val resultMap = try {
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
            mutableMapOf<String, String>()
        }

        val extDir = java.io.File(context.filesDir, "ext_packs")
        if (extDir.exists()) {
            extDir.listFiles()?.forEach { packDir ->
                val extFile = java.io.File(packDir, filePath)
                if (extFile.exists()) {
                    try {
                        val json = JSONObject(extFile.readText())
                        val keys = json.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            resultMap[key] = json.optString(key, "")
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
        return resultMap
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
    replacements: Map<String, String> = emptyMap()
): String {
    val context = LocalContext.current
    return I18n.translateOrDefault(context, key, fallback, replacements)
}

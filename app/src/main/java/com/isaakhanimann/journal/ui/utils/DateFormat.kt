package com.isaakhanimann.journal.ui.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.isaakhanimann.journal.localization.I18n
import java.time.temporal.TemporalAccessor
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * How calendar dates are localized. Clock 12/24 stays in [TimeFormat].
 *
 * Default [FOLLOW_LANGUAGE] uses the in-app language. [FOLLOW_SYSTEM] uses
 * [Locale.getDefault]. The rest pin a specific locale.
 */
enum class DateLocaleOption {
    FOLLOW_LANGUAGE,
    FOLLOW_SYSTEM,
    EN_US,
    ZH_CN,
    ZH_TW
}

/**
 * Process-wide date locale. Composition subscribes; ViewModels/drawables just
 * read the current value, same pattern as [TimeFormat].
 */
object DateFormat {

    private var selectedOption by mutableStateOf(DateLocaleOption.FOLLOW_LANGUAGE)
    private var languageGeneration by mutableStateOf(0)

    fun setOption(value: DateLocaleOption) {
        selectedOption = value
    }

    fun notifyLanguageChanged() {
        languageGeneration++
    }

    fun currentOption(): DateLocaleOption = selectedOption

    fun locale(): Locale {
        languageGeneration
        return when (selectedOption) {
            DateLocaleOption.FOLLOW_SYSTEM -> Locale.getDefault()
            DateLocaleOption.EN_US -> Locale.US
            DateLocaleOption.ZH_CN -> Locale.SIMPLIFIED_CHINESE
            DateLocaleOption.ZH_TW -> Locale.TRADITIONAL_CHINESE
            DateLocaleOption.FOLLOW_LANGUAGE -> localeForLanguageKey(
                I18n.getPreferredLanguageKey() ?: I18n.getCurrentLanguageKey()
            )
        }
    }

    fun localeForLanguageKey(languageKey: String?): Locale {
        val key = languageKey?.lowercase().orEmpty()
        return when {
            key.startsWith("zh_tw") || key.startsWith("zh-tw") || key == "zh_hant" ->
                Locale.TRADITIONAL_CHINESE
            key.startsWith("zh") -> Locale.SIMPLIFIED_CHINESE
            key.startsWith("en") -> Locale.US
            else -> Locale.getDefault()
        }
    }

    fun format(temporal: TemporalAccessor, skeleton: String): String {
        val loc = locale()
        val pattern = bestPattern(skeleton, loc)
        return DateTimeFormatter.ofPattern(pattern, loc).format(temporal)
    }

    /**
     * Uses Android's locale-aware skeleton expansion when available.
     * JVM unit tests fall back to a skeleton-shaped pattern (locale still applied).
     */
    internal fun bestPattern(skeleton: String, locale: Locale): String {
        return try {
            android.text.format.DateFormat.getBestDateTimePattern(locale, skeleton)
        } catch (_: Throwable) {
            jvmFallbackPattern(skeleton)
        }
    }

    private fun jvmFallbackPattern(skeleton: String): String = when {
        skeleton.contains("EEE") && skeleton.contains("y") -> "EEE, d MMM yyyy"
        skeleton.contains("MMMM") && skeleton.contains("y") -> "d MMMM yyyy"
        skeleton.contains("MMM") && skeleton.contains("y") && !skeleton.contains("E") ->
            "d MMM yyyy"
        skeleton.contains("EEE") && !skeleton.contains("y") -> "EEE"
        else -> skeleton
    }
}

package com.isaakhanimann.journal.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

data class I18nText(
    val i18nKey: String,
    val params: Map<String, Any> = emptyMap()
) {
    fun translate(context: Context): String {
        val resolvedParams = params.mapValues { (_, value) ->
            when (value) {
                is I18nText -> value.translate(context)
                else -> value.toString()
            }
        }
        return I18n.translate(context, i18nKey, resolvedParams)
    }

    @Composable
    fun translate(): String {
        val context = LocalContext.current
        return translate(context)
    }
}

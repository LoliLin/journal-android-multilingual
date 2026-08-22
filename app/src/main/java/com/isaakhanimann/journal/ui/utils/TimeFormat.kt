/*
 * Copyright (c) 2022. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 */

package com.isaakhanimann.journal.ui.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Process-wide clock format. Reads from composition subscribe to changes, reads from
 * view models and drawables just see the current value.
 */
object TimeFormat {

    private var systemUses24Hour by mutableStateOf(true)
    private var overrideIs24Hour by mutableStateOf<Boolean?>(null)

    /** Null until the user picks explicitly, in which case the system setting is followed. */
    val is24Hour: Boolean
        get() = overrideIs24Hour ?: systemUses24Hour

    val timePattern: String
        get() = if (is24Hour) "HH:mm" else "h:mm a"

    val hourPattern: String
        get() = if (is24Hour) "HH" else "h a"

    fun refreshSystemDefault(context: Context) {
        systemUses24Hour = android.text.format.DateFormat.is24HourFormat(context)
    }

    fun setUserOverride(value: Boolean?) {
        overrideIs24Hour = value
    }

    fun systemDefaultIs24Hour(): Boolean = systemUses24Hour
}

/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.time

import android.app.TimePickerDialog
import android.content.ClipData
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.localization.i18n
import java.time.LocalDateTime
import kotlinx.coroutines.launch

@Composable
fun TimePickerButton(
    localDateTime: LocalDateTime,
    onChange: (LocalDateTime) -> Unit,
    timeString: String,
    modifier: Modifier = Modifier,
    hasOutline: Boolean = true
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val dialogTheme =
        if (isSystemInDarkTheme()) R.style.DialogThemeDark else R.style.DialogThemeLight
    val timePickerDialog = TimePickerDialog(
        context,
        dialogTheme,
        { _, newHour: Int, newMinute: Int ->
            onChange(
                LocalDateTime.now()
                    .withYear(localDateTime.year)
                    .withMonth(localDateTime.monthValue)
                    .withDayOfMonth(localDateTime.dayOfMonth)
                    .withHour(newHour)
                    .withMinute(newMinute)
            )
        },
        localDateTime.hour,
        localDateTime.minute,
        DateFormat.is24HourFormat(context)
    )

    val copyTip = i18n("copied_to_clipboard")

    val longPressModifier = modifier.pointerInput(timeString) {
        detectTapGestures(
            onLongPress = {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("", timeString)))
                    Toast.makeText(context, copyTip, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (hasOutline) {
        OutlinedButton(onClick = timePickerDialog::show, modifier = longPressModifier) {
            Icon(
                Icons.Outlined.Schedule,
                contentDescription = i18n("open_time_picker")
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(timeString)
        }
    } else {
        TextButton(onClick = timePickerDialog::show, modifier = longPressModifier) {
            Icon(
                Icons.Outlined.Schedule,
                contentDescription = i18n("open_time_picker")
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(timeString)
        }
    }
}

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.time

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.isaakhanimann.journal.localization.i18n
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.ui.utils.getDateWithWeekdayText
import com.isaakhanimann.journal.ui.utils.getShortTimeText
import java.time.LocalDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimePointOrRangePicker(
    onChangeTimePickerOption: (option: IngestionTimePickerOption) -> Unit,
    ingestionTimePickerOption: IngestionTimePickerOption,
    localDateTimeStart: LocalDateTime,
    onChangeStartDateOrTime: (LocalDateTime) -> Unit,
    localDateTimeEnd: LocalDateTime,
    onChangeEndDateOrTime: (LocalDateTime) -> Unit,
    onSelectDurationPreset: (Long) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            onClick = { onChangeTimePickerOption(IngestionTimePickerOption.POINT_IN_TIME) },
            selected = ingestionTimePickerOption == IngestionTimePickerOption.POINT_IN_TIME
        ) {
            Text(i18n("time_point_in_time"))
        }
        SegmentedButton(
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            onClick = { onChangeTimePickerOption(IngestionTimePickerOption.TIME_RANGE) },
            selected = ingestionTimePickerOption == IngestionTimePickerOption.TIME_RANGE
        ) {
            Text(i18n("time_range"))
        }
    }
    AnimatedContent(
        targetState = ingestionTimePickerOption,
        label = "ingestionTimePicker"
    ) { option ->
        when (option) {
            IngestionTimePickerOption.POINT_IN_TIME -> {
                Column {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    DatePickerButton(
                        localDateTime = localDateTimeStart,
                        onChange = onChangeStartDateOrTime,
                        dateString = localDateTimeStart.getDateWithWeekdayText(),
                    )
                    TimePickerButton(
                        localDateTime = localDateTimeStart,
                        onChange = onChangeStartDateOrTime,
                        timeString = localDateTimeStart.getShortTimeText(),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        onChangeStartDateOrTime(LocalDateTime.now())
                    }) {
                        Icon(
                            Icons.Default.Update,
                            contentDescription = i18n("time_update_to_now"),
                        )
                    }
                }
                // Fork feature: quick time adjustments (restored from main's ChooseTimeScreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    androidx.compose.material3.OutlinedButton(onClick = {
                        onChangeStartDateOrTime(localDateTimeStart.minusMinutes(5))
                    }) {
                        Text("-5 min")
                    }
                    androidx.compose.material3.OutlinedButton(onClick = {
                        onChangeStartDateOrTime(localDateTimeStart.minusMinutes(10))
                    }) {
                        Text("-10 min")
                    }
                    androidx.compose.material3.OutlinedButton(onClick = {
                        onChangeStartDateOrTime(localDateTimeStart.minusMinutes(30))
                    }) {
                        Text("-30 min")
                    }
                }
                }
            }

            IngestionTimePickerOption.TIME_RANGE -> {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(i18n("finish_time_start"))
                        IconButton(onClick = {
                            onChangeStartDateOrTime(LocalDateTime.now())
                        }) {
                            Icon(
                                Icons.Default.Update,
                                contentDescription = i18n("time_update_to_now"),
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        DatePickerButton(
                            localDateTime = localDateTimeStart,
                            onChange = onChangeStartDateOrTime,
                            dateString = localDateTimeStart.getDateWithWeekdayText(),
                        )
                        TimePickerButton(
                            localDateTime = localDateTimeStart,
                            onChange = onChangeStartDateOrTime,
                            timeString = localDateTimeStart.getShortTimeText(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fork feature: quick duration presets; the last used one is remembered per ROA
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        DURATION_PRESET_MINUTES.forEach { minutes ->
                            androidx.compose.material3.OutlinedButton(
                                onClick = { onSelectDurationPreset(minutes) }
                            ) {
                                Text(durationPresetLabel(minutes))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(i18n("finish_time_end"))
                        IconButton(onClick = {
                            onChangeEndDateOrTime(LocalDateTime.now())
                        }) {
                            Icon(
                                Icons.Default.Update,
                                contentDescription = i18n("time_update_to_now"),
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        DatePickerButton(
                            localDateTime = localDateTimeEnd,
                            onChange = onChangeEndDateOrTime,
                            dateString = localDateTimeEnd.getDateWithWeekdayText(),
                        )
                        TimePickerButton(
                            localDateTime = localDateTimeEnd,
                            onChange = onChangeEndDateOrTime,
                            timeString = localDateTimeEnd.getShortTimeText(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

internal val DURATION_PRESET_MINUTES: List<Long> = listOf(15L, 30L, 45L, 60L, 120L, 240L)

internal fun durationPresetLabel(minutes: Long): String =
    if (minutes < 60) "${minutes}m" else "${minutes / 60}h"
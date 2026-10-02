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

package com.isaakhanimann.journal.ui.tabs.journal.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.main.bottomBarOverlayDp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class SatelliteActionItem(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
    val isActive: Boolean = false,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JournalSatelliteFab(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSingleClick: () -> Unit,
    isTimeRelativeToNow: Boolean,
    onChangeIsRelative: (Boolean) -> Unit,
    isFavoriteEnabled: Boolean,
    onChangeIsFavorite: (Boolean) -> Unit,
    isSearchEnabled: Boolean,
    onChangeIsSearchEnabled: (Boolean) -> Unit,
    latestExperienceId: Int?,
    navigateToQuickTimedNote: (experienceId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    val animProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "satellite_anim_progress"
    )

    val fabRotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fab_rotation"
    )

    val fabContainerColor by animateColorAsState(
        targetValue = if (isExpanded) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        label = "fab_container_color"
    )

    val fabContentColor by animateColorAsState(
        targetValue = if (isExpanded) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        label = "fab_content_color"
    )

    val satelliteItems = listOf(
        SatelliteActionItem(
            id = "quick_note",
            icon = Icons.Outlined.EditNote,
            label = i18nOrDefault("quick_note_title", "Quick note"),
            contentDescription = i18n("quick_note_title"),
            isActive = false,
            isEnabled = latestExperienceId != null,
            onClick = {
                if (latestExperienceId != null) {
                    navigateToQuickTimedNote(latestExperienceId)
                    onExpandedChange(false)
                }
            }
        ),
        SatelliteActionItem(
            id = "time",
            icon = if (isTimeRelativeToNow) Icons.Filled.Timer else Icons.Outlined.Timer,
            label = i18nOrDefault("journal_satellite_time", i18n("journal_time_relative_to_now")),
            contentDescription = if (isTimeRelativeToNow) {
                i18n("journal_regular_time")
            } else {
                i18n("journal_time_relative_to_now")
            },
            isActive = isTimeRelativeToNow,
            isEnabled = true,
            onClick = {
                onChangeIsRelative(!isTimeRelativeToNow)
                onExpandedChange(false)
            }
        ),
        SatelliteActionItem(
            id = "search",
            icon = if (isSearchEnabled) Icons.Outlined.SearchOff else Icons.Filled.Search,
            label = i18nOrDefault("common_search", "Search"),
            contentDescription = if (isSearchEnabled) i18n("journal_search_off") else i18n("common_search"),
            isActive = isSearchEnabled,
            isEnabled = true,
            onClick = {
                onChangeIsSearchEnabled(!isSearchEnabled)
                onExpandedChange(false)
            }
        ),
        SatelliteActionItem(
            id = "favorite",
            icon = if (isFavoriteEnabled) Icons.Filled.Star else Icons.Outlined.StarOutline,
            label = i18nOrDefault("journal_satellite_favorites", i18n("journal_is_favorite")),
            contentDescription = if (isFavoriteEnabled) i18n("journal_is_favorite") else i18n("journal_is_not_favorite"),
            isActive = isFavoriteEnabled,
            isEnabled = true,
            onClick = {
                onChangeIsFavorite(!isFavoriteEnabled)
                onExpandedChange(false)
            }
        )
    )

    val isMenuVisible = isExpanded || animProgress > 0.01f
    val containerSize = if (isMenuVisible) 210.dp else 56.dp

    val fabCenterXPx = with(density) { 182.dp.toPx() }
    val fabCenterYPx = with(density) { 182.dp.toPx() }
    val halfItemWidthPx = with(density) { 28.dp.toPx() }
    val halfItemHeightPx = with(density) { 31.dp.toPx() }
    val radiusPx = with(density) { 100.dp.toPx() }

    Box(
        modifier = modifier
            .padding(bottom = bottomBarOverlayDp())
            .size(containerSize)
            .then(
                if (isMenuVisible) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onExpandedChange(false)
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (isMenuVisible) {
            satelliteItems.forEachIndexed { index, item ->
                val angleRad = (90f - index * 30f) * (PI.toFloat() / 180f)
                val dxPx = -radiusPx * cos(angleRad)
                val dyPx = -radiusPx * sin(angleRad)

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (fabCenterXPx + dxPx * animProgress - halfItemWidthPx).roundToInt(),
                                y = (fabCenterYPx + dyPx * animProgress - halfItemHeightPx).roundToInt()
                            )
                        }
                        .graphicsLayer(
                            scaleX = animProgress,
                            scaleY = animProgress,
                            alpha = animProgress.coerceIn(0f, 1f)
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(56.dp)
                            .alpha(if (item.isEnabled) 1f else 0.38f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (item.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (item.isActive) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(
                                    enabled = item.isEnabled,
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        item.onClick()
                                    }
                                )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            shadowElevation = 1.dp,
                            modifier = Modifier.clickable(
                                enabled = item.isEnabled,
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    item.onClick()
                                }
                            )
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Surface(
            shape = FloatingActionButtonDefaults.shape,
            color = fabContainerColor,
            contentColor = fabContentColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(56.dp)
                .combinedClickable(
                    onClick = {
                        if (isExpanded) {
                            onExpandedChange(false)
                        } else {
                            onSingleClick()
                        }
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onExpandedChange(!isExpanded)
                    }
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (isExpanded) {
                        i18n("common_close")
                    } else {
                        i18n("journal_ingestion")
                    },
                    modifier = Modifier.graphicsLayer(rotationZ = fabRotation)
                )
            }
        }
    }
}

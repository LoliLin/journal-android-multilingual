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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import com.isaakhanimann.journal.ui.main.bottomBarOverlayDp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val FAB_SIZE = 56.dp
private val SATELLITE_CONTAINER_SIZE = 216.dp
private val SATELLITE_RADIUS = 135.dp
private val SATELLITE_ICON_SIZE = 44.dp

private data class SatelliteActionItem(
    val id: String,
    val icon: ImageVector,
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
            dampingRatio = if (isExpanded) Spring.DampingRatioLowBouncy else Spring.DampingRatioNoBouncy,
            stiffness = if (isExpanded) Spring.StiffnessMediumLow else Spring.StiffnessMedium
        ),
        label = "satellite_anim_progress"
    )

    val fabRotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = if (isExpanded) Spring.DampingRatioLowBouncy else Spring.DampingRatioNoBouncy,
            stiffness = if (isExpanded) Spring.StiffnessMediumLow else Spring.StiffnessMedium
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

    val quickNoteDesc = i18nOrDefault("quick_note_title", "Quick note")
    val regularTimeDesc = i18nOrDefault("journal_regular_time", "Regular time")
    val relativeTimeDesc = i18nOrDefault("journal_time_relative_to_now", "Time relative to now")
    val searchOffDesc = i18nOrDefault("journal_search_off", "Turn off search")
    val searchDesc = i18nOrDefault("common_search", "Search")
    val favoriteDesc = i18nOrDefault("journal_is_favorite", "Remove favorite filter")
    val notFavoriteDesc = i18nOrDefault("journal_is_not_favorite", "Show only favorites")

    val satelliteItems = remember(
        isTimeRelativeToNow,
        isFavoriteEnabled,
        isSearchEnabled,
        latestExperienceId,
        quickNoteDesc,
        regularTimeDesc,
        relativeTimeDesc,
        searchOffDesc,
        searchDesc,
        favoriteDesc,
        notFavoriteDesc
    ) {
        listOf(
            SatelliteActionItem(
                id = "quick_note",
                icon = Icons.Outlined.EditNote,
                contentDescription = quickNoteDesc,
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
                contentDescription = if (isTimeRelativeToNow) regularTimeDesc else relativeTimeDesc,
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
                contentDescription = if (isSearchEnabled) searchOffDesc else searchDesc,
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
                contentDescription = if (isFavoriteEnabled) favoriteDesc else notFavoriteDesc,
                isActive = isFavoriteEnabled,
                isEnabled = true,
                onClick = {
                    onChangeIsFavorite(!isFavoriteEnabled)
                    onExpandedChange(false)
                }
            )
        )
    }

    val isMenuVisible = isExpanded || animProgress > 0.005f
    val containerSize = if (isMenuVisible) SATELLITE_CONTAINER_SIZE else FAB_SIZE

    // Center coordinates of the FAB inside the container (anchored at Alignment.BottomEnd)
    val fabCenterXPx = with(density) { (SATELLITE_CONTAINER_SIZE - FAB_SIZE / 2).toPx() }
    val fabCenterYPx = with(density) { (SATELLITE_CONTAINER_SIZE - FAB_SIZE / 2).toPx() }
    val halfIconSizePx = with(density) { (SATELLITE_ICON_SIZE / 2).toPx() }
    val radiusPx = with(density) { SATELLITE_RADIUS.toPx() }

    Box(
        modifier = modifier
            .padding(bottom = bottomBarOverlayDp())
            .size(containerSize),
        contentAlignment = Alignment.BottomEnd
    ) {
        if (isMenuVisible) {
            val totalItems = satelliteItems.size
            val sweepStartAngleDeg = 90f
            val sweepEndAngleDeg = 0f
            val angleStepDeg = if (totalItems > 1) (sweepStartAngleDeg - sweepEndAngleDeg) / (totalItems - 1) else 0f

            satelliteItems.forEachIndexed { index, item ->
                val angleDeg = sweepStartAngleDeg - index * angleStepDeg
                val angleRad = angleDeg * (PI.toFloat() / 180f)
                val dxPx = -radiusPx * cos(angleRad)
                val dyPx = -radiusPx * sin(angleRad)

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                x = (fabCenterXPx + dxPx * animProgress - halfIconSizePx).roundToInt(),
                                y = (fabCenterYPx + dyPx * animProgress - halfIconSizePx).roundToInt()
                            )
                        }
                        .graphicsLayer(
                            scaleX = animProgress.coerceAtLeast(0f),
                            scaleY = animProgress.coerceAtLeast(0f),
                            alpha = animProgress.coerceIn(0f, 1f)
                        )
                ) {
                    Surface(
                        onClick = { item.onClick() },
                        enabled = item.isEnabled && isExpanded,
                        shape = CircleShape,
                        color = if (item.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else if (!item.isEnabled) {
                            MaterialTheme.colorScheme.surfaceContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        contentColor = if (item.isActive) {
                            MaterialTheme.colorScheme.onPrimary
                        } else if (!item.isEnabled) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        shadowElevation = if (item.isEnabled) 4.dp else 1.dp,
                        modifier = Modifier
                            .size(SATELLITE_ICON_SIZE)
                            .alpha(if (item.isEnabled) 1f else 0.38f)
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
                }
            }
        }

        Surface(
            shape = FloatingActionButtonDefaults.shape,
            color = fabContainerColor,
            contentColor = fabContentColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(FAB_SIZE)
                .combinedClickable(
                    onClickLabel = if (isExpanded) i18n("common_close") else i18n("journal_ingestion"),
                    onLongClickLabel = i18n("journal_satellite_actions"),
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

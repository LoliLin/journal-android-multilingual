/*
 * Copyright (c) 2022. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.utils.keyboard

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

/**
 * WindowInsets.ime reads from the already-tracked IME inset state, so recomposition only
 * happens when the keyboard's height actually changes. The previous implementation used an
 * OnGlobalLayoutListener that fired on every layout pass in the window (including every tab
 * switch and animation frame) and called the IPC-backed getWindowVisibleDisplayFrame() each
 * time, which was a measurable source of main-thread jank.
 */
@Composable
fun isKeyboardOpen(): State<Boolean> {
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val state = remember { mutableStateOf(imeVisible) }
    state.value = imeVisible
    return state
}

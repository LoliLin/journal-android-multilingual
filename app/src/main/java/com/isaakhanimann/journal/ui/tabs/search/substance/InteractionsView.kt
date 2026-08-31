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

package com.isaakhanimann.journal.ui.tabs.search.substance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.data.substances.classes.InteractionType
import com.isaakhanimann.journal.data.substances.classes.Interactions
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.theme.horizontalPadding
import com.isaakhanimann.journal.ui.utils.getInteractionExplanationURLForSubstance
import com.isaakhanimann.journal.ui.utils.rememberOpenLink

@Preview
@Composable
fun InteractionsPreview(
    @PreviewParameter(InteractionsPreviewProvider::class) interactions: Interactions
) {
    InteractionsView(
        interactions = interactions,
        navigateToURL = {},
        substanceURL = "",
        displayNameForSubstance = { it }
    )
}

@Composable
fun InteractionsView(
    interactions: Interactions,
    substanceURL: String,
    navigateToURL: (url: String) -> Unit,
    displayNameForSubstance: (name: String) -> String,
    navigateToSubstance: (substanceName: String) -> Unit = {},
    isSubstance: (name: String) -> Boolean = { false }
) {
    Column {
        if (interactions.dangerous.isNotEmpty()) {
            interactions.dangerous.forEach {
                InteractionRowSubstanceScreen(
                    text = displayNameForSubstance(it),
                    interactionType = InteractionType.DANGEROUS,
                    onClick = if (isSubstance(it)) {
                        { navigateToSubstance(it) }
                    } else {
                        null
                    }
                )
            }
        }
        if (interactions.unsafe.isNotEmpty()) {
            interactions.unsafe.forEach {
                InteractionRowSubstanceScreen(
                    text = displayNameForSubstance(it),
                    interactionType = InteractionType.UNSAFE,
                    onClick = if (isSubstance(it)) {
                        { navigateToSubstance(it) }
                    } else {
                        null
                    }
                )
            }
        }
        if (interactions.uncertain.isNotEmpty()) {
            interactions.uncertain.forEach {
                InteractionRowSubstanceScreen(
                    text = displayNameForSubstance(it),
                    interactionType = InteractionType.UNCERTAIN,
                    onClick = if (isSubstance(it)) {
                        { navigateToSubstance(it) }
                    } else {
                        null
                    }
                )
            }
        }
        InteractionExplanationButton(
            substanceURL = substanceURL,
            navigateToURL = navigateToURL
        )
    }
}

@Composable
fun InteractionExplanationButton(substanceURL: String, navigateToURL: ((url: String) -> Unit)? = null) {
    val openLink = rememberOpenLink()
    TextButton(onClick = {
        val interactionURL = getInteractionExplanationURLForSubstance(substanceURL)
        if (navigateToURL != null) {
            navigateToURL(interactionURL)
        } else {
            openLink(interactionURL)
        }
    }) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = i18n("interaction_open_link")
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(i18n("interaction_explanations"))
    }
}

@Composable
fun InteractionRowSubstanceScreen(
    text: String,
    interactionType: InteractionType,
    verticalPaddingInside: Dp = 2.dp,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RectangleShape,
        color = interactionType.color
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = horizontalPadding,
                vertical = verticalPaddingInside
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = i18n(interactionType.labelKey),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            LazyRow {
                items(interactionType.dangerCount) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = i18n("interaction_warning"),
                        tint = Color.Black,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

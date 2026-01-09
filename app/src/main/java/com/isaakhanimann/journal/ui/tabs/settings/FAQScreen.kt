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

package com.isaakhanimann.journal.ui.tabs.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.tabs.search.substance.SectionWithTitle
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FAQScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(i18n("faq_title")) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            QuestionAnswerRow(
                question = i18n("faq_question_sources"),
                answer = i18n("faq_answer_sources")
            )
            QuestionAnswerRow(
                question = i18n("faq_question_interactions"),
                answer = i18n("faq_answer_interactions")
            )
            QuestionAnswerRow(
                question = i18n("faq_question_changes"),
                answer = i18n("faq_answer_changes")
            )
            QuestionAnswerRow(
                question = i18n("faq_question_timeline"),
                answer = i18n("faq_answer_timeline")
            )
            QuestionAnswerRow(
                question = i18n("faq_question_dosage_dots"),
                answer = i18n("faq_answer_dosage_dots")
            )
        }
    }
}

@Composable
fun QuestionAnswerRow(question: String, answer: String) {
    SectionWithTitle(title = question) {
        Text(
            text = answer,
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .padding(top = 5.dp, bottom = 8.dp)
        )
    }
}


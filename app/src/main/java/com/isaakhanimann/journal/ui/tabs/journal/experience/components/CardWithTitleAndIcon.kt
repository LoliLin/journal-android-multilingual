package com.isaakhanimann.journal.ui.tabs.journal.experience.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Composable
fun CardWithTitleAndIcon(
    modifier: Modifier = Modifier,
    title: String,
    icon: (@Composable () -> Unit)? = null,
    innerPaddingHorizontal: Dp = 10.dp,
    content: @Composable (ColumnScope.() -> Unit)
) {
    ElevatedCard(modifier = modifier.padding(vertical = 5.dp)) {
        Column(modifier = Modifier.padding(bottom = 5.dp)) {
            Row(
                modifier = Modifier
                    .padding(vertical = 3.dp, horizontal = horizontalPadding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    icon()
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Column(
                Modifier.padding(horizontal = innerPaddingHorizontal)
            ) {
                content()
            }
        }
    }
}

@Preview
@Composable
fun CardWithTitleAndIconPreview() {
    CardWithTitleAndIcon(
        title = "Favorite",
        icon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    ) {
        Text(text = i18n("content_label"), modifier = Modifier.fillMaxWidth())
    }

    CardWithTitleAndIcon(
        title = "Profile Photo",
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        Text(text = i18n("content_label"), modifier = Modifier.fillMaxWidth())
    }

    CardWithTitleAndIcon(title = "Plain Title") {
        Text(text = i18n("content_label"), modifier = Modifier.fillMaxWidth())
    }
}
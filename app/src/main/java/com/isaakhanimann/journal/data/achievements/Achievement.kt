package com.isaakhanimann.journal.data.achievement

import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Column
import com.isaakhanimann.journal.localization.I18n
import coil.compose.AsyncImage

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class Achievement(
    val registerName: String,
    val iconPath: String
) {

    fun getLocalizedName(context: Context): String {
        return I18n.translateOrDefault(context, "achievement.$registerName", registerName)
    }

    fun getLocalizedDescription(context: Context): String {
        return I18n.translateOrDefault(context, "achievement.$registerName.desc", "")
    }
}

object AchievementList {
    private val achievements = mutableMapOf<String, Achievement>()

   
    fun register(registerName: String, iconPath: String) {
        achievements[registerName] = Achievement(registerName, iconPath)
    }

    fun get(registerName: String): Achievement? = achievements[registerName]

    fun getAll(): Map<String, Achievement> = achievements.toMap()

    init {
        register("n552aa_pr80", "drawable://n552aa_pr80")
    }
}

@Composable
fun AchievementLogoButton(registerName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val achievement = AchievementList.get(registerName)
    if (achievement != null) {
        var showDialog by remember { mutableStateOf(false) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(i18n("common_done"))
                    }
                },
                title = null,
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = achievement.iconPath,
                            contentDescription = achievement.registerName,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = achievement.getLocalizedName(context),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = achievement.getLocalizedDescription(context),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }
        Box(modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = achievement.iconPath,
                contentDescription = achievement.registerName,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

object AchievementEventBus {
    private val _events = MutableSharedFlow<Achievement>()
    val events: SharedFlow<Achievement> = _events.asSharedFlow()

    suspend fun send(achievement: Achievement) {
        _events.emit(achievement)
    }
}

@Composable
fun AchievementGetToast(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentAchievement by remember { mutableStateOf<Achievement?>(null) }

    LaunchedEffect(Unit) {
        AchievementEventBus.events.collect { achievement ->
            currentAchievement = achievement
            // 1.5秒后自动清除
            delay(1500)
            currentAchievement = null
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = currentAchievement != null,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Card(
            modifier = modifier
                .padding(end = 16.dp, top = 80.dp) 
                .wrapContentSize(),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = currentAchievement?.iconPath,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = currentAchievement?.getLocalizedName(context) ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Achievement Unlocked!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
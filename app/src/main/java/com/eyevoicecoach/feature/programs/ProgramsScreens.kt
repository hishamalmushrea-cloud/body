package com.eyevoicecoach.feature.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyevoicecoach.core.ui.CategoryPill
import com.eyevoicecoach.core.ui.CoachCard
import com.eyevoicecoach.core.ui.EmptyState
import com.eyevoicecoach.core.util.toEasternDigits
import com.eyevoicecoach.domain.catalog.ProgramDay
import com.eyevoicecoach.domain.model.Achievement
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope

/** Lists the six structured offline programs and their progress. */
@Composable
fun ProgramsScreen(items: List<ProgramWithProgress>, onOpen: (String) -> Unit, onAchievements: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("البرامج التدريبية", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("مسارات يومية جاهزة لبناء مهارة متماسكة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onAchievements) { Icon(Icons.Rounded.EmojiEvents, "الإنجازات") }
            }
        }
        items(items, key = { it.program.id }) { item ->
            val program = item.program
            val progress = item.progress
            CoachCard(onClick = { onOpen(program.id) }) {
                Text(program.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(program.description, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${program.durationDays.toString().toEasternDigits()} يوماً · ${progress.completedDays.size.toString().toEasternDigits()} مكتمل", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                LinearProgressIndicator(progress = { progress.percentage(program.durationDays) / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}

/** Shows stages and unlockable daily sessions for one program. */
@Composable
fun ProgramDetailScreen(item: ProgramWithProgress, onStart: () -> Unit, onOpenDay: (ProgramDay) -> Unit, onBack: () -> Unit) {
    val program = item.program
    val progress = item.progress
    val nextDay = progress.nextDay(program.durationDays)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
            Text(program.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(program.description, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${program.durationDays.toString().toEasternDigits()} يوماً في مراحل متدرجة", modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.primary)
            if (progress.startedAt == null) Button(onClick = onStart, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("ابدأ البرنامج") }
            else if (progress.completedAt != null) CategoryPill("✦ اكتمل البرنامج")
        }
        program.stages.forEach { stage ->
            item { Text(stage.title, modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item { Text(stage.description, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(stage.days, key = { it.number }) { day ->
                val completed = day.number in progress.completedDays
                val unlocked = progress.startedAt != null && (completed || day.number <= nextDay)
                Card(colors = CardDefaults.cardColors(containerColor = if (completed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))) {
                    Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("اليوم ${day.number.toString().toEasternDigits()} · ${day.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(if (completed) "مكتمل بعد تقييم التسجيل" else if (unlocked) "جاهز للتدريب" else "أكمل اليوم السابق لفتحه", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall)
                        }
                        if (unlocked) OutlinedButton(onClick = { onOpenDay(day) }) { Text(if (completed) "مراجعة" else "فتح") }
                    }
                }
            }
        }
    }
}

/** Displays the complete eye, body and voice instructions for one program day. */
@Composable
fun ProgramDayScreen(day: ProgramDay, onRecord: () -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
            CategoryPill("${day.stageTitle} · اليوم ${day.number.toString().toEasternDigits()}")
            Text(day.title, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(day.studyText, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge)
        }
        item { GuidanceCard("العين", day.eyeCue) }
        item { GuidanceCard("الجسد", day.bodyCue) }
        item { GuidanceCard("الصوت", day.voiceCue) }
        item { GuidanceCard("تحدي التسجيل", day.recordingChallenge) }
        item { Button(onClick = onRecord, modifier = Modifier.fillMaxWidth()) { Text("سجّل ثم قيّم هذا اليوم") } }
    }
}

/** An elegant labelled practice instruction. */
@Composable
private fun GuidanceCard(title: String, content: String) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    Text(content, modifier = Modifier.padding(top = 6.dp))
}

/** Displays earned and in-progress academic-style badges. */
@Composable
fun AchievementsScreen(items: List<Achievement>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
            Text("الإنجازات", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (items.isEmpty()) EmptyState("◇", "جارٍ إعداد إنجازاتك", "ستظهر هنا مع بدء التدريب.", Modifier.weight(1f))
        else LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = Achievement::id) { achievement ->
                CoachCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(achievement.symbol, style = MaterialTheme.typography.headlineLarge, color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        Column(Modifier.padding(start = 14.dp).weight(1f)) {
                            Text(achievement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(achievement.description, modifier = Modifier.padding(top = 3.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (achievement.isUnlocked) "مكتسبة" else "${achievement.progress.toString().toEasternDigits()} من ${achievement.target.toString().toEasternDigits()}", modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

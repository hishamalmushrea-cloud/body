package com.eyevoicecoach.feature.lifeskills

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyevoicecoach.core.ui.CoachCard
import com.eyevoicecoach.core.util.toEasternDigits
import com.eyevoicecoach.domain.lifeskills.LessonDecision
import com.eyevoicecoach.domain.lifeskills.LifeSkillsLesson
import com.eyevoicecoach.domain.lifeskills.LifeSkillsTrack
import com.eyevoicecoach.domain.model.LifeSkillsProgress

/** Shows the offline catalogue of responsible practical life-skills learning paths. */
@Composable
fun LifeSkillsScreen(
    tracks: List<LifeSkillsTrack>,
    progress: LifeSkillsProgress,
    onOpenTrack: (String) -> Unit,
    onBack: () -> Unit,
) {
    val totalLessons = tracks.sumOf { it.lessons.size }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item { Header("مسارات الحياة والمهارات", onBack) }
        item {
            CoachCard {
                Text("تعلم بهدوء، وطبّق بوعي", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("المحتوى تعليمي وتطبيقي؛ لا يقدم وعوداً مالية أو طبية أو نفسية، ولا يختصر ظروفك الشخصية في قاعدة واحدة.", modifier = Modifier.padding(top = 7.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${progress.completedLessonIds.size.toString().toEasternDigits()} من ${totalLessons.toString().toEasternDigits()} درس مكتمل", modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(progress = { progress.percentage(totalLessons) / 100f }, modifier = Modifier.fillMaxWidth().padding(top = 7.dp))
            }
        }
        items(tracks, key = LifeSkillsTrack::id) { track ->
            val completed = track.lessons.count { it.id in progress.completedLessonIds }
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenTrack(track.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            ) {
                Column(Modifier.padding(17.dp)) {
                    Text(track.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(track.description, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${completed.toString().toEasternDigits()} من ${track.lessons.size.toString().toEasternDigits()} دروس", modifier = Modifier.padding(top = 11.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    LinearProgressIndicator(progress = { completed.toFloat() / track.lessons.size }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                }
            }
        }
    }
}

/** Shows the lessons and the educational safety notice for a single track. */
@Composable
fun LifeSkillsTrackScreen(
    track: LifeSkillsTrack,
    progress: LifeSkillsProgress,
    onOpenLesson: (String) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Header(track.title, onBack)
            Text(track.description, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            CoachCard {
                Text("تنبيه مهم", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(track.safetyNotice, modifier = Modifier.padding(top = 6.dp))
            }
        }
        items(track.lessons, key = LifeSkillsLesson::id) { lesson ->
            val complete = lesson.id in progress.completedLessonIds
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenLesson(lesson.id) },
                colors = CardDefaults.cardColors(containerColor = if (complete) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("الدرس ${lesson.day.toString().toEasternDigits()} · ${lesson.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(lesson.overview, modifier = Modifier.padding(top = 5.dp), maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (complete) Icon(Icons.Rounded.CheckCircle, "مكتمل", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/** Renders deep explanation, an ethical choice exercise and private reflection for a lesson. */
@Composable
fun LifeLessonScreen(
    track: LifeSkillsTrack,
    lesson: LifeSkillsLesson,
    progress: LifeSkillsProgress,
    onComplete: (String, Int?) -> Unit,
    onBack: () -> Unit,
) {
    var reflection by rememberSaveable(lesson.id) { mutableStateOf(progress.reflections[lesson.id].orEmpty()) }
    var selectedOption by rememberSaveable(lesson.id) { mutableStateOf<Int?>(null) }
    val isCompleted = lesson.id in progress.completedLessonIds
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item {
            Header(track.title, onBack)
            Text("الدرس ${lesson.day.toString().toEasternDigits()} · ${lesson.title}", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(lesson.overview, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge)
        }
        item { LessonListCard("الفكرة الأساسية", lesson.keyIdeas) }
        item { LessonListCard("تدريب عملي", lesson.practiceSteps) }
        item { LessonTextCard("طبّق اليوم", lesson.application) }
        item {
            LessonDecisionCard(lesson.decision, selectedOption) { selectedOption = it }
        }
        item {
            CoachCard {
                Text("تأمل وتطبيق خاص", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(lesson.reflectionPrompt, modifier = Modifier.padding(top = 6.dp))
                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = { Text("ملاحظتي أو خطوتي التالية") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    minLines = 3,
                )
                Button(
                    onClick = { onComplete(reflection, selectedOption) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) { Text(if (isCompleted) "تحديث انعكاسي وحفظه" else "أكملت هذا الدرس") }
            }
        }
        item {
            Text("المحفوظ محلياً", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("لا تُرفع انعكاساتك أو اختياراتك إلى أي خادم. يمكنك حذفها من شاشة خصوصيتك.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Shows one lesson section containing concise explanatory bullets. */
@Composable
private fun LessonListCard(title: String, lines: List<String>) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    lines.forEach { line -> Text("• $line", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge) }
}

/** Shows one labelled explanatory lesson section. */
@Composable
private fun LessonTextCard(title: String, content: String) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    Text(content, modifier = Modifier.padding(top = 7.dp), style = MaterialTheme.typography.bodyLarge)
}

/** Allows a transparent, explained response choice without gamifying the user's life. */
@Composable
private fun LessonDecisionCard(decision: LessonDecision, selectedOption: Int?, onSelect: (Int) -> Unit) = CoachCard {
    Text("اختيار تطبيقي", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    Text(decision.prompt, modifier = Modifier.padding(top = 7.dp))
    Column(Modifier.padding(top = 9.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        decision.options.forEachIndexed { index, option ->
            OutlinedButton(onClick = { onSelect(index) }, modifier = Modifier.fillMaxWidth()) { Text(option) }
        }
    }
    selectedOption?.let { selected ->
        val prefix = if (selected == decision.recommendedIndex) "اختيار متزن." else "فكر في الأثر مرة أخرى."
        Text("$prefix ${decision.explanation}", modifier = Modifier.padding(top = 11.dp), color = if (selected == decision.recommendedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
    }
}

/** A consistent detail-page header with an RTL back action. */
@Composable
private fun Header(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

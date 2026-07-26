package com.eyevoicecoach.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyevoicecoach.core.ui.CoachCard
import com.eyevoicecoach.core.ui.EmptyState
import com.eyevoicecoach.domain.catalog.RealWorldSituation
import com.eyevoicecoach.domain.catalog.SpecializedDrill
import com.eyevoicecoach.domain.catalog.TrainingCatalog

/** Lists real-world speaking scenarios for private rehearsal. */
@Composable
fun SituationsScreen(onOpen: (String) -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("مواقف واقعية", "تدرب قبل الموقف الحقيقي بنص واضح وحضور متزن.", onBack) }
        items(TrainingCatalog.situations, key = RealWorldSituation::id) { situation ->
            Card(modifier = Modifier.fillMaxWidth(), onClick = { onOpen(situation.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))) {
                Column(Modifier.padding(16.dp)) {
                    Text(situation.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(situation.recordingChallenge, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
        }
    }
}

/** Gives complete rehearsal guidance for a specific real-world situation. */
@Composable
fun SituationDetailScreen(situation: RealWorldSituation?, onRecord: (Int) -> Unit, onBack: () -> Unit) {
    if (situation == null) {
        EmptyState("⌕", "الموقف غير متاح", "عد إلى مكتبة المواقف واختر موقفاً آخر.", Modifier.fillMaxSize())
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header(situation.title, "جلسة تدريب واقعية خاصة على جهازك.", onBack) }
        item { LibraryCard("نص تدريبي", situation.trainingScript) }
        item { LibraryCard("نصيحة للجسد", situation.bodyAdvice) }
        item { LibraryCard("نصيحة للصوت", situation.voiceAdvice) }
        item { LibraryCard("تحدي التسجيل", situation.recordingChallenge) }
        item { LibraryCard("أخطاء شائعة", situation.commonMistakes) }
        item { Button(onClick = { onRecord(situation.tipId) }, modifier = Modifier.fillMaxWidth()) { Text("سجّل محاكاتك ثم قيّمها") } }
    }
}

/** Groups specialized eye, body and voice drills in one clear library. */
@Composable
fun SpecializedDrillsScreen(onRecord: (Int) -> Unit, onBack: () -> Unit) {
    var category by rememberSaveable { mutableStateOf("الكل") }
    val drills = TrainingCatalog.drills.filter { category == "الكل" || it.category == category }
    Column(Modifier.fillMaxSize()) {
        Header("تمارين متخصصة", "العين والجسد والصوت في مسارات عملية واضحة.", onBack, Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("الكل", "العين", "الجسد", "الصوت")) { option -> FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option) }) }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(drills, key = SpecializedDrill::id) { drill ->
                CoachCard {
                    Text("${drill.category} · ${drill.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(drill.instruction, modifier = Modifier.padding(top = 8.dp))
                    Text("التحدي", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(drill.challenge, modifier = Modifier.padding(top = 3.dp))
                    drill.safetyNotice?.let { notice -> Text(notice, modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(onClick = { onRecord(drill.tipId) }, modifier = Modifier.fillMaxWidth().padding(top = 13.dp)) { Text("سجّل التطبيق") }
                }
            }
        }
    }
}

/** Shared library page header with a back action. */
@Composable
private fun Header(title: String, detail: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(detail, modifier = Modifier.padding(top = 3.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Labelled scenario content card. */
@Composable
private fun LibraryCard(title: String, content: String) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    Text(content, modifier = Modifier.padding(top = 7.dp))
}

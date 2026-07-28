package com.eyevoicecoach.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.time.DayOfWeek
import java.time.LocalDate
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope

/** Shows the no-repeat exercise of the day and direct paths to practice it. */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenDetail: (Int) -> Unit,
    onRecord: (Int) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPrograms: () -> Unit,
    onReset: () -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.isLoading -> androidx.compose.foundation.layout.Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.error != null -> EmptyState("⚠️", "تعذر تحميل تمرين اليوم", state.error, Modifier.fillMaxSize())
        state.tip == null -> ExhaustedContent(onReset)
        else -> {
            val tip = state.tip
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("تمرين اليوم", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("السياق المختار: ${state.context}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onOpenFavorites) { Icon(Icons.Rounded.Favorite, "المفضلة") }
                }
                OutlinedButton(onClick = onOpenPrograms, modifier = Modifier.fillMaxWidth()) { Text("البرامج التدريبية الجاهزة") }
                if (LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY) {
                    CoachCard { Text("🎤 تحدّي الجمعة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text("سجّل النص التمثيلي بعد إتقان التمرين.", modifier = Modifier.padding(top = 6.dp)) }
                }
                CoachCard(onClick = { onOpenDetail(tip.id) }) {
                    CategoryPill("${tip.category} · ${tip.context}")
                    Text(tip.text, modifier = Modifier.padding(top = 15.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text("مثال عملي", modifier = Modifier.padding(top = 20.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(tip.practicalExample, modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.bodyLarge)
                }
                CoachCard {
                    Text("تحديك اليوم", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(tip.dailyChallenge, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodyLarge)
                }
                Button(onClick = { onRecord(tip.id) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Mic, null); Text("  سجّل تطبيقك الآن") }
                OutlinedButton(onClick = { onOpenDetail(tip.id) }, modifier = Modifier.fillMaxWidth()) { Text("عرض التفاصيل وحفظها") }
                ElevatedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Refresh, null); Text("  تمرين آخر") }
            }
        }
    }
}

/** Explains that every bundled exercise has already been seen and offers a deliberate reset. */
@Composable
private fun ExhaustedContent(onReset: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        EmptyState("✨", "أكملت جميع التمارين", "لقد شاهدت كل النصائح المتاحة. يمكنك بدء دورة جديدة متى رغبت.")
        Button(onClick = onReset) { Text("إعادة ضبط السجل") }
    }
}

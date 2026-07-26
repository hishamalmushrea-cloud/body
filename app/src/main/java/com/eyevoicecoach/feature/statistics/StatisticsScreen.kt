package com.eyevoicecoach.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyevoicecoach.core.ui.CoachCard
import com.eyevoicecoach.core.util.toEasternDigits
import com.eyevoicecoach.domain.model.ProgressStats

/** Presents locally calculated, privacy-preserving practice progress. */
@Composable
fun StatisticsScreen(stats: ProgressStats) {
    val ratio = if (stats.totalTips == 0) 0f else stats.viewedCount.toFloat() / stats.totalTips
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("تقدمك", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("كل الأرقام محسوبة على جهازك فقط.", modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            CoachCard {
                Text("رحلة التمارين", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${stats.viewedCount.toString().toEasternDigits()} من ${stats.totalTips.toString().toEasternDigits()} تمريناً", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                LinearProgressIndicator(progress = { ratio.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("♡", "المفضلة", stats.favoriteCount, Modifier.weight(1f))
                MetricCard("🎙", "التسجيلات", stats.recordingCount, Modifier.weight(1f))
            }
        }
        item { Text("حسب المهارة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (stats.categoryCounts.isEmpty()) item { CoachCard { Text("ابدأ أول تمرين لتظهر هنا خريطة تقدمك.") } }
        items(stats.categoryCounts.entries.toList(), key = { it.key }) { (category, count) ->
            CoachCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(category, style = MaterialTheme.typography.titleMedium)
                    Text(count.toString().toEasternDigits(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Displays one compact statistics metric. */
@Composable
private fun MetricCard(symbol: String, label: String, count: Int, modifier: Modifier) = CoachCard(modifier) {
    Text(symbol, style = MaterialTheme.typography.headlineMedium)
    Text(count.toString().toEasternDigits(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Text(label, style = MaterialTheme.typography.bodyMedium)
}

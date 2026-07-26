package com.eyevoicecoach.feature.training

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
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
import com.eyevoicecoach.domain.model.Tip
import java.time.DayOfWeek
import java.time.LocalDate

private val categories = listOf("الكل", "العين", "الجسد", "الصوت")
private val contexts = listOf("الكل", "عام", "مقابلة عمل", "إلقاء كلمة", "اجتماع")

/** Browsable exercise library with category and situation filters. */
@Composable
fun TrainingScreen(state: TrainingUiState, onCategory: (String) -> Unit, onContext: (String) -> Unit, onOpen: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 18.dp)) {
        Text("مكتبة التمارين", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("اختر ما يناسب موقفك الحالي", modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        FilterRow(categories, state.category, onCategory)
        FilterRow(contexts, state.context, onContext)
        if (state.tips.isEmpty()) {
            EmptyState("⌕", "لا توجد تمارين بهذا الاختيار", "جرّب تصفية أخرى من القائمة.", Modifier.weight(1f))
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.tips, key = Tip::id) { tip -> TipListCard(tip, onOpen) }
            }
        }
    }
}

/** Horizontally scrolling choice row used for a training filter. */
@Composable
private fun FilterRow(items: List<String>, selected: String, onSelected: (String) -> Unit) {
    LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item -> FilterChip(selected = selected == item, onClick = { onSelected(item) }, label = { Text(item) }) }
    }
}

/** A compact library exercise card. */
@Composable
private fun TipListCard(tip: Tip, onOpen: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(tip.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            CategoryPill("${tip.category} · ${tip.context}")
            Text(tip.text, modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 3)
            Text("عرض التمرين ←", modifier = Modifier.padding(top = 9.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Full exercise details, Friday script and favorite action. */
@Composable
fun TipDetailScreen(tip: Tip?, favorite: Boolean, onFavorite: () -> Unit, onRecord: (Int) -> Unit, onBack: () -> Unit) {
    if (tip == null) {
        EmptyState("⌛", "جارٍ فتح التمرين", "لحظة واحدة من فضلك.", Modifier.fillMaxSize())
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
                IconButton(onClick = onFavorite) { Icon(if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (favorite) "إزالة من المفضلة" else "إضافة إلى المفضلة", tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
            }
            CategoryPill("${tip.category} · ${tip.context}")
            Text(tip.text, modifier = Modifier.padding(top = 15.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item { DetailCard("مثال عملي", tip.practicalExample) }
        item { DetailCard("تحدي اليوم", tip.dailyChallenge) }
        if (LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY) item { DetailCard("🎤 تحدّي الجمعة", tip.fridayScript) }
        item { ElevatedButton(onClick = { onRecord(tip.id) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Mic, null); Text("  سجّل تطبيقك") } }
    }
}

/** A labeled block in an exercise detail screen. */
@Composable
private fun DetailCard(title: String, text: String) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    Text(text, modifier = Modifier.padding(top = 7.dp), style = MaterialTheme.typography.bodyLarge)
}

/** Favorite exercises accessible without scrolling through the full library. */
@Composable
fun FavoritesScreen(favorites: List<Tip>, onOpen: (Int) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
            Text("المفضلة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        if (favorites.isEmpty()) EmptyState("♡", "لا توجد عناصر مفضلة", "احفظ أي تمرين للرجوع إليه بسرعة.", Modifier.weight(1f))
        else LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(favorites, key = Tip::id) { TipListCard(it, onOpen) }
        }
    }
}

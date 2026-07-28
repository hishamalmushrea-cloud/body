package com.eyevoicecoach.core.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.animation.core.animateFloat

/** A reusable elevated card with the app's consistent rounded visual treatment. */
@Composable
fun CoachCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) { Column(Modifier.padding(20.dp), content = content) }
}

/** A simple centered empty state for an exhausted or empty local collection. */
@Composable
fun EmptyState(icon: String, title: String, detail: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(icon, style = MaterialTheme.typography.displaySmall)
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(detail, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Draws live microphone amplitude bars; it remains visually alive when [amplitudes] are initially empty. */
@Composable
fun LiveWaveform(amplitudes: List<Float>, modifier: Modifier = Modifier) {
    val idleTransition = rememberInfiniteTransition(label = "waveform")
    val idle by idleTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "idleAmplitude",
    )
    val samples = if (amplitudes.isEmpty()) List(36) { idle } else amplitudes
    val primaryColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
    ) {
        val width = size.width / samples.size
        samples.forEachIndexed { index, sample ->
            val barHeight = (sample.coerceIn(0.04f, 1f) * size.height).coerceAtLeast(5.dp.toPx())
            drawLine(
                color = primaryColor,
                start = androidx.compose.ui.geometry.Offset(width * index + width / 2, (size.height - barHeight) / 2),
                end = androidx.compose.ui.geometry.Offset(width * index + width / 2, (size.height + barHeight) / 2),
                strokeWidth = (width * 0.52f).coerceAtMost(8.dp.toPx()),
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Displays a short category badge. */
@Composable
fun CategoryPill(label: String) {
    Box(
        Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

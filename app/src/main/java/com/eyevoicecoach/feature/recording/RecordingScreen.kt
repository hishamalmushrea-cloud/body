package com.eyevoicecoach.feature.recording

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyevoicecoach.core.ui.CoachCard
import com.eyevoicecoach.core.ui.EmptyState
import com.eyevoicecoach.core.ui.LiveWaveform
import com.eyevoicecoach.core.util.toArabicDate
import com.eyevoicecoach.core.util.toArabicDuration
import com.eyevoicecoach.core.util.toArabicPlaybackDuration
import com.eyevoicecoach.domain.model.Recording

/** Provides microphone permission, live waveform recording and complete local playback controls. */
@Composable
fun RecordingScreen(
    canRecord: Boolean,
    recordings: List<Recording>,
    playback: PlaybackState,
    amplitudes: List<Float>,
    onStart: () -> Result<Unit>,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    onTogglePlayback: (Recording) -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onDelete: (Recording) -> Unit,
    onBack: (() -> Unit)?,
) {
    var isRecording by rememberSaveable { mutableStateOf(amplitudes.isNotEmpty()) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }
    var pendingDeletion by remember { mutableStateOf<Recording?>(null) }
    val microphonePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            onStart().onSuccess { isRecording = true }.onFailure { permissionMessage = "تعذر بدء التسجيل. تأكد من أن الميكروفون متاح." }
        } else {
            permissionMessage = "يلزم السماح بالميكروفون لتسجيل تطبيقك. لا نسجل أي صوت دون موافقتك."
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
                else androidx.compose.foundation.layout.Spacer(Modifier.padding(20.dp))
                Text(if (canRecord) "تسجيل التطبيق" else "تسجيلاتك", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(Modifier.padding(20.dp))
            }
        }
        if (canRecord) {
            item {
                CoachCard {
                    Text(if (isRecording) "التسجيل جارٍ الآن" else "طبّق التمرين بصوتك", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("يُحفظ التسجيل داخل جهازك فقط، ثم يُحذف تلقائياً بعد ٧ أيام.", modifier = Modifier.padding(top = 7.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LiveWaveform(amplitudes, Modifier.padding(top = 18.dp))
                    if (isRecording) {
                        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { onStop(); isRecording = false }, modifier = Modifier.weight(1f)) { Icon(Icons.Rounded.Stop, null); Text("  إنهاء وحفظ") }
                            OutlinedButton(onClick = { onCancel(); isRecording = false }, modifier = Modifier.weight(1f)) { Text("إلغاء") }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                                else onStart().onSuccess { isRecording = true }.onFailure { permissionMessage = "تعذر بدء التسجيل." }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        ) { Icon(Icons.Rounded.Mic, null); Text("  ابدأ التسجيل") }
                    }
                }
            }
        } else {
            item { CoachCard { Text("اختر تمريناً أولاً", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("افتح أي تمرين من الرئيسية أو المكتبة، ثم اختر تسجيل التطبيق لحفظ محاولتك.", modifier = Modifier.padding(top = 6.dp)) } }
        }
        item { Text("التسجيلات المحفوظة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (recordings.isEmpty()) item { EmptyState("🎙", "لا توجد تسجيلات بعد", "ستظهر محاولاتك هنا عند حفظها.") }
        items(recordings, key = Recording::id) { recording ->
            RecordingCard(recording, playback, onTogglePlayback, onSeekBy, onSeekTo, onDelete = { pendingDeletion = recording })
        }
    }
    permissionMessage?.let { message ->
        AlertDialog(onDismissRequest = { permissionMessage = null }, confirmButton = { Button(onClick = { permissionMessage = null }) { Text("حسناً") } }, title = { Text("إذن الميكروفون") }, text = { Text(message) })
    }
    pendingDeletion?.let { recording ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("حذف التسجيل؟") },
            text = { Text("سيُحذف الملف من جهازك نهائياً.") },
            confirmButton = { Button(onClick = { onDelete(recording); pendingDeletion = null }) { Text("حذف") } },
            dismissButton = { OutlinedButton(onClick = { pendingDeletion = null }) { Text("إلغاء") } },
        )
    }
}

/** A private audio row with play, pause, seeking and explicit deletion controls. */
@Composable
private fun RecordingCard(
    recording: Recording,
    playback: PlaybackState,
    onTogglePlayback: (Recording) -> Unit,
    onSeekBy: (Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    onDelete: () -> Unit,
) {
    val active = playback.activeUri == recording.uri
    val max = if (active) playback.durationMs.coerceAtLeast(1) else recording.durationSeconds * 1000L.coerceAtLeast(1)
    val position = if (active) playback.positionMs.coerceIn(0, max) else 0L
    CoachCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("تطبيق مسجل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${recording.recordedAt.toArabicDate()} · ${recording.durationSeconds.toArabicDuration()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Rounded.DeleteOutline, "حذف التسجيل") }
        }
        Slider(value = position.toFloat(), onValueChange = { onSeekTo(it.toLong()) }, valueRange = 0f..max.toFloat(), modifier = Modifier.fillMaxWidth().padding(top = 8.dp), enabled = active)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${position.toArabicPlaybackDuration()} / ${max.toArabicPlaybackDuration()}", style = MaterialTheme.typography.labelMedium)
            Row {
                IconButton(onClick = { onSeekBy(-10_000) }, enabled = active) { Icon(Icons.Rounded.Replay10, "تأخير ١٠ ثوانٍ") }
                IconButton(onClick = { onTogglePlayback(recording) }) { Icon(if (active && playback.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, if (active && playback.isPlaying) "إيقاف مؤقت" else "تشغيل") }
                IconButton(onClick = { onSeekBy(10_000) }, enabled = active) { Icon(Icons.Rounded.Forward10, "تقديم ١٠ ثوانٍ") }
            }
        }
    }
}

package com.eyevoicecoach.feature.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eyevoicecoach.core.ui.CoachCard
import com.eyevoicecoach.core.util.toEasternDigits
import com.eyevoicecoach.domain.model.AppTheme
import com.eyevoicecoach.domain.model.UserSettings
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope

private val contextOptions = listOf("عام", "مقابلة عمل", "إلقاء كلمة", "اجتماع")

/** Controls the chosen daily context, app theme and consent-based reminder time. */
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onContext: (String) -> Unit,
    onTheme: (String) -> Unit,
    onReminder: (Boolean, Int, Int) -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
) {
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        onReminder(granted, settings.reminderHour, settings.reminderMinute)
    }
    val chooseTime = remember(settings.reminderHour, settings.reminderMinute) {
        TimePickerDialog(context, { _, hour, minute -> onReminder(settings.remindersEnabled, hour, minute) }, settings.reminderHour, settings.reminderMinute, true)
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("الإعدادات", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item {
            CoachCard {
                Text("سياق تمرين اليوم", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("يتغير تمرين الغد وفقاً لاختيارك. إذا نفدت الخيارات، نعود تلقائياً إلى العام.", modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    contextOptions.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { option -> FilterChip(selected = settings.selectedContext == option, onClick = { onContext(option) }, label = { Text(option) }) }
                        }
                    }
                }
            }
        }
        item {
            CoachCard {
                Text("المظهر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("اختر ثيمة هادئة تناسب تركيزك.", modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    AppTheme.entries.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { theme -> FilterChip(selected = settings.theme == theme, onClick = { onTheme(theme.key) }, label = { Text(theme.label) }) }
                        }
                    }
                }
            }
        }
        item {
            CoachCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("تذكير يومي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("اشعار خاص لممارسة دقيقة واحدة يومياً.", modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.remindersEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) onReminder(false, settings.reminderHour, settings.reminderMinute)
                            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            else onReminder(true, settings.reminderHour, settings.reminderMinute)
                        },
                    )
                }
                if (settings.remindersEnabled) {
                    OutlinedButton(onClick = { chooseTime.show() }, modifier = Modifier.fillMaxWidth().padding(top = 13.dp)) {
                        Text("وقت التذكير: ${formatTime(settings.reminderHour, settings.reminderMinute)}")
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AssistChip(
                            onClick = { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) },
                            label = { Text("السماح بمنبه دقيق") },
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
        item {
            CoachCard {
                Text("الخصوصية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("لا نستخدم الإنترنت ولا حسابات سحابية. التسجيلات في التخزين الداخلي الخاص بالتطبيق، وتُحذف تلقائياً بعد ٧ أيام.", modifier = Modifier.padding(top = 7.dp))
                OutlinedButton(onClick = onPrivacy, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("خصوصيتك وإدارة البيانات") }
            }
        }
        item { OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) { Text("حول التطبيق") } }
    }
}

/** Shows Arabic-Indic local clock digits. */
private fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(hour, minute).toEasternDigits()

/** App information and an explicit summary of the offline privacy model. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
        Text("حول التطبيق", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        CoachCard {
            Text("مدرب العين والجسد والصوت", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("إصدار ١.٥.٠", modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("أداة تدريب شخصية عربية تعمل دون اتصال لمساعدتك على بناء حضور هادئ وواثق.", modifier = Modifier.padding(top = 16.dp))
        }
        CoachCard {
            Text("خصوصيتك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("البيانات والتفضيلات مشفرة محلياً. لا يغادر صوتك جهازك مطلقاً، وتطبق سياسة حذف إلزامية بعد ٧ أيام على التسجيلات.", modifier = Modifier.padding(top = 7.dp))
        }
    }
}

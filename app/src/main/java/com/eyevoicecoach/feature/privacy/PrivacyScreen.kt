package com.eyevoicecoach.feature.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

/** A transparent privacy page with granular deletion controls and an irreversible full reset. */
@Composable
fun PrivacyScreen(
    message: String?,
    onDeleteRecordings: () -> Unit,
    onDeleteHistory: () -> Unit,
    onDeleteAssessments: () -> Unit,
    onDeleteSocialTraining: () -> Unit,
    onResetAll: () -> Unit,
    onClearMessage: () -> Unit,
    onBack: () -> Unit,
) {
    var requestedAction by rememberSaveable { mutableStateOf<PrivacyAction?>(null) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
            Text("خصوصيتك", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        CoachCard {
            Text("بياناتك تبقى لك", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            PrivacyLine("لا نستخدم الإنترنت أو خدمات سحابية.")
            PrivacyLine("لا نرفع التسجيلات أو نشاركها.")
            PrivacyLine("تبقى التسجيلات في جهازك داخل التخزين الخاص بالتطبيق.")
            PrivacyLine("تُحذف التسجيلات تلقائياً بعد ٧ أيام.")
            PrivacyLine("يمكنك حذف بياناتك في أي وقت من هذه الصفحة.")
        }
        Text("إدارة بياناتي", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        DataActionCard("حذف التسجيلات", "يحذف كل ملفات الصوت الخاصة وسجلاتها.") { requestedAction = PrivacyAction.RECORDINGS }
        DataActionCard("حذف السجل والمفضلة", "يعيد دورة النصائح ويزيل التمارين المحفوظة.") { requestedAction = PrivacyAction.HISTORY }
        DataActionCard("حذف التقييمات الذاتية", "يحذف درجاتك وملاحظاتك من دون حذف الصوت.") { requestedAction = PrivacyAction.ASSESSMENTS }
        DataActionCard("حذف بيانات التدريب الاجتماعي", "يحذف جلسات التواصل المؤثر وتقييماتها ودفعاتها المحفوظة.") { requestedAction = PrivacyAction.SOCIAL }
        CoachCard {
            Text("إعادة التطبيق كبداية جديدة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text("تحذف التسجيلات والسجل والمفضلة والتقييمات والبرامج وجلسات التواصل والإعدادات. تبقى مكتبة التمارين المدمجة فقط.", modifier = Modifier.padding(top = 6.dp))
            Button(onClick = { requestedAction = PrivacyAction.RESET }, modifier = Modifier.fillMaxWidth().padding(top = 13.dp)) { Icon(Icons.Rounded.DeleteForever, null); Text("  حذف جميع بياناتي") }
        }
    }
    message?.let { text ->
        AlertDialog(onDismissRequest = onClearMessage, title = { Text("إدارة البيانات") }, text = { Text(text) }, confirmButton = { Button(onClick = onClearMessage) { Text("حسناً") } })
    }
    requestedAction?.let { action ->
        AlertDialog(
            onDismissRequest = { requestedAction = null },
            title = { Text(action.title) },
            text = { Text(action.confirmation) },
            confirmButton = {
                Button(onClick = {
                    when (action) {
                        PrivacyAction.RECORDINGS -> onDeleteRecordings()
                        PrivacyAction.HISTORY -> onDeleteHistory()
                        PrivacyAction.ASSESSMENTS -> onDeleteAssessments()
                        PrivacyAction.SOCIAL -> onDeleteSocialTraining()
                        PrivacyAction.RESET -> onResetAll()
                    }
                    requestedAction = null
                }) { Text("تأكيد الحذف") }
            },
            dismissButton = { OutlinedButton(onClick = { requestedAction = null }) { Text("إلغاء") } },
        )
    }
}

/** One concise statement on the privacy promise. */
@Composable
private fun PrivacyLine(text: String) = Text("• $text", modifier = Modifier.padding(top = 9.dp), style = MaterialTheme.typography.bodyLarge)

/** One granular deletion option. */
@Composable
private fun DataActionCard(title: String, detail: String, onClick: () -> Unit) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(detail, modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text("حذف") }
}

private enum class PrivacyAction(val title: String, val confirmation: String) {
    RECORDINGS("حذف التسجيلات", "سيُحذف كل ملف صوتي محفوظ نهائياً من جهازك."),
    HISTORY("حذف السجل والمفضلة", "سيُحذف سجل التمارين والمفضلة، ولا يمكن استعادته."),
    ASSESSMENTS("حذف التقييمات", "سيُحذف تقييم كل تسجيل وملاحظاته الشخصية."),
    SOCIAL("حذف بيانات التدريب الاجتماعي", "سيُحذف سجل سيناريوهات التواصل وتقييماتها الخاصة."),
    RESET("إعادة التطبيق", "سيُحذف كل ما أنشأته داخل التطبيق ويعود كما كان عند البداية."),
}

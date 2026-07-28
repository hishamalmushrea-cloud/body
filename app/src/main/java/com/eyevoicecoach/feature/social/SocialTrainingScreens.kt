package com.eyevoicecoach.feature.social

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
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.eyevoicecoach.core.ui.EmptyState
import com.eyevoicecoach.core.util.toEasternDigits
import com.eyevoicecoach.domain.model.CommunicationStyle
import com.eyevoicecoach.domain.model.DailyCommunicationBoost
import com.eyevoicecoach.domain.model.PhraseType
import com.eyevoicecoach.domain.model.SocialProgressStats
import com.eyevoicecoach.domain.model.SocialChoiceExercise
import com.eyevoicecoach.domain.social.SocialSkillTracks
import com.eyevoicecoach.domain.model.SocialTrainingContent
import com.eyevoicecoach.domain.model.UserSettings

private val scenarioOptions = listOf(
    "الكل" to null,
    "حديث عابر محترم" to "public_micro_interaction",
    "تعارف مهني" to "networking",
    "لقاء عميل" to "client_meeting",
    "مقابلة عمل" to "job_interview",
    "اجتماع فريق" to "team_meeting",
    "أسئلة بعد عرض" to "presentation_qna",
    "تعارف اجتماعي محترم" to "social_introduction",
    "اعتراض أو خلاف" to "conflict_resolution",
    "اعتراض عميل" to "sales_objection",
    "حوار قيادي" to "leadership_conversation",
    "السوق وخدمة الزبائن" to "market_service",
    "تعامل يومي" to "neighborhood_daily",
    "مزاح خفيف مع الأصدقاء" to "friend_banter",
)

private val trackOptions = listOf("الكل" to null) + SocialSkillTracks.all.map { it.title to it.id }

private val goalOptions = listOf(
    "الكل" to null,
    "بدء الحديث" to "start_respectful_conversation",
    "بناء ثقة" to "build_professional_rapport",
    "إقناع بفكرة" to "understand_need_before_offer",
    "الرد على اعتراض" to "respond_to_objection_respectfully",
    "إنهاء الحديث بلطف" to "end_conversation_kindly",
    "طلب توضيح" to "ask_for_clarification",
    "ترحيب عميل" to "welcome_customer",
    "شرح سعر" to "explain_price",
    "تفاوض عادل" to "negotiate_fairly",
    "تحية ودّية" to "friendly_greeting",
    "عرض مساعدة" to "offer_simple_help",
    "مزاح خفيف" to "light_joke_about_saving",
)

/** Scenario chooser for responsible social influence and communication practice. */
@Composable
fun SocialTrainingScreen(
    settings: UserSettings,
    boost: DailyCommunicationBoost?,
    content: List<SocialTrainingContent>,
    selectedStyle: CommunicationStyle?,
    onScenario: (String?) -> Unit,
    onGoal: (String?) -> Unit,
    onStyle: (CommunicationStyle?) -> Unit,
    onTrack: (String?) -> Unit,
    onRefreshBoost: () -> Unit,
    onAcknowledgeEthics: () -> Unit,
    onOpenCard: (Int, CommunicationStyle?) -> Unit,
    onStyleAssistant: () -> Unit,
    onBack: () -> Unit,
) {
    var selectedScenario by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGoal by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTrack by rememberSaveable { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
                Column(Modifier.weight(1f)) {
                    Text("الذكاء الاجتماعي", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("افهم الناس، اكسب الود، ابنِ احترامك، وتصرّف بثقة ولباقة.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item { CoachCard { Text("كن لطيفًا بلا ضعف، وواثقًا بلا غرور، واجتماعيًا بلا تصنّع.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) } }
        item {
            CoachCard {
                Text("دفعة اليوم", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(boost?.text ?: "أكملت دفعات هذه الدورة. يمكنك بدء دورة جديدة من الخصوصية عند الحاجة.", modifier = Modifier.padding(top = 6.dp))
                OutlinedButton(onClick = onRefreshBoost, modifier = Modifier.fillMaxWidth().padding(top = 11.dp)) { Text("دفعة أخرى") }
            }
        }
        item { FilterSection("مسار فن التعامل", trackOptions, selectedTrack, onSelect = { selectedTrack = it; onTrack(it) }) }
        item { FilterSection("اختر الموقف", scenarioOptions, selectedScenario, onSelect = { selectedScenario = it; onScenario(it) }) }
        item { FilterSection("اختر الهدف", goalOptions, selectedGoal, onSelect = { selectedGoal = it; onGoal(it) }) }
        item {
            CoachCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("نمط التواصل اختياري", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("اختيار أسلوب مناسب، وليس حكماً على شخصية أي إنسان.", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = onStyleAssistant) { Icon(Icons.Rounded.Psychology, "مساعد الأنماط") }
                }
                LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item { FilterChip(selected = selectedStyle == null, onClick = { onStyle(null) }, label = { Text("غير محدد") }) }
                    items(CommunicationStyle.entries.filter { it != CommunicationStyle.UNKNOWN }) { style -> FilterChip(selected = selectedStyle == style, onClick = { onStyle(style) }, label = { Text(style.label.substringBefore(" /")) }) }
                }
            }
        }
        item { Text("بطاقات التدريب", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (content.isEmpty()) item { EmptyState("⌕", "لا توجد بطاقة بهذا الاختيار", "خفف التصفية أو اختر أسلوباً غير محدد.") }
        items(content, key = SocialTrainingContent::id) { card ->
            Card(modifier = Modifier.fillMaxWidth(), onClick = { onOpenCard(card.id, selectedStyle) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))) {
                Column(Modifier.padding(16.dp)) {
                    Text(card.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(card.tipText, modifier = Modifier.padding(top = 6.dp), maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${card.durationMinutes.toString().toEasternDigits()} دقائق · ${SocialSkillTracks.title(card.socialTrack)}", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
    if (!settings.isSocialEthicsAcknowledged) {
        EthicsDialog(onAccept = onAcknowledgeEthics, onLeave = onBack)
    }
}

/** Full interactive ethical role-play card. */
@Composable
fun SocialTrainingCardScreen(content: SocialTrainingContent?, choice: SocialChoiceExercise?, selectedStyle: CommunicationStyle, onRecord: (Int, CommunicationStyle) -> Unit, onBack: () -> Unit) {
    if (content == null) {
        EmptyState("⌛", "جارٍ تجهيز البطاقة", "لحظة واحدة من فضلك.", Modifier.fillMaxSize())
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
            Text(content.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("${SocialSkillTracks.title(content.socialTrack)} · ${scenarioLabel(content.scenarioType)}", modifier = Modifier.padding(top = 5.dp), color = MaterialTheme.colorScheme.primary)
        }
        item { SocialCardSection("الموقف والهدف", content.tipText) }
        item { SocialCardSection("مثال عملي", content.practicalExample) }
        item { SocialCardSection("قاعدة الاحترام", content.boundaryRule) }
        if (selectedStyle != CommunicationStyle.UNKNOWN) item { SocialCardSection("تكييف أسلوبك", selectedStyle.guidance) }
        item { SocialCardSection("العين", content.eyeContactTip) }
        item { SocialCardSection("الجسد", content.bodyLanguageTip) }
        item { SocialCardSection("الصوت", content.voiceTip) }
        PhraseType.entries.forEach { type ->
            val phrases = content.phrases.filter { it.type == type }
            if (phrases.isNotEmpty()) item { SocialCardSection(type.label, phrases.joinToString("\n") { "• ${it.text}" }) }
        }
        choice?.let { exercise -> item { SocialChoiceCard(exercise) } }
        item { SocialCardSection("تحدي التسجيل", content.roleplayPrompt) }
        item { Button(onClick = { onRecord(content.id, selectedStyle) }, modifier = Modifier.fillMaxWidth()) { Text("سجّل محاكاتك ثم قيّمها") } }
    }
}

/** Shows the four non-diagnostic communication styles and a lightweight observation helper. */
@Composable
fun CommunicationStyleGuideScreen(onSelect: (CommunicationStyle) -> Unit, onBack: () -> Unit) {
    var result by rememberSaveable { mutableStateOf<CommunicationStyle?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
            Text("أنماط التواصل", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("هذه الأنماط تساعدك على اختيار أسلوب تواصل مناسب، ولا تمثل حكماً نهائياً على شخصية أي إنسان.", modifier = Modifier.padding(top = 7.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(CommunicationStyle.entries.filter { it != CommunicationStyle.UNKNOWN }, key = CommunicationStyle::code) { style ->
            CoachCard {
                Text(style.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(style.guidance, modifier = Modifier.padding(top = 6.dp))
                OutlinedButton(onClick = { onSelect(style) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("اختيار هذا الأسلوب") }
            }
        }
        item {
            CoachCard {
                Text("مساعد ملاحظة بسيط", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("اختر الوصف الأقرب للسياق، ثم استخدم الاقتراح كمرونة لا كتصنيف ثابت.", modifier = Modifier.padding(top = 6.dp))
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { result = CommunicationStyle.DIRECT }) { Text("يتحدث بسرعة ويركز على النتيجة") }
                    Button(onClick = { result = CommunicationStyle.ANALYTICAL }) { Text("يطلب أرقاماً وتفاصيل") }
                    Button(onClick = { result = CommunicationStyle.EXPRESSIVE }) { Text("يركز على المشاعر والعلاقة") }
                    Button(onClick = { result = CommunicationStyle.SUPPORTIVE }) { Text("يحتاج وقتاً وطمأنة قبل القرار") }
                }
                result?.let { style ->
                    Text("قد يكون الأسلوب الأنسب: ${style.label}. ${style.guidance}", modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/** Local social-practice statistics card list. */
@Composable
fun SocialStatisticsScreen(stats: SocialProgressStats, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { HeaderRow("تقدم التواصل", onBack) }
        item { SocialCardSection("جلسات مكتملة", stats.completedSessions.toString().toEasternDigits()) }
        item { SocialCardSection("أكثر موقف تدربت عليه", stats.mostPracticedScenario?.let(::scenarioLabel) ?: "ابدأ جلسة تدريبية لتظهر النتيجة.") }
        item { SocialCardSection("أكثر مهارة مارستها", stats.strongestModule?.let(::moduleLabel) ?: "لا توجد بيانات بعد.") }
        item { SocialCardSection("متوسط الثقة والاحترام والهدوء", "الثقة ${stats.averageConfidence.toInt().toString().toEasternDigits()} / ٥ · الاحترام ${stats.averageRespect.toInt().toString().toEasternDigits()} / ٥ · الهدوء ${stats.averageCalmness.toInt().toString().toEasternDigits()} / ٥") }
        item { SocialCardSection("تدريب التعامل مع الرفض", stats.rejectionPracticeCount.toString().toEasternDigits()) }
        item { SocialCardSection("الأسلوب المختار غالباً", stats.mostSelectedStyle?.label ?: "غير محدد") }
        item { SocialCardSection("سلسلة التدريب الاجتماعي", "${stats.currentStreak.toString().toEasternDigits()} أيام") }
    }
}

/** Lets the user compare respectful responses before recording the role-play. */
@Composable
private fun SocialChoiceCard(choice: SocialChoiceExercise) {
    var selectedIndex by rememberSaveable(choice.id) { mutableStateOf<Int?>(null) }
    CoachCard {
        Text("اختيار تفاعلي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(choice.prompt, modifier = Modifier.padding(top = 7.dp))
        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            choice.options.forEachIndexed { index, option ->
                OutlinedButton(onClick = { selectedIndex = index }, modifier = Modifier.fillMaxWidth()) { Text(option) }
            }
        }
        selectedIndex?.let { index ->
            val result = if (index == choice.preferredIndex) "اختيار متزن ومحترم. ${choice.explanation}" else "هذا الخيار قد لا يحافظ على الهدوء أو الحدود. ${choice.explanation}"
            Text(result, modifier = Modifier.padding(top = 10.dp), color = if (index == choice.preferredIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}

/** Horizontal filter section for scenarios and objectives. */
@Composable
private fun FilterSection(title: String, values: List<Pair<String, String?>>, selected: String?, onSelect: (String?) -> Unit) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    LazyRow(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(values) { (label, value) -> FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) }) }
    }
}

/** A labelled card section for social scenario guidance. */
@Composable
private fun SocialCardSection(title: String, text: String) = CoachCard {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Text(text, modifier = Modifier.padding(top = 6.dp))
}

/** Mandatory first-use ethical consent notice. */
@Composable
private fun EthicsDialog(onAccept: () -> Unit, onLeave: () -> Unit) {
    AlertDialog(
        onDismissRequest = onLeave,
        title = { Text("تدريب مسؤول ومحترم") },
        text = { Text("هذا التدريب مخصص للتجربة الشخصية والتسجيل الذاتي فقط. لا تسجل أشخاصاً آخرين دون إذنهم، واحترم خصوصية وحدود الجميع. إذا لم يبدُ الطرف الآخر مهتماً، أنهِ الحديث بلطف.") },
        confirmButton = { Button(onClick = onAccept) { Text("أفهم وأوافق") } },
        dismissButton = { OutlinedButton(onClick = onLeave) { Text("رجوع") } },
    )
}

/** Standard title row for social detail pages. */
@Composable
private fun HeaderRow(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, "رجوع") }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

private fun moduleLabel(value: String): String = mapOf(
    "rapport" to "بناء الألفة", "ethical_persuasion" to "الإقناع الأخلاقي", "social_opening" to "بداية المحادثة", "client_communication" to "تواصل العملاء", "personality_adaptation" to "التكيّف مع أنماط التواصل", "active_listening" to "الإصغاء النشط", "objection_handling" to "التعامل مع الاعتراض", "conflict_deescalation" to "تهدئة التوتر", "boundaries" to "احترام الحدود", "negotiation" to "التفاوض", "leadership_conversation" to "الحوار القيادي",
)[value] ?: value

private fun scenarioLabel(value: String): String = scenarioOptions.firstOrNull { it.second == value }?.first ?: value

package com.eyevoicecoach.domain.social

import com.eyevoicecoach.domain.model.SocialSkillTrack
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Curated Arabic and Gulf-appropriate paths for practical social conduct. */
object SocialSkillTracks {
    /** Ten visible paths used to organise فن التعامل cards. */
    val all = listOf(
        SocialSkillTrack("social_basics", "أساسيات الاجتماعية", "السلام، فتح الموضوع، دخول المجموعة، الصمت، وإنهاء الحوار."),
        SocialSkillTrack("elders", "التعامل مع الأكبر سنًا", "احترام واثق، ألقاب مناسبة، اختلاف مهذب، وطلب نصيحة."),
        SocialSkillTrack("rapport", "بناء الود", "ألفة طبيعية، مجاملة صادقة، واهتمام بلا تصنّع."),
        SocialSkillTrack("respect", "التصرف باحترام", "وضوح، التزام، قول لا بأدب، واحترام الوقت."),
        SocialSkillTrack("understanding_needs", "فهم الاحتياج", "هل يريد الشخص إصغاء أم حلاً؟ وكيف تسأل قبل الافتراض."),
        SocialSkillTrack("social_signals", "قراءة الإشارات الاجتماعية", "ملاحظة الراحة أو عدم الاهتمام من دون ادعاء معرفة المشاعر."),
        SocialSkillTrack("anger", "التعامل مع الغضب", "خفض التصعيد، نبرة هادئة، وحدود واضحة."),
        SocialSkillTrack("rejection", "التعامل مع الرفض", "قبول الرفض بهدوء واحترام حرية الطرف الآخر."),
        SocialSkillTrack("council_etiquette", "آداب المجالس", "الدخول، الجلوس، المشاركة والخروج بأدب."),
        SocialSkillTrack("boundaries", "الحدود الشخصية", "لطف بلا تعلق زائد، ومساحة مريحة للجميع."),
        SocialSkillTrack("customer_care", "خدمة الزبائن الودّية", "ترحيب، شرح سعر، تفاوض عادل، شكوى، ومتابعة بإذن."),
        SocialSkillTrack("daily_friendliness", "الودّ والمزاح الخفيف", "تحية يومية، دعوات بلا إلزام، مزاح متبادل وحدود مريحة."),
    )

    /** Finds a path title by identifier. */
    fun title(id: String): String = all.firstOrNull { it.id == id }?.title ?: id
}

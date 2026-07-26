package com.eyevoicecoach.domain.social

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateContentEthicsUseCaseTest {
    private val validate = ValidateContentEthicsUseCase()

    @Test
    fun `ethical wording that protects a right to refuse is accepted`() {
        assertTrue(validate(listOf("إذا لم يبدُ الطرف الآخر مهتماً، أنهِ الحديث بلطف.", "اسأل عن الاحتياج قبل عرض الفكرة.")))
    }

    @Test
    fun `coercive or deceptive wording is rejected`() {
        assertFalse(validate(listOf("اضغط عليه حتى يوافق")))
        assertFalse(validate(listOf("اخدعه ليقبل العرض")))
    }
}

package com.eyevoicecoach.domain.usecase

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingRetentionPolicyTest {
    @Test
    fun `recording exactly seven days old is retained until it is older`() {
        val now = 10_000_000_000L
        val exactlySevenDays = now - TimeUnit.DAYS.toMillis(7)

        assertFalse(RecordingRetentionPolicy.isExpired(exactlySevenDays, now))
        assertTrue(RecordingRetentionPolicy.isExpired(exactlySevenDays - 1, now))
    }
}

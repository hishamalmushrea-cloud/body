package com.eyevoicecoach.domain.social

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialSkillTracksTest {
    @Test
    fun `social skills navigation includes customer care and friendly daily interaction`() {
        assertEquals(12, SocialSkillTracks.all.size)
        assertTrue(SocialSkillTracks.all.any { it.id == "customer_care" })
        assertTrue(SocialSkillTracks.all.any { it.id == "daily_friendliness" })
    }
}

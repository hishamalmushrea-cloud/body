package com.eyevoicecoach.domain.lifeskills

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeSkillsCatalogTest {
    @Test
    fun `catalogue contains seven distinct applied learning tracks and thirty lessons`() {
        assertEquals(7, LifeSkillsCatalog.tracks.size)
        assertEquals(30, LifeSkillsCatalog.tracks.sumOf { it.lessons.size })
        assertTrue(LifeSkillsCatalog.tracks.map { it.id }.toSet().size == LifeSkillsCatalog.tracks.size)
    }

    @Test
    fun `every lesson has steps reflection and a transparent recommended decision`() {
        LifeSkillsCatalog.tracks.flatMap { it.lessons }.forEach { lesson ->
            assertTrue(lesson.keyIdeas.isNotEmpty())
            assertTrue(lesson.practiceSteps.isNotEmpty())
            assertTrue(lesson.reflectionPrompt.isNotBlank())
            assertTrue(lesson.decision.recommendedIndex in lesson.decision.options.indices)
            assertNotNull(LifeSkillsCatalog.lesson(lesson.id))
        }
    }
}

package com.eyevoicecoach.domain.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingCatalogTest {
    @Test
    fun `every ready-made program has a complete sequential daily plan`() {
        assertEquals(6, TrainingCatalog.programs.size)
        TrainingCatalog.programs.forEach { program ->
            assertEquals((1..program.durationDays).toList(), program.days.map { it.number })
            assertTrue(program.stages.isNotEmpty())
        }
    }

    @Test
    fun `situation and specialized drill libraries cover all coaching pillars`() {
        assertEquals(10, TrainingCatalog.situations.size)
        assertTrue(setOf("العين", "الجسد", "الصوت").all { category -> TrainingCatalog.drills.any { it.category == category } })
    }
}

package com.eyevoicecoach.data.repository

import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.domain.model.Achievement
import com.eyevoicecoach.domain.model.ProgramProgress
import com.eyevoicecoach.domain.repository.AchievementRepository
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Computes refined achievements from local history, recordings and program progress only. */
class AchievementRepositoryImpl @Inject constructor(
    database: CoachDatabase,
    private val programProgress: com.eyevoicecoach.domain.repository.ProgramProgressRepository,
) : AchievementRepository {
    private val tips = database.tipDao()
    private val recordings = database.recordingDao()

    override fun observeAchievements(): Flow<List<Achievement>> = combine(
        tips.observeShownDates(),
        recordings.observeAll(),
        programProgress.observeProgress(),
    ) { shownDates, recordingRows, programs ->
        val longestStreak = longestStreak(shownDates)
        val fridayChallenges = recordingRows.count { row ->
            Instant.ofEpochMilli(row.dateRecorded).atZone(ZoneId.systemDefault()).dayOfWeek.value == 5
        }
        val speakerComplete = programs.any { it.programId == "public_speaking" && it.completedAt != null }
        listOf(
            Achievement("first_step", "شارة البداية", "أكملت أول تمرين في مسارك.", "◈", shownDates.isNotEmpty(), shownDates.size.coerceAtMost(1), 1),
            Achievement("clear_voice", "شارة الصوت الواضح", "سجلت خمس جلسات تدريبية خاصة.", "◉", recordingRows.size >= 5, recordingRows.size.coerceAtMost(5), 5),
            Achievement("steadfast", "شارة الثبات", "تدربت سبعة أيام متتالية.", "◆", longestStreak >= 7, longestStreak.coerceAtMost(7), 7),
            Achievement("speaker", "شارة المتحدث", "أكملت برنامج الإلقاء أمام الجمهور.", "✦", speakerComplete, if (speakerComplete) 1 else 0, 1),
            Achievement("friday", "شارة تحدي الجمعة", "أكملت أربعة تسجيلات يوم الجمعة.", "⬟", fridayChallenges >= 4, fridayChallenges.coerceAtMost(4), 4),
        )
    }

    private fun longestStreak(dates: List<Long>): Int {
        val uniqueDays = dates.map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }.distinct().sorted()
        if (uniqueDays.isEmpty()) return 0
        var current = 1
        var best = 1
        uniqueDays.zipWithNext().forEach { (previous, next) ->
            if (previous.plusDays(1) == next) current += 1 else current = 1
            best = maxOf(best, current)
        }
        return best
    }
}

package com.eyevoicecoach.data.repository

import androidx.room.withTransaction
import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.ProgramDayCompletionEntity
import com.eyevoicecoach.data.local.ProgramProgressEntity
import com.eyevoicecoach.domain.catalog.TrainingCatalog
import com.eyevoicecoach.domain.model.ProgramProgress
import com.eyevoicecoach.domain.repository.ProgramProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Room repository that keeps program completion entirely on the device. */
class ProgramProgressRepositoryImpl @Inject constructor(
    private val database: CoachDatabase,
) : ProgramProgressRepository {
    private val progress = database.programProgressDao()

    override fun observeProgress(): Flow<List<ProgramProgress>> = combine(
        progress.observeProgress(),
        progress.observeCompletions(),
    ) { rows, completions ->
        rows.map { row ->
            ProgramProgress(
                programId = row.programId,
                completedDays = completions.filter { it.programId == row.programId }.map { it.dayNumber }.toSet(),
                startedAt = row.startedAt,
                completedAt = row.completedAt,
            )
        }
    }

    override suspend fun start(programId: String) {
        requireNotNull(TrainingCatalog.program(programId)) { "البرنامج غير موجود" }
        progress.start(ProgramProgressEntity(programId, System.currentTimeMillis()))
    }

    override suspend fun completeDay(programId: String, dayNumber: Int, recordingId: Long) = database.withTransaction {
        val program = requireNotNull(TrainingCatalog.program(programId)) { "البرنامج غير موجود" }
        require(dayNumber in 1..program.durationDays) { "رقم يوم غير صالح" }
        progress.start(ProgramProgressEntity(programId, System.currentTimeMillis()))
        progress.completeDay(ProgramDayCompletionEntity(programId, dayNumber, System.currentTimeMillis(), recordingId))
        if (progress.completedDayCount(programId) >= program.durationDays) {
            progress.markCompleted(programId, System.currentTimeMillis())
        }
    }

    override suspend fun clearAll() = database.withTransaction {
        progress.clearCompletions()
        progress.clearProgress()
    }
}

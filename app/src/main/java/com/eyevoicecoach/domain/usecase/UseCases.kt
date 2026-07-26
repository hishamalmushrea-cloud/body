package com.eyevoicecoach.domain.usecase

import com.eyevoicecoach.domain.model.Tip
import com.eyevoicecoach.domain.repository.RecordingRepository
import com.eyevoicecoach.domain.repository.TipRepository
import java.util.Calendar
import javax.inject.Inject

/** Selects a never-viewed daily exercise, with Friday-specific content support. */
class GetDailyTipUseCase @Inject constructor(private val tips: TipRepository) {
    /** Gets and atomically records the next exercise for [context]. */
    suspend operator fun invoke(context: String, calendar: Calendar = Calendar.getInstance()): Tip? =
        tips.getAndMarkNextTip(context, calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
}

/** Restarts the no-repeat exercise rotation. */
class ResetHistoryUseCase @Inject constructor(private val tips: TipRepository) {
    /** Clears all seen-exercise rows. */
    suspend operator fun invoke() = tips.resetHistory()
}

/** Removes private audio recordings beyond the mandatory seven-day retention period. */
class DeleteExpiredRecordingsUseCase @Inject constructor(private val recordings: RecordingRepository) {
    /** Deletes recordings older than seven full days at [nowMillis]. */
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()) =
        recordings.deleteExpiredRecordings(nowMillis)
}

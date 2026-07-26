package com.eyevoicecoach.domain.usecase

import java.util.concurrent.TimeUnit

/** Defines the mandatory local recording retention boundary. */
object RecordingRetentionPolicy {
    private val retentionMillis = TimeUnit.DAYS.toMillis(7)

    /** Returns the exclusive cutoff used to remove recordings older than seven days. */
    fun cutoffAt(nowMillis: Long): Long = nowMillis - retentionMillis

    /** Returns whether a recording created at [recordedAt] must be deleted at [nowMillis]. */
    fun isExpired(recordedAt: Long, nowMillis: Long): Boolean = recordedAt < cutoffAt(nowMillis)
}

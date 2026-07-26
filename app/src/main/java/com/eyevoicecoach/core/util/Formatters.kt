package com.eyevoicecoach.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Converts western digits in this string to Arabic-Indic digits for the Arabic interface. */
fun String.toEasternDigits(): String = buildString {
    this@toEasternDigits.forEach { character ->
        append(if (character in '0'..'9') ('٠' + (character - '0')) else character)
    }
}

/** Formats a duration in seconds with Arabic-Indic digits. */
fun Int.toArabicDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(Locale.ROOT, minutes, seconds).toEasternDigits()
}

/** Formats milliseconds as a compact audio duration with Arabic-Indic digits. */
fun Long.toArabicPlaybackDuration(): String = ((this / 1000).toInt()).toArabicDuration()

/** Formats a stored time as an Arabic calendar date. */
fun Long.toArabicDate(): String = SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(Date(this)).toEasternDigits()

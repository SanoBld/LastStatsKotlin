package com.sanobld.laststats.core

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val NARROW_NBSP = '\u202F'

private fun two(n: Int): String = n.toString().padStart(2, '0')

fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

fun nowSeconds(): Long = Clock.System.now().epochSeconds

/** Hour of the day (0 to 23) in the device time zone. */
fun currentHour(): Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour

/** 1 234 becomes "1.2k", 1 500 000 becomes "1.5M". */
fun fmtCompact(n: Int): String = fmtCompact(n.toLong())

fun fmtCompact(n: Long): String = when {
    n >= 1_000_000 -> fixed(n / 1_000_000.0, 1) + "M"
    n >= 1_000 -> fixed(n / 1_000.0, 1) + "k"
    else -> n.toString()
}

/** Exact number for bars: up to 999 999, then "M". */
fun fmtExact(n: Int): String = if (n >= 1_000_000) fixed(n / 1_000_000.0, 1) + "M" else n.toString()

private fun groups(n: Long, separator: Char): String {
    val s = n.toString()
    val neg = s.startsWith("-")
    val digits = if (neg) s.substring(1) else s
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(separator)
        sb.append(digits[i])
    }
    return if (neg) "-$sb" else sb.toString()
}

/** Groups of three digits separated by a narrow no-break space. */
fun fmtFull(n: Long): String = groups(n, NARROW_NBSP)

fun fmtFull(n: Int): String = fmtFull(n.toLong())

/** Groups of three digits separated by a comma. */
fun fmtComma(n: Long): String = groups(n, ',')

fun fmtComma(n: Int): String = fmtComma(n.toLong())

/** Percentage with one decimal when needed: 12 or 12.5. */
fun fmtPercent(value: Double): String {
    val text = fixed(value, 1)
    return if (text.endsWith(".0")) text.dropLast(2) else text
}

fun fmtSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> fixed(bytes / 1024.0, 1) + " KB"
    bytes < 1024L * 1024 * 1024 -> fixed(bytes / (1024.0 * 1024.0), 1) + " MB"
    else -> fixed(bytes / (1024.0 * 1024.0 * 1024.0), 2) + " GB"
}

/** dd/mm/yyyy */
fun fmtDateDmy(date: LocalDate): String = "${two(date.dayOfMonth)}/${two(date.monthNumber)}/${date.year}"

fun fmtDateDmy(epochMillis: Long): String =
    fmtDateDmy(Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date)

/** "12 Mar" with the localised month abbreviation. */
fun fmtDayMonth(date: LocalDate): String = "${date.dayOfMonth} ${L.months[date.monthNumber]}"

fun fmtDayMonthYear(date: LocalDate): String = "${fmtDayMonth(date)} ${date.year}"

/** Unix seconds to "12 Mar · 08:05" in local time. */
fun fmtTrackDate(uts: Long): String {
    val dt = Instant.fromEpochSeconds(uts).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.dayOfMonth} ${L.months[dt.monthNumber]} · ${two(dt.hour)}:${two(dt.minute)}"
}

/** Last.fm text date "12 Mar 2024, 08:05" to "12 Mar 2024 · 08:05". */
fun fmtDateText(raw: String): String {
    if (raw.isEmpty()) return ""
    val parts = raw.split(", ")
    return if (parts.size == 2) "${parts[0]} · ${parts[1]}" else raw
}

/** Uses the timestamp when there is one, the Last.fm text otherwise. */
fun fmtTrackDateLocal(uts: Long, text: String): String =
    if (uts > 0) fmtTrackDate(uts) else fmtDateText(text)

/** "5min", "3h", "2d" or "4mo". Empty when the timestamp is unknown. */
fun timeAgo(uts: Long, now: Long = nowMillis()): String {
    if (uts <= 0) return ""
    val diffMin = ((now - uts * 1000) / 60_000).coerceAtLeast(0)
    val diffHours = diffMin / 60
    val diffDays = diffHours / 24
    return when {
        diffMin < 60 -> "${diffMin}min"
        diffHours < 24 -> "${diffHours}h"
        diffDays < 30 -> "${diffDays}d"
        else -> "${diffDays / 30}mo"
    }
}

/** Seconds to "m:ss" or "h:mm:ss". */
fun fmtDuration(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "$h:${two(m.toInt())}:${two(sec.toInt())}" else "$m:${two(sec.toInt())}"
}

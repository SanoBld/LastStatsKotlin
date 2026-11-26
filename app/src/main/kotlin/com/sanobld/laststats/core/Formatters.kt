package com.sanobld.laststats.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

private const val NARROW_NBSP = '\u202F'

/** 1 234 becomes "1.2k", 1 500 000 becomes "1.5M". */
fun fmtCompact(n: Int): String = fmtCompact(n.toLong())

fun fmtCompact(n: Long): String = when {
    n >= 1_000_000 -> String.format(Locale.ROOT, "%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format(Locale.ROOT, "%.1fk", n / 1_000.0)
    else -> n.toString()
}

/** Exact number for bars: up to 999 999, then "M". */
fun fmtExact(n: Int): String =
    if (n >= 1_000_000) String.format(Locale.ROOT, "%.1fM", n / 1_000_000.0) else n.toString()

/** Groups of three digits separated by a narrow no-break space. */
fun fmtFull(n: Long): String {
    val s = n.toString()
    val neg = s.startsWith("-")
    val digits = if (neg) s.substring(1) else s
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(NARROW_NBSP)
        sb.append(digits[i])
    }
    return if (neg) "-$sb" else sb.toString()
}

fun fmtFull(n: Int): String = fmtFull(n.toLong())

/** Groups of three digits separated by a comma. */
fun fmtComma(n: Long): String {
    val s = n.toString()
    val neg = s.startsWith("-")
    val digits = if (neg) s.substring(1) else s
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(',')
        sb.append(digits[i])
    }
    return if (neg) "-$sb" else sb.toString()
}

fun fmtComma(n: Int): String = fmtComma(n.toLong())

/** Percentage with one decimal when needed: 12 or 12.5. */
fun fmtPercent(value: Double): String {
    val r = Math.round(value * 10.0) / 10.0
    return if (r % 1.0 == 0.0) r.toLong().toString() else String.format(Locale.ROOT, "%.1f", r)
}

fun fmtSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format(Locale.ROOT, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
}

private fun two(n: Int): String = n.toString().padStart(2, '0')

/** dd/mm/yyyy */
fun fmtDateDmy(date: LocalDate): String = "${two(date.dayOfMonth)}/${two(date.monthValue)}/${date.year}"

fun fmtDateDmy(epochMillis: Long): String =
    fmtDateDmy(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate())

/** "12 Mar" with the localised month abbreviation. */
fun fmtDayMonth(date: LocalDate): String = "${date.dayOfMonth} ${L.months[date.monthValue]}"

fun fmtDayMonthYear(date: LocalDate): String = "${fmtDayMonth(date)} ${date.year}"

/** Unix seconds to "12 Mar · 08:05" in local time. */
fun fmtTrackDate(uts: Long): String {
    val dt = Instant.ofEpochSecond(uts).atZone(ZoneId.systemDefault())
    return "${dt.dayOfMonth} ${L.months[dt.monthValue]} · ${two(dt.hour)}:${two(dt.minute)}"
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
fun timeAgo(uts: Long, nowMillis: Long = System.currentTimeMillis()): String {
    if (uts <= 0) return ""
    val diffMin = ((nowMillis - uts * 1000) / 60_000).coerceAtLeast(0)
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

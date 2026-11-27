package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.L

/** Time ranges Last.fm knows for top lists. [apiValue] is sent as is. */
enum class Period(val apiValue: String, val days: Int?) {
    Week("7day", 7),
    Month("1month", 30),
    Quarter("3month", 90),
    HalfYear("6month", 180),
    Year("12month", 365),
    Overall("overall", null);

    val label: String
        get() = when (this) {
            Week -> L.period7day
            Month -> L.period1month
            Quarter -> L.period3month
            HalfYear -> L.period6month
            Year -> L.period12month
            Overall -> L.periodOverall
        }

    companion object {
        fun fromApi(value: String?): Period = entries.firstOrNull { it.apiValue == value } ?: Overall
    }
}

/** Calendar ranges of the recap story. */
enum class RecapKind {
    Day, Week, Month;

    companion object {
        fun fromIndex(i: Int): RecapKind = entries.getOrElse(i) { Day }
    }
}

package com.sanobld.laststats.data.model

import androidx.annotation.StringRes
import com.sanobld.laststats.R
import com.sanobld.laststats.core.LocaleManager

/** Time ranges Last.fm knows for top lists. [apiValue] is sent as is. */
enum class Period(val apiValue: String, @StringRes val labelRes: Int, val days: Int?) {
    Week("7day", R.string.period7day, 7),
    Month("1month", R.string.period1month, 30),
    Quarter("3month", R.string.period3month, 90),
    HalfYear("6month", R.string.period6month, 180),
    Year("12month", R.string.period12month, 365),
    Overall("overall", R.string.period_overall, null);

    val label: String
        get() = LocaleManager.context.getString(labelRes)

    companion object {
        fun fromApi(value: String?): Period = entries.firstOrNull { it.apiValue == value } ?: Overall
    }
}

/** Calendar ranges of the recap story. */
enum class RecapKind { Day, Week, Month;
    companion object {
        fun fromIndex(i: Int): RecapKind = entries.getOrElse(i) { Day }
    }
}

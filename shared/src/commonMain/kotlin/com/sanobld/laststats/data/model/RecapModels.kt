package com.sanobld.laststats.data.model

import kotlinx.serialization.Serializable

/** One line of a ranking in a recap. [artist] is empty for artists. */
@Serializable
data class RankedEntry(
    val name: String,
    val artist: String = "",
    val count: Int = 0,
    val image: String = "",
)

/** Data of the day, week or month recap story. */
@Serializable
data class RecapData(
    val count: Int = 0,
    val prevCount: Int = 0,
    val uniqueArtists: Int = 0,
    val uniqueTracks: Int = 0,
    val topArtists: List<RankedEntry> = emptyList(),
    val topTracks: List<RankedEntry> = emptyList(),
    val topAlbums: List<RankedEntry> = emptyList(),
    val bars: List<Double> = emptyList(),
    val barLabels: List<String> = emptyList(),
    /** Epoch milliseconds of the first and last day covered. */
    val periodStart: Long? = null,
    val periodEnd: Long? = null,
)

/** Data of the year recap. */
data class YearRecapData(
    val total: Int = 0,
    val activeDays: Int = 0,
    val daysInRange: Int = 0,
    val longestStreak: Int = 0,
    /** YYYY-MM-DD. */
    val busiestDay: String? = null,
    val busiestDayCount: Int = 0,
    val monthly: List<Int> = List(12) { 0 },
    val hourly: List<Int> = List(24) { 0 },
    /** Monday to Sunday. */
    val weekday: List<Int> = List(7) { 0 },
    val artists: List<RankedEntry> = emptyList(),
    val tracks: List<RankedEntry> = emptyList(),
    val albums: List<RankedEntry> = emptyList(),
    val uniqueArtists: Int = 0,
    val uniqueTracks: Int = 0,
    /** Null when unknown (older years are not cached). */
    val newArtists: Int? = null,
    /** Top 50 artists, lower case, for taste matching. */
    val topArtistNames: List<String> = emptyList(),
    /** Top 20 artists, for the share donut. */
    val artistsWide: List<RankedEntry> = emptyList(),
    /** YYYY-MM-DD to scrobbles. */
    val calendar: Map<String, Int> = emptyMap(),
    /** YYYY-MM to running total. */
    val cumulative: Map<String, Int> = emptyMap(),
    val tracksAll: List<RankedEntry> = emptyList(),
    val busiestTrack: RankedEntry? = null,
    val peakTrack: RankedEntry? = null,
    val streakTrack: RankedEntry? = null,
    val randomTrack: RankedEntry? = null,
    val dayRecord: RankedEntry? = null,
    val monthTrack: RankedEntry? = null,
    val artistTrack: RankedEntry? = null,
    val discTrack: RankedEntry? = null,
    val dayRecordDate: String = "",
)

/** Friends part of the year recap. */
data class YearFriends(
    /** Friend with the most artists in common. */
    val twin: String? = null,
    /** Shared artists (display names, in my rank order). */
    val shared: List<String> = emptyList(),
    val sharedCount: Int = 0,
    /** Friend name to avatar url. */
    val images: Map<String, String> = emptyMap(),
    val myImage: String = "",
)

/** Background behind the year recap slides. */
enum class YearBackground { Full, Band, None }

package com.sanobld.laststats.data.model

import kotlinx.serialization.Serializable

enum class TasteType(val key: String) {
    Artists("artists"), Tracks("tracks"), Albums("albums");

    companion object {
        fun fromKey(k: String): TasteType = entries.firstOrNull { it.key == k } ?: Artists
    }
}

/** A recommendation. [artist] is empty for artist entries. */
@Serializable
data class TasteRec(
    val name: String,
    val artist: String = "",
    val type: String = "artists",
    val image: ImageSet = ImageSet.Empty,
    /** Own plays when meaningful, else 0. */
    val playCount: Int = 0,
    /** Seed artists that led to this pick. */
    val reasons: List<String> = emptyList(),
    /** Genre tag, for the "genre" source. */
    val tag: String = "",
)

/** What two people share in one category. */
data class SharedMatch(
    /** "track", "artist" or "album". */
    val type: String,
    val name: String,
    val artist: String = "",
    val imageUrl: String = "",
    val myPlays: Int = 0,
    val theirPlays: Int = 0,
)

/** Result of a taste comparison. [score] is 0 to 1. */
data class TasteAnalysis(
    val score: Double,
    val topMatch: SharedMatch? = null,
    val sharedArtists: List<SharedMatch> = emptyList(),
    val sharedTracks: List<SharedMatch> = emptyList(),
    val sharedAlbums: List<SharedMatch> = emptyList(),
    val dataLabel: String = "",
    val totalSharedArtists: Int = 0,
    val totalSharedTracks: Int = 0,
    val totalSharedAlbums: Int = 0,
    val sharedGenres: List<String> = emptyList(),
    /** 0 to 1 per category: artists, genres, tracks, albums. */
    val breakdown: Map<String, Double> = emptyMap(),
)

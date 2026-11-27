package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.bool
import com.sanobld.laststats.core.double
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.path
import com.sanobld.laststats.core.sanitizeName
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Track(
    val name: String,
    val artist: String = "",
    val album: String = "",
    val url: String = "",
    val mbid: String = "",
    val playCount: Long = 0,
    val listeners: Long = 0,
    val userPlayCount: Long = 0,
    val rank: Int = 0,
    val durationSeconds: Long = 0,
    val image: ImageSet = ImageSet.Empty,
    val loved: Boolean = false,
    val nowPlaying: Boolean = false,
    /** Unix seconds of the scrobble, 0 when it has no date (top lists, now playing). */
    val playedAt: Long = 0,
    /** Date as Last.fm writes it, for when there is no timestamp. */
    val playedAtText: String = "",
    /** Similarity from similar lists, 0 to 1. */
    val match: Double = 0.0,
    val tags: List<String> = emptyList(),
) {
    companion object {
        /** Reads recent, top, loved, search and chart tracks. */
        fun fromJson(el: JsonElement?, defaultArtist: String = ""): Track {
            val o = el.asObject() ?: return Track(name = sanitizeName(el.text()), artist = defaultArtist)
            val artistEl = o["artist"]
            val artist = artistEl.asObject()?.let { it["name"].text().ifEmpty { it["#text"].text() } }
                ?: artistEl.text().ifEmpty { defaultArtist }
            val albumEl = o["album"]
            val album = albumEl.asObject()?.let { it["#text"].text().ifEmpty { it["title"].text() } } ?: albumEl.text()
            // Last.fm gives the duration in milliseconds on recent tracks and in seconds on top lists.
            val rawDuration = o["duration"].long()
            val seconds = if (rawDuration > 10_000) rawDuration / 1000 else rawDuration
            return Track(
                name = sanitizeName(o["name"].text()),
                artist = sanitizeName(artist),
                album = sanitizeName(album),
                url = o["url"].text(),
                mbid = o["mbid"].text(),
                playCount = o["playcount"].long(),
                listeners = o["listeners"].long(),
                userPlayCount = o["userplaycount"].long(),
                rank = o.path("@attr", "rank").long().toInt(),
                durationSeconds = seconds,
                image = ImageSet.fromJson(o["image"]),
                loved = o["loved"].bool(),
                nowPlaying = o.path("@attr", "nowplaying").bool(),
                playedAt = o.path("date", "uts").long(),
                playedAtText = o.path("date", "#text").text(),
                match = o["match"].double(),
                tags = o.path("toptags", "tag").asItems().map { it.asObject()?.get("name").text() }.filter { it.isNotEmpty() },
            )
        }

        fun listFrom(el: JsonElement?): List<Track> = el.asItems().map { fromJson(it) }.filter { it.name.isNotEmpty() }
    }
}

package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.path
import com.sanobld.laststats.core.sanitizeName
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Album(
    val name: String,
    val artist: String = "",
    val url: String = "",
    val mbid: String = "",
    val playCount: Long = 0,
    val listeners: Long = 0,
    val userPlayCount: Long = 0,
    val rank: Int = 0,
    val image: ImageSet = ImageSet.Empty,
    val tags: List<String> = emptyList(),
    val tracks: List<Track> = emptyList(),
) {
    companion object {
        /** The artist is an object in top lists and a plain string in search results. */
        fun fromJson(el: JsonElement?): Album {
            val o = el.asObject() ?: return Album(name = sanitizeName(el.text()))
            val artistEl = o["artist"]
            val artist = artistEl.asObject()?.let { it["name"].text().ifEmpty { it["#text"].text() } } ?: artistEl.text()
            val tracks = o.path("tracks", "track").asItems().map { Track.fromJson(it, defaultArtist = artist) }
            return Album(
                name = sanitizeName(o["name"].text().ifEmpty { o["title"].text() }),
                artist = sanitizeName(artist),
                url = o["url"].text(),
                mbid = o["mbid"].text(),
                playCount = o["playcount"].long(),
                listeners = o["listeners"].long(),
                userPlayCount = o["userplaycount"].long(),
                rank = o.path("@attr", "rank").long().toInt(),
                image = ImageSet.fromJson(o["image"]),
                tags = o.path("tags", "tag").asItems().map { it.asObject()?.get("name").text() }.filter { it.isNotEmpty() },
                tracks = tracks,
            )
        }

        fun listFrom(el: JsonElement?): List<Album> = el.asItems().map { fromJson(it) }.filter { it.name.isNotEmpty() }
    }
}

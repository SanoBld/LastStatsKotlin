package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.double
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.path
import com.sanobld.laststats.core.sanitizeName
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class Artist(
    val name: String,
    val url: String = "",
    val mbid: String = "",
    val playCount: Long = 0,
    val listeners: Long = 0,
    val rank: Int = 0,
    val image: ImageSet = ImageSet.Empty,
    /** Similarity from artist.getSimilar, 0 to 1. */
    val match: Double = 0.0,
    val bio: String = "",
    val tags: List<String> = emptyList(),
) {
    companion object {
        /** Works for top lists, search results, similar artists and artist.getInfo. */
        fun fromJson(el: JsonElement?): Artist {
            val o: JsonObject = el.asObject() ?: return Artist(name = el.text().let(::sanitizeName))
            val tags = o.path("tags", "tag").asItems().map { it.asObject()?.get("name").text() }.filter { it.isNotEmpty() }
            return Artist(
                name = sanitizeName(o["name"].text()),
                url = o["url"].text(),
                mbid = o["mbid"].text(),
                playCount = o["playcount"].long().takeIf { it > 0 } ?: o.path("stats", "playcount").long(),
                listeners = o["listeners"].long().takeIf { it > 0 } ?: o.path("stats", "listeners").long(),
                rank = o.path("@attr", "rank").long().toInt(),
                image = ImageSet.fromJson(o["image"]),
                match = o["match"].double(),
                bio = o.path("bio", "summary").text(),
                tags = tags,
            )
        }

        fun listFrom(el: JsonElement?): List<Artist> = el.asItems().map { fromJson(it) }.filter { it.name.isNotEmpty() }
    }
}

package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.text
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

/** One scrobble of the full local history. */
data class Scrobble(
    /** Unix seconds. */
    val ts: Long,
    val track: String,
    val artist: String,
    val album: String,
) {
    /** Compact form used in the cache files: [ts, track, artist, album]. */
    fun toJsonArray(): JsonArray = buildJsonArray {
        add(JsonPrimitive(ts))
        add(JsonPrimitive(track))
        add(JsonPrimitive(artist))
        add(JsonPrimitive(album))
    }

    companion object {
        fun fromJsonArray(el: JsonElement): Scrobble? {
            val a = el as? JsonArray ?: return null
            if (a.isEmpty()) return null
            return Scrobble(
                ts = a[0].long(),
                track = a.getOrNull(1).text(),
                artist = a.getOrNull(2).text(),
                album = a.getOrNull(3).text(),
            )
        }
    }
}

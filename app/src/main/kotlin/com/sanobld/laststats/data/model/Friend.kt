package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.path
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Friend(
    val name: String,
    val realName: String = "",
    val url: String = "",
    val image: ImageSet = ImageSet.Empty,
    val country: String = "",
    val playCount: Long = 0,
    val registeredAt: Long = 0,
) {
    val displayName: String
        get() = realName.ifEmpty { name }

    companion object {
        fun fromJson(el: JsonElement?): Friend {
            val o = el.asObject() ?: return Friend(name = el.text())
            return Friend(
                name = o["name"].text(),
                realName = o["realname"].text(),
                url = o["url"].text(),
                image = ImageSet.fromJson(o["image"]),
                country = o["country"].text(),
                playCount = o["playcount"].long(),
                registeredAt = o.path("registered", "unixtime").long(),
            )
        }

        fun listFrom(el: JsonElement?): List<Friend> = el.asItems().map { fromJson(it) }.filter { it.name.isNotEmpty() }
    }
}

/** One line of an aggregated library: an artist, an album or a track with its play count. */
@Serializable
data class LibraryItem(
    val name: String,
    /** Empty for artists. */
    val artist: String = "",
    val playCount: Long = 0,
    val image: String = "",
)

/**
 * A friend's library, saved as top lists with play counts (not raw scrobbles).
 * That is all the taste comparison needs.
 */
@Serializable
data class FriendLibrary(
    val artists: List<LibraryItem> = emptyList(),
    val albums: List<LibraryItem> = emptyList(),
    val tracks: List<LibraryItem> = emptyList(),
    /** Unix seconds of the last sync. */
    val lastSync: Long = 0,
)

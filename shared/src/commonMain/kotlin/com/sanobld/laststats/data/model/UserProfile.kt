package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.nowSeconds
import com.sanobld.laststats.core.path
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Result of user.getInfo. */
@Serializable
data class UserProfile(
    val name: String,
    val realName: String = "",
    val url: String = "",
    val image: ImageSet = ImageSet.Empty,
    val country: String = "",
    val playCount: Long = 0,
    val artistCount: Long = 0,
    val albumCount: Long = 0,
    val trackCount: Long = 0,
    /** Unix seconds of the account creation, 0 when unknown. */
    val registeredAt: Long = 0,
) {
    /** Name to show: the real name when there is one. */
    val displayName: String
        get() = realName.ifEmpty { name }

    /** Whole years since the account was created. */
    fun yearsRegistered(now: Long = nowSeconds()): Int {
        if (registeredAt <= 0) return 0
        return ((now - registeredAt) / (365.25 * 86400)).toInt().coerceAtLeast(0)
    }

    companion object {
        fun fromJson(el: JsonElement?): UserProfile? {
            val o = el.asObject() ?: return null
            val name = o["name"].text()
            if (name.isEmpty()) return null
            return UserProfile(
                name = name,
                realName = o["realname"].text(),
                url = o["url"].text(),
                image = ImageSet.fromJson(o["image"]),
                country = o["country"].text(),
                playCount = o["playcount"].long(),
                artistCount = o["artist_count"].long(),
                albumCount = o["album_count"].long(),
                trackCount = o["track_count"].long(),
                registeredAt = o.path("registered", "unixtime").long(),
            )
        }
    }
}

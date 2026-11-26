package com.sanobld.laststats.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A loved track. */
data class FavoriteItem(
    val name: String,
    val artist: String,
    val image: ImageSet = ImageSet.Empty,
    /** Unix seconds when it was loved, 0 when unknown. */
    val lovedAt: Long = 0,
    val url: String = "",
)

/** A folder of saved tracks. Colour is stored as ARGB so it survives JSON. */
@Serializable
data class FavFolder(
    val id: String,
    val name: String,
    val emoji: String = "📁",
    @SerialName("color") val colorValue: Long = 0xFF6750A4,
    @SerialName("desc") val description: String = "",
)

/**
 * A saved track, with just enough data to show it in a folder without asking the API again.
 * Folders only hold tracks. A track can be in several folders.
 */
@Serializable
data class FolderItem(
    val key: String = "",
    val name: String,
    val artist: String,
    val image: String = "",
    /** Epoch milliseconds, to sort the newest first. */
    val addedAt: Long = 0,
)

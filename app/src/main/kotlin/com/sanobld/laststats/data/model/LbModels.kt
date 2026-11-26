package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.path
import com.sanobld.laststats.core.text
import kotlinx.serialization.json.JsonElement

/** One listen from ListenBrainz. */
data class LbListen(
    /** Unix seconds, 0 for the "playing now" entry. */
    val listenedAt: Long,
    val trackName: String,
    val artistName: String,
    val releaseName: String = "",
    val recordingMbid: String = "",
    val releaseMbid: String = "",
    val artistMbids: List<String> = emptyList(),
    val caaId: Long = 0,
    val caaReleaseMbid: String = "",
    val playingNow: Boolean = false,
) {
    companion object {
        fun fromJson(el: JsonElement?, playingNow: Boolean = false): LbListen? {
            val o = el.asObject() ?: return null
            val meta = o["track_metadata"].asObject() ?: return null
            val info = meta["additional_info"].asObject()
            val mbidMapping = meta["mbid_mapping"].asObject()
            val artistMbids = (info?.get("artist_mbids") ?: mbidMapping?.get("artist_mbids")).asItems().map { it.text() }.filter { it.isNotEmpty() }
            return LbListen(
                listenedAt = o["listened_at"].long(),
                trackName = meta["track_name"].text(),
                artistName = meta["artist_name"].text(),
                releaseName = meta["release_name"].text(),
                recordingMbid = (info?.get("recording_mbid") ?: mbidMapping?.get("recording_mbid")).text(),
                releaseMbid = (info?.get("release_mbid") ?: mbidMapping?.get("release_mbid")).text(),
                artistMbids = artistMbids,
                caaId = mbidMapping?.get("caa_id").long(),
                caaReleaseMbid = mbidMapping?.get("caa_release_mbid").text(),
                playingNow = playingNow,
            )
        }
    }
}

/** One entry of a ListenBrainz statistics list (artists, releases or recordings). */
data class LbStatEntry(
    val artistName: String = "",
    val releaseName: String = "",
    val trackName: String = "",
    val listenCount: Long = 0,
    val artistMbids: List<String> = emptyList(),
    val releaseMbid: String = "",
    val recordingMbid: String = "",
    val caaId: Long = 0,
    val caaReleaseMbid: String = "",
) {
    companion object {
        fun fromJson(el: JsonElement?): LbStatEntry? {
            val o = el.asObject() ?: return null
            val mbids = (o["artist_mbids"] ?: o.path("artists")).asItems().mapNotNull {
                it.asObject()?.get("artist_mbid")?.text() ?: it.text().ifEmpty { null }
            }.filter { it.isNotEmpty() }
            return LbStatEntry(
                artistName = o["artist_name"].text(),
                releaseName = o["release_name"].text(),
                trackName = o["track_name"].text(),
                listenCount = o["listen_count"].long(),
                artistMbids = mbids,
                releaseMbid = o["release_mbid"].text(),
                recordingMbid = o["recording_mbid"].text(),
                caaId = o["caa_id"].long(),
                caaReleaseMbid = o["caa_release_mbid"].text(),
            )
        }

        fun listFrom(el: JsonElement?): List<LbStatEntry> = el.asItems().mapNotNull { fromJson(it) }
    }
}

/** Account state after the ListenBrainz token check. */
data class LbUser(
    val name: String,
    val token: String,
    val valid: Boolean = true,
)

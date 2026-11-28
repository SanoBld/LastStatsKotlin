package com.sanobld.laststats.data.remote

import com.sanobld.laststats.core.DataSource
import com.sanobld.laststats.core.LocaleManager
import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.int
import com.sanobld.laststats.core.path
import com.sanobld.laststats.core.text
import com.sanobld.laststats.core.tx
import com.sanobld.laststats.data.model.Album
import com.sanobld.laststats.data.model.Artist
import com.sanobld.laststats.data.model.Friend
import com.sanobld.laststats.data.model.Period
import com.sanobld.laststats.data.model.Tag
import com.sanobld.laststats.data.model.Track
import com.sanobld.laststats.data.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okio.ByteString.Companion.encodeUtf8

/** One page of the listening history. */
data class RecentTracksPage(
    val tracks: List<Track>,
    val page: Int,
    val perPage: Int,
    val totalPages: Int,
    val total: Long,
    val user: String,
)

/** Rank (1 based, -1 when not found) and plays of one item in a top list. */
data class ItemStats(val rank: Int, val plays: Int)

/** The key was refused (error 10 or 26, or HTTP 403): the fallback key can be tried. */
private class KeyRejected(message: String) : Exception(message)

/**
 * Last.fm API client. Every call goes through [ApiHttp], so it is counted and limited.
 * In ListenBrainz mode the calls are answered by [LbRouting] instead.
 * Top lists are returned as Last.fm gives them: the merge of remasters and
 * collaborations is applied by the library repository.
 */
class LastFmService(
    val apiKey: String,
    val username: String,
    val secret: String = "",
    val sessionKey: String = "",
) {
    companion object {
        private const val HOST = "ws.audioscrobbler.com"
        private const val PATH = "/2.0/"
        private const val TIMEOUT_MS = 15_000L
        private val keyErrors = setOf(10, 26)

        /** Key used when the person's own key is refused (set from the settings). */
        var fallbackKey: String = ""

        /** "builtin1", "builtin2"... for the shipped keys, "user" for the person's own. */
        fun keyLabel(key: String): String {
            val i = InternalKeys.pool.indexOf(key)
            return if (i < 0) "user" else "builtin${i + 1}"
        }
    }

    private suspend fun call(params: Map<String, String>): JsonElement {
        if (DataSource.lb) return LbRouting.call(params, username)
        try {
            return get(params, apiKey)
        } catch (e: KeyRejected) {
            val fb = fallbackKey
            if (fb.isEmpty() || fb == apiKey) throw e
            return get(params, fb)
        }
    }

    private suspend fun get(params: Map<String, String>, key: String): JsonElement {
        if (DataSource.lb) throw Exception("Last.fm is disabled in ListenBrainz mode")
        if (!InternalKeys.enabled && InternalKeys.isInternal(key)) throw Exception(tx("lbl_key_needed"))
        val url = ApiHttp.url(HOST, PATH, params + mapOf("api_key" to key, "format" to "json"))
        var res = ApiHttp.get(url, keyLabel = keyLabel(key), timeoutMs = TIMEOUT_MS)
        if (res.status == 429) {
            delay(1_500)
            res = ApiHttp.get(url, keyLabel = keyLabel(key), timeoutMs = TIMEOUT_MS)
        }
        if (res.status == 429) throw Exception("Last.fm rate limit reached (HTTP 429)")
        if (res.status == 403) throw KeyRejected("HTTP 403")
        if (res.status != 200) throw Exception("HTTP ${res.status}")
        val body = ApiHttp.decodeJsonAsync(res)
        val obj = body.asObject()
        if (obj != null && obj["error"] != null) {
            val msg = obj["message"].text().ifEmpty { "Last.fm API error" }
            if (keyErrors.contains(obj["error"].int(-1))) throw KeyRejected(msg)
            throw Exception(msg)
        }
        return body
    }

    // ── Signed calls (login, love) ───────────────────────────────────────────

    private fun sign(params: Map<String, String>): String {
        val sb = StringBuilder()
        for (k in params.keys.sorted()) {
            sb.append(k)
            sb.append(params[k])
        }
        sb.append(secret)
        return sb.toString().encodeUtf8().md5().hex()
    }

    private suspend fun callSigned(params: Map<String, String>, post: Boolean = false): JsonElement {
        if (DataSource.lb) return LbRouting.callSigned(params, apiKey)
        val base = params + ("api_key" to apiKey)
        val full = base + mapOf("api_sig" to sign(base), "format" to "json")
        val res = if (post) {
            ApiHttp.postForm(ApiHttp.url(HOST, PATH), full, keyLabel = keyLabel(apiKey), timeoutMs = TIMEOUT_MS)
        } else {
            ApiHttp.get(ApiHttp.url(HOST, PATH, full), keyLabel = keyLabel(apiKey), timeoutMs = TIMEOUT_MS)
        }
        if (res.status != 200) throw Exception("HTTP ${res.status}")
        val body = ApiHttp.decodeJsonAsync(res)
        val obj = body.asObject()
        if (obj != null && obj["error"] != null) throw Exception(obj["message"].text().ifEmpty { "Last.fm API error" })
        return body
    }

    /** Step 1 of the login: a temporary token. */
    suspend fun getAuthToken(): String = callSigned(mapOf("method" to "auth.getToken")).path("token").text()

    /** Page where the person approves the token. */
    fun authUrl(token: String): String = "https://www.last.fm/api/auth/?api_key=$apiKey&token=$token"

    /** Step 3 of the login: the permanent session key. */
    suspend fun getSessionKey(token: String): String =
        callSigned(mapOf("method" to "auth.getSession", "token" to token)).path("session", "key").text()

    suspend fun loveTrack(track: String, artist: String) {
        callSigned(mapOf("method" to "track.love", "track" to track, "artist" to artist, "sk" to sessionKey), post = true)
    }

    suspend fun unloveTrack(track: String, artist: String) {
        callSigned(mapOf("method" to "track.unlove", "track" to track, "artist" to artist, "sk" to sessionKey), post = true)
    }

    // ── Charts and discovery ─────────────────────────────────────────────────

    suspend fun getChartTopTracks(limit: Int = 15): List<Track> =
        Track.listFrom(call(mapOf("method" to "chart.getTopTracks", "limit" to "$limit")).path("tracks", "track"))

    suspend fun getChartTopArtists(limit: Int = 15): List<Artist> =
        Artist.listFrom(call(mapOf("method" to "chart.getTopArtists", "limit" to "$limit")).path("artists", "artist"))

    suspend fun getSimilarArtists(artist: String, limit: Int = 15): List<Artist> =
        Artist.listFrom(
            call(mapOf("method" to "artist.getSimilar", "artist" to artist, "limit" to "$limit")).path("similarartists", "artist"),
        )

    suspend fun getGeoTopTracks(country: String, limit: Int = 15): List<Track> =
        Track.listFrom(call(mapOf("method" to "geo.getTopTracks", "country" to country, "limit" to "$limit")).path("tracks", "track"))

    suspend fun getTagTopTracks(tag: String, limit: Int = 15): List<Track> =
        Track.listFrom(call(mapOf("method" to "tag.getTopTracks", "tag" to tag, "limit" to "$limit")).path("tracks", "track"))

    // ── User ─────────────────────────────────────────────────────────────────

    suspend fun getUserInfo(user: String? = null): UserProfile? =
        UserProfile.fromJson(call(mapOf("method" to "user.getInfo", "user" to (user ?: username))).path("user"))

    suspend fun getTopArtists(period: Period = Period.Overall, limit: Int = 50, page: Int = 1, user: String? = null): List<Artist> =
        Artist.listFrom(topCall("user.getTopArtists", period, limit, page, user).path("topartists", "artist"))

    suspend fun getTopAlbums(period: Period = Period.Overall, limit: Int = 50, page: Int = 1, user: String? = null): List<Album> =
        Album.listFrom(topCall("user.getTopAlbums", period, limit, page, user).path("topalbums", "album"))

    suspend fun getTopTracks(period: Period = Period.Overall, limit: Int = 50, page: Int = 1, user: String? = null): List<Track> =
        Track.listFrom(topCall("user.getTopTracks", period, limit, page, user).path("toptracks", "track"))

    private suspend fun topCall(method: String, period: Period, limit: Int, page: Int, user: String?): JsonElement =
        call(
            mapOf(
                "method" to method,
                "user" to (user ?: username),
                "period" to period.apiValue,
                "limit" to "$limit",
                "page" to "$page",
            ),
        )

    suspend fun getRecentTracks(limit: Int = 50, page: Int = 1, from: Long? = null, to: Long? = null, user: String? = null): RecentTracksPage {
        val p = mutableMapOf(
            "method" to "user.getRecentTracks",
            "user" to (user ?: username),
            "limit" to "$limit",
            "page" to "$page",
        )
        if (from != null) p["from"] = "$from"
        if (to != null) p["to"] = "$to"
        val root = call(p).path("recenttracks")
        val attr = root.path("@attr")
        return RecentTracksPage(
            tracks = Track.listFrom(root.path("track")),
            page = attr.path("page").int(page),
            perPage = attr.path("perPage").int(limit),
            totalPages = attr.path("totalPages").int(),
            total = attr.path("total").text().toLongOrNull() ?: 0L,
            user = attr.path("user").text().ifEmpty { user ?: username },
        )
    }

    /** The track playing right now, or null. */
    suspend fun getNowPlaying(): Track? = try {
        getRecentTracks(limit = 1).tracks.firstOrNull()?.takeIf { it.nowPlaying }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    /** Scrobbles per month for the last [months] months, keyed "YYYY-MM". */
    suspend fun getMonthlyScrobbles(months: Int = 12): Map<String, Int> {
        val tz = TimeZone.currentSystemDefault()
        val base = Clock.System.todayIn(tz)
        val keys = ArrayList<String>()
        val ranges = ArrayList<Pair<Long, Long>>()
        for (i in months - 1 downTo 0) {
            val from = firstOfMonth(base.year, base.monthNumber - i)
            val to = firstOfMonth(base.year, base.monthNumber - i + 1)
            keys.add("${from.year}-${from.monthNumber.toString().padStart(2, '0')}")
            ranges.add(from.atStartOfDayIn(tz).epochSeconds to to.atStartOfDayIn(tz).epochSeconds)
        }
        val totals = coroutineScope {
            ranges.map { (from, to) ->
                async {
                    try {
                        call(
                            mapOf(
                                "method" to "user.getRecentTracks",
                                "user" to username,
                                "from" to "$from",
                                "to" to "$to",
                                "limit" to "1",
                            ),
                        ).path("recenttracks", "@attr", "total").text().toIntOrNull() ?: 0
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        0
                    }
                }
            }.awaitAll()
        }
        return keys.indices.associate { keys[it] to totals[it] }
    }

    /** Day 1 of a month; [month] may be below 1 or above 12 (the year follows). */
    private fun firstOfMonth(year: Int, month: Int): LocalDate {
        val zero = year * 12 + (month - 1)
        val year0 = if (zero >= 0) zero / 12 else -((-zero + 11) / 12)
        val month0 = zero - year0 * 12
        return LocalDate(year0, month0 + 1, 1)
    }

    suspend fun getLovedTracks(limit: Int = 50): List<Track> =
        Track.listFrom(call(mapOf("method" to "user.getLovedTracks", "user" to username, "limit" to "$limit")).path("lovedtracks", "track"))

    suspend fun getLovedTracksCount(): Int =
        call(mapOf("method" to "user.getLovedTracks", "user" to username, "limit" to "1")).path("lovedtracks", "@attr", "total").text().toIntOrNull() ?: 0

    // ── Artist, album, track details ─────────────────────────────────────────

    private suspend inline fun <T> safe(default: T, block: () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        default
    }

    suspend fun getArtistInfo(artist: String): Artist? = safe(null) {
        val d = call(
            mapOf(
                "method" to "artist.getInfo", "artist" to artist, "username" to username,
                "lang" to LocaleManager.code.value, "autocorrect" to "1",
            ),
        )
        d.path("artist")?.let { Artist.fromJson(it) }
    }

    suspend fun getArtistListeners(artist: String): Int? =
        getArtistInfo(artist)?.listeners?.takeIf { it > 0 }?.toInt()

    suspend fun getArtistTopTracks(artist: String, limit: Int = 10): List<Track> = safe(emptyList()) {
        Track.listFrom(
            call(mapOf("method" to "artist.getTopTracks", "artist" to artist, "limit" to "$limit", "autocorrect" to "1"))
                .path("toptracks", "track"),
        )
    }

    suspend fun getArtistTopAlbums(artist: String, limit: Int = 10): List<Album> = safe(emptyList()) {
        Album.listFrom(
            call(mapOf("method" to "artist.getTopAlbums", "artist" to artist, "limit" to "$limit", "autocorrect" to "1"))
                .path("topalbums", "album"),
        )
    }

    suspend fun getArtistTopTags(artist: String): List<Tag> = safe(emptyList()) {
        Tag.listFrom(call(mapOf("method" to "artist.getTopTags", "artist" to artist, "autocorrect" to "1")).path("toptags", "tag"))
    }

    suspend fun getAlbumInfo(album: String, artist: String): Album? = safe(null) {
        val d = call(
            mapOf(
                "method" to "album.getInfo", "album" to album, "artist" to artist, "username" to username,
                "lang" to LocaleManager.code.value, "autocorrect" to "1",
            ),
        )
        d.path("album")?.let { Album.fromJson(it) }
    }

    suspend fun getTrackInfo(track: String, artist: String): Track? = safe(null) {
        val d = call(
            mapOf("method" to "track.getInfo", "track" to track, "artist" to artist, "username" to username, "autocorrect" to "1"),
        )
        d.path("track")?.let { Track.fromJson(it) }
    }

    // ── Friends ──────────────────────────────────────────────────────────────

    suspend fun getFriends(limit: Int = 50, page: Int = 1, withRecentTrack: Boolean = true): List<Friend> =
        Friend.listFrom(
            call(
                mapOf(
                    "method" to "user.getFriends", "user" to username, "limit" to "$limit",
                    "page" to "$page", "recenttracks" to if (withRecentTrack) "1" else "0",
                ),
            ).path("friends", "user"),
        )

    /** Rank and plays of one item in the top 200 of [period]. [type]: "artists", "albums" or "tracks". */
    suspend fun getUserItemStats(type: String, name: String, artistName: String = "", period: Period = Period.Overall): ItemStats {
        val items: List<Pair<String, Pair<String, Long>>> = when (type) {
            "artists" -> getTopArtists(period = period, limit = 200).map { it.name to ("" to it.playCount) }
            "albums" -> getTopAlbums(period = period, limit = 200).map { it.name to (it.artist to it.playCount) }
            else -> getTopTracks(period = period, limit = 200).map { it.name to (it.artist to it.playCount) }
        }
        for ((i, item) in items.withIndex()) {
            val (n, rest) = item
            val (a, plays) = rest
            if (n == name && (type == "artists" || a == artistName)) return ItemStats(i + 1, plays.toInt())
        }
        return ItemStats(-1, 0)
    }

    // ── Search ───────────────────────────────────────────────────────────────

    /** Finds users. Falls back to an exact name lookup when the search finds nothing. */
    suspend fun searchUsers(query: String, limit: Int = 15, page: Int = 1): List<Friend> {
        try {
            val d = call(mapOf("method" to "user.search", "user" to query, "limit" to "$limit", "page" to "$page"))
            val raw = d.path("results", "usermatches", "user")
            val list = raw.asItems().filter { it is JsonObject }.map { Friend.fromJson(it) }.filter { it.name.isNotEmpty() }
            return list.ifEmpty { searchUserFallback(query) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val msg = (e.message ?: "").lowercase()
            if (msg.contains("no user") || msg.contains("not found") || msg.contains("http 400") || msg.contains("http 403")) {
                return searchUserFallback(query)
            }
            throw e
        }
    }

    private suspend fun searchUserFallback(query: String): List<Friend> = safe(emptyList()) {
        val info = getUserInfo(query) ?: return@safe emptyList()
        listOf(
            Friend(
                name = info.name, realName = info.realName, url = info.url, image = info.image,
                country = info.country, playCount = info.playCount, registeredAt = info.registeredAt,
            ),
        )
    }

    suspend fun searchArtists(query: String, limit: Int = 15, page: Int = 1): List<Artist> =
        Artist.listFrom(
            call(mapOf("method" to "artist.search", "artist" to query, "limit" to "$limit", "page" to "$page"))
                .path("results", "artistmatches", "artist"),
        )

    suspend fun searchAlbums(query: String, limit: Int = 15, page: Int = 1): List<Album> =
        Album.listFrom(
            call(mapOf("method" to "album.search", "album" to query, "limit" to "$limit", "page" to "$page"))
                .path("results", "albummatches", "album"),
        )

    suspend fun searchTracks(query: String, limit: Int = 15, page: Int = 1): List<Track> =
        Track.listFrom(
            call(mapOf("method" to "track.search", "track" to query, "limit" to "$limit", "page" to "$page"))
                .path("results", "trackmatches", "track"),
        )
}

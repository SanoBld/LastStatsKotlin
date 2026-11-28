package com.sanobld.laststats.data.remote

import com.sanobld.laststats.data.model.ApiCategory
import com.sanobld.laststats.data.model.ApiDef
import com.sanobld.laststats.data.model.ApiRule

/** Every external service the app talks to, with its limits. */
object ApiRegistry {
    val lastfm = ApiDef(
        id = "lastfm", name = "Last.fm", category = ApiCategory.Listening,
        hosts = listOf("ws.audioscrobbler.com"),
        official = ApiRule(5, 1_000),
        ceiling = ApiRule(4, 1_000),
        maxWaitMs = 10_000, cooldownMs = 30_000,
    )

    val all: List<ApiDef> = listOf(
        lastfm,
        // MetaBrainz blocks anonymous clients: the app always identifies itself.
        // "Never more than one call per second": calls queue up and X-RateLimit-* headers are honoured.
        ApiDef(
            id = "listenbrainz", name = "ListenBrainz", category = ApiCategory.Listening,
            hosts = listOf("api.listenbrainz.org", "labs.api.listenbrainz.org"),
            headerLimits = true, maxWaitMs = 120_000, needsUserAgent = true,
            official = ApiRule(1, 1_000), ceiling = ApiRule(1, 1_000),
        ),
        ApiDef(
            id = "musicbrainz", name = "MusicBrainz", category = ApiCategory.Metadata,
            hosts = listOf("musicbrainz.org"),
            official = ApiRule(1, 1_000), ceiling = ApiRule(1, 1_100),
            maxWaitMs = 15_000, cooldownMs = 30_000, needsUserAgent = true,
        ),
        ApiDef(
            id = "coverart", name = "Cover Art Archive", category = ApiCategory.Artwork,
            hosts = listOf("coverartarchive.org"), needsUserAgent = true,
        ),
        ApiDef(
            id = "itunes", name = "iTunes Search", category = ApiCategory.Artwork,
            hosts = listOf("itunes.apple.com"),
            official = ApiRule(20, 60_000), ceiling = ApiRule(18, 60_000),
            maxWaitMs = 2_500, cooldownMs = 60_000,
        ),
        ApiDef(
            id = "deezer", name = "Deezer", category = ApiCategory.Artwork,
            hosts = listOf("api.deezer.com"),
            official = ApiRule(50, 5_000), ceiling = ApiRule(40, 5_000),
            maxWaitMs = 3_000, cooldownMs = 10_000,
        ),
        ApiDef(
            id = "audiodb", name = "TheAudioDB", category = ApiCategory.Artwork,
            hosts = listOf("www.theaudiodb.com"),
            official = ApiRule(30, 60_000), ceiling = ApiRule(28, 60_000),
            maxWaitMs = 2_500, cooldownMs = 60_000, sharedKey = true,
        ),
        ApiDef(
            id = "wikimedia", name = "Wikipedia / Wikimedia Commons", category = ApiCategory.Artwork,
            hosts = listOf(
                "en.wikipedia.org", "fr.wikipedia.org", "de.wikipedia.org", "es.wikipedia.org",
                "it.wikipedia.org", "pt.wikipedia.org", "ru.wikipedia.org", "ja.wikipedia.org",
                "zh.wikipedia.org", "ar.wikipedia.org", "commons.wikimedia.org",
            ),
            ceiling = ApiRule(8, 1_000), maxWaitMs = 3_000, needsUserAgent = true,
        ),
        ApiDef(
            id = "ytmusic", name = "YouTube Music", category = ApiCategory.Artwork,
            hosts = listOf("music.youtube.com"),
            ceiling = ApiRule(10, 10_000), maxWaitMs = 3_000, unofficial = true,
        ),
        ApiDef(
            id = "applemusic", name = "Apple Music (animated covers)", category = ApiCategory.Artwork,
            hosts = listOf("amp-api.music.apple.com", "music.apple.com"),
            ceiling = ApiRule(10, 10_000), maxWaitMs = 3_000, unofficial = true,
        ),
        ApiDef(
            id = "lrclib", name = "LRCLIB", category = ApiCategory.Lyrics,
            hosts = listOf("lrclib.net"), needsUserAgent = true,
        ),
        ApiDef(
            id = "translate", name = "Google Translate", category = ApiCategory.Translate,
            hosts = listOf("translate.googleapis.com"),
            ceiling = ApiRule(10, 10_000), maxWaitMs = 5_000, unofficial = true,
        ),
        ApiDef(
            id = "github", name = "GitHub API", category = ApiCategory.Updates,
            hosts = listOf("api.github.com"),
            official = ApiRule(60, 3_600_000),
            headerLimits = true, maxWaitMs = 1_000, needsUserAgent = true,
        ),
        ApiDef(
            id = "githubstatic", name = "GitHub Pages / raw", category = ApiCategory.Updates,
            hosts = listOf("raw.githubusercontent.com", "sanobld.github.io"),
        ),
    )

    /** Image and media downloads from CDNs (Last.fm, Apple, Deezer...): counted together, never throttled. */
    val cdn = ApiDef(id = "cdn", name = "Image CDNs", category = ApiCategory.Other)

    private val byIdMap: Map<String, ApiDef> = buildMap {
        for (d in all) put(d.id, d)
        put(cdn.id, cdn)
    }

    private val byHostMap: Map<String, ApiDef> = buildMap {
        for (d in all) for (h in d.hosts) put(h, d)
    }

    fun byId(id: String): ApiDef? = byIdMap[id]

    /** Definition for [host]; anything unknown is a CDN / other download. */
    fun forHost(host: String): ApiDef = byHostMap[host.lowercase()] ?: cdn
}

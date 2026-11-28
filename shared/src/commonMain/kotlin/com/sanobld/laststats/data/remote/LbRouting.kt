package com.sanobld.laststats.data.remote

import kotlinx.serialization.json.JsonElement

/** Answers Last.fm style calls from ListenBrainz data (see the ListenBrainz adapter). */
interface LbHandler {
    /** Same input and output shape as a Last.fm read call. */
    suspend fun call(params: Map<String, String>, username: String): JsonElement

    /** Same for the signed calls (love, unlove...). [token] is the ListenBrainz token. */
    suspend fun callSigned(params: Map<String, String>, token: String): JsonElement
}

/**
 * In ListenBrainz mode, [LastFmService] sends its calls here instead of to Last.fm.
 * The adapter registers itself at startup.
 */
object LbRouting {
    var handler: LbHandler? = null

    private fun ready(): LbHandler = handler ?: throw IllegalStateException("ListenBrainz is not ready yet")

    suspend fun call(params: Map<String, String>, username: String): JsonElement = ready().call(params, username)

    suspend fun callSigned(params: Map<String, String>, token: String): JsonElement = ready().callSigned(params, token)
}

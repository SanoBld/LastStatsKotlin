package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.nowMillis
import kotlin.math.roundToInt

enum class ApiCategory { Listening, Artwork, Metadata, Lyrics, Translate, Updates, Other }

/** [requests] allowed per [windowMs]. */
data class ApiRule(val requests: Int, val windowMs: Long) {
    val windowSeconds: Int
        get() = (windowMs / 1000.0).roundToInt()
}

/** One external service the app talks to. */
data class ApiDef(
    val id: String,
    val name: String,
    val category: ApiCategory,
    val hosts: List<String> = emptyList(),
    /** Limit published by the provider, null when nothing is published. */
    val official: ApiRule? = null,
    /** Ceiling the app sets for itself, null when it is only tracked. */
    val ceiling: ApiRule? = null,
    /** How long a request may wait for a free slot before it is skipped. 0 = wait (up to 60 s). */
    val maxWaitMs: Long = 0,
    /** Pause after a rate limit answer when the server gives no Retry-After. */
    val cooldownMs: Long = 30_000,
    /** Not an official API: it may change or block without notice. */
    val unofficial: Boolean = false,
    /** Uses a shared public test key. */
    val sharedKey: Boolean = false,
    /** Limits are announced in X-RateLimit-* headers. */
    val headerLimits: Boolean = false,
    /** The provider asks for an identifying User-Agent. */
    val needsUserAgent: Boolean = false,
)

/** Read-only snapshot of one API for the screen. */
data class ApiStat(
    val def: ApiDef,
    val total: Int,
    val errors: Int,
    val throttled: Int,
    val skipped: Int,
    val today: Int,
    val lastHour: Int,
    val lastTs: Long,
    val lastStatus: Int,
    /** Calls inside the app's own ceiling window. */
    val windowUsed: Int,
    val headerLimit: Int? = null,
    val headerRemaining: Int? = null,
    /** When the window announced by the headers resets. */
    val resetAtMs: Long = 0,
    /** Paused until this time, 0 when not paused. */
    val cooldownUntilMs: Long = 0,
) {
    fun paused(nowMs: Long = nowMillis()): Boolean = cooldownUntilMs > nowMs

    val everUsed: Boolean
        get() = total > 0 || skipped > 0
}

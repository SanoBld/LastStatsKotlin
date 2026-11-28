package com.sanobld.laststats.data.remote

import com.sanobld.laststats.core.nowMillis
import com.sanobld.laststats.data.local.BoolPref
import com.sanobld.laststats.data.local.SettingsStore
import com.sanobld.laststats.data.local.StringPref
import com.sanobld.laststats.data.model.ApiDef
import com.sanobld.laststats.data.model.ApiStat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.int
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.parseJson
import com.sanobld.laststats.core.text

private class Live {
    var dTotal = 0; var dErrors = 0; var dThrottled = 0; var dSkipped = 0; var dToday = 0; var dDay = 0
    var sTotal = 0; var sErrors = 0; var sThrottled = 0; var sSkipped = 0; var sToday = 0; var sDay = 0
    var lastTs = 0L
    var lastStatus = 0

    /** Calls inside the app's own ceiling window (time of each acquire). */
    val hits = ArrayList<Long>()

    /** Calls in the last hour of this session. */
    val hour = ArrayList<Long>()
    var cooldownUntil = 0L
    var hLimit: Int? = null
    var hRemaining: Int? = null
    var hResetAt = 0L

    val hasDelta: Boolean
        get() = dTotal != 0 || dErrors != 0 || dThrottled != 0 || dSkipped != 0
}

/**
 * Counts every call per API (saved across launches) and, when the limiter is on,
 * spaces calls so the app stays under each API's ceiling.
 * All state is touched from one serial dispatcher, so it is safe from any coroutine.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object ApiUsage {
    private val storeKey = StringPref("ls_api_usage_v1")
    private val limiterKey = BoolPref("ls_api_limiter")

    private val serial = Dispatchers.Default.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + serial)

    private val live = HashMap<String, Live>()

    /** Per key label: total, today's count, day, pending total, pending today. */
    private val keys = HashMap<String, IntArray>()

    private var flushScheduled = false
    private var flushing = false
    private var settings: SettingsStore? = null

    /** When on, calls wait for a free slot under the API's ceiling. */
    var limiterEnabled: Boolean = false
        private set

    private fun now(): Long = nowMillis()

    private fun day(): Int {
        val d = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return d.year * 10000 + d.monthNumber * 100 + d.dayOfMonth
    }

    private fun l(id: String): Live = live.getOrPut(id) { Live() }

    /** Loads the saved counters and the limiter setting. Call once at startup. */
    fun load(store: SettingsStore) {
        settings = store
        limiterEnabled = store.get(limiterKey)
        val root = parseJson(store.get(storeKey)).asObject() ?: return
        root["api"].asObject()?.forEach { (id, v) ->
            val m = v.asObject() ?: return@forEach
            val x = l(id)
            x.sTotal = m["t"].int(); x.sErrors = m["e"].int(); x.sThrottled = m["r"].int()
            x.sSkipped = m["k"].int(); x.sDay = m["d"].int(); x.sToday = m["n"].int()
            val ls = m["l"].long()
            if (ls > x.lastTs) {
                x.lastTs = ls
                x.lastStatus = m["s"].int(x.lastStatus)
            }
        }
        root["keys"].asObject()?.forEach { (label, v) ->
            val m = v.asObject() ?: return@forEach
            val k = keys.getOrPut(label) { IntArray(5) }
            k[0] = m["t"].int(); k[1] = m["n"].int(); k[2] = m["d"].int()
        }
    }

    fun setLimiter(on: Boolean) {
        limiterEnabled = on
        settings?.put(limiterKey, on)
    }

    /**
     * Waits for a free slot. Returns false when the wait would be longer than the
     * API allows ([ApiDef.maxWaitMs]): the caller skips the call.
     */
    suspend fun acquire(def: ApiDef): Boolean {
        if (!limiterEnabled) return true
        return withContext(serial) {
            val x = l(def.id)
            val start = now()
            val deadline = start + (if (def.maxWaitMs > 0) def.maxWaitMs else 60_000L)
            while (true) {
                val t = now()
                var wait: Long
                if (x.cooldownUntil > t) {
                    wait = x.cooldownUntil - t
                } else if (def.headerLimits && x.hRemaining != null && x.hRemaining!! <= 0 && x.hResetAt > t) {
                    wait = x.hResetAt - t
                } else {
                    val rule = def.ceiling ?: return@withContext true
                    x.hits.removeAll { t - it >= rule.windowMs }
                    if (x.hits.size < rule.requests) {
                        x.hits.add(t)
                        return@withContext true
                    }
                    wait = x.hits.first() + rule.windowMs - t
                }
                if (t + wait > deadline) return@withContext false
                delay(if (wait < 20) 20L else wait + 5)
            }
            @Suppress("UNREACHABLE_CODE")
            false
        }
    }

    /** The API said "too many requests": pause it (honours Retry-After when given). */
    fun reportThrottled(id: String, retryAfterMs: Long? = null) {
        scope.launch {
            val def = ApiRegistry.byId(id)
            val ms = (retryAfterMs ?: def?.cooldownMs ?: 30_000L).coerceIn(1_000L, 15 * 60 * 1000L)
            val until = now() + ms
            val x = l(id)
            if (until > x.cooldownUntil) x.cooldownUntil = until
        }
    }

    /** Reads the X-RateLimit-* headers (names in lower case). */
    fun noteHeaders(id: String, headers: Map<String, String>) {
        scope.launch {
            val x = l(id)
            headers["x-ratelimit-limit"]?.toIntOrNull()?.let { x.hLimit = it }
            headers["x-ratelimit-remaining"]?.toIntOrNull()?.let { x.hRemaining = it }
            val resetIn = headers["x-ratelimit-reset-in"]?.toLongOrNull()
            val reset = headers["x-ratelimit-reset"]?.toLongOrNull()
            if (resetIn != null) {
                x.hResetAt = now() + resetIn * 1000
            } else if (reset != null && reset > 1_000_000_000L) {
                x.hResetAt = reset * 1000
            }
        }
    }

    /** A call was skipped because the wait was too long. */
    fun recordSkipped(id: String) {
        scope.launch {
            l(id).dSkipped++
            dirty()
        }
    }

    fun recordCall(id: String, status: Int, throttled: Boolean = false, error: Boolean = false, keyLabel: String? = null) {
        scope.launch {
            val x = l(id)
            val t = now()
            val d = day()
            if (x.dDay != d) {
                x.dDay = d
                x.dToday = 0
            }
            x.dTotal++
            x.dToday++
            if (throttled) x.dThrottled++ else if (error) x.dErrors++
            x.lastTs = t
            x.lastStatus = status
            x.hour.add(t)
            x.hour.removeAll { t - it > 3_600_000 }
            if (!keyLabel.isNullOrEmpty()) {
                val k = keys.getOrPut(keyLabel) { IntArray(5) }
                k[3]++
                k[4]++
            }
            dirty()
        }
    }

    private fun dirty() {
        if (flushScheduled) return
        flushScheduled = true
        scope.launch {
            delay(3_000)
            flushScheduled = false
            flushNow()
        }
    }

    /** Writes the pending counters to the settings. */
    suspend fun flush() = withContext(serial) { flushNow() }

    private fun flushNow() {
        if (flushing) {
            dirty()
            return
        }
        val store = settings ?: return
        val pending = live.entries.filter { it.value.hasDelta }
        val keyDelta = keys.entries.filter { it.value[3] != 0 }
        if (pending.isEmpty() && keyDelta.isEmpty()) return
        flushing = true
        try {
            val today = day()
            val apiOut = LinkedHashMap<String, JsonObject>()
            for ((id, x) in live) {
                if (x.hasDelta) {
                    val storedToday = if (x.sDay == today) x.sToday else 0
                    x.sTotal += x.dTotal; x.sErrors += x.dErrors; x.sThrottled += x.dThrottled; x.sSkipped += x.dSkipped
                    x.sToday = storedToday + x.dToday; x.sDay = today
                    x.dTotal = 0; x.dErrors = 0; x.dThrottled = 0; x.dSkipped = 0; x.dToday = 0
                }
                if (x.sTotal != 0 || x.sSkipped != 0 || x.sErrors != 0) {
                    apiOut[id] = buildJsonObject {
                        put("t", x.sTotal); put("e", x.sErrors); put("r", x.sThrottled); put("k", x.sSkipped)
                        put("d", x.sDay); put("n", x.sToday); put("l", x.lastTs); put("s", x.lastStatus)
                    }
                }
            }
            val keyOut = LinkedHashMap<String, JsonObject>()
            for ((label, k) in keys) {
                if (k[3] != 0) {
                    val storedToday = if (k[2] == today) k[1] else 0
                    k[0] += k[3]; k[1] = storedToday + k[4]; k[2] = today; k[3] = 0; k[4] = 0
                }
                if (k[0] != 0) keyOut[label] = buildJsonObject { put("t", k[0]); put("n", k[1]); put("d", k[2]) }
            }
            val root = buildJsonObject {
                put("v", 1)
                put("api", JsonObject(apiOut))
                put("keys", JsonObject(keyOut))
            }
            store.put(storeKey, root.toString())
        } catch (_: Exception) {
        } finally {
            flushing = false
        }
    }

    /** Erases every counter. */
    suspend fun reset() = withContext(serial) {
        live.clear()
        keys.clear()
        settings?.remove(storeKey)
    }

    /** Snapshot of one API for the screen. */
    suspend fun stat(def: ApiDef): ApiStat = withContext(serial) {
        val x = l(def.id)
        val t = now()
        val rule = def.ceiling
        var windowUsed = 0
        if (rule != null) {
            x.hits.removeAll { t - it >= rule.windowMs }
            windowUsed = x.hits.size
        }
        x.hour.removeAll { t - it > 3_600_000 }
        val today = day()
        val storedToday = if (x.sDay == today) x.sToday else 0
        val deltaToday = if (x.dDay == today) x.dToday else 0
        val headerOk = def.headerLimits && x.hResetAt > t
        ApiStat(
            def = def,
            total = x.sTotal + x.dTotal,
            errors = x.sErrors + x.dErrors,
            throttled = x.sThrottled + x.dThrottled,
            skipped = x.sSkipped + x.dSkipped,
            today = storedToday + deltaToday,
            lastHour = x.hour.size,
            lastTs = x.lastTs,
            lastStatus = x.lastStatus,
            windowUsed = windowUsed,
            headerLimit = if (headerOk) x.hLimit else null,
            headerRemaining = if (headerOk) x.hRemaining else null,
            resetAtMs = if (headerOk) x.hResetAt else 0,
            cooldownUntilMs = if (x.cooldownUntil > t) x.cooldownUntil else 0,
        )
    }

    /** Usage of one API key label: (total, today). */
    suspend fun keyUsage(label: String): Pair<Int, Int> = withContext(serial) {
        val k = keys[label] ?: return@withContext 0 to 0
        val storedToday = if (k[2] == day()) k[1] else 0
        (k[0] + k[3]) to (storedToday + k[4])
    }
}

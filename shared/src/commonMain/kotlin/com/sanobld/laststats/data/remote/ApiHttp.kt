package com.sanobld.laststats.data.remote

import com.sanobld.laststats.core.AppConstants
import com.sanobld.laststats.core.DataSource
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import com.sanobld.laststats.core.parseJson

/** Answer of an API call, already read into memory. Header names are lower case. */
class ApiResponse(
    val status: Int,
    val headers: Map<String, String>,
    val bytes: ByteArray,
    val host: String,
) {
    val text: String by lazy { bytes.decodeToString() }
    val ok: Boolean get() = status == 200

    /** The limiter skipped this call (wait would be too long). Treated as "no data". */
    val skipped: Boolean get() = headers["x-laststats-skipped"] == "1"

    /** Last.fm is off because ListenBrainz mode is on. */
    val blocked: Boolean get() = headers["x-laststats-blocked"] == "1"

    fun header(name: String): String? = headers[name.lowercase()]
}

/** The server did not answer with JSON (HTML error page, empty body...). */
class ApiBadResponse(val status: Int, val host: String) : Exception(
    "Invalid server response${if (host.isEmpty()) "" else " ($host)"} – HTTP $status",
)

/**
 * The one way the app talks to the network.
 * 1. finds which API the request goes to (from its host),
 * 2. waits for a free slot (or skips) so provider limits are respected,
 * 3. counts the call, its outcome and any rate limit answer (see [ApiUsage]).
 * A skipped call returns a synthetic 429: callers treat non-200 as "no data".
 */
object ApiHttp {
    /** Format required by MetaBrainz: "Application name/version ( contact-url )". */
    val userAgent: String
        get() = AppConstants.USER_AGENT

    // One shared client: connections stay open and are reused.
    private val client: HttpClient by lazy {
        HttpClient {
            expectSuccess = false
            followRedirects = true
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }
        }
    }

    private val lastfmRate = Regex("\"error\"\\s*:\\s*\"?29\"?\\b")
    private val deezerQuota = Regex("\"code\"\\s*:\\s*4\\b")

    /** Builds `https://host/path?query` with every value percent-encoded. */
    fun url(host: String, path: String, params: Map<String, String> = emptyMap()): String {
        val q = params.entries.joinToString("&") { (k, v) -> encode(k) + "=" + encode(v) }
        val p = if (path.startsWith("/")) path else "/$path"
        return "https://$host$p" + if (q.isEmpty()) "" else "?$q"
    }

    /** Percent-encoding of a query part (UTF-8). */
    fun encode(s: String): String {
        val sb = StringBuilder()
        for (b in s.encodeToByteArray()) {
            val c = b.toInt() and 0xFF
            val safe = (c in 'a'.code..'z'.code) || (c in 'A'.code..'Z'.code) || (c in '0'.code..'9'.code) ||
                c == '-'.code || c == '_'.code || c == '.'.code || c == '~'.code
            if (safe) sb.append(c.toChar()) else {
                sb.append('%')
                sb.append("0123456789ABCDEF"[c shr 4])
                sb.append("0123456789ABCDEF"[c and 0x0F])
            }
        }
        return sb.toString()
    }

    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        api: String? = null,
        keyLabel: String? = null,
        timeoutMs: Long? = null,
    ): ApiResponse = run(url, api, keyLabel, headers, timeoutMs) { h, t ->
        send(HttpMethod.Get, url, h, null, t)
    }

    /** POST with a form body (application/x-www-form-urlencoded). */
    suspend fun postForm(
        url: String,
        form: Map<String, String>,
        headers: Map<String, String> = emptyMap(),
        api: String? = null,
        keyLabel: String? = null,
        timeoutMs: Long? = null,
    ): ApiResponse = run(url, api, keyLabel, headers, timeoutMs) { h, t ->
        val params = Parameters.build { form.forEach { (k, v) -> append(k, v) } }
        send(HttpMethod.Post, url, h, FormDataContent(params), t)
    }

    /** POST with a JSON text body. */
    suspend fun postJson(
        url: String,
        json: String,
        headers: Map<String, String> = emptyMap(),
        api: String? = null,
        keyLabel: String? = null,
        timeoutMs: Long? = null,
    ): ApiResponse = run(url, api, keyLabel, headers, timeoutMs) { h, t ->
        send(HttpMethod.Post, url, h, TextContent(json, ContentType.Application.Json), t)
    }

    private suspend fun send(
        method: HttpMethod,
        url: String,
        headers: Map<String, String>,
        body: Any?,
        timeoutMs: Long?,
    ): ApiResponse {
        val res: HttpResponse = client.request(url) {
            this.method = method
            headers.forEach { (k, v) -> header(k, v) }
            if (timeoutMs != null) timeout { requestTimeoutMillis = timeoutMs }
            if (body != null) setBody(body)
        }
        val map = HashMap<String, String>()
        for ((name, values) in res.headers.entries()) {
            if (values.isNotEmpty()) map[name.lowercase()] = values.first()
        }
        return ApiResponse(res.status.value, map, res.bodyAsBytes(), Url(url).host)
    }

    private suspend fun run(
        url: String,
        api: String?,
        keyLabel: String?,
        headers: Map<String, String>,
        timeoutMs: Long?,
        send: suspend (Map<String, String>, Long?) -> ApiResponse,
    ): ApiResponse {
        val host = Url(url).host
        val def = (if (api != null) ApiRegistry.byId(api) else null) ?: ApiRegistry.forHost(host)

        // ListenBrainz mode: the Last.fm API is never contacted (and never counted).
        if (DataSource.lb && def.id == "lastfm") {
            return ApiResponse(403, mapOf("x-laststats-blocked" to "1"), ByteArray(0), host)
        }

        var h = headers
        if (def.needsUserAgent && headers.keys.none { it.equals("user-agent", ignoreCase = true) }) {
            h = headers + ("User-Agent" to userAgent)
        }

        if (!ApiUsage.acquire(def)) {
            ApiUsage.recordSkipped(def.id)
            return ApiResponse(429, mapOf("x-laststats-skipped" to "1"), ByteArray(0), host)
        }

        val res: ApiResponse = try {
            send(h, timeoutMs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiUsage.recordCall(def.id, status = 0, error = true, keyLabel = keyLabel)
            throw e
        }

        var throttled = res.status == 429 || (res.status == 503 && def.id == "musicbrainz")

        // Some APIs answer 200 with a rate limit error inside the JSON body.
        if (!throttled && res.bytes.size < 1500 && (def.id == "lastfm" || def.id == "deezer")) {
            val b = res.text
            if (def.id == "lastfm" && lastfmRate.containsMatchIn(b)) throttled = true
            if (def.id == "deezer" && b.contains("\"error\"") && deezerQuota.containsMatchIn(b)) throttled = true
        }

        if (def.headerLimits) ApiUsage.noteHeaders(def.id, res.headers)

        if (throttled) {
            val ra = (res.headers["retry-after"] ?: res.headers["x-ratelimit-reset-in"])?.toLongOrNull()
            ApiUsage.reportThrottled(def.id, if (ra == null) null else ra * 1000)
        }

        val code = res.status
        ApiUsage.recordCall(
            def.id,
            status = code,
            throttled = throttled,
            error = code >= 500 || code == 401 || code == 403,
            keyLabel = keyLabel,
        )
        return res
    }

    /** Parses the body as JSON. Throws [ApiBadResponse] for HTML or an empty body. */
    fun decodeJson(res: ApiResponse): JsonElement {
        val head = res.text.trimStart()
        if (head.startsWith("<") || head.isEmpty()) throw ApiBadResponse(res.status, res.host)
        return parseJson(head) ?: throw ApiBadResponse(res.status, res.host)
    }

    /** Same as [decodeJson], but big bodies are parsed off the calling thread. */
    suspend fun decodeJsonAsync(res: ApiResponse): JsonElement {
        if (res.bytes.size < 32 * 1024) return decodeJson(res)
        return withContext(Dispatchers.Default) { decodeJson(res) }
    }

}

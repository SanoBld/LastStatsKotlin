package com.sanobld.laststats.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/** Shared parser. Last.fm and ListenBrainz answers are loose, so it is lenient. */
val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = true
    explicitNulls = false
}

fun parseJson(text: String): JsonElement? =
    try {
        AppJson.parseToJsonElement(text)
    } catch (_: Exception) {
        null
    }

fun JsonElement?.asObject(): JsonObject? = this as? JsonObject

fun JsonElement?.asArray(): JsonArray? = this as? JsonArray

/** Last.fm returns a single object instead of a list when there is one item. */
fun JsonElement?.asItems(): List<JsonElement> = when (this) {
    null, JsonNull -> emptyList()
    is JsonArray -> toList()
    else -> listOf(this)
}

fun JsonElement?.text(): String = (this as? JsonPrimitive)?.contentOrNull ?: ""

fun JsonElement?.textOrNull(): String? = (this as? JsonPrimitive)?.contentOrNull

fun JsonElement?.long(default: Long = 0L): Long {
    val p = this as? JsonPrimitive ?: return default
    return p.longOrNull ?: p.doubleOrNull?.toLong() ?: p.contentOrNull?.trim()?.toLongOrNull() ?: default
}

fun JsonElement?.int(default: Int = 0): Int = long(default.toLong()).toInt()

fun JsonElement?.double(default: Double = 0.0): Double {
    val p = this as? JsonPrimitive ?: return default
    return p.doubleOrNull ?: p.contentOrNull?.trim()?.toDoubleOrNull() ?: default
}

fun JsonElement?.bool(default: Boolean = false): Boolean {
    val p = this as? JsonPrimitive ?: return default
    return p.booleanOrNull ?: when (p.contentOrNull?.lowercase()) {
        "true", "1" -> true
        "false", "0" -> false
        else -> default
    }
}

operator fun JsonElement?.get(key: String): JsonElement? = (this as? JsonObject)?.get(key)

/** Follows a chain of keys, null as soon as one is missing. */
fun JsonElement?.path(vararg keys: String): JsonElement? {
    var cur: JsonElement? = this
    for (k in keys) {
        cur = (cur as? JsonObject)?.get(k) ?: return null
    }
    return cur
}

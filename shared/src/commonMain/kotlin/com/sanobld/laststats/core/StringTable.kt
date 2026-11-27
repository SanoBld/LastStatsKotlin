package com.sanobld.laststats.core

import com.sanobld.laststats.resources.Res
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Translation tables for the whole app, on every platform.
 * Files: composeResources/files/strings/<lang>.json (built by tools/gen_strings.py).
 * English is always loaded as the fallback for a missing key.
 */
object StringTable {
    private class Table(val strings: Map<String, String>, val arrays: Map<String, List<String>>)

    private var english: Table? = null
    private var current: Table? = null

    private fun read(code: String): Table? = try {
        val bytes = runBlocking { Res.readBytes("files/strings/$code.json") }
        val root = parseJson(bytes.decodeToString()) as? JsonObject
        val s = (root?.get("s") as? JsonObject)?.mapValues { it.value.text() } ?: emptyMap()
        val a = (root?.get("a") as? JsonObject)?.mapValues { e -> e.value.asItems().map { it.text() } } ?: emptyMap()
        Table(s, a)
    } catch (_: Exception) {
        null
    }

    /** Loads [code] (and English once). Call again to switch language. */
    fun load(code: String) {
        if (english == null) english = read("en")
        current = if (code == "en") english else read(code)
    }

    fun getOrNull(key: String): String? = current?.strings?.get(key) ?: english?.strings?.get(key)

    fun get(key: String): String = getOrNull(key) ?: key

    fun array(key: String): List<String> = current?.arrays?.get(key) ?: english?.arrays?.get(key) ?: emptyList()
}

private val placeholder = Regex("%(?:(\\d+)\\$)?([-+0 ,#]*)(\\d+)?(?:\\.(\\d+))?([sdfxXn%])")

/** Small `String.format` for the translation strings: %s %d %f, %1$s positions, width and precision. */
fun formatArgs(template: String, args: Array<out Any?>): String {
    var next = 0
    return placeholder.replace(template) { m ->
        val conv = m.groupValues[5]
        if (conv == "%") return@replace "%"
        if (conv == "n") return@replace "\n"
        val pos = m.groupValues[1]
        val index = if (pos.isNotEmpty()) pos.toInt() - 1 else next++
        val arg = args.getOrNull(index)
        val flags = m.groupValues[2]
        val width = m.groupValues[3].toIntOrNull() ?: 0
        val precision = m.groupValues[4].toIntOrNull()
        var text = when (conv) {
            "s" -> arg.toString()
            "d" -> ((arg as? Number)?.toLong() ?: arg.toString().toLongOrNull() ?: 0L).toString()
            "x" -> ((arg as? Number)?.toLong() ?: 0L).toString(16)
            "X" -> ((arg as? Number)?.toLong() ?: 0L).toString(16).uppercase()
            else -> fixed(((arg as? Number)?.toDouble() ?: arg.toString().toDoubleOrNull() ?: 0.0), precision ?: 6)
        }
        if (text.length < width) {
            text = if (flags.contains('-')) text.padEnd(width)
            else if (flags.contains('0') && conv != "s") text.padStart(width, '0')
            else text.padStart(width)
        }
        text
    }
}

/** Number with a fixed count of decimals, always with a dot. */
fun fixed(value: Double, decimals: Int): String {
    if (decimals <= 0) return value.roundToLong().toString()
    val f = 10.0.pow(decimals)
    val scaled = (abs(value) * f).roundToLong()
    val whole = scaled / f.toLong()
    val frac = (scaled % f.toLong()).toString().padStart(decimals, '0')
    return (if (value < 0 && scaled != 0L) "-" else "") + "$whole.$frac"
}

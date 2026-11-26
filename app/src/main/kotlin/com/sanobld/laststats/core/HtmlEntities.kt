package com.sanobld.laststats.core

private val hexEntity = Regex("&#x([0-9a-fA-F]+);")
private val decEntity = Regex("&#(\\d+);")

private fun codePointString(cp: Int, original: String): String =
    if (cp in 0..0x10FFFF && !(cp in 0xD800..0xDFFF)) String(Character.toChars(cp)) else original

/**
 * Decodes HTML entities that Last.fm leaves in names (named, decimal and hex).
 * Example: "&#039;" or "&#x27;" becomes an apostrophe.
 */
fun sanitizeName(s: String): String {
    var out = s
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
    out = hexEntity.replace(out) { m ->
        val cp = m.groupValues[1].toIntOrNull(16)
        if (cp == null) m.value else codePointString(cp, m.value)
    }
    out = decEntity.replace(out) { m ->
        val cp = m.groupValues[1].toIntOrNull()
        if (cp == null) m.value else codePointString(cp, m.value)
    }
    return out.trim()
}

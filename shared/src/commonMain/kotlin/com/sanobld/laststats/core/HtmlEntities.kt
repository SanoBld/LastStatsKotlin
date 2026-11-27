package com.sanobld.laststats.core

private val hexEntity = Regex("&#x([0-9a-fA-F]+);")
private val decEntity = Regex("&#(\\d+);")

private fun codePointString(cp: Int, original: String): String = when {
    cp < 0 || cp > 0x10FFFF || cp in 0xD800..0xDFFF -> original
    cp < 0x10000 -> cp.toChar().toString()
    else -> {
        val c = cp - 0x10000
        charArrayOf((0xD800 + (c shr 10)).toChar(), (0xDC00 + (c and 0x3FF)).toChar()).concatToString()
    }
}

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

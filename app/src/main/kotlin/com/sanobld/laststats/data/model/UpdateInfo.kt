package com.sanobld.laststats.data.model

enum class UpdateChannel { Stable, Beta }

enum class DownloadKind { Apk, Installer, Zip, None }

/** One GitHub release. */
data class UpdateInfo(
    val version: String,
    val releaseUrl: String,
    val downloadUrl: String? = null,
    val downloadKind: DownloadKind = DownloadKind.None,
    val notes: String = "",
    /** Epoch milliseconds, null when GitHub gave no date. */
    val publishedAt: Long? = null,
    val isBeta: Boolean = false,
) {
    val hasDownload: Boolean
        get() = !downloadUrl.isNullOrEmpty()

    fun isNewerThan(other: String): Boolean = compareVersions(version, other) > 0
}

private fun splitVersion(v: String): Pair<List<Int>, String> {
    val clean = v.trim().removePrefix("v").substringBefore('+')
    val core = clean.substringBefore('-')
    val pre = if (clean.contains('-')) clean.substringAfter('-') else ""
    return core.split('.').map { part -> part.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 } to pre
}

/** Compares "1.2.3" style versions. A pre-release ("1.2.3-beta.1") is lower than the release. */
fun compareVersions(a: String, b: String): Int {
    val (an, ap) = splitVersion(a)
    val (bn, bp) = splitVersion(b)
    val len = maxOf(an.size, bn.size)
    for (i in 0 until len) {
        val x = an.getOrElse(i) { 0 }
        val y = bn.getOrElse(i) { 0 }
        if (x != y) return x.compareTo(y)
    }
    return when {
        ap == bp -> 0
        ap.isEmpty() -> 1
        bp.isEmpty() -> -1
        else -> ap.compareTo(bp)
    }
}

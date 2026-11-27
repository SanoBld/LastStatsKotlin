package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Last.fm blank "star" picture. It is never shown as a real cover. */
const val LASTFM_PLACEHOLDER_HASH = "2a96cbd8b46e442fc41c2b86b821562f"

private val sizeSegment = Regex("/\\d+x\\d+/")

fun isPlaceholderImage(url: String): Boolean = url.isEmpty() || url.contains(LASTFM_PLACEHOLDER_HASH)

/** The Last.fm CDN also serves 500x500: swap the size segment of the path. */
fun upsampleLastfmImage(url: String): String =
    if (url.contains("lastfm.freetls.fastly.net")) sizeSegment.replaceFirst(url, "/500x500/") else url

/** Pictures of one item at the sizes Last.fm gives. */
@Serializable
data class ImageSet(
    val small: String = "",
    val medium: String = "",
    val large: String = "",
    val extraLarge: String = "",
) {
    /** Best url: extra large first, then the largest one that exists. */
    val best: String
        get() = extraLarge.ifEmpty { large.ifEmpty { medium.ifEmpty { small } } }

    /** Same as [best] but without the Last.fm blank picture. */
    val real: String
        get() = best.let { if (isPlaceholderImage(it)) "" else it }

    fun url(large: Boolean = false): String = if (large) upsampleLastfmImage(best) else best

    val isEmpty: Boolean
        get() = best.isEmpty()

    companion object {
        val Empty = ImageSet()

        fun ofUrl(url: String) = ImageSet(extraLarge = url)

        /** Reads the Last.fm list: [{"#text": url, "size": "small"}, ...]. */
        fun fromJson(el: JsonElement?): ImageSet {
            var small = ""
            var medium = ""
            var large = ""
            var extra = ""
            var last = ""
            for (item in el.asItems()) {
                val o = item.asObject() ?: continue
                val url = o["#text"].text()
                if (url.isEmpty()) continue
                last = url
                when (o["size"].text()) {
                    "small" -> small = url
                    "medium" -> medium = url
                    "large" -> large = url
                    "extralarge" -> extra = url
                    "mega" -> if (extra.isEmpty()) extra = url
                }
            }
            if (extra.isEmpty() && last.isNotEmpty()) extra = last
            return ImageSet(small, medium, large, extra)
        }
    }
}

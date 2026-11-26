package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** One announcement from news.json. Types: feature, fix, update, alert, info. */
@Serializable
data class NewsItem(
    val id: String,
    val type: String = "info",
    val title: String,
    val body: String = "",
    val emoji: String = "",
    val date: String = "",
    val url: String = "",
) {
    companion object {
        fun fromJson(el: JsonElement?): NewsItem? {
            val o = el.asObject() ?: return null
            val title = o["title"].text()
            if (title.isEmpty()) return null
            return NewsItem(
                id = o["id"].text().ifEmpty { "${o["date"].text()}|$title" },
                type = o["type"].text().ifEmpty { "info" },
                title = title,
                body = o["body"].text(),
                emoji = o["emoji"].text(),
                date = o["date"].text(),
                url = o["url"].text(),
            )
        }

        /** Reads {"items": [...]}. */
        fun listFrom(root: JsonElement?): List<NewsItem> =
            root.asObject()?.get("items").asItems().mapNotNull { fromJson(it) }
    }
}

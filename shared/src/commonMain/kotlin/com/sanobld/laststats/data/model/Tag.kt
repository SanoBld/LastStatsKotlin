package com.sanobld.laststats.data.model

import com.sanobld.laststats.core.asItems
import com.sanobld.laststats.core.asObject
import com.sanobld.laststats.core.long
import com.sanobld.laststats.core.text
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Tag(
    val name: String,
    val count: Long = 0,
    val url: String = "",
) {
    companion object {
        fun fromJson(el: JsonElement?): Tag {
            val o = el.asObject() ?: return Tag(name = el.text())
            return Tag(name = o["name"].text(), count = o["count"].long(), url = o["url"].text())
        }

        fun listFrom(el: JsonElement?): List<Tag> = el.asItems().map { fromJson(it) }.filter { it.name.isNotEmpty() }
    }
}

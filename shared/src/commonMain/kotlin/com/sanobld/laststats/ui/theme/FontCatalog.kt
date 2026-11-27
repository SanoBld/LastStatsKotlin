@file:OptIn(ExperimentalResourceApi::class)

package com.sanobld.laststats.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.sanobld.laststats.core.fontFromBytes
import com.sanobld.laststats.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlinx.coroutines.runBlocking

enum class FontGroup { System, Sans, Handwriting, Custom }

/** [label] is empty for the system font: the screen shows its own translated name. */
data class FontOption(val key: String, val label: String, val group: FontGroup)

private class FontFile(val file: String, val weight: Int, val italic: Boolean = false)

object FontCatalog {
    const val DEFAULT_KEY = "GoogleSansFlex"
    const val CUSTOM_KEY = "CustomFont"

    val options: List<FontOption> = listOf(
        FontOption("", "", FontGroup.System),
        FontOption("Inter", "Inter", FontGroup.Sans),
        FontOption("SpaceGrotesk", "Space Grotesk", FontGroup.Sans),
        FontOption("GoogleSansFlex", "Google Sans Flex", FontGroup.Sans),
        FontOption("OpenSans", "Open Sans", FontGroup.Sans),
        FontOption("Roboto", "Roboto", FontGroup.Sans),
        FontOption("RobotoSlab", "Roboto Slab", FontGroup.Sans),
        FontOption("DancingScript", "Dancing Script", FontGroup.Handwriting),
        FontOption("Spirax", "Spirax", FontGroup.Handwriting),
        FontOption("Mynerve", "Mynerve", FontGroup.Handwriting),
        FontOption("SingleDay", "Single Day", FontGroup.Handwriting),
        FontOption("BetaniaPatmosGDL", "Betania Patmos GDL", FontGroup.Handwriting),
    )

    private val files: Map<String, List<FontFile>> = mapOf(
        "Inter" to listOf(
            FontFile("inter_light.otf", 300), FontFile("inter_regular.ttf", 400),
            FontFile("inter_medium.otf", 500), FontFile("inter_semi_bold.ttf", 600),
            FontFile("inter_bold.ttf", 700), FontFile("inter_variable_italic.ttf", 400, italic = true),
        ),
        "GoogleSansFlex" to listOf(
            FontFile("gsf_light.ttf", 300), FontFile("gsf_regular.ttf", 400), FontFile("gsf_medium.ttf", 500),
            FontFile("gsf_semibold.ttf", 600), FontFile("gsf_bold.ttf", 700),
        ),
        "OpenSans" to listOf(
            FontFile("open_sans_light.ttf", 300), FontFile("open_sans_regular.ttf", 400),
            FontFile("open_sans_medium.ttf", 500), FontFile("open_sans_semi_bold.ttf", 600),
            FontFile("open_sans_bold.ttf", 700), FontFile("open_sans_italic.ttf", 400, italic = true),
        ),
        "Roboto" to listOf(
            FontFile("roboto_light.ttf", 300), FontFile("roboto_regular.ttf", 400),
            FontFile("roboto_medium.ttf", 500), FontFile("roboto_bold.ttf", 700),
            FontFile("roboto_italic.ttf", 400, italic = true),
        ),
        "RobotoSlab" to listOf(
            FontFile("roboto_slab_light.ttf", 300), FontFile("roboto_slab_regular.ttf", 400),
            FontFile("roboto_slab_medium.ttf", 500), FontFile("roboto_slab_bold.ttf", 700),
        ),
        "DancingScript" to listOf(
            FontFile("dancing_script_regular.ttf", 400), FontFile("dancing_script_medium.ttf", 500),
            FontFile("dancing_script_semi_bold.ttf", 600), FontFile("dancing_script_bold.ttf", 700),
        ),
        "Spirax" to listOf(FontFile("spirax_regular.ttf", 400)),
        "Mynerve" to listOf(FontFile("mynerve_regular.ttf", 400)),
        "SingleDay" to listOf(FontFile("single_day_regular.ttf", 400)),
        "BetaniaPatmosGDL" to listOf(FontFile("betania_patmos_gdl_regular.ttf", 400)),
        "SpaceGrotesk" to listOf(FontFile("space_grotesk_regular.otf", 400)),
        "SpaceMono" to listOf(FontFile("space_mono_regular.otf", 400)),
    )

    private val cache = HashMap<String, FontFamily?>()

    private fun load(key: String): FontFamily? {
        val list = files[key] ?: return null
        return try {
            val fonts = list.map { f ->
                val bytes = runBlocking { Res.readBytes("font/${f.file}") }
                fontFromBytes(
                    identity = f.file,
                    data = bytes,
                    weight = FontWeight(f.weight),
                    style = if (f.italic) FontStyle.Italic else FontStyle.Normal,
                )
            }
            FontFamily(fonts)
        } catch (_: Exception) {
            null
        }
    }

    /** Family for a saved key. Null means the system font. Built once, then kept. */
    fun familyFor(key: String, custom: FontFamily?): FontFamily? {
        if (key == CUSTOM_KEY) return custom
        if (key.isEmpty()) return null
        return cache.getOrPut(key) { load(key) }
    }

    /** Family used for monospace blocks (logs, code). */
    val monospace: FontFamily = FontFamily.Monospace

    /** Space Mono, used by a few decorative labels. */
    val spaceMono: FontFamily? get() = familyFor("SpaceMono", null)
}

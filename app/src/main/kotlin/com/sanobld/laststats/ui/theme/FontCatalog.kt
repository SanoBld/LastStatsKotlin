package com.sanobld.laststats.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.sanobld.laststats.R

enum class FontGroup { System, Sans, Handwriting, Custom }

/** [label] is empty for the system font: the screen shows its own translated name. */
data class FontOption(val key: String, val label: String, val group: FontGroup)

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

    private val inter by lazy {
        FontFamily(
            Font(R.font.inter_light, FontWeight.W300),
            Font(R.font.inter_regular, FontWeight.W400),
            Font(R.font.inter_medium, FontWeight.W500),
            Font(R.font.inter_semi_bold, FontWeight.W600),
            Font(R.font.inter_bold, FontWeight.W700),
            Font(R.font.inter_variable_italic, FontWeight.W400, FontStyle.Italic),
        )
    }

    private val googleSansFlex by lazy {
        FontFamily(
            Font(R.font.gsf_light, FontWeight.W300),
            Font(R.font.gsf_regular, FontWeight.W400),
            Font(R.font.gsf_medium, FontWeight.W500),
            Font(R.font.gsf_semibold, FontWeight.W600),
            Font(R.font.gsf_bold, FontWeight.W700),
        )
    }

    private val openSans by lazy {
        FontFamily(
            Font(R.font.open_sans_light, FontWeight.W300),
            Font(R.font.open_sans_regular, FontWeight.W400),
            Font(R.font.open_sans_medium, FontWeight.W500),
            Font(R.font.open_sans_semi_bold, FontWeight.W600),
            Font(R.font.open_sans_bold, FontWeight.W700),
            Font(R.font.open_sans_italic, FontWeight.W400, FontStyle.Italic),
        )
    }

    private val roboto by lazy {
        FontFamily(
            Font(R.font.roboto_light, FontWeight.W300),
            Font(R.font.roboto_regular, FontWeight.W400),
            Font(R.font.roboto_medium, FontWeight.W500),
            Font(R.font.roboto_bold, FontWeight.W700),
            Font(R.font.roboto_italic, FontWeight.W400, FontStyle.Italic),
        )
    }

    private val robotoSlab by lazy {
        FontFamily(
            Font(R.font.roboto_slab_light, FontWeight.W300),
            Font(R.font.roboto_slab_regular, FontWeight.W400),
            Font(R.font.roboto_slab_medium, FontWeight.W500),
            Font(R.font.roboto_slab_bold, FontWeight.W700),
        )
    }

    private val dancingScript by lazy {
        FontFamily(
            Font(R.font.dancing_script_regular, FontWeight.W400),
            Font(R.font.dancing_script_medium, FontWeight.W500),
            Font(R.font.dancing_script_semi_bold, FontWeight.W600),
            Font(R.font.dancing_script_bold, FontWeight.W700),
        )
    }

    private val spirax by lazy { FontFamily(Font(R.font.spirax_regular, FontWeight.W400)) }
    private val mynerve by lazy { FontFamily(Font(R.font.mynerve_regular, FontWeight.W400)) }
    private val singleDay by lazy { FontFamily(Font(R.font.single_day_regular, FontWeight.W400)) }
    private val betania by lazy { FontFamily(Font(R.font.betania_patmos_gdl_regular, FontWeight.W400)) }
    private val spaceGrotesk by lazy { FontFamily(Font(R.font.space_grotesk_regular, FontWeight.W400)) }

    /** Family for a saved key. Null means the system font. */
    fun familyFor(key: String, custom: FontFamily?): FontFamily? = when (key) {
        "Inter" -> inter
        "GoogleSansFlex" -> googleSansFlex
        "OpenSans" -> openSans
        "Roboto" -> roboto
        "RobotoSlab" -> robotoSlab
        "DancingScript" -> dancingScript
        "Spirax" -> spirax
        "Mynerve" -> mynerve
        "SingleDay" -> singleDay
        "BetaniaPatmosGDL" -> betania
        "SpaceGrotesk" -> spaceGrotesk
        CUSTOM_KEY -> custom
        else -> null
    }

    /** Family used for monospace blocks (logs, code). */
    val monospace: FontFamily = FontFamily.Monospace

    /** Space Mono, used by a few decorative labels. */
    val spaceMono by lazy { FontFamily(Font(R.font.space_mono_regular, FontWeight.W400)) }
}

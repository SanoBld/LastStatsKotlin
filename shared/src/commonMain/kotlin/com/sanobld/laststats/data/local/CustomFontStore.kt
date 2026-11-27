package com.sanobld.laststats.data.local

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.sanobld.laststats.core.AppConstants
import com.sanobld.laststats.core.fontFromBytes
import com.sanobld.laststats.ui.theme.FontCatalog
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Font imported by the person (.ttf or .otf). It is saved as base64 in the
 * `ls_custom_font_b64` setting, so a backup carries it.
 */
@OptIn(ExperimentalEncodingApi::class)
object CustomFontStore {
    const val FAMILY = FontCatalog.CUSTOM_KEY

    private val _name = MutableStateFlow("")

    /** Display name of the imported font, empty when there is none. */
    val name: StateFlow<String> = _name

    private val _family = MutableStateFlow<FontFamily?>(null)
    val family: StateFlow<FontFamily?> = _family

    /** Registers the saved font. Call at startup and after a restore. */
    fun load(settings: SettingsStore) {
        val b64 = settings.get(Prefs.customFontB64)
        if (b64.isEmpty()) {
            clear()
            return
        }
        try {
            val bytes = Base64.Mime.decode(b64)
            if (!looksLikeFont(bytes)) {
                clear()
                return
            }
            val font = fontFromBytes("custom_font", bytes, FontWeight.Normal, FontStyle.Normal)
            _family.value = FontFamily(font)
            _name.value = settings.get(Prefs.customFontName).ifEmpty { FAMILY }
        } catch (_: Exception) {
            clear()
        }
    }

    /**
     * Saves the font the person picked. [bytes] is the file content, [fileName] its name.
     * Returns null on success, else "size", "invalid" or "cancel".
     */
    fun import(settings: SettingsStore, bytes: ByteArray?, fileName: String?): String? {
        if (bytes == null) return "cancel"
        if (bytes.size > AppConstants.CUSTOM_FONT_MAX_BYTES) return "size"
        if (!looksLikeFont(bytes)) return "invalid"
        val name = (fileName ?: FAMILY).replace(Regex("\\.(ttf|otf)$", RegexOption.IGNORE_CASE), "")
        settings.putAll {
            Prefs.customFontB64.write(it, Base64.Default.encode(bytes))
            Prefs.customFontName.write(it, name)
        }
        load(settings)
        return if (_name.value.isEmpty()) "invalid" else null
    }

    fun remove(settings: SettingsStore) {
        settings.remove(Prefs.customFontB64)
        settings.remove(Prefs.customFontName)
        if (settings.get(Prefs.appFont) == FAMILY) settings.put(Prefs.appFont, "")
        clear()
    }

    private fun clear() {
        _family.value = null
        _name.value = ""
    }

    /** TrueType, OpenType and font collections start with a known tag. */
    private fun looksLikeFont(b: ByteArray): Boolean {
        if (b.size < 12) return false
        val tag = b.copyOfRange(0, 4).decodeToString()
        return tag == "OTTO" || tag == "true" || tag == "ttcf" ||
            (b[0] == 0.toByte() && b[1] == 1.toByte() && b[2] == 0.toByte() && b[3] == 0.toByte())
    }
}

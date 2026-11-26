package com.sanobld.laststats.data.local

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface as ComposeTypeface
import com.sanobld.laststats.core.AppConstants
import com.sanobld.laststats.ui.theme.FontCatalog
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Font imported by the person (.ttf or .otf). It is saved as base64 in the
 * `ls_custom_font_b64` setting, so a backup carries it.
 */
object CustomFontStore {
    const val FAMILY = FontCatalog.CUSTOM_KEY

    private val _name = MutableStateFlow("")
    /** Display name of the imported font, empty when there is none. */
    val name: StateFlow<String> = _name

    private val _family = MutableStateFlow<FontFamily?>(null)
    val family: StateFlow<FontFamily?> = _family

    /** Registers the saved font. Call at startup and after a restore. */
    fun load(context: Context, settings: SettingsStore) {
        val b64 = settings.get(Prefs.customFontB64)
        if (b64.isEmpty()) {
            clear()
            return
        }
        try {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            if (!looksLikeFont(bytes)) {
                clear()
                return
            }
            val file = File(context.applicationContext.cacheDir, "custom_font.bin")
            file.writeBytes(bytes)
            val typeface = Typeface.createFromFile(file)
            _family.value = FontFamily(ComposeTypeface(typeface))
            _name.value = settings.get(Prefs.customFontName).ifEmpty { FAMILY }
        } catch (_: Exception) {
            clear()
        }
    }

    /** Returns null on success, else "size", "invalid" or "cancel". */
    suspend fun import(context: Context, settings: SettingsStore, uri: Uri?): String? {
        if (uri == null) return "cancel"
        val read = withContext(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                val label = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                    val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (i >= 0 && c.moveToFirst()) c.getString(i) else null
                }
                bytes to label
            } catch (_: Exception) {
                null to null
            }
        }
        val bytes = read.first ?: return "invalid"
        if (bytes.size > AppConstants.CUSTOM_FONT_MAX_BYTES) return "size"
        if (!looksLikeFont(bytes)) return "invalid"
        val name = (read.second ?: FAMILY).replace(Regex("\\.(ttf|otf)$", RegexOption.IGNORE_CASE), "")
        settings.putAll {
            Prefs.customFontB64.write(it, Base64.encodeToString(bytes, Base64.NO_WRAP))
            Prefs.customFontName.write(it, name)
        }
        load(context, settings)
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
        val tag = String(b, 0, 4, Charsets.ISO_8859_1)
        return tag == "OTTO" || tag == "true" || tag == "ttcf" ||
            (b[0] == 0.toByte() && b[1] == 1.toByte() && b[2] == 0.toByte() && b[3] == 0.toByte())
    }
}

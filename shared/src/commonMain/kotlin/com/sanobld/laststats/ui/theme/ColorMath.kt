package com.sanobld.laststats.ui.theme

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** RGB and HSL conversion without platform code. Hue 0..360, saturation and lightness 0..1. */
object ColorMath {
    fun colorToHSL(argb: Int, out: FloatArray) {
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC
        var h: Float
        val s: Float
        val l = (maxC + minC) / 2f
        if (maxC == minC) {
            h = 0f
            s = 0f
        } else {
            h = when (maxC) {
                r -> ((g - b) / delta) % 6f
                g -> (b - r) / delta + 2f
                else -> (r - g) / delta + 4f
            }
            s = delta / (1f - abs(2f * l - 1f))
        }
        h = (h * 60f) % 360f
        if (h < 0f) h += 360f
        out[0] = h.coerceIn(0f, 360f)
        out[1] = s.coerceIn(0f, 1f)
        out[2] = l.coerceIn(0f, 1f)
    }

    fun HSLToColor(hsl: FloatArray): Int {
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]
        val c = (1f - abs(2f * l - 1f)) * s
        val m = l - 0.5f * c
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val (r1, g1, b1) = when ((h / 60f).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val r = ((r1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255f).roundToInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}

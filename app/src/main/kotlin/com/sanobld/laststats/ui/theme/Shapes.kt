package com.sanobld.laststats.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Material 3 shapes of the app. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val SnackbarShape = RoundedCornerShape(20.dp)
val MenuShape = RoundedCornerShape(20.dp)
val DialogShape = RoundedCornerShape(28.dp)

// ── Cookie shape ─────────────────────────────────────────────────────────────

/**
 * A circle with soft bumps. [amplitude] 0 is a perfect circle, 0.08 a cookie.
 * Changing the amplitude is animated by the callers (the press effect).
 */
data class CookieShape(val lobes: Int = 8, val amplitude: Float = 0.07f) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(cookiePath(size, lobes, amplitude))
}

fun cookiePath(size: Size, lobes: Int, amplitude: Float): Path {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = min(size.width, size.height) / 2f
    val steps = 180
    val path = Path()
    for (i in 0..steps) {
        val a = (i.toFloat() / steps) * (PI * 2).toFloat()
        val k = 1f - amplitude * (1f - cos(lobes * a)) / 2f
        val x = cx + r * k * cos(a)
        val y = cy + r * k * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

val FabShape = CookieShape(lobes = 8, amplitude = 0.05f)

// ── Buttons: pill at rest, softer square while pressed ───────────────────────

/** Radius goes from half the height (pill) to [pressedRadius] as [fraction] goes 0 to 1. */
data class PillMorphShape(val fraction: Float, val pressedRadius: Dp = 14.dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val pill = min(size.width, size.height) / 2f
        val pressed = with(density) { pressedRadius.toPx() }.coerceAtMost(pill)
        val r = pill + (pressed - pill) * fraction.coerceIn(0f, 1f)
        return Outline.Rounded(RoundRect(Rect(Offset.Zero, size), CornerRadius(r, r)))
    }
}

/** Shape of a button: animated between pill and soft square. */
@Composable
fun animatedPillShape(pressed: Boolean): Shape {
    val f by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = M3Motion.spatialFast(),
        label = "pillShape",
    )
    return PillMorphShape(f)
}

/** Round buttons stay round when pressed. */
val RoundButtonShape: Shape = CircleShape

// ── Shapes for images ────────────────────────────────────────────────────────

const val ImageShapeCount = 8

/** Same text always gives the same shape. */
fun shapeIndexFor(seed: String): Int {
    var h = 17
    for (u in seed) h = (h * 31 + u.code) and 0x7fffffff
    return h % ImageShapeCount
}

/** Material You shapes whose corners follow the size of the box. */
data class ImageShape(val index: Int) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = min(size.width, size.height)
        val full = Rect(Offset.Zero, size)
        fun rounded(tl: Float, tr: Float, br: Float, bl: Float): Outline = Outline.Rounded(
            RoundRect(
                full,
                topLeft = CornerRadius(tl, tl),
                topRight = CornerRadius(tr, tr),
                bottomRight = CornerRadius(br, br),
                bottomLeft = CornerRadius(bl, bl),
            ),
        )
        return when (((index % ImageShapeCount) + ImageShapeCount) % ImageShapeCount) {
            0 -> Outline.Generic(cookiePath(size, 8, 0.07f))
            1 -> rounded(s / 2, s / 2, s / 2, s / 2)
            2 -> Outline.Generic(cookiePath(size, 4, 0.2f))
            3 -> rounded(s / 2, s / 2, s * 0.14f, s * 0.14f)
            4 -> Outline.Generic(cookiePath(size, 12, 0.07f))
            5 -> rounded(s * 0.36f, s * 0.36f, s * 0.36f, s * 0.36f)
            6 -> rounded(s * 0.5f, s * 0.14f, s * 0.5f, s * 0.14f)
            else -> Outline.Generic(cookiePath(size, 2, 0.2f))
        }
    }
}

/** Shape for the current setting (Settings > Appearance). */
fun resolveImageShape(mode: String, index: Int): Shape {
    if (mode == "square") return ProportionalRoundShape(0.14f)
    if (mode == "circle") return CircleShape
    if (mode.startsWith("shape:")) {
        val n = mode.substring(6).toIntOrNull()
        if (n != null) return ImageShape(n)
    }
    return ImageShape(index)
}

/** Rounded square whose radius is a share of the shortest side. */
data class ProportionalRoundShape(val share: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = min(size.width, size.height) * share
        return Outline.Rounded(RoundRect(Rect(Offset.Zero, size), CornerRadius(r, r)))
    }
}

// ── Sheets, dialogs and snackbars share one motion ───────────────────────────

object M3Anim {
    const val SHEET_ENTER_MS = 400
    const val SHEET_EXIT_MS = 250
    const val DIALOG_ENTER_MS = 250
    const val DIALOG_EXIT_MS = 150
    const val SNACKBAR_ENTER_MS = 420
    const val SNACKBAR_EXIT_MS = 380
}

/** Shows a snackbar and replaces the one on screen instead of queueing behind it. */
suspend fun SnackbarHostState.showApp(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(message, actionLabel, withDismissAction, duration)
}

/** Floating snackbars tinted with the accent. */
@Composable
fun AppSnackbarHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    SnackbarHost(hostState = state, modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) { data ->
        Snackbar(
            snackbarData = data,
            shape = SnackbarShape,
            containerColor = cs.secondaryContainer,
            contentColor = cs.onSecondaryContainer,
            actionColor = cs.primary,
            dismissActionContentColor = cs.onSecondaryContainer,
        )
    }
}

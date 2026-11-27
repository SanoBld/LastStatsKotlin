package com.sanobld.laststats.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.material3.ColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Text scale of the recap "story" screen, reused by the rest of the app. */
object AppText {
    val hero = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.W900, lineHeight = 56.sp)
    val title = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W800)
    val itemTitle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.W800)
    val body = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.W700)
    val badge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W700)
    val label = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.W600)
    val caption = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.W600)
}

object AppRadius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 14.dp
    val xl = 20.dp
    val smShape = RoundedCornerShape(sm)
    val mdShape = RoundedCornerShape(md)
    val lgShape = RoundedCornerShape(lg)
    val xlShape = RoundedCornerShape(xl)
}

object AppMotion {
    const val FAST_MS = 200
    const val BASE_MS = 260
    const val SLOW_MS = 350
    val curve = CubicBezierEasing(0f, 0f, 0.58f, 1f)
    val curveEmphasized = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)
}

/** Accent wash behind the top of a page (story pages, dashboard, detail sheets). */
fun storyHeaderGradient(scheme: ColorScheme): Brush =
    Brush.verticalGradient(
        0f to scheme.primaryContainer.copy(alpha = 0.55f),
        0.32f to scheme.surface,
        1f to scheme.surface,
    )

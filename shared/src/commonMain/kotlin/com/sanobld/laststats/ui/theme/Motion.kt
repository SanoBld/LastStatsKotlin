package com.sanobld.laststats.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sanobld.laststats.core.PlatformCaps

/** True when the system turned animations off or eco mode is on. */
val LocalReducedMotion = staticCompositionLocalOf { false }

/** Soft blur allowed: setting on, eco mode off. */
val LocalBlurEnabled = staticCompositionLocalOf { true }

/** Material 3 easing, durations and springs (as curves) in one place. */
object M3Motion {
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** "Spatial" moves, sizes and shapes: a little overshoot. */
    val SpatialFast: Easing = CubicBezierEasing(0.42f, 1.67f, 0.21f, 0.90f)
    val SpatialDefault: Easing = CubicBezierEasing(0.38f, 1.21f, 0.22f, 1.00f)
    val SpatialSlow: Easing = CubicBezierEasing(0.39f, 1.29f, 0.35f, 0.98f)

    /** "Effects": colour and opacity, no overshoot. */
    val EffectsFast: Easing = CubicBezierEasing(0.31f, 0.94f, 0.34f, 1.00f)
    val EffectsDefault: Easing = CubicBezierEasing(0.34f, 0.80f, 0.34f, 1.00f)
    val EffectsSlow: Easing = CubicBezierEasing(0.34f, 0.88f, 0.34f, 1.00f)

    val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)
    val EaseIn: Easing = CubicBezierEasing(0.42f, 0f, 1f, 1f)
    val EaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
    val EaseOutCubic: Easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)
    val EaseInOutCubic: Easing = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1f)

    const val SpatialFastMs = 350
    const val SpatialDefaultMs = 500
    const val SpatialSlowMs = 650
    const val EffectsFastMs = 150
    const val EffectsDefaultMs = 200
    const val EffectsSlowMs = 300
    const val ShortMs = 150
    const val MediumMs = 300
    const val LongMs = 450

    fun <T> spatialFast(): FiniteAnimationSpec<T> = tween(SpatialFastMs, easing = SpatialFast)
    fun <T> spatialDefault(): FiniteAnimationSpec<T> = tween(SpatialDefaultMs, easing = SpatialDefault)
    fun <T> spatialSlow(): FiniteAnimationSpec<T> = tween(SpatialSlowMs, easing = SpatialSlow)
    fun <T> effectsFast(): FiniteAnimationSpec<T> = tween(EffectsFastMs, easing = EffectsFast)
    fun <T> effectsDefault(): FiniteAnimationSpec<T> = tween(EffectsDefaultMs, easing = EffectsDefault)
    fun <T> effectsSlow(): FiniteAnimationSpec<T> = tween(EffectsSlowMs, easing = EffectsSlow)
    fun <T> emphasized(ms: Int = MediumMs): FiniteAnimationSpec<T> = tween(ms, easing = Emphasized)
}

/** Part of a 0..1 progress that runs between [begin] and [end], shaped by [easing]. */
fun interval(t: Float, begin: Float, end: Float, easing: Easing = M3Motion.EaseOut): Float {
    if (end <= begin) return if (t >= end) 1f else 0f
    return easing.transform(((t - begin) / (end - begin)).coerceIn(0f, 1f))
}

// ── Blur helper ──────────────────────────────────────────────────────────────

/** Soft blur of [radiusPx] on Android 12 and later. Nothing on older versions. */
fun GraphicsLayerScope.softBlur(radiusPx: Float) {
    if (PlatformCaps.blur && radiusPx > 0.3f) {
        renderEffect = BlurEffect(radiusPx, radiusPx, TileMode.Decal)
    }
}

// ── Where a tapped item was (for "rise" page transitions) ────────────────────

/** Last tapped item bounds and the root size. Used as the start of a rise transition. */
object M3Origin {
    var rect: Rect? = null
    var rootWidth: Float = 0f
    var rootHeight: Float = 0f

    fun clear() {
        rect = null
    }

    /** Pivot of the scale, from the tapped item. Centre when unknown. */
    fun transformOrigin(): TransformOrigin {
        val r = rect
        if (r == null || r.isEmpty || rootWidth <= 0f || rootHeight <= 0f) return TransformOrigin.Center
        return TransformOrigin(
            (r.center.x / rootWidth).coerceIn(0f, 1f),
            (r.center.y / rootHeight).coerceIn(0f, 1f),
        )
    }

    /** Start scale: smaller when it grows out of a small item. */
    fun startScale(): Float {
        val r = rect
        if (r == null || r.isEmpty || rootWidth <= 0f || rootHeight <= 0f) return 0.93f
        return if (r.width * r.height < rootWidth * rootHeight * 0.6f) 0.84f else 0.93f
    }
}

// ── Page transitions for Navigation Compose ──────────────────────────────────

object M3Transitions {
    private const val SLIDE_IN_MS = 450
    private const val SLIDE_OUT_MS = 350
    private const val RISE_IN_MS = 480
    private const val RISE_OUT_MS = 380

    // Page to page: the new page slides in a little and fades, the old one drifts and stays solid.
    fun slideEnter(): EnterTransition =
        slideInHorizontally(tween(SLIDE_IN_MS, easing = M3Motion.Emphasized)) { (it * 0.28f).toInt() } +
            fadeIn(tween((SLIDE_IN_MS * 0.4f).toInt(), easing = M3Motion.EaseOut))

    fun slideExit(): ExitTransition =
        slideOutHorizontally(tween(SLIDE_IN_MS, easing = M3Motion.Emphasized)) { -(it * 0.12f).toInt() }

    fun slidePopEnter(): EnterTransition =
        slideInHorizontally(tween(SLIDE_OUT_MS, easing = M3Motion.EaseInOutCubic)) { -(it * 0.12f).toInt() }

    fun slidePopExit(): ExitTransition =
        slideOutHorizontally(tween(SLIDE_OUT_MS, easing = M3Motion.EaseInOutCubic)) { (it * 0.28f).toInt() } +
            fadeOut(tween(SLIDE_OUT_MS, easing = M3Motion.EaseIn))

    // Detail pages: the page rises, growing from the tapped item when it is known.
    fun riseEnter(): EnterTransition =
        scaleIn(
            animationSpec = tween(RISE_IN_MS, easing = M3Motion.Emphasized),
            initialScale = M3Origin.startScale(),
            transformOrigin = M3Origin.transformOrigin(),
        ) + slideInVertically(tween(RISE_IN_MS, easing = M3Motion.Emphasized)) { (it * 0.08f).toInt() } +
            fadeIn(tween((RISE_IN_MS * 0.4f).toInt(), easing = M3Motion.EaseOut))

    fun riseExit(): ExitTransition =
        scaleOut(tween(RISE_IN_MS, easing = M3Motion.Emphasized), targetScale = 0.94f)

    fun risePopEnter(): EnterTransition =
        scaleIn(tween(RISE_OUT_MS, easing = M3Motion.EaseInOutCubic), initialScale = 0.94f)

    fun risePopExit(): ExitTransition =
        scaleOut(
            animationSpec = tween(RISE_OUT_MS, easing = M3Motion.EaseInOutCubic),
            targetScale = M3Origin.startScale(),
            transformOrigin = M3Origin.transformOrigin(),
        ) + slideOutVertically(tween(RISE_OUT_MS, easing = M3Motion.EaseInOutCubic)) { (it * 0.08f).toInt() } +
            fadeOut(tween(RISE_OUT_MS, easing = M3Motion.EaseIn))

    // One-time flows (setup, onboarding, home): the old page fades out, then the new one fades in.
    fun fadeThroughEnter(): EnterTransition =
        fadeIn(tween(300, delayMillis = 105, easing = M3Motion.EaseOut))

    fun fadeThroughExit(): ExitTransition =
        fadeOut(tween(105, easing = M3Motion.EaseIn))

    fun fadeThroughPopEnter(): EnterTransition =
        fadeIn(tween(200, delayMillis = 70, easing = M3Motion.EaseOut))

    fun fadeThroughPopExit(): ExitTransition =
        fadeOut(tween(70, easing = M3Motion.EaseIn))

    // Child pages of the same flow: rises and fades in, the old one fades and shrinks a little.
    fun sharedAxisEnter(): EnterTransition =
        slideInVertically(tween(SLIDE_IN_MS, easing = M3Motion.SpatialDefault)) { (it * 0.08f).toInt() } +
            fadeIn(tween(220, easing = M3Motion.EaseOut))

    fun sharedAxisExit(): ExitTransition =
        scaleOut(tween(SLIDE_IN_MS, easing = M3Motion.Emphasized), targetScale = 0.96f) +
            fadeOut(tween(160, easing = M3Motion.EaseIn))

    fun sharedAxisPopEnter(): EnterTransition =
        scaleIn(tween(SLIDE_OUT_MS, easing = M3Motion.EaseInOutCubic), initialScale = 0.96f) +
            fadeIn(tween(200, easing = M3Motion.EaseOut))

    fun sharedAxisPopExit(): ExitTransition =
        slideOutVertically(tween(SLIDE_OUT_MS, easing = M3Motion.EaseInOutCubic)) { (it * 0.08f).toInt() } +
            fadeOut(tween(160, easing = M3Motion.EaseIn))
}

// ── Content arriving: rises, grows and sharpens ──────────────────────────────

/** [progress] runs 0 to 1. Reduced motion shows the content at once. */
@Composable
fun Modifier.m3Reveal(progress: () -> Float): Modifier {
    val reduced = LocalReducedMotion.current
    if (reduced) return this
    val blurOn = LocalBlurEnabled.current
    val density = LocalDensity.current
    val offsetPx = with(density) { 36.dp.toPx() }
    val blurPx = with(density) { 8.dp.toPx() }
    return graphicsLayer {
        val v = progress().coerceIn(0f, 1f)
        val k = M3Motion.SpatialDefault.transform(v)
        alpha = M3Motion.EaseOut.transform((v * 2f).coerceIn(0f, 1f))
        translationY = offsetPx * (1f - k)
        val sc = 0.92f + 0.08f * k
        scaleX = sc
        scaleY = sc
        if (blurOn && v < 1f) softBlur(blurPx * (1f - v))
    }
}

/** Plays the reveal once when it enters the screen. [delayMs] staggers lists. */
@Composable
fun M3Reveal(
    modifier: Modifier = Modifier,
    delayMs: Int = 0,
    content: @Composable () -> Unit,
) {
    val reduced = LocalReducedMotion.current
    val progress = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            if (delayMs > 0) kotlinx.coroutines.delay(delayMs.toLong())
            progress.animateTo(1f, tween(M3Motion.SpatialDefaultMs, easing = androidx.compose.animation.core.LinearEasing))
        }
    }
    Box(modifier.m3Reveal { progress.value }) { content() }
}

/**
 * Switches between states: the old content sinks and softens, the new content
 * rises and sharpens. Not a plain cross-fade.
 */
@Composable
fun <T> M3Switcher(
    targetState: T,
    modifier: Modifier = Modifier,
    durationMs: Int = 450,
    label: String = "m3switcher",
    content: @Composable (T) -> Unit,
) {
    val reduced = LocalReducedMotion.current
    val offsetPx = with(LocalDensity.current) { 36.dp.roundToPx() }
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            val d = if (reduced) M3Motion.ShortMs else durationMs
            val enter = if (reduced) {
                fadeIn(tween(d, easing = M3Motion.EaseOut))
            } else {
                fadeIn(tween(d, delayMillis = d / 4, easing = M3Motion.EaseOut)) +
                    slideInVertically(tween(d, delayMillis = d / 4, easing = M3Motion.SpatialDefault)) { offsetPx } +
                    scaleIn(tween(d, delayMillis = d / 4, easing = M3Motion.SpatialDefault), initialScale = 0.92f)
            }
            val exit = if (reduced) {
                fadeOut(tween(d / 2, easing = M3Motion.EaseIn))
            } else {
                fadeOut(tween(d / 2, easing = M3Motion.EaseIn)) +
                    scaleOut(tween(d / 2, easing = M3Motion.EaseIn), targetScale = 0.97f)
            }
            enter togetherWith exit using SizeTransform(clip = false)
        },
        label = label,
    ) { state -> content(state) }
}

// ── Top level destinations (bottom bar or rail) ──────────────────────────────

/**
 * Tabs cross-fade with a small directional slide and a soft blur.
 * Every visited tab stays composed, so it keeps its state. A tab is composed
 * the first time it is shown.
 */
@Composable
fun M3FadeThroughStack(
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit,
) {
    val reduced = LocalReducedMotion.current
    val blurOn = LocalBlurEnabled.current && !reduced
    val density = LocalDensity.current
    val slidePx = with(density) { 24.dp.toPx() }
    val slideOutPx = with(density) { 16.dp.toPx() }
    val blurPx = with(density) { 5.dp.toPx() }

    class Holder(var current: Int, var previous: Int)

    val holder = remember { Holder(index, -1) }
    val visited = remember { mutableSetOf(index) }
    if (holder.current != index) {
        holder.previous = holder.current
        holder.current = index
    }
    visited.add(index)

    val progress = remember(index) { Animatable(if (holder.previous < 0) 1f else 0f) }
    LaunchedEffect(index) {
        if (progress.value < 1f) {
            progress.animateTo(1f, tween(if (reduced) M3Motion.ShortMs else 340, easing = androidx.compose.animation.core.LinearEasing))
        }
    }

    Box(modifier.fillMaxSize()) {
        for (i in 0 until count) {
            if (i !in visited) continue
            val isCurrent = i == index
            val isPrevious = i == holder.previous && progress.value < 1f
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val t = progress.value
                        val dir = if (holder.current >= holder.previous) 1f else -1f
                        var a = 0f
                        var sc = 1f
                        var dx = 0f
                        var sigma = 0f
                        if (isCurrent) {
                            val k = M3Motion.EmphasizedDecelerate.transform(t)
                            a = interval(t, 0.1f, 0.8f, M3Motion.EaseOut)
                            if (!reduced) {
                                sc = 0.98f + 0.02f * k
                                dx = dir * slidePx * (1f - k)
                                sigma = if (blurOn) blurPx * (1f - k) else 0f
                            }
                        } else if (isPrevious) {
                            val k = interval(t, 0f, 0.45f, M3Motion.EaseIn)
                            a = 1f - k
                            if (!reduced) {
                                dx = -dir * slideOutPx * k
                                sigma = if (blurOn) blurPx * 0.8f * k else 0f
                            }
                        }
                        alpha = a.coerceIn(0f, 1f)
                        scaleX = sc
                        scaleY = sc
                        translationX = dx
                        softBlur(sigma)
                    }
                    .then(if (isCurrent) Modifier else Modifier.pointerInput(Unit) { })
            ) {
                content(i)
            }
        }
    }
}

// ── Letter by letter blur reveal (titles, recap text) ────────────────────────

/**
 * Each letter sharpens from a blur and rises, one after the other.
 * [progress] runs 0 to 1. Reduced motion shows the text at once.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun M3BlurText(
    text: String,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    align: TextAlign = TextAlign.Center,
) {
    val reduced = LocalReducedMotion.current
    if (reduced) {
        Text(text, modifier = modifier, style = style, textAlign = align)
        return
    }
    val blurOn = LocalBlurEnabled.current
    val density = LocalDensity.current
    val risePx = with(density) { 14.dp.toPx() }
    val blurPx = with(density) { 8.dp.toPx() }
    val total = text.length.coerceIn(1, 80)

    val words = remember(text) {
        var n = 0
        text.split(' ').map { w ->
            val letters = ArrayList<Pair<Int, String>>()
            var i = 0
            while (i < w.length) {
                val len = if (w[i].isHighSurrogate() && i + 1 < w.length && w[i + 1].isLowSurrogate()) 2 else 1
                letters.add(n++ to w.substring(i, i + len))
                i += len
            }
            letters
        }
    }
    val horizontal = when (align) {
        TextAlign.Center -> Arrangement.Center
        TextAlign.End, TextAlign.Right -> Arrangement.End
        else -> Arrangement.Start
    }
    FlowRow(modifier = modifier, horizontalArrangement = horizontal) {
        for (word in words) {
            Row(Modifier.padding(end = 6.dp)) {
                for ((index, letter) in word) {
                    Text(
                        text = letter,
                        style = style,
                        modifier = Modifier.graphicsLayer {
                            val a = (index.toFloat() / total) * 0.55f
                            val k = ((progress() - a) / 0.45f).coerceIn(0f, 1f)
                            val e = M3Motion.EaseOutCubic.transform(k)
                            alpha = e
                            translationY = risePx * (1f - e)
                            if (blurOn) softBlur(blurPx * (1f - e))
                        },
                    )
                }
            }
        }
    }
}

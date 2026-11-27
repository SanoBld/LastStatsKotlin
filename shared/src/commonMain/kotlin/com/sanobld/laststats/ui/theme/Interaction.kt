package com.sanobld.laststats.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** How strong the "pressed" fill is. It fills the exact shape of the widget. */
const val PRESS_ALPHA = 0.12f

/** Scale of a tappable while pressed (1 = no bounce). */
const val BOUNCE_SCALE = 0.97f

/** Default corner of a tappable that gets no shape. */
val PressRadius = 16.dp

/** Top bar: always the same colour, never turns grey when content scrolls. */
fun m3TopBarColor(s: ColorScheme): Color = s.surface

// ── Indication: no round ripple, the pressed state fills the shape ───────────

class PressIndication(private val color: Color) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        PressIndicationNode(interactionSource, color)

    override fun equals(other: Any?): Boolean = other is PressIndication && other.color == color

    override fun hashCode(): Int = color.hashCode()
}

private class PressIndicationNode(
    private val interactionSource: InteractionSource,
    private val color: Color,
) : Modifier.Node(), DrawModifierNode {
    private val presses = mutableListOf<PressInteraction.Press>()
    private val hovers = mutableListOf<HoverInteraction.Enter>()

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { i ->
                when (i) {
                    is PressInteraction.Press -> presses.add(i)
                    is PressInteraction.Release -> presses.remove(i.press)
                    is PressInteraction.Cancel -> presses.remove(i.press)
                    is HoverInteraction.Enter -> hovers.add(i)
                    is HoverInteraction.Exit -> hovers.remove(i.enter)
                }
                invalidateDraw()
            }
        }
    }

    override fun androidx.compose.ui.graphics.drawscope.ContentDrawScope.draw() {
        drawContent()
        when {
            presses.isNotEmpty() -> drawRect(color)
            hovers.isNotEmpty() -> drawRect(color.copy(alpha = color.alpha / 2f))
        }
    }
}

@Composable
fun rememberPressIndication(): PressIndication {
    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = PRESS_ALPHA)
    return remember(color) { PressIndication(color) }
}

// ── THE tappable of the app ──────────────────────────────────────────────────

/**
 * Use it instead of clickable everywhere.
 * - no round ripple, the pressed state fills the exact shape
 * - a rounded shape is used when none is given (never a sharp square)
 * - small bounce (scale down while the finger is down)
 * - [captureOrigin] remembers where the item is, so the next page can grow out of it
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.m3Clickable(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(PressRadius),
    enabled: Boolean = true,
    bounce: Boolean = true,
    bounceScale: Float = BOUNCE_SCALE,
    captureOrigin: Boolean = false,
    role: Role? = null,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val canBounce = bounce && enabled
    val scale by animateFloatAsState(
        targetValue = if (pressed && canBounce && !reduced) bounceScale else 1f,
        animationSpec = M3Motion.spatialFast(),
        label = "bounce",
    )
    val indication = rememberPressIndication()
    val bounds = remember { arrayOfNulls<androidx.compose.ui.geometry.Rect>(1) }
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .then(if (captureOrigin) Modifier.onGloballyPositioned { bounds[0] = it.boundsInRoot() } else Modifier)
        .combinedClickable(
            interactionSource = source,
            indication = indication,
            enabled = enabled,
            role = role,
            onLongClick = onLongClick,
            onClick = {
                if (captureOrigin) M3Origin.rect = bounds[0]
                onClick()
            },
        )
}

// ── Tooltip in the app style ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTooltip(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip(
                containerColor = cs.surfaceContainerHighest,
                contentColor = cs.onSurface,
                shape = RoundedCornerShape(50),
            ) {
                Text(text, fontWeight = FontWeight.W700, style = MaterialTheme.typography.labelLarge)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        content = content,
    )
}

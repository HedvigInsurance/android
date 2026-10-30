package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.Plus
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import kotlin.math.roundToInt

/** The size a [PaymentMethodTile]'s mark is drawn at. */
val PaymentMethodMarkSize = 39.dp

/** The size a [PaymentMethodPillow]'s mark is drawn at. */
val PaymentMethodPillowMarkSize = 28.dp

private val TileSize = 74.dp
private val PillowSize = 40.dp
private val BadgeSize = 24.dp
private val BadgeOffset = 8.dp

/**
 * The outlined tile a payment method is shown on while it is being connected or given up, with an
 * optional status badge on its top-right corner. [mark] is the method's brand mark, drawn at
 * [PaymentMethodMarkSize]; it inherits the tile's content colour, so a monochrome mark reads
 * correctly in both themes.
 */
@Composable
fun PaymentMethodTile(
  modifier: Modifier = Modifier,
  badge: @Composable (() -> Unit)? = null,
  mark: @Composable () -> Unit = PlusMark,
) {
  BadgedTile(badge, modifier) {
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = HedvigTheme.colorScheme.backgroundPrimary,
      contentColor = HedvigTheme.colorScheme.fillPrimary,
      modifier = Modifier
        .size(TileSize)
        .hedvigDropShadow(HedvigTheme.shapes.cornerXXLarge)
        .dashedBorder(
          color = HedvigTheme.colorScheme.borderSecondary,
          shape = HedvigTheme.shapes.cornerXXLarge,
        ),
    ) {
      Box(Modifier.size(TileSize), contentAlignment = Alignment.Center) {
        mark()
      }
    }
  }
}

/** The mark a [PaymentMethodTile] shows before a method has been picked. */
@Composable
fun PaymentMethodPlusMark(modifier: Modifier = Modifier) {
  Icon(
    imageVector = HedvigIcons.Plus,
    contentDescription = null,
    tint = HedvigTheme.colorScheme.fillSecondary,
    modifier = modifier,
  )
}

/**
 * The small tile a payment method's brand mark sits on in a list row. The defaults suit a
 * full-colour mark; a monochrome one reads better inverted, on [ColorScheme.fillBlack].
 */
@Composable
fun PaymentMethodPillow(
  modifier: Modifier = Modifier,
  containerColor: Color = HedvigTheme.colorScheme.fillWhite,
  contentColor: Color = HedvigTheme.colorScheme.fillBlack,
  mark: @Composable () -> Unit,
) {
  Surface(
    shape = HedvigTheme.shapes.cornerSmall,
    color = containerColor,
    contentColor = contentColor,
    modifier = modifier.size(PillowSize),
  ) {
    Box(Modifier.size(PillowSize), contentAlignment = Alignment.Center) {
      mark()
    }
  }
}

/** A circular status badge for the corner of a [PaymentMethodTile] or the Hedvig symbol. */
@Composable
fun PaymentMethodTileBadge(
  icon: ImageVector,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(BadgeSize)
      .clip(CircleShape)
      .background(containerColor),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
      modifier = Modifier.size(16.dp),
    )
  }
}

/**
 * The method being connected, linked by pulsing dots to the Hedvig symbol, laid out in the way money
 * moves for [direction]. [destinationBadge] marks the tile on the receiving end once the hand-off has
 * landed. [method] is the picked method and [mark] draws its brand mark; while [method] is null
 * nothing is picked yet, so the Hedvig symbol stands alone and the method tile and dots grow out from
 * it once a method is picked. Picking another method cross-fades the mark, so a screen whose method
 * never changes can pass anything, such as [Unit].
 */
@Composable
fun <T : Any> PaymentMethodHandoverIllustration(
  method: T?,
  modifier: Modifier = Modifier,
  destinationBadge: @Composable (() -> Unit)? = null,
  loadingState: LoadingState = LoadingState.PROCESSING,
  direction: PaymentHandoverDirection = PaymentHandoverDirection.ToHedvig,
  mark: @Composable (method: T) -> Unit,
) {
  val methodFraction = animateFloatAsState(
    targetValue = if (method != null) 1f else 0f,
    animationSpec = tween(MotionTokens.DurationMedium2.toInt(), easing = MotionTokens.EasingStandardCubicBezier),
    label = "method side",
  )
  val methodSide = Modifier.collapsedHorizontally(methodFraction)
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
  ) {
    when (direction) {
      PaymentHandoverDirection.ToHedvig -> {
        PaymentMethodTile(mark = { SwappingMark(method, mark) }, modifier = methodSide)
        HandoverDots(loadingState, methodFraction, methodSide)
        HedvigTile(badge = destinationBadge)
      }

      PaymentHandoverDirection.FromHedvig -> {
        HedvigTile()
        HandoverDots(loadingState, methodFraction, methodSide)
        PaymentMethodTile(mark = { SwappingMark(method, mark) }, badge = destinationBadge, modifier = methodSide)
      }
    }
  }
}

private val PlusMark: @Composable () -> Unit = { PaymentMethodPlusMark(Modifier.size(PaymentMethodMarkSize)) }

/**
 * The [mark] of [method], or the plus while there is none, with a change of method cross-faded: the
 * old mark shrinks away as the new one grows in.
 */
@Composable
private fun <T : Any> SwappingMark(method: T?, mark: @Composable (method: T) -> Unit) {
  AnimatedContent(
    targetState = method,
    transitionSpec = {
      val spec = tween<Float>(MotionTokens.DurationMedium2.toInt(), easing = MotionTokens.EasingStandardCubicBezier)
      (fadeIn(spec) + scaleIn(spec, initialScale = 0.5f)) togetherWith
        (fadeOut(spec) + scaleOut(spec, targetScale = 0.5f))
    },
    contentAlignment = Alignment.Center,
    label = "method mark",
  ) { shownMethod ->
    if (shownMethod != null) mark(shownMethod) else PlusMark()
  }
}

/**
 * The dots between the tiles. They breathe on an infinite transition, so while the method side is
 * fully collapsed they leave the composition rather than animating unseen; their collapsed width is
 * zero, so the row does not move when they do.
 */
@Composable
private fun HandoverDots(loadingState: LoadingState, methodFraction: State<Float>, modifier: Modifier) {
  val shown by remember(methodFraction) { derivedStateOf { methodFraction.value > 0f } }
  if (shown) {
    Box(modifier.padding(horizontal = 16.dp)) {
      when (loadingState) {
        LoadingState.PROCESSING -> ThreeDotsLoading()

        LoadingState.ACTIVE -> ThreeDotsLoaded()

        LoadingState.INACTIVE -> ThreeDotsLoaded(
          stableColor = ColorSchemeKeyTokens.SurfaceSecondaryTransparent.value,
        )
      }
    }
  }
}

/**
 * Lays the content out in [fraction] of its own width, centred in that space, fading and shrinking it
 * in step. Measuring the content in full and scaling only the space it claims is what lets a
 * wrap-content width animate to and from zero.
 *
 * Fading renders the content into an offscreen layer of its own bounds, which would clip a drop
 * shadow drawn outside them, so the layer is padded out by [HedvigDropShadowExtent] and the padding
 * is taken back out of the reported size.
 */
private fun Modifier.collapsedHorizontally(fraction: State<Float>): Modifier = this
  .layout { measurable, constraints ->
    val bleed = HedvigDropShadowExtent.roundToPx()
    val placeable = measurable.measure(constraints.copy(minWidth = 0).offset(2 * bleed, 2 * bleed))
    val width = ((placeable.width - 2 * bleed) * fraction.value).roundToInt()
    layout(width, placeable.height - 2 * bleed) {
      placeable.placeRelative((width - placeable.width) / 2, -bleed)
    }
  }
  .graphicsLayer {
    val shown = fraction.value
    alpha = shown
    scaleX = 0.5f + 0.5f * shown
    scaleY = scaleX
  }
  .padding(HedvigDropShadowExtent)

/** Which way money moves between the member's method and Hedvig, which decides the tiles' order. */
enum class PaymentHandoverDirection {
  /** Payins: the method on the left, handing over to Hedvig on the right. */
  ToHedvig,

  /** Payouts: Hedvig on the left, handing over to the method on the right. */
  FromHedvig,
}

@Composable
private fun HedvigTile(badge: (@Composable () -> Unit)? = null, modifier: Modifier = Modifier) {
  BadgedTile(badge, modifier) {
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = HedvigTheme.colorScheme.fillBlack,
      contentColor = HedvigTheme.colorScheme.fillWhite,
      border = HedvigTheme.colorScheme.borderPrimary,
      modifier = Modifier.size(TileSize),
    ) {
      Box(Modifier.size(TileSize), contentAlignment = Alignment.Center) {
        Icon(
          imageVector = HedvigIcons.HelipadOutline,
          contentDescription = null,
          modifier = Modifier.size(65.dp),
        )
      }
    }
  }
}

/** [tile] with [badge], when there is one, overlapping its top-right corner. */
@Composable
private fun BadgedTile(badge: (@Composable () -> Unit)?, modifier: Modifier = Modifier, tile: @Composable () -> Unit) {
  Box(modifier) {
    tile()
    if (badge != null) {
      Box(Modifier.align(Alignment.TopEnd).offset(BadgeOffset, -BadgeOffset)) {
        badge()
      }
    }
  }
}

enum class LoadingState {
  PROCESSING,
  ACTIVE,
  INACTIVE,
}

@HedvigPreview
@Composable
private fun PreviewPaymentMethodHandoverIllustration(
  @PreviewParameter(HandoverPreviewStateProvider::class) state: HandoverPreviewState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PaymentMethodHandoverIllustration(
        method = if (state.picked) Unit else null,
        direction = state.direction,
        modifier = Modifier.size(width = 210.dp, height = 74.dp),
      ) {
        Icon(HedvigIcons.Trustly, null, Modifier.size(PaymentMethodMarkSize))
      }
    }
  }
}

private data class HandoverPreviewState(val direction: PaymentHandoverDirection, val picked: Boolean)

private class HandoverPreviewStateProvider :
  CollectionPreviewParameterProvider<HandoverPreviewState>(
    PaymentHandoverDirection.entries.flatMap { direction ->
      listOf(false, true).map { picked -> HandoverPreviewState(direction, picked) }
    },
  )

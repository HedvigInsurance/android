package com.hedvig.android.feature.claim.chat.ui.step.audiorecording

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

/**
 * Write and Record each grow into the card they open, and each card shrinks back into its button on the way out.
 * The Record button's mic travels into the voice card's record control as one object.
 *
 * A card's surface stays opaque while it morphs and only its content fades. A fading layer is drawn offscreen and
 * clipped to its own bounds, which would hide the card's shadow until the fade ends, and a shadow under content
 * that is still fading shows through it. The card is drawn in place, beneath the button it grows from, which fades
 * out on top of it. Its shadow grows with it, and it starts and ends as a pill, the shape of that button.
 */
internal class AnswerCardMorph(
  val writeButton: Modifier,
  val recordButton: Modifier,
  val textCard: Modifier,
  val voiceCard: Modifier,
  val cardContent: Modifier,
  val mic: Modifier,
)

/** The [AnswerCardMorph] for the content [visibility] belongs to, with cards lifted to [cardElevation]. */
@Composable
internal fun SharedTransitionScope.answerCardMorph(
  visibility: AnimatedVisibilityScope,
  cardElevation: Dp,
): AnswerCardMorph {
  val textAnswer = rememberSharedContentState(InputMode.Text)
  val voiceAnswer = rememberSharedContentState(InputMode.Voice)
  val mic = rememberSharedContentState(SHARED_MIC_KEY)
  val elevation by visibility.transition.animateDp { if (it == EnterExitState.Visible) cardElevation else 0.dp }
  val pillFraction by visibility.transition.animateFloat(
    transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
  ) { if (it == EnterExitState.Visible) 0f else 1f }
  val cardSurface = Modifier.shadow(
    elevation = elevation,
    shape = TowardsPillShape(HedvigTheme.shapes.cornerXLarge, pillFraction),
    clip = true,
  )
  return AnswerCardMorph(
    writeButton = buttonBounds(textAnswer, visibility),
    recordButton = buttonBounds(voiceAnswer, visibility),
    textCard = cardBounds(textAnswer, visibility).then(cardSurface),
    voiceCard = cardBounds(voiceAnswer, visibility).then(cardSurface),
    cardContent = with(visibility) {
      Modifier.skipToLookaheadSize().animateEnterExit(enter = fadeThroughIn, exit = fadeThroughOut)
    },
    mic = Modifier.sharedBounds(
      mic,
      animatedVisibilityScope = visibility,
      enter = fadeIn(tween(200)),
      exit = fadeOut(tween(200)),
      zIndexInOverlay = 1f,
    ),
  )
}

/**
 * Only the row of ways to answer fades here; the cards fade with their own shared bounds. Content on its way out
 * stays until the morph has finished, since a button morphing into a card is drawn from the leaving row.
 */
internal fun AnimatedContentTransitionScope<InputMode>.answerInputTransform(): ContentTransform {
  val enter = if (targetState == InputMode.Resting) fadeIn(tween(220, delayMillis = 90)) else EnterTransition.None
  val exit = if (initialState == InputMode.Resting) fadeOut(tween(90)) else ExitTransition.None
  return enter togetherWith (exit + ExitTransition.KeepUntilTransitionsFinished) using SizeTransform(clip = false)
}

private fun SharedTransitionScope.buttonBounds(
  answer: SharedContentState,
  visibility: AnimatedVisibilityScope,
): Modifier = Modifier.sharedBounds(
  answer,
  animatedVisibilityScope = visibility,
  enter = fadeThroughIn,
  exit = fadeThroughOut,
  resizeMode = ResizeMode.RemeasureToBounds,
)

private fun SharedTransitionScope.cardBounds(
  answer: SharedContentState,
  visibility: AnimatedVisibilityScope,
): Modifier = Modifier.sharedBounds(
  answer,
  animatedVisibilityScope = visibility,
  enter = EnterTransition.None,
  exit = ExitTransition.None,
  resizeMode = ResizeMode.RemeasureToBounds,
  renderInOverlayDuringTransition = false,
)

/** [shape] with its corners rounded further towards a pill: [pillFraction] 0 is [shape] itself, 1 is a pill. */
private data class TowardsPillShape(val shape: Shape, val pillFraction: Float) : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val base = Path().apply { addOutline(shape.createOutline(size, layoutDirection, density)) }
    val radius = CornerRadius(pillFraction * size.minDimension / 2)
    val pill = Path().apply { addRoundRect(RoundRect(size.toRect(), radius)) }
    return Outline.Generic(Path.combine(PathOperation.Intersect, base, pill))
  }
}

private const val SHARED_MIC_KEY = "claim_chat_answer_mic"

// What a button and its card show fades through: the leaving content is gone before the arriving content shows, so
// the two are never drawn over each other.
private val fadeThroughOut = fadeOut(tween(100))
private val fadeThroughIn = fadeIn(tween(150, delayMillis = 100))

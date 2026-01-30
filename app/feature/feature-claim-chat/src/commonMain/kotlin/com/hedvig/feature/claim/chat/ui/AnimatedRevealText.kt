package com.hedvig.feature.claim.chat.ui

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
internal fun AnimatedRevealText(
  text: String,
  visibleState: MutableTransitionState<Boolean>,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  onAnimationFinished: () -> Unit = {},
) {
  val charAnimDuration: Int = 150
  var visibleChars by remember { mutableStateOf(0) }
  var speedUpAnimation  by remember { mutableStateOf(false) }
  val charDelay by rememberUpdatedState(calculateCharDelay(text, speedUpAnimation))

  LaunchedEffect(visibleState.targetState, text) {
    if (visibleState.targetState) {
      visibleChars = 0
      text.toCharArray().forEachIndexed { index, char ->
        visibleChars = index + 1
        val specialCharDelayMultiplier = when (char) {
          ',' -> 5
          in listOf('.', '?', '!', '\n', '\t') -> 10
          else -> 1
        }
        delay(charDelay * specialCharDelayMultiplier)
      }
      delay(charAnimDuration.toLong())
      onAnimationFinished()
    } else {
      visibleChars = 0
    }
  }
  val baseColor = style.color.takeOrElse { LocalContentColor.current }
  HedvigText(
    text = buildAnnotatedString {
      text.forEachIndexed { index, char ->
        if (index < visibleChars) {
          val progress by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(charAnimDuration),
            label = "char_$index",
          )
          withStyle(
            style = SpanStyle(color = baseColor.copy(alpha = progress)),
          ) {
            append(char)
          }
        } else {
          withStyle(style = SpanStyle(color = baseColor.copy(alpha = 0f))) {
            append(char)
          }
        }
      }
    },
    style = style,
    modifier = modifier.clickable(
      interactionSource = null,
      indication = null,
      enabled = text.toCharArray().size >= visibleChars,
    ) {
      speedUpAnimation = true
    },
  )
}

/**
 * Speed multiplier decreases for longer text to avoid tedious animations
 * Short text (≤50 chars): full speed (1.0x multiplier)
 * Medium text (50-450 chars): linear interpolation
 * Long text (≥450 chars): 5x faster (0.2x multiplier)
 */
@Composable
private fun calculateCharDelay(text: String, artificiallySpeedUpAnimation: Boolean): Duration {
  return remember(text, artificiallySpeedUpAnimation) {
    val textLength = text.length
    val baseRegularDelayMillis = 20

    val shortTextThreshold = 50
    val longTextThreshold = 350
    val slowMultiplier = 1.0
    val fastMultiplier = 0.2

    // Extreme fallback for extreme cases
    val superLongThreshold = 800
    val fastestMultiplier = 0.001

    val speedMultiplier = when {
      textLength <= shortTextThreshold -> {
        slowMultiplier
      }

      textLength >= superLongThreshold -> {
        fastestMultiplier
      }

      textLength >= longTextThreshold -> {
        fastMultiplier
      }

      else -> {
        val characterRange = longTextThreshold - shortTextThreshold
        val charactersAboveThreshold = textLength - shortTextThreshold
        val interpolationProgress = charactersAboveThreshold.toDouble() / characterRange
        slowMultiplier - interpolationProgress * (slowMultiplier - fastMultiplier)
      }
    }.coerceIn(
      minimumValue = fastestMultiplier,
      maximumValue = if (artificiallySpeedUpAnimation) fastestMultiplier else slowMultiplier,
    )

    (baseRegularDelayMillis * speedMultiplier)
      .coerceAtLeast((baseRegularDelayMillis * fastestMultiplier))
      .milliseconds
  }
}

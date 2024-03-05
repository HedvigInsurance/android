package com.hedvig.android.feature.forever.ui

import androidx.annotation.FloatRange
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun DiscountPieChart(
  totalPrice: Float,
  totalExistingDiscount: Float,
  incentive: Float,
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.typeElement,
  discountColor: Color = MaterialTheme.colorScheme.onTypeContainer,
  incentiveColor: Color = MaterialTheme.colorScheme.typeContainer,
) {
  val transition = rememberInfiniteTransition(label = "transition")
  val targetForeverIncentiveValue = calculateAngleDegrees(totalPrice, incentive)
  val foreverIncentiveSweep by transition.animateFloat(
    initialValue = 0f,
    targetValue = targetForeverIncentiveValue,
    animationSpec = InfiniteRepeatableSpec(
      animation = keyframes {
        durationMillis = 4100
        0f at 0
        0f at 1800 using FastOutSlowInEasing
        targetForeverIncentiveValue at 2300
        targetForeverIncentiveValue at 3600 using FastOutSlowInEasing
        0f at 4100
      },
      repeatMode = RepeatMode.Restart,
    ),
    label = "incentive sweep animation",
  )
  Canvas(
    modifier = modifier.size(215.dp),
    onDraw = {
      drawCircle(color = containerColor)
      drawArc(
        brush = SolidColor(discountColor),
        startAngle = 270f,
        sweepAngle = calculateAngleDegrees(totalPrice, totalExistingDiscount),
        useCenter = true,
      )
      drawArc(
        brush = SolidColor(incentiveColor),
        startAngle = calculateAngleDegrees(totalPrice, totalExistingDiscount) + 270f,
        sweepAngle = foreverIncentiveSweep,
        useCenter = true,
      )
    },
  )
}

@FloatRange(0.0, 360.0)
private fun calculateAngleDegrees(totalPrice: Float, totalDiscount: Float): Float {
  return if (totalPrice == 0f) {
    0f
  } else {
    (totalDiscount / totalPrice) * 360
  }.coerceAtMost(360f)
}

@Composable
@HedvigPreview
fun PreviewDiscountPieChart() {
  HedvigTheme {
    Surface {
      Column {
        DiscountPieChart(
          totalPrice = 290f,
          totalExistingDiscount = 20f,
          incentive = 10f,
        )
      }
    }
  }
}

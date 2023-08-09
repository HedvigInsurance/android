package com.hedvig.android.feature.forever.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun ColumnScope.DiscountPieChart(
  containerColor: Color = MaterialTheme.colorScheme.typeElement,
  discountColor: Color = MaterialTheme.colorScheme.secondaryContainer,
  incentiveColor: Color = MaterialTheme.colorScheme.surface,
  totalPrice: Float,
  totalDiscount: Float,
  incentive: Float,
) {
  val transition = rememberInfiniteTransition(label = "transition")
  val sweep by transition.animateFloat(
    initialValue = 0f,
    targetValue = calculateAngle(totalPrice, incentive),
    animationSpec = InfiniteRepeatableSpec(
      animation = keyframes {
        durationMillis = 5400
        0f at 0
        0f at 1800 with FastOutSlowInEasing
        30f at 2300
        30f at 3600 with FastOutSlowInEasing
        0f at 4100
        0f at 5400
      },
      repeatMode = RepeatMode.Reverse,
    ),
    label = "animation",
  )

  Canvas(
    modifier = Modifier
      .size(215.dp)
      .align(Alignment.CenterHorizontally),
    onDraw = {
      drawCircle(color = containerColor)
      drawArc(
        brush = SolidColor(discountColor),
        startAngle = 270f,
        sweepAngle = calculateAngle(totalPrice, totalDiscount),
        useCenter = true,
      )
      drawArc(
        brush = SolidColor(incentiveColor),
        startAngle = calculateAngle(totalPrice, totalDiscount) + 270f,
        sweepAngle = sweep,
        useCenter = true,
      )
    },
  )
}

private fun calculateAngle(
  totalPrice: Float,
  totalDiscount: Float,
): Float {
  return if (totalPrice == 0f) {
    0f
  } else {
    (totalDiscount / totalPrice) * 360
  }
}

@Composable
@HedvigPreview
fun PreviewDiscountPieChart() {
  HedvigTheme {
    Surface {
      Column {
        DiscountPieChart(
          totalPrice = 290f,
          totalDiscount = 20f,
          incentive = 10f,
        )
      }
    }
  }
}

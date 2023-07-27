package com.hedvig.app.feature.referrals.ui.tab

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.doOnDetach
import com.hedvig.app.R
import com.hedvig.app.databinding.ReferralsHeaderBinding
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.safeLet

fun bindPieChart(
  uiState: ReferralsUiState,
): ReferralsHeaderBinding.() -> Unit = { ->
  (piechart.getTag(R.id.slice_blink_animation) as? ValueAnimator)?.cancel()
  piechartPlaceholder.hideShimmer()

  safeLet(
    uiState.grossPriceAmount,
    uiState.potentialDiscountAmount,
    uiState.currentDiscountAmount,
  ) { gpa, pda, cda ->
    val pdaAsPercentage = (pda.number.doubleValueExact() / gpa.number.doubleValueExact()).toFloat() * 100
    val cdaAsPercentage = (cda.number.doubleValueExact() / gpa.number.doubleValueExact()).toFloat() * 100
    val rest = 100f - (pdaAsPercentage + cdaAsPercentage)

    val potentialDiscountColor = piechart.context.compatColor(R.color.forever_orange_300)
    val restColor = piechart.context.compatColor(R.color.forever_orange_500)

    val segments = listOfNotNull(
      if (cdaAsPercentage != 0f) {
        PieChartSegment(
          CURRENT_DISCOUNT_SLICE,
          cdaAsPercentage,
          piechart.context.colorAttr(com.google.android.material.R.attr.colorSurface),
        )
      } else {
        null
      },
      PieChartSegment(POTENTIAL_DISCOUNT_SLICE, pdaAsPercentage, potentialDiscountColor),
      PieChartSegment(REST_SLICE, rest, restColor),
    )
    piechart.reveal(
      segments,
    ) {
      ValueAnimator.ofFloat(1f, 0f).apply {
        duration = SLICE_BLINK_DURATION
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { va ->
          piechart.segments = piechart.segments.map { segment ->
            if (segment.id == POTENTIAL_DISCOUNT_SLICE) {
              return@map segment.copy(
                color = boundedColorLerp(
                  potentialDiscountColor,
                  restColor,
                  va.animatedFraction,
                ),
              )
            }
            segment
          }
        }
        piechart.setTag(R.id.slice_blink_animation, this)
        piechart.doOnDetach { cancel() }
        start()
      }
    }
  }
}

private const val CURRENT_DISCOUNT_SLICE = 0
private const val POTENTIAL_DISCOUNT_SLICE = 1
private const val REST_SLICE = 2

private const val SLICE_BLINK_DURATION = 800L

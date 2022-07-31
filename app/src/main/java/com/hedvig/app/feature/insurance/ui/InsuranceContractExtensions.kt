package com.hedvig.app.feature.insurance.ui

import android.net.Uri
import androidx.core.view.isVisible
import coil.ImageLoader
import coil.load
import com.hedvig.android.market.MarketManager
import com.hedvig.android.owldroid.graphql.type.TypeOfContractGradientOption
import com.hedvig.app.R
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.util.extensions.compatColor

fun ContractCardViewState.bindTo(
  binding: InsuranceContractCardBinding,
  marketManager: MarketManager,
  imageLoader: ImageLoader,
) =
  binding.apply {
    firstStatusPill.isVisible = firstStatusPillText != null
    firstStatusPill.text = firstStatusPillText
    secondStatusPill.isVisible = secondStatusPillText != null
    secondStatusPill.text = secondStatusPillText

    logo.load(
      logoUrls?.iconByTheme(root.context)?.let { Uri.parse(it) },
      imageLoader,
    ) {
      fallback(hedvig.resources.R.drawable.ic_hedvig_h)
    }

    bindBackgroundColor(gradientOption)

    contractName.text = displayName
    contractPills.adapter = ContractPillAdapter(marketManager).also { adapter ->
      adapter.submitList(detailPills)
    }
    // Prevent this `RecyclerView` from eating clicks in the parent `MaterialCardView`.
    // Alternative implementation path: extend `RecyclerView` and make `onTouchEvent` always return `false`.
    contractPills.suppressLayout(true)
  }

private fun InsuranceContractCardBinding.bindBackgroundColor(
  gradientOption: TypeOfContractGradientOption?,
) {
  when (gradientOption) {
    TypeOfContractGradientOption.GRADIENT_ONE, TypeOfContractGradientOption.UNKNOWN__ -> {
      container.setBackgroundResource(R.drawable.gradient_summer_sky)
      blur.setColorFilter(
        blur.context.compatColor(R.color.blur_summer_sky),
      )
    }
    TypeOfContractGradientOption.GRADIENT_TWO -> {
      container.setBackgroundResource(R.drawable.gradient_fall_sunset)
      blur.setColorFilter(
        blur.context.compatColor(R.color.blur_fall_sunset),
      )
    }
    TypeOfContractGradientOption.GRADIENT_THREE -> {
      container.setBackgroundResource(R.drawable.gradient_spring_fog)
      blur.setColorFilter(
        blur.context.compatColor(R.color.blur_spring_fog),
      )
    }
    TypeOfContractGradientOption.GRADIENT_FOUR -> {
      container.setBackgroundResource(R.drawable.gradient_purple_haze)
      blur.isVisible = false
    }
    TypeOfContractGradientOption.GRADIENT_FIVE -> {
      container.setBackgroundResource(R.drawable.gradient_yellow_peach)
      blur.isVisible = false
    }
    else -> {
      container.setBackgroundColor(container.context.compatColor(R.color.default_insurance_card))
      blur.isVisible = false
    }
  }
}

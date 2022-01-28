package com.hedvig.app.feature.insurance.ui

import androidx.core.view.isVisible
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption
import com.hedvig.app.R
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.compatColor

fun ContractCardViewState.bindTo(
    binding: InsuranceContractCardBinding,
    marketManager: MarketManager
) =
    binding.apply {
        firstStatusPill.isVisible = firstStatusPillText != null
        firstStatusPill.text = firstStatusPillText
        secondStatusPill.isVisible = secondStatusPillText != null
        secondStatusPill.text = secondStatusPillText

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
    gradientOption: TypeOfContractGradientOption?
) {
    when (gradientOption) {
        TypeOfContractGradientOption.GRADIENT_ONE, TypeOfContractGradientOption.UNKNOWN__ -> {
            container.setBackgroundResource(R.drawable.gradient_summer_sky)
            blur.setColorFilter(
                blur.context.compatColor(R.color.blur_summer_sky)
            )
        }
        TypeOfContractGradientOption.GRADIENT_TWO -> {
            container.setBackgroundResource(R.drawable.gradient_fall_sunset)
            blur.setColorFilter(
                blur.context.compatColor(R.color.blur_fall_sunset)
            )
        }
        TypeOfContractGradientOption.GRADIENT_THREE -> {
            container.setBackgroundResource(R.drawable.gradient_spring_fog)
            blur.setColorFilter(
                blur.context.compatColor(R.color.blur_spring_fog)
            )
        }
        else -> {
            container.setBackgroundColor(container.context.compatColor(R.color.default_insurance_card))
            blur.isVisible = false
        }
    }
}

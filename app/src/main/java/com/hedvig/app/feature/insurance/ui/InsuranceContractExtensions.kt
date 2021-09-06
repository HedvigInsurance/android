package com.hedvig.app.feature.insurance.ui

import androidx.core.view.isVisible
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.TypeOfContractGradientOption
import com.hedvig.app.R
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.remove

fun InsuranceQuery.Contract.bindTo(
    binding: InsuranceContractCardBinding,
    marketManager: MarketManager
) =
    binding.apply {
        val firstStatus = statusPills.getOrNull(0)
        firstStatusPill.isVisible = firstStatus != null
        firstStatusPill.text = firstStatus
        val secondStatus = statusPills.getOrNull(1)
        secondStatusPill.isVisible = secondStatus != null
        secondStatusPill.text = secondStatus

        status.fragments.contractStatusFragment.let { contractStatus ->
            contractStatus.asPendingStatus?.let {
                bindBackgroundColor(gradientOption)
            }
            contractStatus.asActiveInFutureStatus?.let {
                removeBackgroundColor()
            }
            contractStatus.asActiveInFutureAndTerminatedInFutureStatus?.let {
                removeBackgroundColor()
            }
            contractStatus.asTerminatedInFutureStatus?.let {
                bindBackgroundColor(gradientOption)
            }
            contractStatus.asTerminatedTodayStatus?.let {
                bindBackgroundColor(gradientOption)
            }
            contractStatus.asTerminatedStatus?.let {
                removeBackgroundColor()
            }
            contractStatus.asActiveStatus?.let {
                bindBackgroundColor(gradientOption)
            }
        }

        contractName.text = displayName
        contractPills.adapter = ContractPillAdapter(marketManager).also { adapter ->
            adapter.submitList(detailPills)
        }
        // Prevent this `RecyclerView` from eating clicks in the parent `MaterialCardView`.
        // Alternative implementation path: extend `RecyclerView` and make `onTouchEvent` always return `false`.
        contractPills.suppressLayout(true)
    }

private fun InsuranceContractCardBinding.removeBackgroundColor() {
    container.setBackgroundColor(container.context.getColor(R.color.hedvig_light_gray))
    blur.remove()
}

private fun InsuranceContractCardBinding.bindBackgroundColor(
    gradientOption: TypeOfContractGradientOption
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
    }
}

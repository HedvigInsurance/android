package com.hedvig.app.feature.insurance.ui

import android.net.Uri
import androidx.core.view.isVisible
import coil.ImageLoader
import com.hedvig.android.core.ui.insurance.toDrawable
import com.hedvig.android.market.MarketManager
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.ui.coil.load

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

    container.background = gradientType.toDrawable(root.context)

    contractName.text = displayName
    contractPills.adapter = ContractPillAdapter(marketManager).also { adapter ->
      adapter.submitList(detailPills)
    }
    // Prevent this `RecyclerView` from eating clicks in the parent `MaterialCardView`.
    // Alternative implementation path: extend `RecyclerView` and make `onTouchEvent` always return `false`.
    contractPills.suppressLayout(true)
  }

package com.hedvig.android.market

import androidx.annotation.StringRes
import com.hedvig.android.market.Market.SE

// TODO: MarketCleanup
val Market.label: Int
  @StringRes
  get() = when (this) {
    SE -> hedvig.resources.R.string.market_sweden
  }

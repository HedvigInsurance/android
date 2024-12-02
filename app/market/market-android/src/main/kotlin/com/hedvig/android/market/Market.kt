package com.hedvig.android.market

import androidx.annotation.StringRes
import com.hedvig.android.market.Market.DK
import com.hedvig.android.market.Market.NO
import com.hedvig.android.market.Market.SE

val Market.label: Int
  @StringRes
  get() = when (this) {
    SE -> hedvig.resources.R.string.market_sweden
    NO -> hedvig.resources.R.string.market_norway
    DK -> hedvig.resources.R.string.market_denmark
  }

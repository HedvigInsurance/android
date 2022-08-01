package com.hedvig.app.feature.zignsec

import android.os.Parcelable
import com.hedvig.android.market.Market
import kotlinx.parcelize.Parcelize

@Parcelize
data class SimpleSignAuthenticationData(
  val market: Market,
) : Parcelable

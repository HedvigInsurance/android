package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.apollo.graphql.type.QuoteBundleAppConfigurationPostSignStep

enum class PostSignScreen {
  CONNECT_PAYIN,
  MOVE,
  CROSS_SELL;

  companion object {
    fun from(postSignStep: QuoteBundleAppConfigurationPostSignStep) = when (postSignStep) {
      QuoteBundleAppConfigurationPostSignStep.CONNECT_PAYIN -> CONNECT_PAYIN
      QuoteBundleAppConfigurationPostSignStep.MOVE -> MOVE
      QuoteBundleAppConfigurationPostSignStep.CROSS_SELL -> CROSS_SELL
      QuoteBundleAppConfigurationPostSignStep.UNKNOWN__ -> CONNECT_PAYIN
    }
  }
}

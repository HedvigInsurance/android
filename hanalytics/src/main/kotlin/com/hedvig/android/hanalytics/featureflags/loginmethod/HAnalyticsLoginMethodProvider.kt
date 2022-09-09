package com.hedvig.android.hanalytics.featureflags.loginmethod

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.LoginMethod

internal class HAnalyticsLoginMethodProvider(
  private val hAnalytics: HAnalytics,
) : LoginMethodProvider {
  override suspend fun getLoginMethod(): LoginMethod {
    return hAnalytics.loginMethod()
  }
}

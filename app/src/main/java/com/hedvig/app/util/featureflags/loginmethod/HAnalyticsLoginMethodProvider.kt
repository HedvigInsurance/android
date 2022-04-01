package com.hedvig.app.util.featureflags.loginmethod

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.LoginMethod

class HAnalyticsLoginMethodProvider(
    private val hAnalytics: HAnalytics,
) : LoginMethodProvider {
    override suspend fun getLoginMethod(): LoginMethod {
        return hAnalytics.loginMethod()
    }
}

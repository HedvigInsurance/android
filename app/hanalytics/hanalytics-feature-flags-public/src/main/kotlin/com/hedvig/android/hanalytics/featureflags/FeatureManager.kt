package com.hedvig.android.hanalytics.featureflags

import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.loginmethod.LoginMethodProvider
import com.hedvig.android.hanalytics.featureflags.paymenttype.PaymentTypeProvider

interface FeatureManager : FeatureFlagProvider, LoginMethodProvider, PaymentTypeProvider {
  suspend fun invalidateExperiments()
}

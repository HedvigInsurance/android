package com.hedvig.app.util.featureflags

import com.hedvig.app.util.featureflags.flags.FeatureFlagProvider
import com.hedvig.app.util.featureflags.loginmethod.LoginMethodProvider
import com.hedvig.app.util.featureflags.paymenttype.PaymentTypeProvider

class FeatureManager(
    private val featureFlagProvider: FeatureFlagProvider,
    private val loginMethodProvider: LoginMethodProvider,
    private val paymentTypeProvider: PaymentTypeProvider,
) : FeatureFlagProvider by featureFlagProvider,
    LoginMethodProvider by loginMethodProvider,
    PaymentTypeProvider by paymentTypeProvider

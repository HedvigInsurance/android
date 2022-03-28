package com.hedvig.app.util.featureflags

import com.hedvig.app.util.featureflags.flags.FeatureFlagProvider
import com.hedvig.app.util.featureflags.loginmethod.LoginMethodProvider
import com.hedvig.app.util.featureflags.paymenttype.PaymentTypeProvider

interface FeatureManager : FeatureFlagProvider, LoginMethodProvider, PaymentTypeProvider

class FeatureManagerImpl(
    private val featureFlagProvider: FeatureFlagProvider,
    private val loginMethodProvider: LoginMethodProvider,
    private val paymentTypeProvider: PaymentTypeProvider,
) : FeatureManager,
    FeatureFlagProvider by featureFlagProvider,
    LoginMethodProvider by loginMethodProvider,
    PaymentTypeProvider by paymentTypeProvider

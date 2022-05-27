package com.hedvig.app.util

import com.hedvig.app.featureManagerModule
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature
import com.hedvig.hanalytics.PaymentType
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class FeatureFlagRule(
    vararg flags: Pair<Feature, Boolean>,
    private val paymentType: PaymentType? = PaymentType.TRUSTLY,
) : ExternalResource() {
    @Suppress("RemoveExplicitTypeArguments")
    private val mockFeatureManagerModule = module {
        single<FeatureManager> {
            val mock = mockk<FeatureManager>()
            flags.forEach { coEvery { mock.isFeatureEnabled(it.first) } returns it.second }
            paymentType?.let { coEvery { mock.getPaymentType() } returns it }
            mock
        }
    }

    override fun before() {
        unloadKoinModules(featureManagerModule)
        loadKoinModules(mockFeatureManagerModule)
    }

    override fun after() {
        unloadKoinModules(mockFeatureManagerModule)
        loadKoinModules(featureManagerModule)
    }
}

package com.hedvig.app.util

import com.hedvig.app.featureRuntimeBehaviorModule
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import io.mockk.every
import io.mockk.mockk
import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class FeatureFlagRule(
    vararg flags: Pair<Feature, Boolean>
) : ExternalResource() {
    private val mockModule = module {
        single {
            val mock = mockk<FeatureManager>()
            flags.forEach { every { mock.isFeatureEnabled(it.first) } returns it.second }
            mock
        }
    }

    override fun before() {
        unloadKoinModules(featureRuntimeBehaviorModule)
        loadKoinModules(mockModule)
    }

    override fun after() {
        unloadKoinModules(mockModule)
        loadKoinModules(featureRuntimeBehaviorModule)
    }
}

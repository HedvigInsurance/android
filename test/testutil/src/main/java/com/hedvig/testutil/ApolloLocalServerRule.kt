package com.hedvig.testutil

import org.junit.rules.ExternalResource
import org.koin.core.context.loadKoinModules

class ApolloLocalServerRule: ExternalResource() {

    override fun before() {
        loadKoinModules(listOf(apolloTestModule))
    }

}

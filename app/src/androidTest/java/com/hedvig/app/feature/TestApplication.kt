package com.hedvig.app.feature

import androidx.test.espresso.IdlingRegistry
import com.apollographql.apollo.test.espresso.ApolloIdlingResource
import com.hedvig.app.HedvigApplication

class TestApplication : HedvigApplication() {
    override val graphqlUrl = "http://localhost:8080/"

    override fun onCreate() {
        super.onCreate()

        val idlingResource =
            ApolloIdlingResource.create("ApolloIdlingResource", apolloClientWrapper.apolloClient)
        IdlingRegistry
            .getInstance()
            .register(idlingResource)
    }
}

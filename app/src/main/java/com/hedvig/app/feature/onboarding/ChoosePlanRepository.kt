package com.hedvig.app.feature.onboarding

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.util.LocaleManager

class ChoosePlanRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend fun bundles(): Response<ChoosePlanQuery.Data> {
        val locale = localeManager.defaultLocale().rawValue
        val choosePlanQuery = ChoosePlanQuery(locale)
        return apolloClient.query(choosePlanQuery).await()
    }
}

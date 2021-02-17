package com.hedvig.onboarding.chooseplan

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.util.apollo.toLocaleString

class ChoosePlanRepository(
    private val apolloClient: ApolloClient,
    defaultLocale: Locale
) {
    private val locale = defaultLocale.toLocaleString()

    suspend fun bundles() =
        apolloClient.query(ChoosePlanQuery(locale)).await()
}

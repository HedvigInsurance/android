package com.hedvig.app.feature.onboarding

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toLocaleString

class ChoosePlanRepository(
    private val apolloClient: ApolloClient,
    context: Context,
) {
    private val locale = defaultLocale(context).toLocaleString()

    suspend fun bundles() =
        apolloClient.query(ChoosePlanQuery(locale)).await()
}

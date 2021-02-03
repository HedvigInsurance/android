package com.hedvig.app.feature.onboarding

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toLocaleString

class ChoosePlanRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    defaultLocale: Locale
) {
    private val locale = defaultLocale.toLocaleString()

    suspend fun bundles() =
        apolloClientWrapper.apolloClient.query(ChoosePlanQuery(locale)).await()
}

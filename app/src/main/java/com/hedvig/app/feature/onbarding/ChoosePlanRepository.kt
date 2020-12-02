package com.hedvig.app.feature.onbarding

import android.content.Context
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toLocaleString

class ChoosePlanRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    context: Context
) {
    private val locale = defaultLocale(context).toLocaleString()

    suspend fun getBundles() =
        apolloClientWrapper.apolloClient.query(ChoosePlanQuery(locale)).await()
}

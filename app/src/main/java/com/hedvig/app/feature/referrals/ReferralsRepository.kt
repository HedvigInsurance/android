package com.hedvig.app.feature.referrals

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper

class ReferralsRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun referralsAsync() = apolloClientWrapper
        .apolloClient
        .query(ReferralsQuery())
        .toDeferred()
}

package com.hedvig.app.feature.referrals

import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred

class ReferralsRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun referralsAsync() = apolloClientWrapper
        .apolloClient
        .query(ReferralsQuery())
        .toDeferred()
}

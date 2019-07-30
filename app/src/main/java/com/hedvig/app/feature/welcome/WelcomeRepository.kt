package com.hedvig.app.feature.welcome

import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.Observable
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper

class WelcomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun fetchWelcomeScreens(): Observable<WelcomeQuery.Data?> {
        val welcomeQuery = WelcomeQuery
            .builder()
            .locale(Locale.SV_SE)
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(welcomeQuery))
            .map { it.data() }
    }
}

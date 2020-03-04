package com.hedvig.app.feature.welcome

import android.content.Context
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import io.reactivex.Observable

class WelcomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun fetchWelcomeScreens(): Observable<WelcomeQuery.Data?> {
        val welcomeQuery = WelcomeQuery(
            locale = defaultLocale(context)
        )

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(welcomeQuery))
            .map { it.data() }
    }
}

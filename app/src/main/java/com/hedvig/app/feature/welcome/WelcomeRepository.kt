package com.hedvig.app.feature.welcome

import android.content.Context
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.Observable
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale

class WelcomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun fetchWelcomeScreens(): Observable<WelcomeQuery.Data?> {
        val welcomeQuery = WelcomeQuery
            .builder()
            .locale(defaultLocale(context))
            .build()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(welcomeQuery))
            .map { it.data() }
    }
}

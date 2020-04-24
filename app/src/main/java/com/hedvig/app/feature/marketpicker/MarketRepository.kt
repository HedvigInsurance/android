package com.hedvig.app.feature.marketpicker

import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.android.owldroid.graphql.PickedLocaleMutation
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred

class MarketRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    fun geoAsync() = apolloClientWrapper
        .apolloClient
        .query(GeoQuery())
        .toDeferred()

    fun updatePickedLocaleAsync(locale: Locale) = apolloClientWrapper
        .apolloClient
        .mutate(PickedLocaleMutation(locale))
        .toDeferred()
}


package com.hedvig.app.feature.marketing.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.util.apollo.defaultLocale

class MarketingRepository(
    private val apolloClient: ApolloClient,
    private val context: Context
) {
    suspend fun marketingBackground() = apolloClient
        .query(MarketingBackgroundQuery(defaultLocale(context).rawValue))
        .await()

    fun hasSelectedMarket() = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getBoolean(MarketingActivity.HAS_SELECTED_MARKET, false)
}

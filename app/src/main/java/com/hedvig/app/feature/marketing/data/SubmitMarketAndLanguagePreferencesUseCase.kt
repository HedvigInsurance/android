package com.hedvig.app.feature.marketing.data

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.UpdateLanguageMutation
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class SubmitMarketAndLanguagePreferencesUseCase(
    private val apolloClient: ApolloClient,
    private val context: Context,
    private val localeManager: LocaleManager,
    private val marketManager: MarketManager,
) {
    suspend operator fun invoke(language: Language, market: Market) = apolloClient
        .mutate(
            UpdateLanguageMutation(
                makeLocaleString(language.apply(context), market),
                localeManager.defaultLocale(),
            )
        )
        .safeQuery()
        .toEither()
        .tap { marketManager.hasSelectedMarket = true }
}

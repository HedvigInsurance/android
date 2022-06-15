package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.MarketingBackgroundQuery
import com.hedvig.android.owldroid.graphql.type.UserInterfaceStyle
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.safeLet

class GetMarketingBackgroundUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend operator fun invoke() = apolloClient
        .query(MarketingBackgroundQuery(localeManager.defaultLocale().rawValue))
        .safeQuery()
        .toEither()
        .map { it.appMarketingImages.first() }
        .map { appMarketingImage ->
            safeLet(
                appMarketingImage.image?.url,
                appMarketingImage.blurhash,
                appMarketingImage.userInterfaceStyle
            ) { url, blurHash, userInterfaceStyle ->
                MarketingBackground(
                    url = url,
                    blurHash = blurHash,
                    theme = when (userInterfaceStyle) {
                        UserInterfaceStyle.Light -> MarketingBackground.Theme.LIGHT
                        UserInterfaceStyle.Dark -> MarketingBackground.Theme.DARK
                        else -> MarketingBackground.Theme.DARK
                    }
                )
            }
        }
}

data class MarketingBackground(
    val url: String,
    val blurHash: String,
    val theme: Theme,
) {
    enum class Theme {
        LIGHT,
        DARK,
    }
}

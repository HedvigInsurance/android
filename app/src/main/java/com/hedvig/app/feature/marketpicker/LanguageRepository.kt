package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.hedvig.android.owldroid.graphql.UpdateLanguageMutation
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.makeLocaleString
import e
import i

class LanguageRepository(
    private val apolloClient: ApolloClient,
    private val marketManager: MarketManager,
    private val context: Context,
    private val defaultLocale: Locale
) {

    fun uploadLanguage(language: Language) {
        language.apply(context).let {
            val acceptLanguage = makeLocaleString(it, marketManager.market)
            uploadLanguage(acceptLanguage, defaultLocale)
        }
    }

    fun uploadLanguage(acceptLanguage: String, locale: Locale) {
        apolloClient
            .mutate(UpdateLanguageMutation(acceptLanguage, locale))
            .enqueue(object : ApolloCall.Callback<UpdateLanguageMutation.Data>() {
                override fun onFailure(e: ApolloException) {
                    e { "$e Failed to update language" }
                }

                override fun onResponse(response: Response<UpdateLanguageMutation.Data>) {
                    i { "Successfully updated language" }
                }
            })
    }
}

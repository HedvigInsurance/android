package com.hedvig.app.feature.marketpicker

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.hedvig.android.owldroid.graphql.UpdateLanguageMutation
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.feature.marketing.ui.MarketingActivity
import com.hedvig.app.feature.settings.SettingsActivity
import e
import i

class LanguageRepository(
    private val apolloClient: ApolloClient,
    private val context: Context
) {
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

    fun persistLanguageAndMarket(language: String?, market: String?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit(commit = true) {
            putString(Market.MARKET_SHARED_PREF, market)
            putString(SettingsActivity.SETTING_LANGUAGE, language)
            putBoolean(MarketingActivity.HAS_SELECTED_MARKET, true)
        }
    }
}

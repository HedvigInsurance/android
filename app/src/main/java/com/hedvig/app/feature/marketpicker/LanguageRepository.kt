package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.hedvig.android.owldroid.graphql.UpdateLanguageMutation
import com.hedvig.app.ApolloClientWrapper
import e
import i

class LanguageRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun setLanguage(acceptLanguage: String) = apolloClientWrapper
        .apolloClient
        .mutate(UpdateLanguageMutation(acceptLanguage))
        .enqueue(object : ApolloCall.Callback<UpdateLanguageMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                e { "$e Failed to update language" }
            }

            override fun onResponse(response: Response<UpdateLanguageMutation.Data>) {
                i { "Successfully updated language" }
            }
        })
}

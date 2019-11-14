package com.hedvig.app.feature.language

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.hedvig.android.owldroid.graphql.UpdateLanguageMutation
import com.hedvig.app.ApolloClientWrapper
import timber.log.Timber

class LanguageRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun setLanguage(acceptLanguage: String) = apolloClientWrapper
        .apolloClient
        .mutate(UpdateLanguageMutation(acceptLanguage))
        .enqueue(object : ApolloCall.Callback<UpdateLanguageMutation.Data>() {
            override fun onFailure(e: ApolloException) {
                Timber.e(e, "Failed to update language")
            }

            override fun onResponse(response: Response<UpdateLanguageMutation.Data>) {
                Timber.i("Successfully updated language")
            }
        })
}

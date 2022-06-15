package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.UpdateLanguageMutation
import com.hedvig.android.owldroid.graphql.type.Locale
import e
import i

class LanguageRepository(
    private val apolloClient: ApolloClient,
) {

    suspend fun uploadLanguage(acceptLanguage: String, locale: Locale) {
        apolloClient
            .mutation(UpdateLanguageMutation(acceptLanguage, locale))
            .execute()
            .also {
                if (it.hasErrors() || it.data == null) {
                    e { "Failed to update language: Errors: ${it.errors}, data: ${it.data}" }
                } else {
                    i { "Successfully updated language" }
                }
            }
    }
}

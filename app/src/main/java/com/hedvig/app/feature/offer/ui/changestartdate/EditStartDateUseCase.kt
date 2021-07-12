package com.hedvig.app.feature.offer.ui.changestartdate

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import com.hedvig.android.owldroid.graphql.RemoveStartDateMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.extensions.replace
import e
import java.lang.IllegalArgumentException
import java.time.LocalDate

class EditStartDateUseCase(
    private val apolloClient: ApolloClient,
    private val offerRepository: OfferRepository
) {
    suspend fun removeStartDate(
        id: String,
        idsInBundle: List<String>
    ): QueryResult<RemoveStartDateMutation.Data> {
        val mutation = RemoveStartDateMutation(id)
        return when (val response = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Success -> {
                removeStartDateFromCache(id, idsInBundle, response.data)
                response
            }
            is QueryResult.Error -> response
        }
    }

    suspend fun setStartDate(
        id: String,
        idsInBundle: List<String>,
        date: LocalDate
    ): QueryResult<ChooseStartDateMutation.Data> {
        val mutation = ChooseStartDateMutation(id, date)
        return when (val response = apolloClient.mutate(mutation).safeQuery()) {
            is QueryResult.Success -> {
                writeStartDateToCache(id, idsInBundle, response.data)
                response
            }
            is QueryResult.Error -> response
        }
    }

    private fun writeStartDateToCache(id: String, ids: List<String>, data: ChooseStartDateMutation.Data) {
        val cachedData = apolloClient
            .apolloStore
            .read(offerRepository.offerQuery(ids))
            .execute()

        val newId = data.editQuote.asCompleteQuote?.id
        if (newId == null) {
            e { "Id is null" }
            return
        }

        val modifiedQuote = cachedData.quoteBundle.quotes.find { it.id == id }?.copy(
            startDate = data.editQuote.asCompleteQuote?.startDate
        ) ?: throw IllegalArgumentException("Could not find quote with id $id")

        val newQuotes = cachedData.quoteBundle.quotes.replace(modifiedQuote) {
            it.id == id
        }

        val newData = cachedData.copy(
            quoteBundle = cachedData.quoteBundle.copy(
                quotes = newQuotes
            )
        )

        apolloClient
            .apolloStore
            .writeAndPublish(offerRepository.offerQuery(ids), newData)
            .execute()
    }

    private fun removeStartDateFromCache(id: String, ids: List<String>, data: RemoveStartDateMutation.Data) {
        val cachedData = apolloClient
            .apolloStore
            .read(offerRepository.offerQuery(ids))
            .execute()

        val newId = data.removeStartDate.asCompleteQuote?.id
        if (newId == null) {
            e { "Id is null" }
            return
        }

        val modifiedQuote = cachedData.quoteBundle.quotes.find { it.id == id }?.copy(
            startDate = data.removeStartDate.asCompleteQuote?.startDate
        ) ?: throw IllegalArgumentException("Could not find quote with id $id")

        val newQuotes = cachedData.quoteBundle.quotes.replace(modifiedQuote) {
            it.id == id
        }

        val newData = cachedData.copy(
            quoteBundle = cachedData.quoteBundle.copy(
                quotes = newQuotes
            )
        )

        apolloClient
            .apolloStore
            .writeAndPublish(offerRepository.offerQuery(ids), newData)
            .execute()
    }
}

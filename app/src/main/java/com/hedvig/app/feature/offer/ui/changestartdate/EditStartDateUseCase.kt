package com.hedvig.app.feature.offer.ui.changestartdate

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RemoveStartDateMutation
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.extensions.replace
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
                if (response.data.removeStartDate.asCompleteQuote?.id != null) {
                    writeStartDateToCache(id, idsInBundle, response.data.removeStartDate.asCompleteQuote?.startDate)
                }
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
                if (response.data.editQuote.asCompleteQuote?.id != null) {
                    writeStartDateToCache(id, idsInBundle, response.data.editQuote.asCompleteQuote?.startDate)
                }
                response
            }
            is QueryResult.Error -> response
        }
    }

    private fun writeStartDateToCache(id: String, ids: List<String>, startDate: LocalDate?) {
        val cachedOffer = getCachedOffer(ids)
        val newData = createNewOfferFromStartDate(cachedOffer, id, startDate)
        publishNewOffer(ids, newData)
    }

    private fun createNewOfferFromStartDate(
        cachedOffer: OfferQuery.Data,
        id: String,
        startDate: LocalDate?
    ): OfferQuery.Data {
        val modifiedInception = createModifiedInception(
            cachedData = cachedOffer,
            id = id,
            startDate = startDate
        )

        return cachedOffer.copy(
            quoteBundle = cachedOffer.quoteBundle.copy(
                inception = modifiedInception
            )
        )
    }

    private fun publishNewOffer(ids: List<String>, newData: OfferQuery.Data) {
        apolloClient.apolloStore
            .writeAndPublish(offerRepository.offerQuery(ids), newData)
            .execute()
    }

    private fun getCachedOffer(ids: List<String>) = apolloClient.apolloStore
        .read(offerRepository.offerQuery(ids))
        .execute()

    private fun createModifiedInception(
        cachedData: OfferQuery.Data,
        id: String,
        startDate: LocalDate?
    ): OfferQuery.Inception1 {
        val modifiedIndependentInception = cachedData.quoteBundle
            .inception
            .asIndependentInceptions
            ?.inceptions
            ?.find { it.correspondingQuote.asCompleteQuote1?.id == id }
            ?.copy(startDate = startDate)

        return with(cachedData.quoteBundle.inception) {
            if (modifiedIndependentInception != null) {
                copy(
                    asIndependentInceptions = asIndependentInceptions?.copy(
                        inceptions = asIndependentInceptions?.inceptions
                            ?.replace(modifiedIndependentInception) { it.correspondingQuote.asCompleteQuote1?.id == id }
                            ?: asIndependentInceptions?.inceptions ?: emptyList()
                    )
                )
            } else {
                copy(
                    asConcurrentInception = asConcurrentInception?.copy(startDate = startDate)
                )
            }
        }
    }
}

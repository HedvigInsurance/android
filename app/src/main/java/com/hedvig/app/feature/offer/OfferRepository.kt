package com.hedvig.app.feature.offer

import android.content.Context
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import com.hedvig.android.owldroid.graphql.OfferClosedMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveStartDateMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.graphql.SignStatusQuery
import com.hedvig.android.owldroid.graphql.SignStatusSubscription
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.extensions.toDeferred
import com.hedvig.app.util.extensions.toFlow
import e
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

class OfferRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    private lateinit var offerQuery: OfferQuery

    fun loadOffer(): Flow<Response<OfferQuery.Data>> {
        offerQuery = OfferQuery(defaultLocale(context))

        return apolloClientWrapper
            .apolloClient
            .query(offerQuery)
            .watcher()
            .toFlow()
    }

    fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(offerQuery)
            .execute()

        if (cachedData.lastQuoteOfMember.asCompleteQuote == null)
            return

        val newCost = cachedData.lastQuoteOfMember.asCompleteQuote.insuranceCost.copy(
            fragments = OfferQuery.InsuranceCost.Fragments(costFragment = data.redeemCode.cost.fragments.costFragment)
        )

        val newData = cachedData
            .copy(
                lastQuoteOfMember = cachedData.lastQuoteOfMember.copy(
                    asCompleteQuote = cachedData.lastQuoteOfMember.asCompleteQuote.copy(
                        insuranceCost = newCost
                    )
                ),
                redeemedCampaigns = listOf(
                    OfferQuery.RedeemedCampaign(
                        fragments = OfferQuery.RedeemedCampaign.Fragments(
                            incentiveFragment = data.redeemCode.campaigns[0].fragments.incentiveFragment
                        )
                    )
                )
            )

        apolloClientWrapper
            .apolloClient
            .apolloStore()
            .writeAndPublish(offerQuery, newData)
            .execute()
    }

    suspend fun removeDiscount() = apolloClientWrapper
        .apolloClient
        .mutate(RemoveDiscountCodeMutation())
        .toDeferred()
        .await()

    fun removeDiscountFromCache() {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore()
            .read(offerQuery)
            .execute()

        if (cachedData.lastQuoteOfMember.asCompleteQuote == null)
            return

        val oldCostFragment =
            cachedData.lastQuoteOfMember.asCompleteQuote.insuranceCost.fragments.costFragment
        val newCostFragment = oldCostFragment
            .copy(
                monthlyDiscount = oldCostFragment
                    .monthlyDiscount
                    .copy(amount = "0.00"),
                monthlyNet = oldCostFragment
                    .monthlyNet
                    .copy(amount = oldCostFragment.monthlyGross.amount)
            )

        val newData = cachedData
            .copy(
                lastQuoteOfMember = cachedData.lastQuoteOfMember.copy(
                    asCompleteQuote = cachedData.lastQuoteOfMember.asCompleteQuote.copy(
                        insuranceCost = cachedData.lastQuoteOfMember.asCompleteQuote.insuranceCost.copy(
                            fragments = OfferQuery.InsuranceCost.Fragments(costFragment = newCostFragment)
                        )
                    )
                ),
                redeemedCampaigns = emptyList()
            )

        apolloClientWrapper
            .apolloClient
            .apolloStore()
            .writeAndPublish(offerQuery, newData)
            .execute()
    }

    suspend fun triggerOpenChatFromOffer() =
        apolloClientWrapper.apolloClient.mutate(OfferClosedMutation()).toDeferred().await()

    suspend fun startSign() =
        apolloClientWrapper.apolloClient.mutate(SignOfferMutation()).toDeferred().await()

    fun subscribeSignStatus() =
        apolloClientWrapper
            .apolloClient
            .subscribe(SignStatusSubscription())
            .toFlow()

    suspend fun fetchSignStatus() =
        apolloClientWrapper.apolloClient.query(SignStatusQuery())
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .toDeferred()
            .await()

    suspend fun chooseStartDate(id: String, date: LocalDate) =
        apolloClientWrapper.apolloClient.mutate(
            ChooseStartDateMutation(
                id,
                date
            )
        ).toDeferred().await()

    fun writeStartDateToCache(data: ChooseStartDateMutation.Data) {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore()
            .read(offerQuery)
            .execute()

        val newDate = data.editQuote.asCompleteQuote?.startDate
        val newId = data.editQuote.asCompleteQuote?.id
        if (newId == null) {
            e { "Id is null" }
            return
        }

        cachedData.lastQuoteOfMember.asCompleteQuote?.let { completeQuote ->
            val newData = cachedData
                .copy(
                    lastQuoteOfMember = OfferQuery.LastQuoteOfMember(
                        asCompleteQuote = completeQuote
                            .copy(
                                id = newId,
                                startDate = newDate
                            )
                    )
                )

            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(offerQuery, newData)
                .execute()
        }
    }

    suspend fun removeStartDate(id: String) =
        apolloClientWrapper
            .apolloClient
            .mutate(RemoveStartDateMutation(id))
            .toDeferred()
            .await()

    fun removeStartDateFromCache(data: RemoveStartDateMutation.Data) {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore()
            .read(offerQuery)
            .execute()

        val newDate = data.removeStartDate.asCompleteQuote?.startDate
        val newId = data.removeStartDate.asCompleteQuote?.id
        if (newId == null) {
            e { "Id is null" }
            return
        }

        cachedData.lastQuoteOfMember.asCompleteQuote?.let { completeQuote ->
            val newData = cachedData
                .copy(
                    lastQuoteOfMember = OfferQuery.LastQuoteOfMember(
                        asCompleteQuote = completeQuote
                            .copy(
                                id = newId,
                                startDate = newDate
                            )
                    )
                )

            apolloClientWrapper
                .apolloClient
                .apolloStore()
                .writeAndPublish(offerQuery, newData)
                .execute()
        }
    }
}

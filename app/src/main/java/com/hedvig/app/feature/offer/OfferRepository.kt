package com.hedvig.app.feature.offer

import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import com.hedvig.android.owldroid.graphql.OfferClosedMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveStartDateMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.graphql.SignStatusQuery
import com.hedvig.android.owldroid.graphql.SignStatusSubscription
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper
import e
import java.time.LocalDate

class OfferRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    defaultLocale: Locale
) {
    private val offerQuery = OfferQuery(defaultLocale)

    fun offer() = apolloClientWrapper
        .apolloClient
        .query(offerQuery)
        .watcher()
        .toFlow()

    fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore
            .read(offerQuery)
            .execute()

        if (cachedData.lastQuoteOfMember.asCompleteQuote == null)
            return

        val newCost = cachedData.lastQuoteOfMember.asCompleteQuote!!.insuranceCost.copy(
            fragments = OfferQuery.InsuranceCost.Fragments(costFragment = data.redeemCode.cost.fragments.costFragment)
        )

        val newData = cachedData
            .copy(
                lastQuoteOfMember = cachedData.lastQuoteOfMember.copy(
                    asCompleteQuote = cachedData.lastQuoteOfMember.asCompleteQuote!!.copy(
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
            .apolloStore
            .writeAndPublish(offerQuery, newData)
            .execute()
    }

    suspend fun removeDiscount() = apolloClientWrapper
        .apolloClient
        .mutate(RemoveDiscountCodeMutation())
        .await()

    fun removeDiscountFromCache() {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
            .read(offerQuery)
            .execute()

        if (cachedData.lastQuoteOfMember.asCompleteQuote == null)
            return

        val oldCostFragment =
            cachedData.lastQuoteOfMember.asCompleteQuote!!.insuranceCost.fragments.costFragment
        val newCostFragment = oldCostFragment
            .copy(
                monthlyDiscount = oldCostFragment
                    .monthlyDiscount
                    .copy(
                        fragments = CostFragment.MonthlyDiscount.Fragments(
                            oldCostFragment.monthlyDiscount.fragments.monetaryAmountFragment.copy(
                                amount = "0.00"
                            )
                        )
                    ),
                monthlyNet = oldCostFragment
                    .monthlyNet
                    .copy(
                        fragments = CostFragment.MonthlyNet.Fragments(
                            oldCostFragment.monthlyNet.fragments.monetaryAmountFragment.copy(
                                amount = oldCostFragment.monthlyGross.fragments.monetaryAmountFragment.amount
                            )
                        )
                    )
            )

        val newData = cachedData
            .copy(
                lastQuoteOfMember = cachedData.lastQuoteOfMember.copy(
                    asCompleteQuote = cachedData.lastQuoteOfMember.asCompleteQuote!!.copy(
                        insuranceCost = cachedData.lastQuoteOfMember.asCompleteQuote!!.insuranceCost.copy(
                            fragments = OfferQuery.InsuranceCost.Fragments(costFragment = newCostFragment)
                        )
                    )
                ),
                redeemedCampaigns = emptyList()
            )

        apolloClientWrapper
            .apolloClient
            .apolloStore
            .writeAndPublish(offerQuery, newData)
            .execute()
    }

    suspend fun triggerOpenChatFromOffer() =
        apolloClientWrapper.apolloClient.mutate(OfferClosedMutation()).await()

    suspend fun startSign() =
        apolloClientWrapper.apolloClient.mutate(SignOfferMutation()).await()

    fun subscribeSignStatus() =
        apolloClientWrapper
            .apolloClient
            .subscribe(SignStatusSubscription())
            .toFlow()

    suspend fun fetchSignStatus() =
        apolloClientWrapper.apolloClient.query(SignStatusQuery())
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .build()
            .await()

    suspend fun chooseStartDate(id: String, date: LocalDate) =
        apolloClientWrapper.apolloClient.mutate(
            ChooseStartDateMutation(
                id,
                date
            )
        ).await()

    fun writeStartDateToCache(data: ChooseStartDateMutation.Data) {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
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
                .apolloStore
                .writeAndPublish(offerQuery, newData)
                .execute()
        }
    }

    suspend fun removeStartDate(id: String) =
        apolloClientWrapper
            .apolloClient
            .mutate(RemoveStartDateMutation(id))
            .await()

    fun removeStartDateFromCache(data: RemoveStartDateMutation.Data) {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore
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
                .apolloStore
                .writeAndPublish(offerQuery, newData)
                .execute()
        }
    }
}

package com.hedvig.app.feature.offer

import android.content.Context
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import com.hedvig.android.owldroid.graphql.ContractStatusQuery
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
import e
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate
import timber.log.Timber

class OfferRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    private lateinit var offerQuery: OfferQuery

    fun loadOffer(): Observable<Response<OfferQuery.Data>> {
        offerQuery = OfferQuery(defaultLocale(context))

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(offerQuery).watcher())
    }

    fun loadContracts(): Flow<Response<ContractStatusQuery.Data>> = apolloClientWrapper
        .apolloClient
        .query(ContractStatusQuery())
        .watcher()
        .toFlow()

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

    fun removeDiscount() = Rx2Apollo.from(
        apolloClientWrapper.apolloClient.mutate(RemoveDiscountCodeMutation())
    )

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

    fun triggerOpenChatFromOffer() =
        Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(OfferClosedMutation()))

    fun startSign() = Rx2Apollo
        .from(
            apolloClientWrapper
                .apolloClient
                .mutate(SignOfferMutation())
        )

    fun subscribeSignStatus() = Rx2Apollo
        .from(
            apolloClientWrapper
                .apolloClient
                .subscribe(SignStatusSubscription())
        )

    fun fetchSignStatus() = Rx2Apollo
        .from(
            apolloClientWrapper.apolloClient.query(SignStatusQuery()).httpCachePolicy(
                HttpCachePolicy.NETWORK_ONLY
            )
        )

    fun chooseStartDate(id: String, date: LocalDate) = Rx2Apollo
        .from(apolloClientWrapper.apolloClient.mutate(ChooseStartDateMutation(id, date)))

    fun writeStartDateToCache(data: ChooseStartDateMutation.Data) {
        val cachedData = apolloClientWrapper
            .apolloClient
            .apolloStore()
            .read(offerQuery)
            .execute()

        val newDate = data.editQuote.asCompleteQuote?.startDate
        val newId = data.editQuote.asCompleteQuote?.id
        if (newId == null) {
            Timber.e("Id is null")
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

    fun removeStartDate(id: String) = Rx2Apollo
        .from(apolloClientWrapper.apolloClient.mutate(RemoveStartDateMutation(id)))

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

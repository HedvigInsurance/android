package com.hedvig.app.feature.offer

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.OfferClosedMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.graphql.SignStatusQuery
import com.hedvig.android.owldroid.graphql.SignStatusSubscription
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable

class OfferRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    private lateinit var offerQuery: OfferQuery

    fun loadOffer(): Observable<Response<OfferQuery.Data>> {
        offerQuery = OfferQuery()

        return Rx2Apollo
            .from(apolloClientWrapper.apolloClient.query(offerQuery).watcher())
    }

    fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClientWrapper.apolloClient
            .apolloStore()
            .read(offerQuery)
            .execute()

        val newCost = cachedData.insurance.cost?.copy(
            fragments = OfferQuery.Cost.Fragments(
                costFragment = data.redeemCode.cost.fragments.costFragment
            )
        )

        val newData = cachedData.copy(
            insurance = cachedData.insurance.copy(cost = newCost),
            redeemedCampaigns = listOf(
                OfferQuery.RedeemedCampaign(
                    __typename = "RedeemedCampaign",
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

        val oldCostFragment = cachedData.insurance.cost?.fragments?.costFragment ?: return
        val newCostFragment = oldCostFragment.copy(
            monthlyDiscount = oldCostFragment.monthlyDiscount.copy(amount = "0.00"),
            monthlyNet = oldCostFragment.monthlyNet.copy(amount = oldCostFragment.monthlyGross.amount)
        )

        val newData = cachedData.copy(
            insurance = cachedData.insurance.copy(
                cost = cachedData.insurance.cost.copy(
                    fragments = OfferQuery.Cost.Fragments(costFragment = newCostFragment)
                )
            ),
            redeemedCampaigns = listOf()
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
}

package com.hedvig.app.feature.offer

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.OfferClosedMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
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

        val newCost = cachedData.insurance.cost?.toBuilder()
            ?.fragments(OfferQuery.Cost.Fragments.builder().costFragment(data.redeemCode.cost.fragments.costFragment).build())
            ?.build()

        val newData = cachedData
            .toBuilder()
            .insurance(cachedData.insurance.toBuilder().cost(newCost).build())
            .redeemedCampaigns(
                listOf(
                    OfferQuery.RedeemedCampaign
                        .builder()
                        .fragments(
                            OfferQuery.RedeemedCampaign.Fragments.builder().incentiveFragment(
                                data.redeemCode.campaigns[0].fragments.incentiveFragment
                            ).build()
                        )
                        .__typename("RedeemedCampaign")
                        .build()
                )
            )
            .build()

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
        val newCostFragment = oldCostFragment
            .toBuilder()
            .monthlyDiscount(oldCostFragment.monthlyDiscount.toBuilder().amount("0.00").build())
            .monthlyNet(oldCostFragment.monthlyNet.toBuilder().amount(oldCostFragment.monthlyGross.amount).build())
            .build()

        val newData = cachedData
            .toBuilder()
            .insurance(
                cachedData.insurance.toBuilder().cost(
                    cachedData.insurance.cost?.toBuilder()?.fragments(
                        OfferQuery.Cost.Fragments.builder().costFragment(
                            newCostFragment
                        ).build()
                    )?.build()
                ).build()
            )
            .redeemedCampaigns(listOf())
            .build()

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
}

package com.hedvig.app.feature.offer

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.graphql.LastQuoteIdQuery
import com.hedvig.android.owldroid.graphql.OfferClosedMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.graphql.SignStatusQuery
import com.hedvig.android.owldroid.graphql.SignStatusSubscription
import com.hedvig.app.util.LocaleManager
import kotlinx.coroutines.flow.map

class OfferRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    fun offerQuery(ids: List<String>) = OfferQuery(localeManager.defaultLocale(), ids)

    fun offer(ids: List<String>) = apolloClient
        .query(offerQuery(ids))
        .watcher()
        .toFlow()
        .map(::toDataOrError)

    sealed class OfferResult {
        object HasContracts : OfferResult()

        data class Error(val message: String? = null) : OfferResult()
        data class Success(
            val data: OfferQuery.Data
        ) : OfferResult()
    }

    private fun toDataOrError(response: Response<OfferQuery.Data>): OfferResult {
        response.errors?.let {
            return OfferResult.Error(it.firstOrNull()?.message)
        }
        val data = response.data ?: return OfferResult.Error()
        return OfferResult.Success(data)
    }

    fun writeDiscountToCache(ids: List<String>, data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClient
            .apolloStore
            .read(offerQuery(ids))
            .execute()

        val newCost = cachedData.quoteBundle.bundleCost.copy(
            fragments = OfferQuery.BundleCost.Fragments(costFragment = data.redeemCode.cost.fragments.costFragment)
        )

        val newData = cachedData
            .copy(
                quoteBundle = cachedData.quoteBundle.copy(
                    bundleCost = newCost
                ),
                redeemedCampaigns = listOf(
                    OfferQuery.RedeemedCampaign(
                        fragments = OfferQuery.RedeemedCampaign.Fragments(
                            incentiveFragment = data.redeemCode.campaigns[0].fragments.incentiveFragment
                        )
                    )
                )
            )

        apolloClient
            .apolloStore
            .writeAndPublish(offerQuery(ids), newData)
            .execute()
    }

    suspend fun removeDiscount() = apolloClient
        .mutate(RemoveDiscountCodeMutation())
        .await()

    fun removeDiscountFromCache(ids: List<String>) {
        val cachedData = apolloClient
            .apolloStore
            .read(offerQuery(ids))
            .execute()

        val oldCostFragment =
            cachedData.quoteBundle.bundleCost.fragments.costFragment
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
                quoteBundle = cachedData.quoteBundle.copy(
                    bundleCost = OfferQuery.BundleCost(
                        fragments = OfferQuery.BundleCost.Fragments(newCostFragment)
                    )
                ),
                redeemedCampaigns = emptyList()
            )

        apolloClient
            .apolloStore
            .writeAndPublish(offerQuery(ids), newData)
            .execute()
    }

    suspend fun triggerOpenChatFromOffer() = apolloClient
        .mutate(OfferClosedMutation())
        .await()

    suspend fun startSign() = apolloClient
        .mutate(SignOfferMutation())
        .await()

    fun subscribeSignStatus() = apolloClient
        .subscribe(SignStatusSubscription())
        .toFlow()

    suspend fun fetchSignStatus() = apolloClient
        .query(SignStatusQuery())
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .build()
        .await()

    fun quoteIdOfLastQuoteOfMember(): ApolloCall<LastQuoteIdQuery.Data> = apolloClient
        .query(LastQuoteIdQuery())

    fun refreshOfferQuery(ids: List<String>) = apolloClient
        .query(offerQuery(ids))
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
}

package com.hedvig.app.feature.offer

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.computations.ensureNotNull
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.QuoteCartQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.toOfferModel
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

class OfferRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
    private val featureManager: FeatureManager,
) {

    private val offerFlow = MutableSharedFlow<Either<ErrorMessage, OfferModel>>(1)

    fun offerQuery(ids: List<String>) = OfferQuery(localeManager.defaultLocale(), ids)

    suspend fun getQuoteIds(quoteCartId: QuoteCartId): Either<ErrorMessage, List<String>> = queryQuoteCart(quoteCartId)
        .map { it.quoteBundle.quotes }
        .map { quotes -> quotes.map { it.id } }

    fun offerFlow(ids: List<String>): Flow<Either<ErrorMessage, OfferModel>> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            offerFlow.asSharedFlow()
        } else {
            apolloClient
                .query(offerQuery(ids))
                .watcher()
                .toFlow()
                .map { it.toResult() }
        }
    }

    private fun Response<OfferQuery.Data>.toResult(): Either<ErrorMessage, OfferModel> = when {
        errors != null -> ErrorMessage(errors!!.firstOrNull()?.message).left()
        data == null -> ErrorMessage().left()
        else -> data!!.toOfferModel().right()
    }

    suspend fun queryAndEmitOffer(quoteCartId: QuoteCartId?, quoteIds: List<String>) {
        val offer = if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            queryQuoteCart(quoteCartId)
        } else {
            queryOffer(quoteIds)
        }
        offerFlow.tryEmit(offer)
    }

    private suspend fun queryQuoteCart(quoteCartId: QuoteCartId?): Either<ErrorMessage, OfferModel> = either {
        ensureNotNull(quoteCartId) { ErrorMessage("No quote cart id found") }

        val result = apolloClient
            .query(QuoteCartQuery(localeManager.defaultLocale(), quoteCartId.id))
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .safeQuery()
            .toEither { ErrorMessage(it) }
            .bind()

        ensureNotNull(result.quoteCart.fragments.quoteCartFragment.bundle) {
            ErrorMessage("No quotes in offer, please try again")
        }

        result.quoteCart.fragments.quoteCartFragment.toOfferModel()
    }

    private suspend fun queryOffer(ids: List<String>): Either<ErrorMessage, OfferModel> = either {
        ensure(ids.isNotEmpty()) { ErrorMessage("No quote ids found") }

        apolloClient
            .query(offerQuery(ids))
            .toBuilder()
            .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
            .build()
            .safeQuery()
            .toEither { ErrorMessage(it) }
            .bind()
            .toOfferModel()
    }

    fun writeDiscountToCache(ids: List<String>, data: RedeemReferralCodeMutation.Data) {
        val cachedData = apolloClient
            .apolloStore
            .read(offerQuery(ids))
            .execute()

        val newCost = cachedData.quoteBundle.fragments.copy(
            quoteBundleFragment = cachedData.quoteBundle.fragments.quoteBundleFragment.copy(
                bundleCost = cachedData.quoteBundle.fragments.quoteBundleFragment.bundleCost.copy(
                    fragments = QuoteBundleFragment.BundleCost.Fragments(
                        costFragment = data.redeemCode.cost.fragments.costFragment
                    )
                )
            )
        )

        val newData = cachedData
            .copy(
                quoteBundle = cachedData.quoteBundle.copy(
                    fragments = newCost
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

        val oldCostFragment = cachedData.quoteBundle.fragments.quoteBundleFragment.bundleCost.fragments.costFragment
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
                    fragments = cachedData.quoteBundle.fragments.copy(
                        quoteBundleFragment = cachedData.quoteBundle.fragments.quoteBundleFragment.copy(
                            bundleCost = QuoteBundleFragment.BundleCost(
                                fragments = QuoteBundleFragment.BundleCost.Fragments(newCostFragment)
                            )
                        )
                    ),
                ),
                redeemedCampaigns = emptyList()
            )

        apolloClient
            .apolloStore
            .writeAndPublish(offerQuery(ids), newData)
            .execute()
    }
}

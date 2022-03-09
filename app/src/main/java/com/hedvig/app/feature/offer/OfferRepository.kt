package com.hedvig.app.feature.offer

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.QuoteCartQuery
import com.hedvig.android.owldroid.graphql.QuoteCartSubscription
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.RemoveDiscountCodeMutation
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.toOfferModel
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.safeSubscription
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class OfferRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
    private val featureManager: FeatureManager,
) {
    fun offerQuery(ids: List<String>) = OfferQuery(localeManager.defaultLocale(), ids)

    fun offer(ids: NonEmptyList<String>): Flow<Either<ErrorMessage, OfferModel>> {
        return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            val subscription = QuoteCartSubscription(localeManager.defaultLocale(), ids.first())
            apolloClient.subscribe(subscription)
                .safeSubscription()
                .map { it.toResult() }
                .onStart {
                    val query = QuoteCartQuery(localeManager.defaultLocale(), ids.first())
                    val result = apolloClient
                        .query(query)
                        .safeQuery()
                        .toEither { ErrorMessage(it) }
                        .map { it.quoteCart.fragments.quoteCartFragment.toOfferModel() }
                    emit(result)
                }
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

    private fun QueryResult<QuoteCartSubscription.Data>.toResult(): Either<ErrorMessage, OfferModel> = when (this) {
        is QueryResult.Error -> ErrorMessage(message).left()
        is QueryResult.Success -> data.quoteCart?.fragments?.quoteCartFragment?.toOfferModel()?.right()
            ?: ErrorMessage().left()
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

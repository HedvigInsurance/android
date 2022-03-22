package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ExpressionResult
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature

suspend fun EmbarkStoryQuery.Passage.getOfferKeysOrNull(
    valueStore: ValueStore,
    featureManager: FeatureManager
): List<String>? {
    return if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
        getQuoteCartRedirectKeysOrNull(valueStore)
    } else {
        getOfferRedirectKeysOrNull(valueStore)
    }
}

private fun EmbarkStoryQuery.Passage.getOfferRedirectKeysOrNull(valueStore: ValueStore): List<String>? {
    return offerRedirect?.data
        ?.keys
        ?.takeIf { it.isNotEmpty() }
        ?: variantedOfferRedirects.firstOrNull {
            evaluateExpression(
                it.data.expression.fragments.expressionFragment,
                valueStore
            ) is ExpressionResult.True
            // TODO Pass `allKeys` to OfferActivity in order to show possible variations (up-sell) for offer
        }?.data?.selectedKeys
}

private fun EmbarkStoryQuery.Passage.getQuoteCartRedirectKeysOrNull(valueStore: ValueStore): List<String> {
    return quoteCartOfferRedirects
        .takeIf { it.isNotEmpty() }
        ?.firstOrNull {
            evaluateExpression(
                it.data.expression.fragments.expressionFragment,
                valueStore
            ) is ExpressionResult.True
        }?.data?.id?.let {
            listOf(it)
        } ?: emptyList()
}

package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ExpressionResult
import com.hedvig.app.feature.embark.ValueStore

fun EmbarkStoryQuery.Passage.getOfferKeysOrNull(valueStore: ValueStore): List<String>? {
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

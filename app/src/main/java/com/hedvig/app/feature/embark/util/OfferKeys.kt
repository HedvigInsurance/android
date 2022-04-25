package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ExpressionResult
import com.hedvig.app.feature.embark.ValueStore

fun EmbarkStoryQuery.Passage.getOfferKeyOrNull(valueStore: ValueStore): String? {
    return quoteCartOfferRedirects
        .takeIf { it.isNotEmpty() }
        ?.firstOrNull {
            evaluateExpression(
                it.data.expression.fragments.expressionFragment,
                valueStore
            ) is ExpressionResult.True
        }?.data?.id
}

fun EmbarkStoryQuery.Passage.getSelectedContractTypes(valueStore: ValueStore): List<SelectedContractType> {
    return quoteCartOfferRedirects
        .takeIf { it.isNotEmpty() }
        ?.firstOrNull {
            evaluateExpression(
                it.data.expression.fragments.expressionFragment,
                valueStore
            ) is ExpressionResult.True
        }
        ?.data
        ?.selectedInsuranceTypes
        ?.map { SelectedContractType(it) } ?: emptyList()
}

@JvmInline
value class SelectedContractType(val id: String)

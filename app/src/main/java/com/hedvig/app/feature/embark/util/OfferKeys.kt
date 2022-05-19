package com.hedvig.app.feature.embark.util

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ExpressionResult
import com.hedvig.app.feature.embark.ValueStore
import kotlinx.parcelize.Parcelize

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
@Parcelize
value class SelectedContractType(val id: String) : Parcelable

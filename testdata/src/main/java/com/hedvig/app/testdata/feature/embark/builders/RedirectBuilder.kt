package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.ExpressionFragment

data class RedirectBuilder(
    private val to: String,
    private val expression: ExpressionFragment,
    private val passedExpressionKey: String? = null,
    private val passedExpressionValue: String? = null,
) {
    fun build() = EmbarkStoryQuery.Redirect(
        __typename = "",
        asEmbarkRedirectUnaryExpression = expression.fragments.basicExpressionFragment.asEmbarkExpressionUnary?.let {
            EmbarkStoryQuery.AsEmbarkRedirectUnaryExpression(
                __typename = "",
                unaryType = it.unaryType,
                to = to,
                passedExpressionKey = passedExpressionKey,
                passedExpressionValue = passedExpressionValue
            )
        },
        asEmbarkRedirectBinaryExpression = expression.fragments.basicExpressionFragment.asEmbarkExpressionBinary?.let {
            EmbarkStoryQuery.AsEmbarkRedirectBinaryExpression(
                __typename = "",
                binaryType = it.binaryType,
                to = to,
                key = it.key,
                value = it.value,
                passedExpressionKey = passedExpressionKey,
                passedExpressionValue = passedExpressionValue
            )
        },
        asEmbarkRedirectMultipleExpressions = expression.asEmbarkExpressionMultiple?.let {
            EmbarkStoryQuery.AsEmbarkRedirectMultipleExpressions(
                __typename = "",
                multipleExpressionType = it.multipleType,
                to = to,
                passedExpressionKey = passedExpressionKey,
                passedExpressionValue = passedExpressionValue,
                subExpressions = it.subExpressions.map { se ->
                    EmbarkStoryQuery.SubExpression(
                        __typename = "",
                        fragments = EmbarkStoryQuery.SubExpression.Fragments(
                            ExpressionFragment(
                                __typename = "",
                                fragments = ExpressionFragment.Fragments(se.fragments.basicExpressionFragment),
                                asEmbarkExpressionMultiple = null
                            )
                        )
                    )
                }
            )
        }
    )
}

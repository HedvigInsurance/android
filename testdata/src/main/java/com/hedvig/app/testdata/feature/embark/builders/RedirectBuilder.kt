package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class RedirectBuilder(
    private val to: String,
    private val expression: ExpressionFragment,
    private val passedExpressionKey: String? = null,
    private val passedExpressionValue: String? = null,
) {
    fun build() = EmbarkStoryQuery.Redirect(
        asEmbarkRedirectUnaryExpression = expression.fragments.basicExpressionFragment.asEmbarkExpressionUnary?.let {
            EmbarkStoryQuery.AsEmbarkRedirectUnaryExpression(
                unaryType = it.unaryType,
                to = to,
                passedExpressionKey = passedExpressionKey,
                passedExpressionValue = passedExpressionValue
            )
        },
        asEmbarkRedirectBinaryExpression = expression.fragments.basicExpressionFragment.asEmbarkExpressionBinary?.let {
            EmbarkStoryQuery.AsEmbarkRedirectBinaryExpression(
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
                multipleExpressionType = it.multipleType,
                to = to,
                passedExpressionKey = passedExpressionKey,
                passedExpressionValue = passedExpressionValue,
                subExpressions = it.subExpressions.map { se ->
                    EmbarkStoryQuery.SubExpression(
                        fragments = EmbarkStoryQuery.SubExpression.Fragments(
                            ExpressionFragment(
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

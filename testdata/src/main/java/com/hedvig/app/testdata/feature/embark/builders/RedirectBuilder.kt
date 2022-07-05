package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.BasicExpressionFragment
import com.hedvig.android.owldroid.graphql.fragment.ExpressionFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkRedirectBinaryExpression
import com.hedvig.android.owldroid.graphql.type.EmbarkRedirectMultipleExpressions
import com.hedvig.android.owldroid.graphql.type.EmbarkRedirectUnaryExpression

data class RedirectBuilder(
    private val to: String,
    private val expression: ExpressionFragment,
    private val passedExpressionKey: String? = null,
    private val passedExpressionValue: String? = null,
) {
    private sealed interface EmbarkRedirectExpressionType {
        data class Unary(val data: BasicExpressionFragment.AsEmbarkExpressionUnary) : EmbarkRedirectExpressionType
        data class Binary(val data: BasicExpressionFragment.AsEmbarkExpressionBinary) : EmbarkRedirectExpressionType
        data class Multiple(val data: ExpressionFragment.AsEmbarkExpressionMultiple) : EmbarkRedirectExpressionType

        val unary: BasicExpressionFragment.AsEmbarkExpressionUnary?
            get() = (this as? Unary)?.data

        val binary: BasicExpressionFragment.AsEmbarkExpressionBinary?
            get() = (this as? Binary)?.data

        val multiple: ExpressionFragment.AsEmbarkExpressionMultiple?
            get() = (this as? Multiple)?.data

        val typename: String
            get() = when (this) {
                is Unary -> EmbarkRedirectUnaryExpression.type.name
                is Binary -> EmbarkRedirectBinaryExpression.type.name
                is Multiple -> EmbarkRedirectMultipleExpressions.type.name
            }

        companion object {
            fun fromExpressionFragment(expression: ExpressionFragment): EmbarkRedirectExpressionType {
                expression.fragments.basicExpressionFragment.asEmbarkExpressionUnary?.let { return Unary(it) }
                expression.fragments.basicExpressionFragment.asEmbarkExpressionBinary?.let { return Binary(it) }
                expression.asEmbarkExpressionMultiple?.let { return Multiple(it) }
                error("expression $expression can not build an EmbarkStoryQuery.Redirect")
            }
        }
    }

    fun build(): EmbarkStoryQuery.Redirect {
        val embarkRedirectExpressionType = EmbarkRedirectExpressionType.fromExpressionFragment(expression)
        return EmbarkStoryQuery.Redirect(
            __typename = embarkRedirectExpressionType.typename,
            asEmbarkRedirectUnaryExpression = embarkRedirectExpressionType.unary?.let {
                EmbarkStoryQuery.AsEmbarkRedirectUnaryExpression(
                    __typename = embarkRedirectExpressionType.typename,
                    unaryType = it.unaryType,
                    to = to,
                    passedExpressionKey = passedExpressionKey,
                    passedExpressionValue = passedExpressionValue,
                )
            },
            asEmbarkRedirectBinaryExpression = embarkRedirectExpressionType.binary?.let {
                EmbarkStoryQuery.AsEmbarkRedirectBinaryExpression(
                    __typename = embarkRedirectExpressionType.typename,
                    binaryType = it.binaryType,
                    to = to,
                    key = it.key,
                    value = it.value,
                    passedExpressionKey = passedExpressionKey,
                    passedExpressionValue = passedExpressionValue,
                )
            },
            asEmbarkRedirectMultipleExpressions = embarkRedirectExpressionType.multiple?.let {
                EmbarkStoryQuery.AsEmbarkRedirectMultipleExpressions(
                    __typename = embarkRedirectExpressionType.typename,
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
                                    asEmbarkExpressionMultiple = null,
                                ),
                            ),
                        )
                    },
                )
            },
        )
    }
}

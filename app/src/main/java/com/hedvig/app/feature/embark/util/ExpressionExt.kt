package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.BasicExpressionFragment
import com.hedvig.android.owldroid.graphql.fragment.ExpressionFragment

fun EmbarkStoryQuery.Redirect.toExpressionFragment() = ExpressionFragment(
    __typename = "",
    fragments = ExpressionFragment.Fragments(
        BasicExpressionFragment(
            __typename = "",
            asEmbarkExpressionUnary = asEmbarkRedirectUnaryExpression?.let {
                BasicExpressionFragment.AsEmbarkExpressionUnary(
                    __typename = "",
                    unaryType = it.unaryType,
                    text = null,
                )
            },
            asEmbarkExpressionBinary = asEmbarkRedirectBinaryExpression?.let {
                BasicExpressionFragment.AsEmbarkExpressionBinary(
                    __typename = "",
                    binaryType = it.binaryType,
                    key = it.key,
                    value = it.value,
                    text = null,
                )
            },
        ),
    ),
    asEmbarkExpressionMultiple = asEmbarkRedirectMultipleExpressions?.let {
        ExpressionFragment.AsEmbarkExpressionMultiple(
            __typename = "",
            multipleType = it.multipleExpressionType,
            text = null,
            subExpressions = it.subExpressions.map { se ->
                ExpressionFragment.SubExpression2(
                    __typename = "",
                    fragments = ExpressionFragment.SubExpression2.Fragments(
                        se.fragments.expressionFragment.fragments.basicExpressionFragment,
                    ),
                    asEmbarkExpressionMultiple1 = se
                        .fragments.expressionFragment.asEmbarkExpressionMultiple?.let { asMulti ->
                            ExpressionFragment.AsEmbarkExpressionMultiple1(
                                __typename = "",
                                multipleType = asMulti.multipleType,
                                text = asMulti.text,
                                subExpressions = asMulti.subExpressions.map { se2 ->
                                    ExpressionFragment.SubExpression1(
                                        __typename = "",
                                        fragments = ExpressionFragment.SubExpression1.Fragments(
                                            se2.fragments.basicExpressionFragment,
                                        ),
                                        asEmbarkExpressionMultiple2 = se2
                                            .asEmbarkExpressionMultiple1?.let { asMulti2 ->
                                                ExpressionFragment.AsEmbarkExpressionMultiple2(
                                                    __typename = "",
                                                    multipleType = asMulti2.multipleType,
                                                    text = asMulti2.text,
                                                    subExpressions = asMulti2.subExpressions.map { se3 ->
                                                        ExpressionFragment.SubExpression(
                                                            __typename = "",
                                                            fragments = ExpressionFragment
                                                                .SubExpression
                                                                .Fragments(
                                                                    se3.fragments.basicExpressionFragment,
                                                                ),
                                                        )
                                                    },
                                                )
                                            },
                                    )
                                },
                            )
                        },
                )
            },
        )
    },
)

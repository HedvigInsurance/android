package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.fragment.BasicExpressionFragment
import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

fun EmbarkStoryQuery.Redirect.toExpressionFragment() = ExpressionFragment(
    fragments = ExpressionFragment.Fragments(
        BasicExpressionFragment(
            asEmbarkExpressionUnary = asEmbarkRedirectUnaryExpression?.let {
                BasicExpressionFragment.AsEmbarkExpressionUnary(
                    unaryType = it.unaryType,
                    text = null
                )
            },
            asEmbarkExpressionBinary = asEmbarkRedirectBinaryExpression?.let {
                BasicExpressionFragment.AsEmbarkExpressionBinary(
                    binaryType = it.binaryType,
                    key = it.key,
                    value = it.value,
                    text = null
                )
            },
        )
    ),
    asEmbarkExpressionMultiple = asEmbarkRedirectMultipleExpressions?.let {
        ExpressionFragment.AsEmbarkExpressionMultiple(
            multipleType = it.multipleExpressionType,
            text = null,
            subExpressions = it.subExpressions.map { se ->
                ExpressionFragment.SubExpression2(
                    fragments = ExpressionFragment.SubExpression2.Fragments(
                        se.fragments.expressionFragment.fragments.basicExpressionFragment
                    ),
                    asEmbarkExpressionMultiple1 = se
                        .fragments.expressionFragment.asEmbarkExpressionMultiple?.let { asMulti ->
                            ExpressionFragment.AsEmbarkExpressionMultiple1(
                                multipleType = asMulti.multipleType,
                                text = asMulti.text,
                                subExpressions = asMulti.subExpressions.map { se2 ->
                                    ExpressionFragment.SubExpression1(
                                        fragments = ExpressionFragment.SubExpression1.Fragments(
                                            se2.fragments.basicExpressionFragment
                                        ),
                                        asEmbarkExpressionMultiple2 = se2
                                            .asEmbarkExpressionMultiple1?.let { asMulti2 ->
                                                ExpressionFragment.AsEmbarkExpressionMultiple2(
                                                    multipleType = asMulti2.multipleType,
                                                    text = asMulti2.text,
                                                    subExpressions = asMulti2.subExpressions.map { se3 ->
                                                        ExpressionFragment.SubExpression(
                                                            fragments = ExpressionFragment
                                                                .SubExpression
                                                                .Fragments(
                                                                    se3.fragments.basicExpressionFragment
                                                                )
                                                        )
                                                    }
                                                )
                                            }
                                    )
                                }
                            )
                        }
                )
            }
        )
    }
)

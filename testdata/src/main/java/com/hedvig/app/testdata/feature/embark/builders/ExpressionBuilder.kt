package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.BasicExpressionFragment
import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary

data class ExpressionBuilder(
    private val type: ExpressionType,
    private val text: String = "test message",
    private val key: String = "",
    private val value: String = "",
    private val subExpressions: List<ExpressionFragment> = emptyList(),
) {
    fun build() = ExpressionFragment(
        fragments = ExpressionFragment.Fragments(
            BasicExpressionFragment(
                asEmbarkExpressionUnary = if (type == ExpressionType.ALWAYS || type == ExpressionType.NEVER) {
                    BasicExpressionFragment.AsEmbarkExpressionUnary(
                        unaryType = when (type) {
                            ExpressionType.ALWAYS -> EmbarkExpressionTypeUnary.ALWAYS
                            ExpressionType.NEVER -> EmbarkExpressionTypeUnary.NEVER
                            else -> throw Error("Unreachable")
                        },
                        text = text
                    )
                } else {
                    null
                },
                asEmbarkExpressionBinary = if (
                    type == ExpressionType.EQUALS ||
                    type == ExpressionType.NOT_EQUALS ||
                    type == ExpressionType.GREATER_THAN ||
                    type == ExpressionType.GREATER_THAN_OR_EQUALS ||
                    type == ExpressionType.LESS_THAN ||
                    type == ExpressionType.LESS_THAN_OR_EQUALS ||
                    type == ExpressionType.NOT_EQUALS
                ) {
                    BasicExpressionFragment.AsEmbarkExpressionBinary(
                        binaryType = when (type) {
                            ExpressionType.EQUALS -> EmbarkExpressionTypeBinary.EQUALS
                            ExpressionType.NOT_EQUALS -> EmbarkExpressionTypeBinary.NOT_EQUALS
                            ExpressionType.GREATER_THAN -> EmbarkExpressionTypeBinary.MORE_THAN
                            ExpressionType.GREATER_THAN_OR_EQUALS -> EmbarkExpressionTypeBinary.MORE_THAN_OR_EQUALS
                            ExpressionType.LESS_THAN -> EmbarkExpressionTypeBinary.LESS_THAN
                            ExpressionType.LESS_THAN_OR_EQUALS -> EmbarkExpressionTypeBinary.LESS_THAN_OR_EQUALS
                            else -> throw Error("Unreachable")
                        },
                        key = key,
                        value = value,
                        text = text
                    )
                } else {
                    null
                },
            )
        ),
        asEmbarkExpressionMultiple = if (type == ExpressionType.AND || type == ExpressionType.OR) {
            ExpressionFragment.AsEmbarkExpressionMultiple(
                multipleType = when (type) {
                    ExpressionType.AND -> EmbarkExpressionTypeMultiple.AND
                    ExpressionType.OR -> EmbarkExpressionTypeMultiple.OR
                    else -> throw Error("Unreachable")
                },
                text = text,
                subExpressions = subExpressions.map { subEx ->
                    ExpressionFragment.SubExpression2(
                        fragments = ExpressionFragment.SubExpression2.Fragments(
                            subEx.fragments.basicExpressionFragment
                        ),
                        asEmbarkExpressionMultiple1 = subEx.asEmbarkExpressionMultiple?.let { asMulti ->
                            ExpressionFragment.AsEmbarkExpressionMultiple1(
                                multipleType = asMulti.multipleType,
                                text = asMulti.text,
                                subExpressions = asMulti.subExpressions.map { subEx2 ->
                                    ExpressionFragment.SubExpression1(
                                        fragments = ExpressionFragment.SubExpression1.Fragments(
                                            subEx2.fragments.basicExpressionFragment
                                        ),
                                        asEmbarkExpressionMultiple2 = subEx2
                                            .asEmbarkExpressionMultiple1
                                            ?.let { asMulti2 ->
                                                ExpressionFragment.AsEmbarkExpressionMultiple2(
                                                    multipleType = asMulti2.multipleType,
                                                    text = asMulti2.text,
                                                    subExpressions = asMulti2.subExpressions.map { subEx3 ->
                                                        ExpressionFragment.SubExpression(
                                                            fragments = ExpressionFragment.SubExpression.Fragments(
                                                                subEx3.fragments.basicExpressionFragment
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
                },
            )
        } else {
            null
        }
    )

    enum class ExpressionType {
        ALWAYS,
        NEVER,
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN,
        LESS_THAN_OR_EQUALS,
        AND,
        OR
    }
}

package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.fragment.BasicExpressionFragment
import com.hedvig.android.owldroid.graphql.fragment.ExpressionFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkExpressionBinary
import com.hedvig.android.owldroid.graphql.type.EmbarkExpressionMultiple
import com.hedvig.android.owldroid.graphql.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.graphql.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.graphql.type.EmbarkExpressionTypeUnary
import com.hedvig.android.owldroid.graphql.type.EmbarkExpressionUnary

data class ExpressionBuilder(
    private val type: ExpressionType,
    private val text: String = "test message",
    private val key: String = "",
    private val value: String = "",
    private val subExpressions: List<ExpressionFragment> = emptyList(),
) {
    fun build(): ExpressionFragment {
        return ExpressionFragment(
            __typename = type.typename,
            fragments = ExpressionFragment.Fragments(
                BasicExpressionFragment(
                    __typename = type.typename,
                    asEmbarkExpressionUnary = if (type.isUnary) {
                        BasicExpressionFragment.AsEmbarkExpressionUnary(
                            __typename = type.typename,
                            unaryType = when (type) {
                                ExpressionType.ALWAYS -> EmbarkExpressionTypeUnary.ALWAYS
                                ExpressionType.NEVER -> EmbarkExpressionTypeUnary.NEVER
                                else -> throw Error("Unreachable")
                            },
                            text = text,
                        )
                    } else {
                        null
                    },
                    asEmbarkExpressionBinary = if (type.isBinary) {
                        BasicExpressionFragment.AsEmbarkExpressionBinary(
                            __typename = type.typename,
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
                            text = text,
                        )
                    } else {
                        null
                    },
                ),
            ),
            asEmbarkExpressionMultiple = if (type.isMultiple) {
                ExpressionFragment.AsEmbarkExpressionMultiple(
                    __typename = type.typename,
                    multipleType = when (type) {
                        ExpressionType.AND -> EmbarkExpressionTypeMultiple.AND
                        ExpressionType.OR -> EmbarkExpressionTypeMultiple.OR
                        else -> throw Error("Unreachable")
                    },
                    text = text,
                    subExpressions = subExpressions.map { subEx ->
                        ExpressionFragment.SubExpression2(
                            __typename = type.typename,
                            fragments = ExpressionFragment.SubExpression2.Fragments(
                                subEx.fragments.basicExpressionFragment,
                            ),
                            asEmbarkExpressionMultiple1 = subEx.asEmbarkExpressionMultiple?.let { asMulti ->
                                ExpressionFragment.AsEmbarkExpressionMultiple1(
                                    __typename = type.typename,
                                    multipleType = asMulti.multipleType,
                                    text = asMulti.text,
                                    subExpressions = asMulti.subExpressions.map { subEx2 ->
                                        ExpressionFragment.SubExpression1(
                                            __typename = type.typename,
                                            fragments = ExpressionFragment.SubExpression1.Fragments(
                                                subEx2.fragments.basicExpressionFragment,
                                            ),
                                            asEmbarkExpressionMultiple2 = subEx2
                                                .asEmbarkExpressionMultiple1
                                                ?.let { asMulti2 ->
                                                    ExpressionFragment.AsEmbarkExpressionMultiple2(
                                                        __typename = type.typename,
                                                        multipleType = asMulti2.multipleType,
                                                        text = asMulti2.text,
                                                        subExpressions = asMulti2.subExpressions.map { subEx3 ->
                                                            ExpressionFragment.SubExpression(
                                                                __typename = "",
                                                                fragments = ExpressionFragment.SubExpression.Fragments(
                                                                    subEx3.fragments.basicExpressionFragment,
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
            } else {
                null
            },
        )
    }

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
        OR,
        ;

        val isUnary: Boolean
            get() = this == ALWAYS || this == NEVER

        val isBinary: Boolean
            get() = this == EQUALS ||
                this == NOT_EQUALS ||
                this == GREATER_THAN ||
                this == GREATER_THAN_OR_EQUALS ||
                this == LESS_THAN ||
                this == LESS_THAN_OR_EQUALS

        val isMultiple: Boolean
            get() = this == AND || this == OR

        val typename: String
            get() = when {
                isUnary -> EmbarkExpressionUnary.type.name
                isBinary -> EmbarkExpressionBinary.type.name
                isMultiple -> EmbarkExpressionMultiple.type.name
                else -> error("type $this must be mapped to some __typename")
            }
    }
}

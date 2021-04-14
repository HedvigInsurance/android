package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.fragment.SubExpressionFragment
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary

data class ExpressionBuilder(
    private val type: ExpressionType,
    private val text: String = "test message",
    private val key: String = "",
    private val value: String = "",
    private val subExpressions: List<SubExpressionFragment> = emptyList()
) {
    fun build() = MessageFragment.Expression(
        asEmbarkExpressionUnary = if (type == ExpressionType.ALWAYS || type == ExpressionType.NEVER) {
            MessageFragment.AsEmbarkExpressionUnary(
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
        asEmbarkExpressionBinary = if (type == ExpressionType.EQUALS || type == ExpressionType.NOT_EQUALS || type == ExpressionType.GREATER_THAN || type == ExpressionType.GREATER_THAN_OR_EQUALS || type == ExpressionType.LESS_THAN || type == ExpressionType.LESS_THAN_OR_EQUALS || type == ExpressionType.NOT_EQUALS) {
            MessageFragment.AsEmbarkExpressionBinary(
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
        asEmbarkExpressionMultiple = if (type == ExpressionType.AND || type == ExpressionType.OR) {
            MessageFragment.AsEmbarkExpressionMultiple(
                multipleType = when (type) {
                    ExpressionType.AND -> EmbarkExpressionTypeMultiple.AND
                    ExpressionType.OR -> EmbarkExpressionTypeMultiple.OR
                    else -> throw Error("Unreachable")
                },
                text = text,
                subExpressions = subExpressions.map {
                    MessageFragment.SubExpression(
                        fragments = MessageFragment.SubExpression.Fragments(
                            it
                        )
                    )
                }
            )
        } else {
            null
        }
    )

    fun buildSubExpression() = build().intoSubExpression()

    fun buildSubSubExpression() = build().intoSubExpression().intoSubSubExpression()

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

fun MessageFragment.Expression.intoSubExpression() = SubExpressionFragment(
    asEmbarkExpressionUnary = this.asEmbarkExpressionUnary?.let {
        SubExpressionFragment.AsEmbarkExpressionUnary(
            unaryType = it.unaryType,
            text = it.text
        )
    },
    asEmbarkExpressionBinary = this.asEmbarkExpressionBinary?.let {
        SubExpressionFragment.AsEmbarkExpressionBinary(
            binaryType = it.binaryType,
            key = it.key,
            value = it.value,
            text = it.text
        )
    },
    asEmbarkExpressionMultiple = this.asEmbarkExpressionMultiple?.let {
        SubExpressionFragment.AsEmbarkExpressionMultiple(
            multipleType = it.multipleType,
            text = it.text,
            subExpressions = it.subExpressions.map { se -> se.fragments.subExpressionFragment.intoSubSubExpression() }
        )
    }
)

fun SubExpressionFragment.intoSubSubExpression() = SubExpressionFragment.SubExpression(
    asEmbarkExpressionUnary1 = this.asEmbarkExpressionUnary?.let {
        SubExpressionFragment.AsEmbarkExpressionUnary1(
            unaryType = it.unaryType,
            text = it.text
        )
    },
    asEmbarkExpressionBinary1 = this.asEmbarkExpressionBinary?.let {
        SubExpressionFragment.AsEmbarkExpressionBinary1(
            binaryType = it.binaryType,
            key = it.key,
            value = it.value,
            text = it.text
        )
    }
)

package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import com.hedvig.app.feature.embark.ExpressionResult
import com.hedvig.app.feature.embark.ValueStore

fun evaluateExpression(expression: ExpressionFragment, valueStore: ValueStore): ExpressionResult {
    expression.fragments.basicExpressionFragment.asEmbarkExpressionUnary?.let { unaryExpression ->
        return when (unaryExpression.unaryType) {
            EmbarkExpressionTypeUnary.ALWAYS -> ExpressionResult.True(unaryExpression.text)
            EmbarkExpressionTypeUnary.NEVER -> ExpressionResult.False
            else -> ExpressionResult.False
        }
    }
    expression.fragments.basicExpressionFragment.asEmbarkExpressionBinary?.let { binaryExpression ->
        when (binaryExpression.binaryType) {
            EmbarkExpressionTypeBinary.EQUALS -> {
                if (binaryExpression.value == "null") {
                    return if (
                        (valueStore.get(binaryExpression.key) == null || valueStore.get(binaryExpression.key) == "") &&
                        valueStore.getList(binaryExpression.key) == null
                    ) {
                        ExpressionResult.True(binaryExpression.text)
                    } else {
                        ExpressionResult.False
                    }
                }
                if (valueStore.get(binaryExpression.key) == binaryExpression.value) {
                    return ExpressionResult.True(binaryExpression.text)
                }
            }
            EmbarkExpressionTypeBinary.NOT_EQUALS -> {
                if (binaryExpression.value == "null") {
                    return if (
                        valueStore.get(binaryExpression.key) == null &&
                        valueStore.getList(binaryExpression.key) == null
                    ) {
                        ExpressionResult.False
                    } else {
                        ExpressionResult.True(binaryExpression.text)
                    }
                }
                val stored = valueStore.get(binaryExpression.key)
                    ?: return ExpressionResult.False
                if (stored != binaryExpression.value) {
                    return ExpressionResult.True(binaryExpression.text)
                }
            }
            EmbarkExpressionTypeBinary.MORE_THAN,
            EmbarkExpressionTypeBinary.MORE_THAN_OR_EQUALS,
            EmbarkExpressionTypeBinary.LESS_THAN,
            EmbarkExpressionTypeBinary.LESS_THAN_OR_EQUALS,
            -> {
                val storedAsInt = valueStore.get(binaryExpression.key)?.toIntOrNull()
                    ?: return ExpressionResult.False
                val valueAsInt =
                    binaryExpression.value.toIntOrNull() ?: return ExpressionResult.False

                val evaluatesToTrue = when (binaryExpression.binaryType) {
                    EmbarkExpressionTypeBinary.MORE_THAN -> storedAsInt > valueAsInt
                    EmbarkExpressionTypeBinary.MORE_THAN_OR_EQUALS -> storedAsInt >= valueAsInt
                    EmbarkExpressionTypeBinary.LESS_THAN -> storedAsInt < valueAsInt
                    EmbarkExpressionTypeBinary.LESS_THAN_OR_EQUALS -> storedAsInt <= valueAsInt
                    else -> false
                }

                if (evaluatesToTrue) {
                    return ExpressionResult.True(binaryExpression.text)
                }
            }
            else -> {
            }
        }
        return ExpressionResult.False
    }
    expression.asEmbarkExpressionMultiple?.let { multipleExpression ->
        val results =
            multipleExpression.subExpressions.map {
                evaluateExpression(
                    ExpressionFragment(
                        fragments = ExpressionFragment.Fragments(it.fragments.basicExpressionFragment),
                        asEmbarkExpressionMultiple = it.asEmbarkExpressionMultiple1?.let { asMulti ->
                            ExpressionFragment.AsEmbarkExpressionMultiple(
                                multipleType = asMulti.multipleType,
                                text = asMulti.text,
                                subExpressions = asMulti.subExpressions.map { se ->
                                    ExpressionFragment.SubExpression2(
                                        fragments = ExpressionFragment.SubExpression2.Fragments(
                                            se.fragments.basicExpressionFragment
                                        ),
                                        asEmbarkExpressionMultiple1 = se
                                            .asEmbarkExpressionMultiple2?.let { asMulti2 ->
                                                ExpressionFragment.AsEmbarkExpressionMultiple1(
                                                    multipleType = asMulti2.multipleType,
                                                    text = asMulti2.text,
                                                    subExpressions = asMulti2.subExpressions.map { se2 ->
                                                        ExpressionFragment.SubExpression1(
                                                            fragments = ExpressionFragment.SubExpression1.Fragments(
                                                                se2.fragments.basicExpressionFragment
                                                            ),
                                                            asEmbarkExpressionMultiple2 = null,
                                                        )
                                                    }
                                                )
                                            }
                                    )
                                }
                            )
                        },
                    ),
                    valueStore,
                )
            }
        when (multipleExpression.multipleType) {
            EmbarkExpressionTypeMultiple.AND -> {
                if (results.all { it is ExpressionResult.True }) {
                    return ExpressionResult.True(multipleExpression.text)
                }
            }
            EmbarkExpressionTypeMultiple.OR -> {
                if (results.any { it is ExpressionResult.True }) {
                    return ExpressionResult.True(multipleExpression.text)
                }
            }
            else -> {
            }
        }
    }
    return ExpressionResult.False
}

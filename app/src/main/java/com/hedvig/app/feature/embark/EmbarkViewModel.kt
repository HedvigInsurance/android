package com.hedvig.app.feature.embark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.fragment.SubExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import kotlinx.coroutines.launch

sealed class ExpressionResult {
    data class True(
        val resultValue: String?
    ) : ExpressionResult()

    object False : ExpressionResult()
}

abstract class EmbarkViewModel : ViewModel() {
    private val _data = MutableLiveData<EmbarkStoryQuery.Passage>()
    val data: LiveData<EmbarkStoryQuery.Passage> = _data

    abstract fun load(name: String)

    protected lateinit var storyData: EmbarkStoryQuery.Data

    private val store = HashMap<String, String>()

    protected fun displayInitialPassage() {
        storyData.embarkStory?.let { story ->
            _data.postValue(preProcessPassage(story.passages.find { it.id == story.startPassage }))
        }
    }

    fun putInStore(key: String, value: String) {
        store[key] = value
    }

    fun navigateToPassage(passageName: String) {
        storyData.embarkStory?.let { story ->
            val nextPassage = story.passages.find { it.name == passageName }
            _data.postValue(preProcessPassage(nextPassage))
        }
    }

    private fun preProcessPassage(passage: EmbarkStoryQuery.Passage?): EmbarkStoryQuery.Passage? {
        if (passage == null) {
            return null
        }
        return passage.copy(
            messages = passage.messages.mapNotNull {
                preProcessMessage(it)
            }
        )
    }

    private fun preProcessMessage(message: EmbarkStoryQuery.Message): EmbarkStoryQuery.Message? {
        if (message.expressions.isEmpty()) {
            return message.copy(text = interpolateMessage(store, message.text))
        }

        val expressionText = message
            .expressions
            .map(::evaluateExpression)
            .filterIsInstance<ExpressionResult.True>()
            .firstOrNull()
            ?.resultValue
            ?: return null

        return message.copy(text = interpolateMessage(store, expressionText))
    }

    private fun evaluateExpression(expression: EmbarkStoryQuery.Expression): ExpressionResult {
        expression.fragments.expressionFragment.asEmbarkExpressionUnary?.let { unaryExpression ->
            return when (unaryExpression.unaryType) {
                EmbarkExpressionTypeUnary.ALWAYS -> ExpressionResult.True(unaryExpression.text)
                EmbarkExpressionTypeUnary.NEVER -> ExpressionResult.False
                else -> ExpressionResult.False
            }
        }
        expression.fragments.expressionFragment.asEmbarkExpressionBinary?.let { binaryExpression ->
            when (binaryExpression.binaryType) {
                EmbarkExpressionTypeBinary.EQUALS -> {
                    if (store[binaryExpression.key] == binaryExpression.value) {
                        return ExpressionResult.True(binaryExpression.text)
                    }
                }
                EmbarkExpressionTypeBinary.NOT_EQUALS -> {
                    val stored = store[binaryExpression.key] ?: return ExpressionResult.False
                    if (stored != binaryExpression.value) {
                        return ExpressionResult.True(binaryExpression.text)
                    }
                }
                EmbarkExpressionTypeBinary.MORE_THAN,
                EmbarkExpressionTypeBinary.MORE_THAN_OR_EQUALS,
                EmbarkExpressionTypeBinary.LESS_THAN,
                EmbarkExpressionTypeBinary.LESS_THAN_OR_EQUALS -> {
                    val storedAsInt =
                        store[binaryExpression.key]?.toIntOrNull() ?: return ExpressionResult.False
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
        expression.fragments.expressionFragment.asEmbarkExpressionMultiple?.let { multipleExpression ->
            val results =
                multipleExpression.subExpressions.map { evaluateExpression(it.fragments.subExpressionFragment.into()) }
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

    companion object {
        private val REPLACEMENT_FINDER = Regex("\\{[\\w.]+\\}")

        private fun interpolateMessage(store: Map<String, String>, message: String) =
            REPLACEMENT_FINDER
                .findAll(message)
                .fold(message) { acc, curr ->
                    val fromStore = store[curr.value.removeSurrounding("{", "}")] ?: return acc
                    acc.replace(curr.value, fromStore)
                }

        private fun SubExpressionFragment.into(): EmbarkStoryQuery.Expression =
            EmbarkStoryQuery.Expression(fragments = EmbarkStoryQuery.Expression.Fragments(
                ExpressionFragment(
                    asEmbarkExpressionUnary = asEmbarkExpressionUnary?.let {
                        ExpressionFragment.AsEmbarkExpressionUnary(
                            unaryType = it.unaryType,
                            text = it.text
                        )
                    },
                    asEmbarkExpressionBinary = asEmbarkExpressionBinary?.let {
                        ExpressionFragment.AsEmbarkExpressionBinary(
                            binaryType = it.binaryType,
                            key = it.key,
                            value = it.value,
                            text = it.text
                        )
                    },
                    asEmbarkExpressionMultiple = asEmbarkExpressionMultiple?.let {
                        ExpressionFragment.AsEmbarkExpressionMultiple(
                            multipleType = it.multipleType,
                            text = it.text,
                            subExpressions = it.subExpressions.map { se -> se.into() }
                        )
                    }
                )
            )
            )

        private fun SubExpressionFragment.SubExpression.into(): ExpressionFragment.SubExpression =
            ExpressionFragment.SubExpression(
                fragments = ExpressionFragment.SubExpression.Fragments(
                    SubExpressionFragment(
                        asEmbarkExpressionUnary = asEmbarkExpressionUnary1?.let {
                            SubExpressionFragment.AsEmbarkExpressionUnary(
                                unaryType = it.unaryType,
                                text = it.text
                            )
                        },
                        asEmbarkExpressionBinary = asEmbarkExpressionBinary1?.let {
                            SubExpressionFragment.AsEmbarkExpressionBinary(
                                binaryType = it.binaryType,
                                key = it.key,
                                value = it.value,
                                text = it.text
                            )
                        },
                        asEmbarkExpressionMultiple = null
                    )
                )
            )
    }
}

class EmbarkViewModelImpl(
    private val embarkRepository: EmbarkRepository
) : EmbarkViewModel() {

    override fun load(name: String) {
        viewModelScope.launch {
            val result = runCatching {
                embarkRepository
                    .embarkStoryAsync(name)
                    .await()
            }

            result.getOrNull()?.data()?.let { d ->
                storyData = d
                displayInitialPassage()
            }
        }
    }
}

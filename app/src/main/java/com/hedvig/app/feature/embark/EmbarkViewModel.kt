package com.hedvig.app.feature.embark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import kotlinx.coroutines.launch

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
            .mapNotNull(::evaluateExpression)
            .firstOrNull()
            ?: return null

        return message.copy(text = interpolateMessage(store, expressionText))
    }

    private fun evaluateExpression(expression: EmbarkStoryQuery.Expression): String? {
        expression.fragments.expressionFragment.asEmbarkExpressionUnary?.let { unaryExpression ->
            return when (unaryExpression.unaryType) {
                EmbarkExpressionTypeUnary.ALWAYS -> unaryExpression.text
                EmbarkExpressionTypeUnary.NEVER -> null
                else -> null
            }
        }
        expression.fragments.expressionFragment.asEmbarkExpressionBinary?.let { binaryExpression ->
            if (binaryExpression.binaryType == EmbarkExpressionTypeBinary.EQUALS) {
                if (store[binaryExpression.key] == binaryExpression.value) {
                    return binaryExpression.text
                }
            }
            return null
        }
        return null
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

package com.hedvig.app.feature.embark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.fragment.SubExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import com.hedvig.app.util.safeLet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Stack

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

    abstract suspend fun callGraphQLQuery(query: String): JSONObject?

    protected lateinit var storyData: EmbarkStoryQuery.Data

    private val store = HashMap<String, String>()
    private val backStack = Stack<String>()

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
            if (nextPassage?.redirects?.isNotEmpty() == true) {
                nextPassage.redirects.forEach { redirect ->
                    if (evaluateExpression(redirect.into()) is ExpressionResult.True) {
                        redirect.passedKeyValue?.let { (key, value) -> putInStore(key, value) }
                        redirect.to?.let { to ->
                            navigateToPassage(to)
                            return
                        }
                    }
                }
            }
            nextPassage?.api?.let { api ->
                api.fragments.apiFragment.asEmbarkApiGraphQLQuery?.let { graphQLQuery ->
                    viewModelScope.launch {
                        callGraphQLQuery(graphQLQuery.data.query)
                        graphQLQuery.data.next?.name?.let { navigateToPassage(it) }
                    }
                    return
                }
            }
            _data.value?.name?.let { backStack.push(it) }
            _data.postValue(preProcessPassage(nextPassage))
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.isEmpty()) {
            return false
        }
        val passageName = backStack.pop()

        storyData.embarkStory?.let { story ->
            val nextPassage = story.passages.find { it.name == passageName }
            _data.postValue(preProcessPassage(nextPassage))
            return true
        }
        return false
    }

    fun preProcessResponse(passageName: String): String? {
        val response = storyData
            .embarkStory
            ?.passages
            ?.find { it.name == passageName }
            ?.response
            ?: return null

        response.fragments.messageFragment?.let { message ->
            preProcessMessage(message)?.let { return it.text }
        }
        return null
    }

    private fun preProcessPassage(passage: EmbarkStoryQuery.Passage?): EmbarkStoryQuery.Passage? {
        if (passage == null) {
            return null
        }
        return passage.copy(
            messages = passage.messages.mapNotNull { message ->
                val messageFragment =
                    preProcessMessage(message.fragments.messageFragment) ?: return@mapNotNull null
                message.copy(
                    fragments = EmbarkStoryQuery.Message.Fragments(messageFragment)
                )
            }
        )
    }

    private fun preProcessMessage(message: MessageFragment): MessageFragment? {
        if (message.expressions.isEmpty()) {
            return message.copy(
                text = interpolateMessage(store, message.text)
            )
        }

        val expressionText = message
            .expressions
            .map(::evaluateExpression)
            .filterIsInstance<ExpressionResult.True>()
            .firstOrNull()
            ?.resultValue
            ?: return null

        return message.copy(
            text = interpolateMessage(store, expressionText)
        )
    }

    private fun evaluateExpression(expression: MessageFragment.Expression): ExpressionResult {
        expression.asEmbarkExpressionUnary?.let { unaryExpression ->
            return when (unaryExpression.unaryType) {
                EmbarkExpressionTypeUnary.ALWAYS -> ExpressionResult.True(unaryExpression.text)
                EmbarkExpressionTypeUnary.NEVER -> ExpressionResult.False
                else -> ExpressionResult.False
            }
        }
        expression.asEmbarkExpressionBinary?.let { binaryExpression ->
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
        expression.asEmbarkExpressionMultiple?.let { multipleExpression ->
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

        private fun SubExpressionFragment.into(): MessageFragment.Expression =
            MessageFragment.Expression(
                asEmbarkExpressionUnary = asEmbarkExpressionUnary?.let {
                    MessageFragment.AsEmbarkExpressionUnary(
                        unaryType = it.unaryType,
                        text = it.text
                    )
                },
                asEmbarkExpressionBinary = asEmbarkExpressionBinary?.let {
                    MessageFragment.AsEmbarkExpressionBinary(
                        binaryType = it.binaryType,
                        key = it.key,
                        value = it.value,
                        text = it.text
                    )
                },
                asEmbarkExpressionMultiple = asEmbarkExpressionMultiple?.let {
                    MessageFragment.AsEmbarkExpressionMultiple(
                        multipleType = it.multipleType,
                        text = it.text,
                        subExpressions = it.subExpressions.map { se -> se.into() }
                    )
                }
            )

        private fun SubExpressionFragment.SubExpression.into(): MessageFragment.SubExpression =
            MessageFragment.SubExpression(
                fragments = MessageFragment.SubExpression.Fragments(
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

        private fun EmbarkStoryQuery.Redirect.into(): MessageFragment.Expression =
            MessageFragment.Expression(
                asEmbarkExpressionUnary = asEmbarkRedirectUnaryExpression?.let {
                    MessageFragment.AsEmbarkExpressionUnary(
                        unaryType = it.unaryType,
                        text = null
                    )
                },
                asEmbarkExpressionBinary = asEmbarkRedirectBinaryExpression?.let {
                    MessageFragment.AsEmbarkExpressionBinary(
                        binaryType = it.binaryType,
                        key = it.key,
                        value = it.value,
                        text = null
                    )
                },
                asEmbarkExpressionMultiple = asEmbarkRedirectMultipleExpressions?.let {
                    MessageFragment.AsEmbarkExpressionMultiple(
                        multipleType = it.multipleExpressionType,
                        text = null,
                        subExpressions = it.subExpressions.map { se ->
                            MessageFragment.SubExpression(
                                fragments = MessageFragment.SubExpression.Fragments(se.fragments.subExpressionFragment)
                            )
                        }
                    )
                }
            )

        private val EmbarkStoryQuery.Redirect.to: String?
            get() {
                asEmbarkRedirectUnaryExpression?.let { return it.to }
                asEmbarkRedirectBinaryExpression?.let { return it.to }
                asEmbarkRedirectMultipleExpressions?.let { return it.to }

                return null
            }

        private val EmbarkStoryQuery.Redirect.passedKeyValue: Pair<String, String>?
            get() {
                asEmbarkRedirectUnaryExpression?.let { asUnary ->
                    return safeLet(
                        asUnary.passedExpressionKey,
                        asUnary.passedExpressionValue
                    ) { key, value -> Pair(key, value) }
                }
                asEmbarkRedirectBinaryExpression?.let { asBinary ->
                    return safeLet(
                        asBinary.passedExpressionKey,
                        asBinary.passedExpressionValue
                    ) { key, value -> Pair(key, value) }
                }
                asEmbarkRedirectMultipleExpressions?.let { asMultiple ->
                    return safeLet(
                        asMultiple.passedExpressionKey,
                        asMultiple.passedExpressionValue
                    ) { key, value -> Pair(key, value) }
                }
                return null
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

    override suspend fun callGraphQLQuery(query: String): JSONObject? {
        val result = runCatching { embarkRepository.graphQLQuery(query) }

        if (result.isFailure) {
            TODO("Will be implemented in another PR")
        }

        return withContext(Dispatchers.IO) {
            result.getOrNull()?.body?.string()?.let { JSONObject(it) }
        }
    }
}

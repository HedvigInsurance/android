package com.hedvig.app.feature.embark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.BasicExpressionFragment
import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import com.hedvig.android.owldroid.type.EmbarkExternalRedirectLocation
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.embark.util.VariableExtractor
import com.hedvig.app.util.Percent
import com.hedvig.app.util.getWithDotNotation
import com.hedvig.app.util.plus
import com.hedvig.app.util.safeLet
import com.hedvig.app.util.toStringArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Stack
import kotlin.math.max

abstract class EmbarkViewModel(
    private val tracker: EmbarkTracker,
    private val valueStore: ValueStore
) : ViewModel() {
    private val _data = MutableLiveData<EmbarkModel>()
    val data: LiveData<EmbarkModel> = _data

    protected val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Event> = _events

    sealed class Event {
        data class Offer(val ids: List<String>) : Event()
        data class Error(val message: String? = null) : Event()
        object Close : Event()
        object Chat : Event()
    }

    abstract fun fetchStory(name: String)

    abstract suspend fun callGraphQL(query: String, variables: JSONObject? = null): JSONObject?

    protected lateinit var storyData: EmbarkStoryQuery.Data
    private lateinit var loginStatus: LoginStatus

    private val backStack = Stack<String>()
    private var totalSteps: Int = 0

    protected fun setInitialState(loginStatus: LoginStatus) {
        storyData.embarkStory?.let { story ->
            valueStore.computedValues = story.getComputedValues()
            this.loginStatus = loginStatus
            val firstPassage = story.passages.first { it.id == story.startPassage }

            totalSteps = getPassagesLeft(firstPassage)

            val model = EmbarkModel(
                passage = preProcessPassage(firstPassage),
                navigationDirection = NavigationDirection.INITIAL,
                progress = currentProgress(firstPassage),
                isLoggedIn = loginStatus == LoginStatus.LOGGED_IN,
                hasTooltips = firstPassage.tooltips.isNotEmpty()
            )
            _data.postValue(model)

            firstPassage.tracks.forEach { track ->
                tracker.track(track.eventName, trackingData(track))
            }
        }
    }

    fun putInStore(key: String, value: String) {
        valueStore.put(key, value)
    }

    fun putInStore(key: String, value: List<String>) {
        valueStore.put(key, value)
    }

    fun getPrefillFromStore(key: String) = valueStore.prefill.get(key)

    fun getFromStore(key: String) = valueStore.get(key)

    fun getListFromStore(keys: List<String>): List<String> {
        return keys.map {
            valueStore.getList(it) ?: listOfNotNull(valueStore.get(it))
        }.flatten()
    }

    fun submitAction(nextPassageName: String, submitIndex: Int = 0) {
        data.value?.passage?.let { currentPassage ->
            currentPassage.action?.api(submitIndex)?.let { api ->
                api.asEmbarkApiGraphQLQuery?.let { graphQLQuery ->
                    handleGraphQLQuery(graphQLQuery)
                    return
                }
                api.asEmbarkApiGraphQLMutation?.let { graphQLMutation ->
                    handleGraphQLMutation(graphQLMutation)
                    return
                }
            }
        }
        navigateToPassage(nextPassageName)
    }

    private fun navigateToPassage(passageName: String) {
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
            nextPassage?.offerRedirect?.data?.keys?.takeIf { it.isNotEmpty() }?.let { keys ->
                val ids = getListFromStore(keys)
                _events.tryEmit(Event.Offer(ids))
                return
            }
            nextPassage?.externalRedirect?.data?.location?.let { location ->
                when (location) {
                    EmbarkExternalRedirectLocation.OFFER -> {
                        val id = getFromStore("quoteId")
                        if (id == null) {
                            _events.tryEmit(Event.Error())
                            return
                        }
                        _events.tryEmit(Event.Offer(listOf(id)))
                        return
                    }
                    EmbarkExternalRedirectLocation.CLOSE -> {
                        _events.tryEmit(Event.Close)
                        return
                    }
                    EmbarkExternalRedirectLocation.CHAT -> {
                        _events.tryEmit(Event.Chat)
                        return
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
            nextPassage?.api?.let { api ->
                api.fragments.apiFragment.asEmbarkApiGraphQLQuery?.let { graphQLQuery ->
                    handleGraphQLQuery(graphQLQuery)
                    return
                }
                api.fragments.apiFragment.asEmbarkApiGraphQLMutation?.let { graphQLMutation ->
                    handleGraphQLMutation(graphQLMutation)
                    return
                }
            }
            _data.value?.passage?.name?.let {
                valueStore.commitVersion()
                backStack.push(it)
            }
            val model = EmbarkModel(
                passage = preProcessPassage(nextPassage),
                navigationDirection = NavigationDirection.FORWARDS,
                progress = currentProgress(nextPassage),
                isLoggedIn = loginStatus == LoginStatus.LOGGED_IN,
                hasTooltips = nextPassage?.tooltips?.isNotEmpty() == true
            )
            _data.postValue(model)
            nextPassage?.tracks?.forEach { track ->
                tracker.track(track.eventName, trackingData(track))
            }
        }
    }

    private fun trackingData(track: EmbarkStoryQuery.Track) = when {
        track.includeAllKeys -> JSONObject(valueStore.toMap())
        track.eventKeys.filterNotNull().isNotEmpty() -> JSONObject(
            track.eventKeys.filterNotNull()
                .map { it to valueStore.get(it) }.toMap()
        )
        else -> null
    }?.let { data ->
        track.customData?.let { data + it } ?: data
    }

    private fun currentProgress(passage: EmbarkStoryQuery.Passage?): Percent {
        if (passage == null) {
            return Percent(0)
        }
        val passagesLeft = getPassagesLeft(passage)
        val progress = ((totalSteps.toFloat() - passagesLeft.toFloat()) / totalSteps.toFloat()) * 100
        return Percent(progress.toInt())
    }

    private fun handleGraphQLQuery(graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery) {
        viewModelScope.launch {
            val variables = if (graphQLQuery.queryData.variables.isNotEmpty()) {
                val variables = graphQLQuery.queryData.variables.map { it.fragments.graphQLVariablesFragment }
                VariableExtractor.extractVariables(variables, valueStore)
            } else {
                null
            }
            val result = runCatching { callGraphQL(graphQLQuery.queryData.query, variables) }

            when {
                result.isFailure -> navigateToPassage(graphQLQuery.getPassageNameFromError())
                result.hasErrors() -> {
                    if (graphQLQuery.queryData.errors.any { it.fragments.graphQLErrorsFragment.contains != null }) {
                        TODO("Handle matched error")
                    }
                    navigateToPassage(graphQLQuery.getPassageNameFromError())
                }
                result.isSuccess -> {
                    val response = result.getOrNull()?.getJSONObject("data") ?: return@launch

                    graphQLQuery.queryData.results.forEach { r ->
                        val key = r.fragments.graphQLResultsFragment.as_
                        when (val value = response.getWithDotNotation(r.fragments.graphQLResultsFragment.key)) {
                            is JSONArray -> putInStore(key, value.toStringArray())
                            is JSONObject -> putInStore(key, value.toString())
                            else -> putInStore(key, value.toString())
                        }
                    }

                    graphQLQuery.queryData.next?.fragments?.embarkLinkFragment?.name?.let {
                        navigateToPassage(
                            it
                        )
                    }
                }
            }
        }
    }

    private fun handleGraphQLMutation(graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation) {
        viewModelScope.launch {
            val variables = if (graphQLMutation.mutationData.variables.isNotEmpty()) {
                val variables = graphQLMutation.mutationData.variables.map { it.fragments.graphQLVariablesFragment }
                VariableExtractor.extractVariables(variables, valueStore)
            } else {
                null
            }
            val result = runCatching { callGraphQL(graphQLMutation.mutationData.mutation, variables) }

            val passageName = graphQLMutation.mutationData.errors
                .first().fragments.graphQLErrorsFragment
                .next.fragments.embarkLinkFragment.name

            when {
                result.isFailure -> navigateToPassage(passageName)
                result.hasErrors() -> {
                    val containsErrors = graphQLMutation
                        .mutationData
                        .errors.any { it.fragments.graphQLErrorsFragment.contains != null }
                    if (containsErrors) {
                        TODO("Handle matched error")
                    }
                    navigateToPassage(passageName)
                }
                result.isSuccess -> {
                    val response = result.getOrNull()?.getJSONObject("data") ?: return@launch

                    graphQLMutation.mutationData.results.filterNotNull().forEach { r ->
                        val key = r.fragments.graphQLResultsFragment.as_
                        when (val value = response.getWithDotNotation(r.fragments.graphQLResultsFragment.key)) {
                            is JSONArray -> putInStore(key, value.toStringArray())
                            is JSONObject -> putInStore(key, value.toString())
                            else -> putInStore(key, value.toString())
                        }
                    }
                    graphQLMutation.mutationData.next?.fragments?.embarkLinkFragment?.name?.let {
                        navigateToPassage(it)
                    }
                }
            }
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.isEmpty()) {
            return false
        }
        val passageName = backStack.pop()

        storyData.embarkStory?.let { story ->
            _data.value?.passage?.name?.let { currentPassageName ->
                tracker.track("Passage Go Back - $currentPassageName")
            }
            val nextPassage = story.passages.find { it.name == passageName }
            val model = EmbarkModel(
                passage = preProcessPassage(nextPassage),
                navigationDirection = NavigationDirection.BACKWARDS,
                progress = currentProgress(nextPassage),
                isLoggedIn = loginStatus == LoginStatus.LOGGED_IN,
                hasTooltips = nextPassage?.tooltips?.isNotEmpty() == true
            )
            _data.postValue(model)

            valueStore.rollbackVersion()
            return true
        }
        return false
    }

    fun preProcessResponse(passageName: String): Response? {
        val response = storyData
            .embarkStory
            ?.passages
            ?.find { it.name == passageName }
            ?.response
            ?: return null

        response.fragments.messageFragment?.let { message ->
            preProcessMessage(message)?.let { return Response.SingleResponse(it.text) }
        }

        response.fragments.responseExpressionFragment?.let { exp ->
            preProcessMessage(
                MessageFragment(
                    text = exp.text,
                    expressions = exp.expressions.map {
                        MessageFragment.Expression(
                            fragments = MessageFragment.Expression.Fragments(it.fragments.expressionFragment)
                        )
                    }
                )
            )?.let { return Response.SingleResponse(it.text) }
        }

        response.asEmbarkGroupedResponse?.let { groupedResponse ->
            val titleExpression = groupedResponse.title.fragments.responseExpressionFragment
            val title = preProcessMessage(
                MessageFragment(
                    text = titleExpression.text,
                    expressions = titleExpression.expressions.map {
                        MessageFragment.Expression(
                            fragments = MessageFragment.Expression.Fragments(it.fragments.expressionFragment)
                        )
                    }
                )
            )?.text

            val items = groupedResponse.items.mapNotNull { item ->
                preProcessMessage(item.fragments.messageFragment)?.text
            }.toMutableList()

            groupedResponse.each?.let { each ->
                val multiActionItems = valueStore.getMultiActionItems(each.key)
                items += multiActionItems.mapNotNull { mai ->
                    val maiView = object : ValueStoreView {
                        override fun get(key: String) = mai[key]
                        override fun getList(key: String): List<String>? = null
                    }
                    preProcessMessage(each.content.fragments.messageFragment, maiView)?.text
                }
            }

            return Response.GroupedResponse(
                title = title,
                groups = items
            )
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

    private fun getPassagesLeft(passage: EmbarkStoryQuery.Passage) = passage.allLinks
        .map { findMaxDepth(it.fragments.embarkLinkFragment.name) }
        .fold(0) { acc, i -> max(acc, i) }

    private fun findMaxDepth(passageName: String, previousDepth: Int = 0): Int {
        val passage = storyData.embarkStory?.passages?.find { it.name == passageName }
        val links = passage?.allLinks?.map { it.fragments.embarkLinkFragment.name }

        if (links?.size == 0 || links == null) {
            return previousDepth
        }

        return links
            .map { findMaxDepth(it, previousDepth + 1) }
            .fold(0) { acc, i -> max(acc, i) }
    }

    private fun preProcessMessage(
        message: MessageFragment,
        valueStoreView: ValueStoreView = valueStore,
    ): MessageFragment? {
        if (message.expressions.isEmpty()) {
            return message.copy(
                text = interpolateMessage(message.text, valueStoreView)
            )
        }

        val expressionText = message
            .expressions
            .map { evaluateExpression(it.fragments.expressionFragment) }
            .filterIsInstance<ExpressionResult.True>()
            .firstOrNull()
            ?.resultValue
            ?: return null

        return message.copy(
            text = interpolateMessage(expressionText)
        )
    }

    private fun evaluateExpression(expression: ExpressionFragment): ExpressionResult {
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
                    if (valueStore.get(binaryExpression.key) == binaryExpression.value) {
                        return ExpressionResult.True(binaryExpression.text)
                    }
                }
                EmbarkExpressionTypeBinary.NOT_EQUALS -> {
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
                        )
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

    private fun interpolateMessage(message: String, store: ValueStoreView = valueStore) =
        REPLACEMENT_FINDER
            .findAll(message)
            .fold(message) { acc, curr ->
                val key = curr.value.removeSurrounding("{", "}")
                val fromStore = store.get(key) ?: return@fold acc
                acc.replace(curr.value, fromStore)
            }

    companion object {
        private val REPLACEMENT_FINDER = Regex("\\{[\\w.]+\\}")

        private fun Result<JSONObject?>.hasErrors() = getOrNull()?.has("errors") == true

        private fun EmbarkStoryQuery.Redirect.into(): ExpressionFragment =
            ExpressionFragment(
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
    private val embarkRepository: EmbarkRepository,
    private val loginStatusService: LoginStatusService,
    tracker: EmbarkTracker,
    valueStore: ValueStore,
    storyName: String,
) : EmbarkViewModel(tracker, valueStore) {

    init {
        fetchStory(storyName)
    }

    override fun fetchStory(name: String) {
        viewModelScope.launch {
            val result = runCatching {
                embarkRepository.embarkStory(name)
            }
            if (result.isFailure) {
                _events.tryEmit(
                    Event.Error(
                        result.getOrNull()?.errors?.toString()
                            ?: result.exceptionOrNull()?.message
                    )
                )
                return@launch
            }
            val loginStatus = loginStatusService.getLoginStatus()
            result.getOrNull()?.data?.let { d ->
                storyData = d
                setInitialState(loginStatus)
            }
        }
    }

    override suspend fun callGraphQL(query: String, variables: JSONObject?) =
        withContext(Dispatchers.IO) {
            embarkRepository.graphQLQuery(query, variables).body?.string()?.let { JSONObject(it) }
        }
}

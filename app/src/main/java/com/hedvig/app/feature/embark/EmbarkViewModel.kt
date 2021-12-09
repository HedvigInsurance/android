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
import com.hedvig.android.owldroid.type.EmbarkExternalRedirectLocation
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
import com.hedvig.app.feature.embark.extensions.api
import com.hedvig.app.feature.embark.extensions.getComputedValues
import com.hedvig.app.feature.embark.util.VariableExtractor
import com.hedvig.app.feature.embark.util.evaluateExpression
import com.hedvig.app.util.Percent
import com.hedvig.app.util.plus
import com.hedvig.app.util.safeLet
import java.util.Stack
import kotlin.math.max
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

abstract class EmbarkViewModel(
    private val tracker: EmbarkTracker,
    private val valueStore: ValueStore,
    private val graphQLQueryUseCase: GraphQLQueryUseCase,
    private val triggerFreeTextChatUseCase: TriggerFreeTextChatUseCase,
) : ViewModel() {
    private val _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = _viewState

    protected val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    data class ViewState(
        val passage: EmbarkStoryQuery.Passage?,
        val navigationDirection: NavigationDirection,
        val progress: Percent,
        val isLoggedIn: Boolean,
        val hasTooltips: Boolean,
    )

    sealed class Event {
        data class Offer(val ids: List<String>) : Event()
        data class Error(val message: String? = null) : Event()
        object Close : Event()
        object Chat : Event()
    }

    abstract fun fetchStory(name: String)

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
            navigateToPassage(firstPassage.name)
        }
    }

    fun putInStore(key: String, value: String) {
        valueStore.put(key, value)
    }

    fun putInStore(key: String, value: List<String>) {
        valueStore.put(key, value)
    }

    fun getPrefillFromStore(key: String) = valueStore.prefill.get(key)

    private fun getFromStore(key: String) = valueStore.get(key)

    private fun getListFromStore(keys: List<String>): List<String> {
        return keys.map {
            valueStore.getList(it) ?: listOfNotNull(valueStore.get(it))
        }.flatten()
    }

    fun submitAction(nextPassageName: String, submitIndex: Int = 0) {
        viewState.value?.passage?.let { currentPassage ->
            currentPassage.action?.api(submitIndex)?.let { api ->
                callApi(api)
            }
        }
        navigateToPassage(nextPassageName)
    }

    private fun navigateToPassage(passageName: String) {
        val nextPassage = storyData.embarkStory?.passages?.find { it.name == passageName }
        val redirectPassage = getRedirectPassageAndPutInStore(nextPassage?.redirects)
        val keys = nextPassage?.offerRedirect?.data?.keys?.takeIf { it.isNotEmpty() }
        val location = nextPassage?.externalRedirect?.data?.location
        val api = nextPassage?.api?.fragments?.apiFragment

        when {
            storyData.embarkStory == null || nextPassage == null -> _events.trySend(Event.Error())
            redirectPassage != null -> navigateToPassage(redirectPassage)
            keys != null && keys.isNotEmpty() -> {
                val ids = getListFromStore(keys)
                _events.trySend(Event.Offer(ids))
            }
            location != null -> handleRedirectLocation(location)
            api != null -> callApi(api)
            else -> setupPassageAndEmitState(nextPassage)
        }
    }

    private fun setupPassageAndEmitState(nextPassage: EmbarkStoryQuery.Passage) {
        _viewState.value?.passage?.name?.let {
            valueStore.commitVersion()
            backStack.push(it)
        }
        val state = ViewState(
            passage = preProcessPassage(nextPassage),
            navigationDirection = NavigationDirection.FORWARDS,
            progress = currentProgress(nextPassage),
            isLoggedIn = loginStatus == LoginStatus.LOGGED_IN,
            hasTooltips = nextPassage.tooltips.isNotEmpty(),
        )
        _viewState.postValue(state)
        _loadingState.update { false }
        nextPassage.tracks.forEach { track ->
            tracker.track(track.eventName, trackingData(track))
        }
    }

    private fun callApi(apiFragment: ApiFragment) {
        _loadingState.update { true }

        val graphQLQuery = apiFragment.asEmbarkApiGraphQLQuery
        val graphQLMutation = apiFragment.asEmbarkApiGraphQLMutation
        when {
            graphQLQuery != null -> handleGraphQLQuery(graphQLQuery)
            graphQLMutation != null -> handleGraphQLMutation(graphQLMutation)
            else -> {
                _loadingState.update { false }
                _events.trySend(Event.Error())
            }
        }
    }

    private fun handleRedirectLocation(location: EmbarkExternalRedirectLocation) {
        when (location) {
            EmbarkExternalRedirectLocation.OFFER -> {
                val id = getFromStore("quoteId")
                if (id == null) {
                    _events.trySend(Event.Error())
                } else {
                    _events.trySend(Event.Offer(listOf(id)))
                }
            }
            EmbarkExternalRedirectLocation.CLOSE -> {
                _events.trySend(Event.Close)
            }
            EmbarkExternalRedirectLocation.CHAT -> {
                viewModelScope.launch {
                    triggerFreeTextChatUseCase.invoke()
                    _events.trySend(Event.Chat)
                }
            }
            else -> {
                // Do nothing
            }
        }
    }

    private fun getRedirectPassageAndPutInStore(redirects: List<EmbarkStoryQuery.Redirect>?): String? {
        redirects?.forEach { redirect ->
            if (evaluateExpression(redirect.into(), valueStore) is ExpressionResult.True) {
                redirect.passedKeyValue?.let { (key, value) -> putInStore(key, value) }
                redirect.to?.let { to ->
                    return to
                }
            }
        }
        return null
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
            val variables = graphQLQuery.queryData.variables
                .takeIf { it.isNotEmpty() }
                ?.map { it.fragments.graphQLVariablesFragment }
                ?.let { VariableExtractor.extractVariables(it, valueStore) }

            val fileVariables = graphQLQuery.queryData.variables
                .takeIf { it.isNotEmpty() }
                ?.map { it.fragments.graphQLVariablesFragment }
                ?.let { VariableExtractor.extractFileVariable(it, valueStore) }
                ?: emptyList()

            val result = graphQLQueryUseCase.executeQuery(graphQLQuery, variables, fileVariables)
            handleQueryResult(result)
        }
    }

    private fun handleGraphQLMutation(graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation) {
        viewModelScope.launch {
            val variables = graphQLMutation.mutationData.variables
                .takeIf { it.isNotEmpty() }
                ?.map { it.fragments.graphQLVariablesFragment }
                ?.let { VariableExtractor.extractVariables(it, valueStore) }

            val fileVariables = graphQLMutation.mutationData.variables
                .takeIf { it.isNotEmpty() }
                ?.map { it.fragments.graphQLVariablesFragment }
                ?.let { VariableExtractor.extractFileVariable(it, valueStore) }
                ?: emptyList()

            val result = graphQLQueryUseCase.executeMutation(graphQLMutation, variables, fileVariables)
            handleQueryResult(result)
        }
    }

    private fun handleQueryResult(result: GraphQLQueryResult) {
        // todo there was an empty Loading event here that was just to trigger the beginDelayedTransition or what?
        _loadingState.update { false }

        when (result) {
            // TODO Handle errors 
            is GraphQLQueryResult.Error -> navigateToPassage(result.passageName)
            is GraphQLQueryResult.ValuesFromResponse -> {
                result.arrayValues.forEach {
                    valueStore.put(it.first, it.second)
                }
                result.objectValues.forEach {
                    valueStore.put(it.first, it.second)
                }

                if (result.passageName != null) {
                    navigateToPassage(result.passageName)
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
            _viewState.value?.passage?.name?.let { currentPassageName ->
                tracker.track("Passage Go Back - $currentPassageName")
            }
            val nextPassage = story.passages.find { it.name == passageName }
            val model = ViewState(
                passage = preProcessPassage(nextPassage),
                navigationDirection = NavigationDirection.BACKWARDS,
                progress = currentProgress(nextPassage),
                isLoggedIn = loginStatus == LoginStatus.LOGGED_IN,
                hasTooltips = nextPassage?.tooltips?.isNotEmpty() == true,
            )
            _loadingState.update { false }
            _viewState.postValue(model)

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
            .map { evaluateExpression(it.fragments.expressionFragment, valueStore) }
            .filterIsInstance<ExpressionResult.True>()
            .firstOrNull()
            ?.resultValue
            ?: return null

        return message.copy(
            text = interpolateMessage(expressionText)
        )
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
    graphQLQueryUseCase: GraphQLQueryUseCase,
    triggerFreeTextChatUseCase: TriggerFreeTextChatUseCase,
    tracker: EmbarkTracker,
    valueStore: ValueStore,
    storyName: String,
) : EmbarkViewModel(tracker, valueStore, graphQLQueryUseCase, triggerFreeTextChatUseCase) {

    init {
        fetchStory(storyName)
    }

    override fun fetchStory(name: String) {
        viewModelScope.launch {
            val result = runCatching {
                embarkRepository.embarkStory(name)
            }
            if (result.isFailure) {
                _events.trySend(
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
}

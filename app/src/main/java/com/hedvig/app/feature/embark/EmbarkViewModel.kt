package com.hedvig.app.feature.embark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExternalRedirectLocation
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.embark.extensions.api
import com.hedvig.app.feature.embark.extensions.getComputedValues
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.embark.util.evaluateExpression
import com.hedvig.app.feature.embark.util.getFileVariables
import com.hedvig.app.feature.embark.util.getOfferKeysOrNull
import com.hedvig.app.feature.embark.util.getVariables
import com.hedvig.app.feature.embark.util.toExpressionFragment
import com.hedvig.app.util.ProgressPercentage
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.asMap
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.safeLet
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Stack
import kotlin.math.max

private const val QUOTE_CART_ID_KEY = "quoteCartId"
private const val QUOTE_ID_KEY = "quoteId"

abstract class EmbarkViewModel(
    private val valueStore: ValueStore,
    private val graphQLQueryUseCase: GraphQLQueryUseCase,
    private val chatRepository: ChatRepository,
    private val hAnalytics: HAnalytics,
    private val storyName: String,
    loginStatusService: LoginStatusService,
    private val featureManager: FeatureManager,
) : ViewModel() {
    private val _passageState = MutableLiveData<PassageState>()
    val passageState: LiveData<PassageState> = _passageState

    private val loginStatus = loginStatusService
        .getLoginStatusAsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val viewState: LiveData<ViewState> =
        passageState.asFlow()
            .combine(loginStatus.filterNotNull()) { passageState, loginStatus ->
                ViewState(
                    passageState,
                    loginStatus == LoginStatus.LOGGED_IN
                )
            }
            .asLiveData()
    protected val _events = Channel<Event>(Channel.UNLIMITED)

    val events = _events.receiveAsFlow()
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    data class ViewState(
        val passageState: PassageState,
        val isLoggedIn: Boolean,
    )

    data class PassageState(
        val passage: EmbarkStoryQuery.Passage?,
        val navigationDirection: NavigationDirection,
        val progressPercentage: ProgressPercentage,
        val hasTooltips: Boolean,
    )

    sealed class Event {
        data class Offer(
            val quoteIds: List<String>,
            val quoteCartId: String?,
        ) : Event()

        data class Error(val message: String? = null) : Event()
        object Close : Event()
        object Chat : Event()
    }

    abstract fun fetchStory(name: String)

    protected lateinit var storyData: EmbarkStoryQuery.Data

    private val backStack = Stack<String>()
    private var totalSteps: Int = 0

    init {
        hAnalytics.screenViewEmbark(storyName)
    }

    protected fun setInitialState() {
        storyData.embarkStory?.let { story ->
            valueStore.computedValues = story.getComputedValues()
            val firstPassage = story.passages.first { it.id == story.startPassage }

            totalSteps = getPassagesLeft(firstPassage)
            navigateToPassage(firstPassage.name)
        }
    }

    fun putInStore(key: String, value: String?) {
        valueStore.put(key, value)
    }

    fun putInStore(key: String, value: List<String>) {
        valueStore.put(key, value)
    }

    fun getPrefillFromStore(key: String) = valueStore.prefill.get(key)

    fun submitAction(nextPassageName: String, submitIndex: Int = 0) {
        val apiFromAction = viewState.value?.passageState?.passage?.action?.api(submitIndex)
        if (apiFromAction != null) {
            callApi(apiFromAction)
        } else {
            navigateToPassage(nextPassageName)
        }
    }

    private fun navigateToPassage(passageName: String) {
        val nextPassage = storyData.embarkStory?.passages?.find { it.name == passageName }
        val redirectPassage = getRedirectPassageAndPutInStore(nextPassage?.redirects)
        val location = nextPassage?.externalRedirect?.data?.location
        val api = nextPassage?.api?.fragments?.apiFragment

        val keys = nextPassage?.getOfferKeysOrNull(valueStore, featureManager)

        when {
            storyData.embarkStory == null || nextPassage == null -> _events.trySend(Event.Error())
            redirectPassage != null -> navigateToPassage(redirectPassage)
            keys != null && keys.isNotEmpty() -> {
                // For offers, there is a problem with the Offer screen not committing before this stage is reached,
                //  meaning that the old values were returned from getList/get.
                valueStore.withCommittedVersion {
                    val ids = keys.flatMap { this.getList(it) ?: listOfNotNull(this.get(it)) }
                    val quoteCartId = this.get(QUOTE_CART_ID_KEY)
                    _events.trySend(Event.Offer(ids, quoteCartId))
                }
            }
            location != null -> handleRedirectLocation(location)
            api != null -> callApi(api)
            else -> setupPassageAndEmitState(nextPassage)
        }
    }

    private fun setupPassageAndEmitState(nextPassage: EmbarkStoryQuery.Passage) {
        _passageState.value?.passage?.name?.let {
            valueStore.commitVersion()
            backStack.push(it)
        }
        val passageState = PassageState(
            passage = preProcessPassage(nextPassage),
            navigationDirection = NavigationDirection.FORWARDS,
            progressPercentage = currentProgress(nextPassage),
            hasTooltips = nextPassage.tooltips.isNotEmpty(),
        )
        _passageState.postValue(passageState)
        _loadingState.update { false }
        nextPassage.tracks.forEach { track ->
            hAnalytics.embarkTrack(storyName, track.eventName, trackingData(track))
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

    private fun handleGraphQLQuery(graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery) {
        viewModelScope.launch {
            val variables = graphQLQuery.getVariables(valueStore)
            val fileVariables = graphQLQuery.getFileVariables(valueStore)
            val result = graphQLQueryUseCase.executeQuery(graphQLQuery, variables, fileVariables)
            handleQueryResult(result)
        }
    }

    private fun handleGraphQLMutation(graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation) {
        viewModelScope.launch {
            val variables = graphQLMutation.getVariables(valueStore)
            val fileVariables = graphQLMutation.getFileVariables(valueStore)
            val result = graphQLQueryUseCase.executeMutation(graphQLMutation, variables, fileVariables)
            handleQueryResult(result)
        }
    }

    private fun handleQueryResult(result: GraphQLQueryResult) {
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

    private fun handleRedirectLocation(location: EmbarkExternalRedirectLocation) {
        hAnalytics.embarkExternalRedirect(location.rawValue)
        when (location) {
            EmbarkExternalRedirectLocation.OFFER -> sendOfferId()
            EmbarkExternalRedirectLocation.CLOSE -> _events.trySend(Event.Close)
            EmbarkExternalRedirectLocation.CHAT -> triggerChat()
            else -> {
                // Do nothing
            }
        }
    }

    private fun sendOfferId() {
        val id = valueStore.get(QUOTE_ID_KEY)
        val quoteCartId = valueStore.get(QUOTE_CART_ID_KEY)
        val event = if (id == null) {
            Event.Error()
        } else {
            Event.Offer(
                quoteIds = listOf(id),
                quoteCartId = quoteCartId,
            )
        }
        _events.trySend(event)
    }

    private fun triggerChat() {
        viewModelScope.launch {
            val event = when (chatRepository.triggerFreeTextChat()) {
                is Either.Left -> Event.Error()
                is Either.Right -> Event.Chat
            }
            _events.trySend(event)
        }
    }

    private fun getRedirectPassageAndPutInStore(redirects: List<EmbarkStoryQuery.Redirect>?): String? {
        redirects?.forEach { redirect ->
            if (evaluateExpression(redirect.toExpressionFragment(), valueStore) is ExpressionResult.True) {
                redirect.passedKeyValue?.let { (key, value) -> putInStore(key, value) }
                redirect.to?.let { to ->
                    return to
                }
            }
        }
        return null
    }

    private fun trackingData(track: EmbarkStoryQuery.Track) = when {
        track.includeAllKeys -> valueStore.toMap()
        track.eventKeys.filterNotNull().isNotEmpty() ->
            track
                .eventKeys
                .filterNotNull()
                .associateWith { valueStore.get(it) }
        else -> emptyMap()
    }.let { data ->
        track.customData?.let { data + it.asMap() } ?: data
    }

    private fun currentProgress(passage: EmbarkStoryQuery.Passage?): ProgressPercentage {
        if (passage == null) {
            return ProgressPercentage(0f)
        }
        val passagesLeft = getPassagesLeft(passage)
        val progress = ((totalSteps.toFloat() - passagesLeft.toFloat()) / totalSteps.toFloat())
        return ProgressPercentage.safeValue(progress)
    }

    fun navigateBack(): Boolean {
        if (backStack.isEmpty()) {
            return false
        }
        val passageName = backStack.pop()

        storyData.embarkStory?.let { story ->
            _passageState.value?.passage?.name?.let { currentPassageName ->
                hAnalytics.embarkPassageGoBack(storyName, currentPassageName)
            }
            val nextPassage = story.passages.find { it.name == passageName }
            val passageState = PassageState(
                passage = preProcessPassage(nextPassage),
                navigationDirection = NavigationDirection.BACKWARDS,
                progressPercentage = currentProgress(nextPassage),
                hasTooltips = nextPassage?.tooltips?.isNotEmpty() == true,
            )
            _loadingState.update { false }
            _passageState.postValue(passageState)

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
    loginStatusService: LoginStatusService,
    graphQLQueryUseCase: GraphQLQueryUseCase,
    chatRepository: ChatRepository,
    valueStore: ValueStore,
    hAnalytics: HAnalytics,
    storyName: String,
    private val createQuoteCartUseCase: CreateQuoteCartUseCase,
    private val featureManager: FeatureManager,
) : EmbarkViewModel(
    valueStore,
    graphQLQueryUseCase,
    chatRepository,
    hAnalytics,
    storyName,
    loginStatusService,
    featureManager,
) {

    init {
        fetchStory(storyName)
    }

    override fun fetchStory(name: String) {
        viewModelScope.launch {
            if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
                when (val quoteCartResult = createQuoteCartUseCase.invoke()) {
                    is Either.Left -> _events.trySend(Event.Error(quoteCartResult.value.message))
                    is Either.Right -> putInStore(QUOTE_CART_ID_KEY, quoteCartResult.value.id)
                }
            }

            when (val result = embarkRepository.embarkStory(name)) {
                is QueryResult.Error -> _events.trySend(Event.Error(result.message))
                is QueryResult.Success -> {
                    storyData = result.data
                    setInitialState()
                }
            }
        }
    }
}

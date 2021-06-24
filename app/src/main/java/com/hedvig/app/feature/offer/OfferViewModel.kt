package com.hedvig.app.feature.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.usecase.GetQuotesUseCase
import com.hedvig.app.feature.perils.PerilItem
import e
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class OfferViewModel : ViewModel() {
    protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading(OfferItemsBuilder.createLoadingItem()))
    val viewState: StateFlow<ViewState> = _viewState
    abstract val autoStartToken: LiveData<SignOfferMutation.Data>
    abstract val signStatus: LiveData<SignStatusFragment>
    abstract val signError: LiveData<Boolean>
    abstract fun removeDiscount()
    abstract fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data)
    abstract fun triggerOpenChat(done: () -> Unit)
    abstract fun startSign()
    abstract fun clearPreviousErrors()
    abstract fun manuallyRecheckSignStatus()
    abstract fun chooseStartDate(id: String, date: LocalDate)
    abstract fun removeStartDate(id: String)

    sealed class ViewState {
        data class OfferItems(
            val topOfferItems: List<OfferModel>,
            val perils: List<PerilItem>,
            val documents: List<DocumentItems>,
            val insurableLimitsItems: List<InsurableLimitItem>,
            val bottomOfferItems: List<OfferModel.Footer>,
        ) : ViewState()

        sealed class Error : ViewState() {
            data class GeneralError(val message: String?) : Error()
            object EmptyResponse : Error()
        }

        class Loading(val loadingItem: List<OfferModel.Loading>) : ViewState()
        object HasContracts : ViewState()
    }
}

class OfferViewModelImpl(
    _quoteIds: List<String>,
    private val offerRepository: OfferRepository,
    private val getQuotesUseCase: GetQuotesUseCase,
) : OfferViewModel() {

    private lateinit var quoteIds: List<String>

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch {
            when (val idsResult = getQuotesUseCase(_quoteIds)) {
                is GetQuotesUseCase.Result.Success -> {
                    quoteIds = idsResult.ids
                    idsResult
                        .data
                        .map(::toViewState)
                        .onEach { _viewState.value = it }
                        .catch { _viewState.value = ViewState.Error.GeneralError(it.message) }
                }
                GetQuotesUseCase.Result.Error -> {
                    _viewState.value = ViewState.Error.GeneralError("")
                }
            }
        }
    }

    private fun toViewState(response: Response<OfferQuery.Data>): ViewState {
        return response.errors?.let {
            ViewState.Error.GeneralError(it.firstOrNull()?.message)
        } ?: response.data?.let { data ->
            if (data.contracts.isNotEmpty()) {
                ViewState.HasContracts
            } else {
                val topOfferItems = OfferItemsBuilder.createTopOfferItems(data)
                val perilItems = OfferItemsBuilder.createPerilItems(data.quoteBundle.quotes[0])
                val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(data.quoteBundle.quotes[0])
                val documentItems = OfferItemsBuilder.createDocumentItems(data.quoteBundle.quotes[0])
                val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems()
                ViewState.OfferItems(topOfferItems, perilItems, documentItems, insurableLimitsItems, bottomOfferItems)
            }
        } ?: ViewState.Error.EmptyResponse
    }

    override fun removeDiscount() {
        viewModelScope.launch {
            val result = runCatching { offerRepository.removeDiscount() }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            result.getOrNull()?.let { removeDiscountFromCache() }
        }
    }

    private fun removeDiscountFromCache() {
        offerRepository.removeDiscountFromCache(quoteIds)
    }

    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) =
        offerRepository.writeDiscountToCache(quoteIds, data)

    override fun triggerOpenChat(done: () -> Unit) {
        viewModelScope.launch {
            val result = runCatching { offerRepository.triggerOpenChatFromOffer() }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            result.getOrNull()?.let { done() }
        }
    }

    override fun startSign() {
        viewModelScope.launch {
            offerRepository.subscribeSignStatus()
                .onEach { response ->
                    response.data?.signStatus?.status?.fragments?.signStatusFragment?.let {
                        signStatus.postValue(
                            it
                        )
                    }
                }
                .catch { e(it) }
                .launchIn(this)

            val response = runCatching { offerRepository.startSign() }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { autoStartToken.postValue(it) }
        }
    }

    override fun clearPreviousErrors() {
        signError.value = false
    }

    override fun manuallyRecheckSignStatus() {
        viewModelScope.launch {
            val response = runCatching { offerRepository.fetchSignStatus() }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.signStatus?.fragments?.signStatusFragment
                ?.let { signStatus.postValue(it) }
        }
    }

    override fun chooseStartDate(id: String, date: LocalDate) {
        viewModelScope.launch {
            val response = runCatching {
                offerRepository.chooseStartDate(id, date)
            }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let {
                offerRepository.writeStartDateToCache(quoteIds, it)
            } ?: run {
                e { "Missing data when choosing start date" }
            }
        }
    }

    override fun removeStartDate(id: String) {
        viewModelScope.launch {
            val response = runCatching {
                offerRepository.removeStartDate(id)
            }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { offerRepository.removeStartDateFromCache(quoteIds, it) }
        }
    }
}

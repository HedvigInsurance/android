package com.hedvig.app.feature.offer.ui.changestartdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.extensions.epochMillisToLocalDate
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChangeDateBottomSheetViewModel(
    private val tracker: OfferTracker,
    private val editStartDateUseCase: EditStartDateUseCase,
    private val data: ChangeDateBottomSheetData
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Inceptions(data.inceptions))
    val viewState: StateFlow<ViewState> = _viewState

    private val selectedDates = mutableMapOf<String, LocalDate>()

    fun onDateSelected(isConcurrent: Boolean, quoteId: String, epochMillis: Long) {
        val date = epochMillis.epochMillisToLocalDate()
        if (isConcurrent) {
            selectedDates[quoteId] = date
        } else {
            data.inceptions.forEach {
                selectedDates[it.quoteId] = date
            }
        }
    }

    fun onSwitchChecked(quoteId: String, checked: Boolean) {
        if (checked) {
            tracker.activateOnInsuranceEnd()
            viewModelScope.launch {
                _viewState.value = ViewState.Loading(true)
                val result = editStartDateUseCase.removeStartDate(
                    id = quoteId,
                    idsInBundle = data.idsInBundle
                )
                _viewState.value = when (result) {
                    is QueryResult.Error -> ViewState.Error(result.message)
                    is QueryResult.Success -> ViewState.Loading(false)
                }
            }
        }
    }

    fun onChooseDateClicked() {
        _viewState.value = if (selectedDates.isNotEmpty()) {
            ViewState.ShowConfirmationDialog
        } else {
            ViewState.Dismiss
        }
    }

    fun onDialogConfirmed() {
        setNewDateAndDismiss()
    }

    private fun setNewDateAndDismiss() {
        viewModelScope.launch {
            tracker.changeDateContinue()
            _viewState.value = ViewState.Loading(true)
            val results = selectedDates.map {
                editStartDateUseCase.setStartDate(
                    id = it.key,
                    idsInBundle = data.idsInBundle,
                    date = it.value
                )
            }
            if (results.any { it is QueryResult.Error }) {
                val message = (results.first { it is QueryResult.Error } as? QueryResult.Error.QueryError)?.message
                _viewState.value = ViewState.Error(message)
            } else {
                _viewState.value = ViewState.Dismiss
            }
        }
    }
/*
    override fun chooseStartDate(id: String, date: LocalDate) {
        _viewState.postValue(
            OfferViewModel.ViewState.OfferItems(
                OfferItemsBuilder.createOfferItems(
                    MockOfferViewModel.mockData.copy(
                        quoteBundle = MockOfferViewModel.mockData.quoteBundle.copy(
                            quotes = MockOfferViewModel.mockData.quoteBundle.quotes.map {
                                it.copy(
                                    startDate = date
                                )
                            }
                        )
                    )
                ),
                listOf()
            )
        )
    }

    override fun removeStartDate(id: String) {
        _viewState.postValue(
            OfferViewModel.ViewState.OfferItems(
                OfferItemsBuilder.createOfferItems(
                    MockOfferViewModel.mockData.copy(
                        quoteBundle = MockOfferViewModel.mockData.quoteBundle.copy(
                            quotes = MockOfferViewModel.mockData.quoteBundle.quotes.map {
                                it.copy(startDate = null)
                            }
                        )
                    )
                ),
                listOf()
            )
        )
    }
 */

    sealed class ViewState {
        object Dismiss : ViewState()
        data class Loading(val showLoading: Boolean) : ViewState()
        data class Error(val message: String? = null) : ViewState()
        object ShowConfirmationDialog : ViewState()
        data class Inceptions(
            val inceptions: List<ChangeDateBottomSheetData.Inception>
        ) : ViewState()
    }
}

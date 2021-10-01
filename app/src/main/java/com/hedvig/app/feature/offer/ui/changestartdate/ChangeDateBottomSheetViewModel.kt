package com.hedvig.app.feature.offer.ui.changestartdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.offer.OfferTracker
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.extensions.epochMillisToLocalDate
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ChangeDateBottomSheetViewModel(
    private val tracker: OfferTracker,
    private val editStartDateUseCase: EditStartDateUseCase,
    private val data: ChangeDateBottomSheetData
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.Inceptions(data.inceptions))
    val viewState = _viewState.asStateFlow()

    private val selectedDates = mutableMapOf<String, LocalDate>()

    fun shouldOpenDatePicker(): Boolean = viewState.value !is ViewState.Loading

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

    fun onDialogConfirmed() {
        setNewDateAndDismiss()
    }

    fun setNewDateAndDismiss() {
        viewModelScope.launch {
            tracker.changeDateContinue()
            _viewState.value = ViewState.Loading(true)
            val results = coroutineScope {
                selectedDates.map { dateMapEntry ->
                    async {
                        editStartDateUseCase.setStartDate(
                            id = dateMapEntry.key,
                            idsInBundle = data.idsInBundle,
                            date = dateMapEntry.value
                        )
                    }
                }.awaitAll()
            }
            if (results.any { it is QueryResult.Error }) {
                val message = (results.first { it is QueryResult.Error } as? QueryResult.Error.QueryError)?.message
                _viewState.value = ViewState.Error(message)
            } else {
                _viewState.value = ViewState.Dismiss
            }
        }
    }

    sealed class ViewState {
        object Dismiss : ViewState()
        data class Loading(val showLoading: Boolean) : ViewState()
        data class Error(val message: String? = null) : ViewState()
        data class Inceptions(
            val inceptions: List<ChangeDateBottomSheetData.Inception>
        ) : ViewState()
    }
}

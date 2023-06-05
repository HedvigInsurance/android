package com.hedvig.app.feature.offer.ui.changestartdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.sequence
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.util.extensions.epochMillisToLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ChangeDateBottomSheetViewModel(
  private val quoteCartEditStartDateUseCase: QuoteCartEditStartDateUseCase,
  private val data: ChangeDateBottomSheetData,
  private val offerRepository: OfferRepository,
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
      _viewState.value = ViewState.Loading(true)
      viewModelScope.launch {
        removeDateOnQuoteCart(data.quoteCartId, quoteId)
      }
    }
  }

  private suspend fun removeDateOnQuoteCart(quoteCartId: QuoteCartId, quoteId: String) {
    val state = quoteCartEditStartDateUseCase.removeStartDate(quoteCartId, quoteId)
      .fold(
        ifLeft = { ViewState.Error(it.message) },
        ifRight = { ViewState.Loading(false) },
      )
    _viewState.value = state
  }

  fun setNewDateAndDismiss() {
    viewModelScope.launch {
      _viewState.value = ViewState.Loading(true)
      setDateOnQuoteCart(data.quoteCartId)
    }
  }

  private suspend fun setDateOnQuoteCart(quoteCartId: QuoteCartId) {
    selectedDates.map { dateMapEntry ->
      quoteCartEditStartDateUseCase.setStartDate(
        quoteCartId = quoteCartId,
        quoteId = dateMapEntry.key,
        date = dateMapEntry.value,
      )
    }
      .sequence()
      .fold(
        ifLeft = { ViewState.Error(it.message) },
        ifRight = {
          offerRepository.fetchNewOffer(data.quoteCartId)
          _viewState.value = ViewState.Dismiss
        },
      )
  }

  sealed class ViewState {
    object Dismiss : ViewState()
    data class Loading(val showLoading: Boolean) : ViewState()
    data class Error(val message: String? = null) : ViewState()
    data class Inceptions(
      val inceptions: List<ChangeDateBottomSheetData.Inception>,
    ) : ViewState()
  }
}

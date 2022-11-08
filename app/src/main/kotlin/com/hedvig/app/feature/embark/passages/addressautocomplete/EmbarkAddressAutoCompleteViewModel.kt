package com.hedvig.app.feature.embark.passages.addressautocomplete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class EmbarkAddressAutoCompleteViewModel(
  initialAddress: DanishAddress?,
) : ViewModel() {

  data class ViewState(
    val address: DanishAddress?,
  ) {
    val canProceed: Boolean
      get() = address != null
  }

  private val address: MutableStateFlow<DanishAddress?> = MutableStateFlow(initialAddress)
  val viewState: StateFlow<ViewState> = address.map { address ->
    ViewState(address)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5.seconds),
    initialValue = ViewState(initialAddress),
  )

  fun updateAddressSelected(newAddress: DanishAddress?) {
    address.update {
      newAddress
    }
  }
}

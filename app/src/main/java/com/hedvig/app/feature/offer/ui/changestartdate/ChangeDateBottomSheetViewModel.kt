package com.hedvig.app.feature.offer.ui.changestartdate

import androidx.lifecycle.ViewModel
import com.hedvig.app.util.extensions.epochMillisToLocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChangeDateBottomSheetViewModel(data: ChangeDateBottomSheetData) : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState(data))
    val viewState: StateFlow<ViewState> = _viewState

    fun onDateSelected(epochMillis: Long) {
        val selectedDateTime = epochMillis.epochMillisToLocalDate()
        _viewState.value = _viewState.value.copy(selectedDateTime = selectedDateTime)
    }

    data class ViewState(
        val id: String,
        val selectedDateTime: LocalDateTime = LocalDateTime.now(),
        val hasSwitchableInsurer: Boolean
    ) {
        constructor(data: ChangeDateBottomSheetData) : this(
            id = data.id,
            selectedDateTime = LocalDateTime.now(),
            hasSwitchableInsurer = data.hasSwitchableInsurer
        )
    }
}

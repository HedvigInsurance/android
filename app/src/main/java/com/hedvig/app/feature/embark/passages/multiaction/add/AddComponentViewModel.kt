package com.hedvig.app.feature.embark.passages.multiaction.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hedvig.app.feature.embark.passages.multiaction.ComponentState
import com.hedvig.app.feature.embark.passages.multiaction.MultiAction
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.startWith
import java.util.UUID

class AddComponentViewModel(
    private val multiActionParams: MultiActionParams,
    private val componentState: ComponentState?
) : ViewModel() {

    val input = MutableStateFlow(componentState?.input)
    val dropDownSelection = MutableStateFlow(componentState?.dropDownSelection)
    val switchSelection = MutableStateFlow(componentState?.switch)

    val viewState: LiveData<ViewState> = combine(
        input.debounce(500),
        dropDownSelection,
        ::validateSate
    ).asLiveData()

    val componentResultEvent = LiveEvent<MultiAction.Component>()

    private fun validateSate(input: String?, dropDownSelection: String?) = when {
        input == null || dropDownSelection == null -> ViewState.NoSelection
        input.isBlank() -> ViewState.Error.NoInput
        input.toInt() < multiActionParams.components.firstOrNull()?.number?.minValue ?: 0 -> ViewState.Error.MinInput
        input.toInt() > multiActionParams.components.firstOrNull()?.number?.maxValue ?: 0 -> ViewState.Error.MaxInput
        else -> ViewState.Valid
    }

    private fun createComponent() = MultiAction.Component(
        id = componentState?.id ?: UUID.randomUUID().mostSignificantBits,
        selectedDropDown = MultiAction.Component.KeyValue(
            key = multiActionParams.components.first().dropdown?.key ?: "",
            value = dropDownSelection.value ?: ""
        ),
        input = MultiAction.Component.KeyValue(
            key = multiActionParams.components.first().number?.key ?: "",
            value = input.value ?: ""
        ),
        switch = switchSelection.value ?: false,
    )

    fun onContinue() {
        val component = createComponent()
        componentResultEvent.postValue(component)
    }

    sealed class ViewState {
        object Valid : ViewState()
        object NoSelection : ViewState()
        sealed class Error : ViewState() {
            object MaxInput : Error()
            object MinInput : Error()
            object NoInput : Error()
        }
    }
}

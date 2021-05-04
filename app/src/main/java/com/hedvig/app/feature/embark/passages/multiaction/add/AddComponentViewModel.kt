package com.hedvig.app.feature.embark.passages.multiaction.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionComponent
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionParams
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.startWith
import java.util.UUID

class AddComponentViewModel(
    private val component: MultiActionItem.Component?,
    multiActionParams: MultiActionParams
) : ViewModel() {

    private val dropDownStates = MutableStateFlow(emptyMap<String, DropDownState>())
    private val switchStates = MutableStateFlow(emptyMap<String, SwitchState>())
    private val inputStates = MutableStateFlow(emptyMap<String, NumberState>())

    init {
        multiActionParams.components.forEach {
            when (it) {
                is MultiActionComponent.Dropdown -> onDropDownChanged(it.key, null)
                is MultiActionComponent.Number -> onNumberChanged(it.key, null, it.minValue, it.maxValue, null)
                is MultiActionComponent.Switch -> onSwitchChanged(it.key, it.defaultValue)
            }
        }
    }

    val inputsViewState = inputStates.asLiveData()

    val viewState: LiveData<ViewState> = combine(
        inputStates,
        dropDownStates,
        ::validateSate
    )
        .onStart { emit(ViewState.Invalid) }
        .asLiveData()

    val componentResultEvent = LiveEvent<MultiActionItem.Component>()

    private fun validateSate(inputs: Map<String, NumberState>, dropDowns: Map<String, DropDownState>) = when {
        inputs.values.any { it !is NumberState.Valid } -> ViewState.Invalid
        dropDowns.values.any { it !is DropDownState.Selected } -> ViewState.Invalid
        else -> ViewState.Valid
    }

    private fun validateNumberInput(key: String, input: String?, minValue: Int?, maxValue: Int?, unit: String?) = when {
        input.isNullOrBlank() -> NumberState.NoInput
        input.toInt() < minValue ?: 0 -> NumberState.Error.MinInput
        input.toInt() > maxValue ?: 0 -> NumberState.Error.MaxInput
        else -> NumberState.Valid(key, input, unit)
    }

    private fun createComponent() = MultiActionItem.Component(
        id = component?.id ?: UUID.randomUUID().mostSignificantBits,
        selectedDropDowns = dropDownStates.value.map {
            MultiActionItem.DropDown(
                key = it.key,
                value = (it.value as? DropDownState.Selected)?.value ?: ""
            )
        },
        inputs = inputStates.value.map {
            MultiActionItem.Input(
                key = (it.value as? NumberState.Valid)?.key ?: "",
                value = (it.value as? NumberState.Valid)?.input ?: "",
                unit = (it.value as? NumberState.Valid)?.unit ?: "",
            )
        },
        switches = switchStates.value.map {
            MultiActionItem.Switch(
                key = it.key,
                value = it.value.checked
            )
        }
    )

    fun onContinue() {
        val component = createComponent()
        componentResultEvent.postValue(component)
    }

    fun onNumberChanged(key: String, value: String?, minValue: Int?, maxValue: Int?, unit: String?) {
        inputStates.value = inputStates.value.toMutableMap().apply {
            put(key, validateNumberInput(key, value, minValue, maxValue, unit))
        }
    }

    fun onSwitchChanged(key: String, checked: Boolean) {
        switchStates.value = switchStates.value.toMutableMap().apply {
            put(key, SwitchState(checked))
        }
    }

    fun onDropDownChanged(key: String, value: String?) {
        val state = if (value == null) {
            DropDownState.NotSelected
        } else {
            DropDownState.Selected(value)
        }

        dropDownStates.value = dropDownStates.value.toMutableMap().apply {
            put(key, state)
        }
    }

    sealed class NumberState {
        data class Valid(
            val key: String,
            val input: String,
            val unit: String?,
        ) : NumberState()

        object NoInput : NumberState()

        sealed class Error : NumberState() {
            object MaxInput : Error()
            object MinInput : Error()
        }
    }

    sealed class DropDownState {
        data class Selected(val value: String) : DropDownState()
        object NotSelected : DropDownState()
    }

    data class SwitchState(val checked: Boolean)

    sealed class ViewState {
        object Invalid : ViewState()
        object Valid : ViewState()
    }
}

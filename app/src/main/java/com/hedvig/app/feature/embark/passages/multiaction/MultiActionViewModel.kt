package com.hedvig.app.feature.embark.passages.multiaction

import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem.AddButton
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem.Component
import com.hedvig.app.util.extensions.replace
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map

class MultiActionViewModel(
    private val multiActionParams: MultiActionParams,
) : ViewModel() {

    private val _addedComponents = MutableStateFlow<List<Component>>(listOf())
    val components = _addedComponents.map { components ->
        if (components.size < multiActionParams.maxAmount) {
            val addButton = AddButton(multiActionParams.addLabel)
            listOf(addButton) + components
        } else {
            components
        }
    }

    private val _newComponent = MutableSharedFlow<Component?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val newComponent: SharedFlow<Component?> = _newComponent

    fun onComponentCreated(component: Component) {
        _addedComponents.value = if (_addedComponents.value.find { it.id == component.id } != null) {
            _addedComponents.value.replace(component) { it.id == component.id }
        } else {
            _addedComponents.value.plus(component)
        }
    }

    fun onComponentClicked(id: Long) {
        _addedComponents.value
            .find { it.id == id }
            ?.let(::createNewComponent)
    }

    fun createNewComponent(state: Component? = null) {
        _newComponent.tryEmit(state)
    }

    fun onComponentRemoved(id: Long) {
        _addedComponents.value = _addedComponents.value.filterNot { it.id == id }
    }

    fun onContinue(addToStore: (String, String) -> Unit) {
        _addedComponents.value.forEachIndexed { index, component ->
            component.inputs.forEach { input ->
                addToStore("${multiActionParams.key}[$index]${input.key}", input.value)
            }
            component.selectedDropDowns.forEach { dropDown ->
                addToStore("${multiActionParams.key}[$index]${dropDown.key}", dropDown.value)
                addToStore("${multiActionParams.key}[$index]${dropDown.key}.Label", dropDown.text)
            }
            component.switches.forEach { switch ->
                addToStore("${multiActionParams.key}[$index]${switch.key}", switch.value.toString())
            }
        }
        _addedComponents.value.size.let { addToStore("${multiActionParams.key}Result", it.toString()) }
    }
}

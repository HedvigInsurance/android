package com.hedvig.app.feature.embark.passages.multiaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hedvig.app.feature.embark.passages.multiaction.MultiActionItem.*
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.extensions.replace

class MultiActionViewModel(
    private val multiActionParams: MultiActionParams
) : ViewModel() {

    private val _addedComponents = MutableLiveData<List<Component>>(listOf())
    val components =_addedComponents.map { components ->
        if (components.size < multiActionParams.maxAmount) {
            val addButton = AddButton(::createNewComponent)
            listOf(addButton) + components
        } else {
            components
        }
    }

    val newComponent = LiveEvent<Component?>()

    fun onComponentCreated(component: Component) {
        _addedComponents.value = if (_addedComponents.value?.find { it.id == component.id } != null) {
            _addedComponents.value?.replace(component) { it.id == component.id }
        } else {
            _addedComponents.value?.plus(component) ?: listOf(component)
        }
    }

    fun onComponentClicked(id: Long) {
        _addedComponents.value
            ?.find { it.id == id }
            ?.let(::createNewComponent)
    }

    private fun createNewComponent(state: Component? = null) {
        newComponent.value = state
    }

    fun onComponentRemoved(id: Long) {
        _addedComponents.value = _addedComponents.value?.filterNot { it.id == id }
    }

    fun onContinue(addToStore: (String, String) -> Unit) {
        _addedComponents.value?.forEach { component ->
            component.inputs.forEach { input ->
                addToStore(input.key, input.value)
            }
            component.selectedDropDowns.forEach { dropDown ->
                addToStore(dropDown.key, dropDown.value)
            }
            component.switches.forEach { switch ->
                addToStore(switch.key, switch.value.toString())
            }
        }
    }
}

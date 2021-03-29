package com.hedvig.app.feature.embark.passages.multiaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.hedvig.app.feature.embark.passages.multiaction.MultiAction.*
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.extensions.replace

class MultiActionViewModel(
    private val multiActionParams: MultiActionParams
) : ViewModel() {

    private val _addedComponents = MutableLiveData<List<Component>>(listOf())
    val components: LiveData<List<MultiAction>> = Transformations.map(_addedComponents) { components ->
        val addButton = AddButton(::createNewComponent)
        listOf(addButton) + components
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
        val componentData = multiActionParams.components.first()
        _addedComponents.value?.forEach { addedComponent ->
            componentData.number?.key?.let { numberKey ->
                addToStore(numberKey, addedComponent.input)
            }
            componentData.dropdown?.key?.let { dropDownKey ->
                addToStore(dropDownKey, addedComponent.selectedDropDown)
            }
            componentData.switch?.key?.let { switchKey ->
                addToStore(switchKey, addedComponent.switch.toString())
            }
        }
    }
}


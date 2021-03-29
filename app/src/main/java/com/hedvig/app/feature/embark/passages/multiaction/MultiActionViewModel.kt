package com.hedvig.app.feature.embark.passages.multiaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.embark.passages.multiaction.MultiAction.*

class MultiActionViewModel(
    private val multiActionParams: MultiActionParams
) : ViewModel() {

    private val _addedComponents = MutableLiveData<List<Component>>(listOf())
    val components: LiveData<List<MultiAction>> = Transformations.map(_addedComponents) { components ->
        val addButton = AddButton(::createNewComponent)
        listOf(addButton) + components
    }

    private val _newComponent = MutableLiveData<ComponentState?>()
    val newComponent: LiveData<ComponentState?> = _newComponent

    fun onContinue() {

    }

    private fun createNewComponent(state: ComponentState? = null) {
        _newComponent.value = state
    }

    fun onComponentCreated(component: Component) {
        _addedComponents.value = if (_addedComponents.value?.find { it.id == component.id } != null) {
            _addedComponents.value?.replace(component) { it.id == component.id }
        } else {
            _addedComponents.value?.plus(component) ?: listOf(component)
        }
    }

    fun onComponentClicked(id: Long) {
        _addedComponents.value?.find { it.id == id }?.let {
            val state = ComponentState(
                id = it.id,
                dropDownSelection = it.selectedDropDown.value,
                input = it.input.value,
                switch = it.switch
            )
            createNewComponent(state)
        }
    }

    fun onComponentRemoved(id: Long) {
        _addedComponents.value = _addedComponents.value?.filterNot { it.id == id }
    }

    fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
        return map {
            if (block(it)) newValue else it
        }
    }

}


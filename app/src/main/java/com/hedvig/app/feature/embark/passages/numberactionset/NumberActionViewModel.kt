package com.hedvig.app.feature.embark.passages.numberactionset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hedvig.app.util.extensions.replace

class NumberActionViewModel(
    private val data: NumberActionParams,
) : ViewModel() {

    private val inputs = MutableLiveData(data.numberActions.map { action -> NumberInput(action.key) })
    val valid: LiveData<Boolean> = inputs.map { inputs -> inputs.all { it.valid } }

    fun setInputValue(key: String, value: String) {
        inputs.value = inputs.value?.replace(
            NumberInput(
                key = key,
                input = value,
                valid = isValid(value.toIntOrNull(), data.numberActions.first { it.key == key })
            )
        ) {
            it.key == key
        }
    }

    private fun isValid(number: Int?, action: NumberActionParams.NumberAction) = when {
        number == null -> false
        action.minValue != null && action.maxValue != null -> number in action.minValue..action.maxValue
        action.maxValue != null -> number <= action.maxValue
        action.minValue != null -> number >= action.minValue
        else -> true
    }

    fun onContinue(storeFunction: (String, String) -> Unit) {
        storeFunction("${data.passageName}Result", inputs.value?.first()?.input ?: "")
        inputs.value?.map {
            storeFunction(it.key, it.input ?: "")
        }
    }

    fun getAllInput() = inputs.value?.map { it.input }?.joinToString(" ")

    data class NumberInput(
        val key: String,
        val input: String? = null,
        val valid: Boolean = false
    )
}

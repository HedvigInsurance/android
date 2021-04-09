package com.hedvig.app.feature.embark.passages.textaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class TextActionViewModel(data: TextActionParameter) : ViewModel() {
    private val _isValid = MutableLiveData((data.keys.indices).map { Pair(it, false) }.toMap())
    val isValid = _isValid.map { iv ->
        iv.values.all { it }
    }

    private val _inputs = MutableLiveData(data.keys.indices.map { Pair(it, "") }.toMap())
    val inputs: LiveData<Map<Int, String>> = _inputs

    fun setInputValue(index: Int, value: String) {
        _inputs.value?.let { ipts ->
            _inputs.value = ipts.toMutableMap().also { it[index] = value }
        }
    }

    fun updateIsValid(position: Int, isValid: Boolean) {
        val new = _isValid.value?.toMutableMap()
        new?.put(position, isValid)
        _isValid.value = new
    }
}

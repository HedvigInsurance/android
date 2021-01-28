package com.hedvig.app.feature.embark.passages.textactionset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TextActionSetViewModel : ViewModel() {
    private val _isValid = MutableLiveData<HashMap<Int, Boolean>>(hashMapOf())
    val isValid: LiveData<HashMap<Int, Boolean>> = _isValid

    private val _inputs = MutableLiveData<Map<Int, String>>()
    val inputs: LiveData<Map<Int, String>> = _inputs

    fun initializeInputs(data: TextActionSetData) {
        if (_inputs.value == null) {
            _inputs.value = (0..data.keys.size).map { Pair(it, "") }.toMap()
        }
    }

    fun setInputValue(index: Int, value: String) {
        _inputs.value?.let { ipts ->
            _inputs.value = ipts.toMutableMap().also { it[index] = value }
        }
    }

    fun updateIsValid(position: Int, isValid: Boolean) {
        val old = _isValid.value
        old?.put(position, isValid)
        _isValid.postValue(old)
    }

    override fun onCleared() {
        super.onCleared()
    }
}

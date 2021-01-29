package com.hedvig.app.feature.embark.passages.textactionset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TextActionSetViewModel : ViewModel() {
    private val _isValid = MutableLiveData<HashMap<Int, Boolean>>(hashMapOf())
    val isValid: LiveData<HashMap<Int, Boolean>> = _isValid

    fun updateIsValid(position: Int, isValid: Boolean) {
        val old = _isValid.value
        old?.put(position, isValid)
        _isValid.postValue(old)
    }
}

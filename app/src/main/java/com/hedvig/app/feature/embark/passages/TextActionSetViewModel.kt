package com.hedvig.app.feature.embark.passages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TextActionSetViewModel : ViewModel() {
    private val _hasTextAndIsValid = MutableLiveData<HashMap<Int, Boolean>>(hashMapOf())
    val hasTextAndIsValid: LiveData<HashMap<Int, Boolean>> = _hasTextAndIsValid

    fun updateHasTextAndIsValidHashMap(position: Int, hasTextAndIsValid: Boolean) {
        val old = _hasTextAndIsValid.value
        old?.put(position, hasTextAndIsValid)
        _hasTextAndIsValid.postValue(old)
    }
}

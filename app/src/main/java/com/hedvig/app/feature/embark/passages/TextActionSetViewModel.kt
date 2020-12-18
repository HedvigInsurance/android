package com.hedvig.app.feature.embark.passages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TextActionSetViewModel : ViewModel() {
    private val _hasText = MutableLiveData<HashMap<Int, Boolean>>(hashMapOf())
    val hasText: LiveData<HashMap<Int, Boolean>> = _hasText

    fun updateIsEmptyHashMap(position: Int, hasText: Boolean) {
        val old = _hasText.value
        old?.put(position, hasText)
        _hasText.postValue(old)
    }
}

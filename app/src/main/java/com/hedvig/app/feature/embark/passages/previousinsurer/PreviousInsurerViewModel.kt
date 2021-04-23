package com.hedvig.app.feature.embark.passages.previousinsurer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PreviousInsurerViewModel : ViewModel() {

    private val _previousInsurer = MutableLiveData<PreviousInsurerItem.Insurer?>()

    val previousInsurer: LiveData<PreviousInsurerItem.Insurer?> = _previousInsurer

    init {
        _previousInsurer.value = null
    }

    fun setPreviousInsurer(previousInsurer: PreviousInsurerItem.Insurer) {
        _previousInsurer.value = previousInsurer
    }
}

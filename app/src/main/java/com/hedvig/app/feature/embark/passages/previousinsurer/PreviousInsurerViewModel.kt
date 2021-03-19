package com.hedvig.app.feature.embark.passages.previousinsurer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class PreviousInsurerViewModel : ViewModel() {
    abstract val previousInsurer: LiveData<String?>
    abstract fun setPreviousInsurer(id: String)
}

class PreviousInsurerViewModelImpl : PreviousInsurerViewModel() {

    private val _previousInsurer = MutableLiveData<String?>()

    override val previousInsurer: LiveData<String?>
        get() = _previousInsurer

    init {
        _previousInsurer.value = null
    }

    override fun setPreviousInsurer(id: String) {
        _previousInsurer.value = id
    }

}

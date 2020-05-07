package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class LoggedInFragmentViewModel : ViewModel() {
    abstract val scroll: MutableLiveData<Float>
}

class LoggedInFragmentViewModelImpl : LoggedInFragmentViewModel() {
    override val scroll = MutableLiveData<Float>()
}

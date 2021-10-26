package com.hedvig.app.feature.changeaddress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressViewModel
import com.hedvig.app.feature.home.ui.changeaddress.ViewState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MockChangeAddressViewModel : ChangeAddressViewModel() {

    override val viewState: LiveData<ViewState>
        get() = mockedState

    override fun reload() {
        viewModelScope.launch {
            val tempValue = mockedState.value
            mockedState.value = ViewState.Loading
            delay(2000)
            mockedState.value = tempValue
        }
    }

    override suspend fun triggerFreeTextChat() = Unit

    companion object {
        var mockedState: MutableLiveData<ViewState> = MutableLiveData()
    }
}

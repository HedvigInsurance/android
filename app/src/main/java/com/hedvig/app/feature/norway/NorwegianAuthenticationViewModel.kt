package com.hedvig.app.feature.norway

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class NorwegianAuthenticationViewModel : ViewModel() {
    abstract val redirectUrl: LiveData<String>
}

class NorwegianAuthenticationViewModelImpl(
    private val repository: NorwegianAuthenticationRepository
) : NorwegianAuthenticationViewModel() {
    override val redirectUrl = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            val response = repository
                .authAsync()
                .await()

            redirectUrl.postValue(response.data()?.norwegianBankIdAuth?.redirectUrl)
        }
    }
}

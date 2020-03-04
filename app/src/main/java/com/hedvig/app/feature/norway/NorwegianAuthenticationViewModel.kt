package com.hedvig.app.feature.norway

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NorwegianAuthenticationViewModel(
    private val repository: NorwegianAuthenticationRepository
) : ViewModel() {
    val redirectUrl = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            val response = repository
                .authAsync()
                .await()

            redirectUrl.postValue(response.data()?.norwegianBankIdAuth?.redirectUrl)
        }
    }
}

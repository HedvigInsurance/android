package com.hedvig.app.feature.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import e
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val welcomeRepository: WelcomeRepository
) : ViewModel() {

    private val _data = MutableLiveData<WelcomeQuery.Data>()
    val data: LiveData<WelcomeQuery.Data> = _data

    fun fetch() {
        viewModelScope.launch {
            val response = runCatching { welcomeRepository.fetchWelcomeScreens() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { _data.postValue(it) }
        }
    }
}

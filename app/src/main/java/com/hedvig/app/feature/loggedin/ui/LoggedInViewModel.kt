package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.service.RemoteConfig
import com.hedvig.app.service.RemoteConfigData
import kotlinx.coroutines.launch

abstract class LoggedInViewModel : ViewModel() {
    abstract val remoteConfig: LiveData<RemoteConfigData>
}

class LoggedInViewModelImpl(
    private val remoteConfigService: RemoteConfig
) : LoggedInViewModel() {
    override val remoteConfig = MutableLiveData<RemoteConfigData>()

    init {
        viewModelScope.launch {
            val config = remoteConfigService.fetch()
            remoteConfig.postValue(config)
        }
    }
}

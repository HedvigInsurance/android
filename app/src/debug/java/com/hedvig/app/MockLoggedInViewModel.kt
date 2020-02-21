package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.service.RemoteConfigData

class MockLoggedInViewModel : LoggedInViewModel() {
    override val remoteConfig = MutableLiveData<RemoteConfigData>()

    init {
        remoteConfig.postValue(RemoteConfigData(keyGearEnabled = true))
    }
}

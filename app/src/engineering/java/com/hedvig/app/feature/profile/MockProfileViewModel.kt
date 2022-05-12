package com.hedvig.app.feature.profile

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.util.LiveEvent

class MockProfileViewModel : ProfileViewModel() {
    override val dirty = MutableLiveData<Boolean>()
    override val trustlyUrl = LiveEvent<String>()

    init {
        load()
    }

    override fun load() {
        if (!shouldError) {
            _data.value = ViewState.Success(profileData)
        } else {
            _data.value = ViewState.Error
            shouldError = false
        }
    }

    override fun selectCashback(id: String) = Unit
    override fun saveInputs(emailInput: String, phoneNumberInput: String) = Unit
    override fun emailChanged(newEmail: String) = Unit
    override fun phoneNumberChanged(newPhoneNumber: String) = Unit

    override fun onLogout() {
    }

    companion object {
        var profileData = PROFILE_DATA
        var shouldError = false
    }
}

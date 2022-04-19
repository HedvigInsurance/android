package com.hedvig.app.feature.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.tab.ProfileQueryDataToProfileUiStateMapper
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MockProfileViewModel(
    private val profileQueryDataToProfileUiStateMapper: ProfileQueryDataToProfileUiStateMapper,
) : ProfileViewModel() {
    override val dirty = MutableLiveData<Boolean>()
    override val trustlyUrl = LiveEvent<String>()

    private val _data: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)
    override val data: StateFlow<ViewState> = _data.asStateFlow()

    init {
        reload()
    }

    override fun reload() {
        viewModelScope.launch {
            if (!shouldError) {
                _data.value = ViewState.Success(profileQueryDataToProfileUiStateMapper.map(profileData))
            } else {
                _data.value = ViewState.Error
                shouldError = false
            }
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

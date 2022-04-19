package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.profile.ui.tab.ProfileUiState
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class ProfileViewModel : ViewModel() {
    sealed interface ViewState {
        data class Success(val profileUiState: ProfileUiState) : ViewState
        object Error : ViewState
        object Loading : ViewState
    }

    abstract val data: StateFlow<ViewState>
    abstract val dirty: MutableLiveData<Boolean>
    abstract val trustlyUrl: LiveEvent<String>

    abstract fun reload()
    abstract fun selectCashback(id: String)
    abstract fun saveInputs(emailInput: String, phoneNumberInput: String)
    abstract fun emailChanged(newEmail: String)
    abstract fun phoneNumberChanged(newPhoneNumber: String)
    abstract fun onLogout()

    protected val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    sealed class Event {
        object Logout : Event()
        data class Error(val message: String?) : Event()
    }
}

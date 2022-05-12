package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class ProfileViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val data: ProfileQuery.Data) : ViewState()
        object Error : ViewState()
        object Loading : ViewState()
    }

    protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
    val data = _data.asStateFlow()
    abstract val dirty: MutableLiveData<Boolean>
    abstract val trustlyUrl: LiveEvent<String>

    abstract fun load()
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

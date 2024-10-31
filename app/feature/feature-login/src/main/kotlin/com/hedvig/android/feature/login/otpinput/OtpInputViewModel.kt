package com.hedvig.android.feature.login.otpinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpInputViewModel(
  private val verifyUrl: String,
  private val resendUrl: String,
  credential: String,
  private val authTokenService: AuthTokenService,
  coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
) : ViewModel(coroutineScope) {
  private val _viewState = MutableStateFlow(ViewState.initial(credential = credential))
  val viewState = combine(
    _viewState,
    authTokenService.authStatus,
  ) { viewState, authStatus ->
    viewState.copy(
      navigateToLoginScreen = authStatus is AuthStatus.LoggedIn,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    _viewState.value,
  )

  private val _events = Channel<Event>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  fun setInput(value: String) {
    _viewState.update {
      it.copy(input = value, networkErrorMessage = null)
    }
  }

  fun submitCode(code: String) {
    _viewState.update {
      it.copy(loadingCode = true, networkErrorMessage = null)
    }

    viewModelScope.launch {
    }
  }

  fun resendCode() {
    _viewState.update {
      it.copy(networkErrorMessage = null, loadingResend = true)
    }
    viewModelScope.launch {}
  }

  fun dismissError() {
    _viewState.update {
      it.copy(networkErrorMessage = null)
    }
  }

  private suspend fun submitAuthCode(otpResult: Unit) {}

  private fun setErrorState(message: String) {
    _viewState.update {
      it.copy(networkErrorMessage = message, loadingCode = false)
    }
  }

  data class ViewState(
    val input: String = "",
    val credential: String,
    val networkErrorMessage: String?,
    val loadingResend: Boolean,
    val loadingCode: Boolean,
    val navigateToLoginScreen: Boolean,
  ) {
    companion object {
      fun initial(credential: String): ViewState = ViewState(
        credential = credential,
        input = "",
        networkErrorMessage = null,
        loadingResend = false,
        loadingCode = false,
        navigateToLoginScreen = false,
      )
    }
  }

  sealed class Event {
    object CodeResent : Event()
  }
}

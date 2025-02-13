package com.hedvig.android.feature.impersonation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.impersonation.ImpersonationReceiverViewModel.ViewState.Error
import com.hedvig.android.feature.impersonation.ImpersonationReceiverViewModel.ViewState.Loading
import com.hedvig.android.feature.impersonation.ImpersonationReceiverViewModel.ViewState.Success
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class ImpersonationReceiverActivity : ComponentActivity() {
  val hedvigDeepLinkContainer: HedvigDeepLinkContainer by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    loadKoinModules(module)

    val token = intent.data?.getQueryParameter("authorizationCode")
      ?: error("authorizationCode not found in query parameter")
    val viewModel = getViewModel<ImpersonationReceiverViewModel> { parametersOf(token) }

    viewModel
      .events
      .flowWithLifecycle(lifecycle)
      .onEach {
        startActivity(Intent(Intent.ACTION_VIEW, hedvigDeepLinkContainer.home.first().toUri()))
        finish()
      }
      .launchIn(lifecycleScope)

    setContent {
      val state by viewModel.state.collectAsStateWithLifecycle()

      HedvigTheme {
        Box(modifier = Modifier.fillMaxSize()) {
          HedvigText(
            text = when (val viewState = state) {
              is Error -> "Error for code [$token]: ${viewState.message}"
              Loading -> "Loading..."
              Success -> "Impersonation successful"
            },
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            style = HedvigTheme.typography.headlineMedium,
          )
        }
      }
    }
  }

  override fun onDestroy() {
    unloadKoinModules(module)
    super.onDestroy()
  }

  companion object {
    val module = module {
      viewModel { params ->
        com.hedvig.android.feature.impersonation.ImpersonationReceiverViewModel(
          params.get(),
          get(),
          get(),
        )
      }
    }
  }
}

class ImpersonationReceiverViewModel(
  exchangeToken: String,
  authTokenService: AuthTokenService,
  authRepository: AuthRepository,
) : ViewModel() {
  sealed class ViewState {
    object Loading : ViewState()

    object Success : ViewState()

    data class Error(val message: String?) : ViewState()
  }

  private val _state = MutableStateFlow<ViewState>(Loading)
  val state = _state.asStateFlow()

  object GoToLoggedInActivityEvent

  private val _events = Channel<GoToLoggedInActivityEvent>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch {
      when (val result = authRepository.exchange(AuthorizationCodeGrant(exchangeToken))) {
        is AuthTokenResult.Error -> _state.update { Error(result.toString()) }
        is AuthTokenResult.Success -> {
          authTokenService.loginWithTokens(result.accessToken, result.refreshToken)
          _state.update { Success }
          delay(500.milliseconds)
          _events.send(GoToLoggedInActivityEvent)
        }
      }
    }
  }
}

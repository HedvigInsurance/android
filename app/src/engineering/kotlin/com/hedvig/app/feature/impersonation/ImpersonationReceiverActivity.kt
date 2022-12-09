package com.hedvig.app.feature.impersonation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.NetworkAuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import kotlin.time.Duration.Companion.milliseconds

class ImpersonationReceiverActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    loadKoinModules(module)

    val viewModel = getViewModel<ImpersonationReceiverViewModel> {
      val token = intent.data?.getQueryParameter("token")?.split("=")?.get(1)
        ?: intent?.getStringExtra("token")?.split("=")?.get(1)
      parametersOf(token)
    }

    viewModel
      .events
      .flowWithLifecycle(lifecycle)
      .onEach {
        startActivity(LoggedInActivity.newInstance(this, withoutHistory = true))
        finish()
      }
      .launchIn(lifecycleScope)

    setContent {
      val state by viewModel.state.collectAsState()

      HedvigTheme {
        Box(modifier = Modifier.fillMaxSize()) {
          Text(
            text = when (val viewState = state) {
              is ImpersonationReceiverViewModel.ViewState.Error -> "Error: ${viewState.message}"
              ImpersonationReceiverViewModel.ViewState.Loading -> "Loading..."
              ImpersonationReceiverViewModel.ViewState.Success -> "Impersonation successful"
            },
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4,
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
        ImpersonationReceiverViewModel(params.get(), get(), get(), get())
      }
    }
  }
}

class ImpersonationReceiverViewModel(
  exchangeToken: String,
  authTokenService: AuthTokenService,
  networkAuthRepository: NetworkAuthRepository,
  featureManager: FeatureManager,
) : ViewModel() {
  sealed class ViewState {
    object Loading : ViewState()
    object Success : ViewState()
    data class Error(val message: String?) : ViewState()
  }

  private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
  val state = _state.asStateFlow()

  object GoToLoggedInActivityEvent

  private val _events = Channel<GoToLoggedInActivityEvent>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch {
      when (val result = networkAuthRepository.exchange(AuthorizationCodeGrant(exchangeToken))) {
        is AuthTokenResult.Error -> _state.update { ViewState.Error(result.message) }
        is AuthTokenResult.Success -> {
          authTokenService.updateTokens(result.accessToken, result.refreshToken)
          featureManager.invalidateExperiments()
          _state.update { ViewState.Success }
          delay(500.milliseconds)
          _events.send(GoToLoggedInActivityEvent)
        }
      }
    }
  }
}

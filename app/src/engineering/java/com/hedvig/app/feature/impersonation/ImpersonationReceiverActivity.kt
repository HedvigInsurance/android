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
import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.owldroid.graphql.ExchangeTokenMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.featureflags.FeatureManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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
        ImpersonationReceiverViewModel(params.get(), get(), get(), get(), get())
      }
    }
  }
}

class ImpersonationReceiverViewModel(
  exchangeToken: String,
  apolloClient: ApolloClient,
  authenticationTokenService: AuthenticationTokenService,
  loginStatusService: LoginStatusService,
  featureManager: FeatureManager,
) : ViewModel() {
  sealed class ViewState {
    object Loading : ViewState()
    object Success : ViewState()
    data class Error(val message: String?) : ViewState()
  }

  private val _state = MutableStateFlow<ViewState>(ViewState.Loading)
  val state = _state.asStateFlow()

  object Event

  private val _events = Channel<Event>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  init {
    viewModelScope.launch {
      if (authenticationTokenService.authenticationToken == null) {
        authenticationTokenService.authenticationToken = "123"
      }
      when (
        val result = apolloClient
          .mutation(ExchangeTokenMutation(exchangeToken))
          .safeQuery()
          .toEither()
      ) {
        is Either.Left -> {
          _state.value = ViewState.Error(result.value.message)
        }
        is Either.Right -> {
          val newToken = result.value.exchangeToken.asExchangeTokenSuccessResponse?.token
          if (newToken == null) {
            _state.value = ViewState.Error("Did not receive token")
            return@launch
          }
          authenticationTokenService.authenticationToken = newToken
          loginStatusService.isLoggedIn = true
          featureManager.invalidateExperiments()
          _state.value = ViewState.Success
          delay(500.milliseconds)
          _events.send(Event)
        }
      }
    }
  }
}

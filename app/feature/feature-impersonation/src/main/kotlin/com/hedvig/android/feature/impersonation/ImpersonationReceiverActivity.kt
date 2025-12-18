package com.hedvig.android.feature.impersonation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    ComposeFoundationFlags.isNewContextMenuEnabled = false
    super.onCreate(savedInstanceState)
    loadKoinModules(module)

    val token = intent.data?.getQueryParameter("authorizationCode")
      ?: error("authorizationCode not found in query parameter")
    val viewModel = getViewModel<ImpersonationReceiverViewModel> { parametersOf(token) }

    setContent {
      val state by viewModel.uiState.collectAsStateWithLifecycle()

      LaunchedEffect(state.navigateToHome) {
        if (state.navigateToHome) {
          startActivity(Intent(Intent.ACTION_VIEW, hedvigDeepLinkContainer.home.first().toUri()))
          finish()
        }
      }

      HedvigTheme {
        Box(modifier = Modifier.fillMaxSize()) {
          HedvigText(
            text = when {
              state.errorMessage != null -> "Error for code [$token]: ${state.errorMessage}"
              state.isSuccess -> "Impersonation successful"
              else -> "Loading..."
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
        ImpersonationReceiverViewModel(
          params.get(),
          get(),
          get(),
        )
      }
    }
  }
}

internal class ImpersonationReceiverViewModel(
  exchangeToken: String,
  authTokenService: AuthTokenService,
  authRepository: AuthRepository,
) : MoleculeViewModel<ImpersonationEvent, ImpersonationUiState>(
    initialState = ImpersonationUiState(),
    presenter = ImpersonationPresenter(
      exchangeToken = exchangeToken,
      exchange = authRepository::exchange,
      loginWithTokens = authTokenService::loginWithTokens,
    ),
  )

internal class ImpersonationPresenter(
  private val exchangeToken: String,
  private val exchange: suspend (AuthorizationCodeGrant) -> AuthTokenResult,
  private val loginWithTokens: suspend (
    accessToken: com.hedvig.authlib.AccessToken,
    refreshToken: com.hedvig.authlib.RefreshToken,
  ) -> Unit,
) : MoleculePresenter<ImpersonationEvent, ImpersonationUiState> {
  @Composable
  override fun MoleculePresenterScope<ImpersonationEvent>.present(
    lastState: ImpersonationUiState,
  ): ImpersonationUiState {
    var uiState by remember { mutableStateOf(lastState) }

    // Auto-start the exchange on first launch
    LaunchedEffect(Unit) {
      if (lastState.isSuccess || lastState.navigateToHome) {
        // State preserved from previous composition
        return@LaunchedEffect
      }

      when (val result = exchange(AuthorizationCodeGrant(exchangeToken))) {
        is AuthTokenResult.Error -> {
          uiState = uiState.copy(errorMessage = result.toString())
        }
        is AuthTokenResult.Success -> {
          loginWithTokens(result.accessToken, result.refreshToken)
          uiState = uiState.copy(isSuccess = true)
          delay(500.milliseconds)
          uiState = uiState.copy(navigateToHome = true)
        }
      }
    }

    return uiState
  }
}

internal sealed interface ImpersonationEvent

internal data class ImpersonationUiState(
  val isSuccess: Boolean = false,
  val errorMessage: String? = null,
  val navigateToHome: Boolean = false,
)

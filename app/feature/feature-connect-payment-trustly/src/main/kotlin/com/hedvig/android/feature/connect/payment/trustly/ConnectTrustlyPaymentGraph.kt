package com.hedvig.android.feature.connect.payment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.connectTrustlyPaymentGraph() {
  composable<AppDestination.ConnectPaymentTrustly>() {
    val viewModel: TrustlyViewModel = koinViewModel()
    TrustlyDestination(viewModel)
  }
}

@Composable
internal fun TrustlyDestination(viewModel: TrustlyViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TrustlyScreen(uiState)
}

@Composable
private fun TrustlyScreen(uiState: TrustlyUiState) {
}

internal class TrustlyViewModel : MoleculeViewModel<TrustlyEvent, TrustlyUiState>(
  TrustlyUiState,
  TrustlyPresenter(),
)

internal class TrustlyPresenter : MoleculePresenter<TrustlyEvent, TrustlyUiState> {
  @Composable
  override fun MoleculePresenterScope<TrustlyEvent>.present(lastState: TrustlyUiState): TrustlyUiState {
    return TrustlyUiState
  }
}

internal object TrustlyEvent
internal object TrustlyUiState

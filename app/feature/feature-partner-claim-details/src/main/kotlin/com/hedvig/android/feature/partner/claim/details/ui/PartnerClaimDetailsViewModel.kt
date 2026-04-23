package com.hedvig.android.feature.partner.claim.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.merge
import arrow.core.raise.either
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.feature.partner.claim.details.data.GetPartnerClaimDetailUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlinx.coroutines.flow.collectLatest
import octopus.type.ClaimStatus

internal class PartnerClaimDetailsViewModel(
  claimId: String,
  getPartnerClaimDetailUseCase: GetPartnerClaimDetailUseCase,
) : MoleculeViewModel<PartnerClaimDetailEvent, PartnerClaimDetailUiState>(
    PartnerClaimDetailUiState.Loading,
    PartnerClaimDetailPresenter(claimId, getPartnerClaimDetailUseCase),
  )

private class PartnerClaimDetailPresenter(
  private val claimId: String,
  private val getPartnerClaimDetailUseCase: GetPartnerClaimDetailUseCase,
) : MoleculePresenter<PartnerClaimDetailEvent, PartnerClaimDetailUiState> {
  @Composable
  override fun MoleculePresenterScope<PartnerClaimDetailEvent>.present(
    lastState: PartnerClaimDetailUiState,
  ): PartnerClaimDetailUiState {
    var state by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    CollectEvents { event ->
      when (event) {
        PartnerClaimDetailEvent.Retry -> loadIteration++
      }
    }
    LaunchedEffect(loadIteration) {
      getPartnerClaimDetailUseCase.invoke(claimId).collectLatest { result ->
        state = either<PartnerClaimDetailUiState, PartnerClaimDetailUiState> {
          result.mapLeft { PartnerClaimDetailUiState.Error }.bind()
        }.merge()
      }
    }
    return state
  }
}

internal sealed interface PartnerClaimDetailEvent {
  data object Retry : PartnerClaimDetailEvent
}

internal sealed interface PartnerClaimDetailUiState {
  data object Loading : PartnerClaimDetailUiState

  data object Error : PartnerClaimDetailUiState

  data class Content(
    val claimStatusCardUiState: ClaimStatusCardUiState,
    val claimStatus: ClaimStatus?,
    val displayItems: List<DisplayItem>,
    val termsConditionsUrl: String?,
  ) : PartnerClaimDetailUiState
}

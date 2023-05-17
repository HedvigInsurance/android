package com.hedvig.app.feature.insurance.ui.detail.coverage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.app.feature.perils.Peril
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

internal class CoverageViewModel(
  contractId: String,
  getContractCoverageUseCase: GetContractCoverageUseCase,
) : ViewModel() {

  private val retryChannel = RetryChannel()

  val uiState: StateFlow<CoverageUiState> = retryChannel.mapLatest {
    getContractCoverageUseCase.invoke(contractId).fold(
      ifLeft = { CoverageUiState.Error },
      ifRight = { contractCoverage ->
        CoverageUiState.Success(
          contractCoverage.contractDisplayName,
          contractCoverage.contractPerils,
          contractCoverage.insurableLimits,
        )
      },
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    CoverageUiState.Loading,
  )

  fun reload() {
    retryChannel.retry()
  }
}

internal sealed interface CoverageUiState {
  object Loading : CoverageUiState
  object Error : CoverageUiState
  data class Success(
    val contractDisplayName: String,
    val perilItems: ImmutableList<Peril>,
    val insurableLimitItems: ImmutableList<ContractCoverage.InsurableLimit>,
  ) : CoverageUiState
}

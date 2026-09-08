package com.hedvig.android.feature.insurances.insurance.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract.EstablishedInsuranceContract
import com.hedvig.android.feature.insurances.data.InsuranceContract.PendingInsuranceContract
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

internal sealed interface InsuranceScreenEvent {
  data object RetryLoading : InsuranceScreenEvent
}

internal data class InsuranceUiState(
  val contracts: List<EstablishedInsuranceContract>,
  val pendingContracts: List<PendingInsuranceContract>,
  val crossSells: List<CrossSell>,
  val addonBannerInfoList: List<AddonBannerInfo>,
  val quantityOfCancelledInsurances: Int,
  val shouldSuggestMovingFlow: Boolean,
  val hasError: Boolean,
  val isLoading: Boolean,
  val isRetrying: Boolean,
) {
  companion object {
    val initialState = InsuranceUiState(
      contracts = listOf(),
      pendingContracts = listOf(),
      crossSells = listOf(),
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = false,
      hasError = false,
      isLoading = true,
      isRetrying = false,
      addonBannerInfoList = emptyList(),
    )
  }
}

internal class InsurancePresenter(
  private val getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  private val getCrossSellsUseCase: GetCrossSellsUseCase,
  private val getAddonBannerInfoUseCase: GetAddonBannerInfoUseCase,
) : MoleculePresenter<InsuranceScreenEvent, InsuranceUiState> {
  @Composable
  override fun MoleculePresenterScope<InsuranceScreenEvent>.present(lastState: InsuranceUiState): InsuranceUiState {
    var insuranceData by remember {
      mutableStateOf<InsuranceData>(
        InsuranceData.fromUiState(lastState),
      )
    }
    var isLoading by remember { mutableStateOf(lastState.isLoading) }
    var isRetrying by remember { mutableStateOf(false) }
    var didFailToLoad by remember { mutableStateOf(false) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        InsuranceScreenEvent.RetryLoading -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      val isRetryingIteration = loadIteration != 0
      Snapshot.withMutableSnapshot {
        didFailToLoad = false
        isRetrying = isRetryingIteration
      }
      loadInsuranceData(
        getInsuranceContractsUseCase = getInsuranceContractsUseCase,
        getCrossSellsUseCase = getCrossSellsUseCase,
        getAddonBannerInfoUseCase = getAddonBannerInfoUseCase,
      ).collectLatest { result ->
        result.fold(
          ifLeft = {
            Snapshot.withMutableSnapshot {
              isLoading = false
              isRetrying = false
              didFailToLoad = true
              insuranceData = InsuranceData.Empty
            }
          },
          ifRight = { insuranceDataResult ->
            Snapshot.withMutableSnapshot {
              isLoading = false
              isRetrying = false
              didFailToLoad = false
              insuranceData = insuranceDataResult
            }
          },
        )
      }
    }

    return InsuranceUiState(
      contracts = insuranceData.contracts,
      pendingContracts = insuranceData.pendingContracts,
      crossSells = insuranceData.crossSells,
      quantityOfCancelledInsurances = insuranceData.quantityOfCancelledInsurances,
      shouldSuggestMovingFlow = insuranceData.isEligibleToPerformMovingFlow,
      hasError = didFailToLoad && !isLoading && !isRetrying,
      isLoading = isLoading,
      isRetrying = isRetrying,
      addonBannerInfoList = insuranceData.addonBannerInfoList,
    )
  }
}

private fun loadInsuranceData(
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
  getAddonBannerInfoUseCase: GetAddonBannerInfoUseCase,
): Flow<Either<ErrorMessage, InsuranceData>> {
  return combine(
    getInsuranceContractsUseCase.invoke(),
    flow { emit(getCrossSellsUseCase.invoke()) },
    getAddonBannerInfoUseCase.invoke(AddonBannerSource.INSURANCES_TAB),
  ) { contractsResult, crossSellsDataResult, travelAddonBannerInfoResult ->
    either {
      val result = contractsResult.bind()
      val contracts = result.filterIsInstance<EstablishedInsuranceContract>()
      val pendingContracts = result.filterIsInstance<PendingInsuranceContract>()
      val crossSells = crossSellsDataResult.bind()
      val travelAddonBannerInfo = travelAddonBannerInfoResult.bind()
      val insuranceCards = contracts.filterNot(EstablishedInsuranceContract::isTerminated)

      InsuranceData(
        contracts = insuranceCards,
        pendingContracts = pendingContracts,
        crossSells = crossSells,
        quantityOfCancelledInsurances = contracts.count(EstablishedInsuranceContract::isTerminated),
        isEligibleToPerformMovingFlow = contracts.any {
          !it.isTerminated && it.upcomingInsuranceAgreement == null && it.supportsAddressChange
        },
        addonBannerInfoList = travelAddonBannerInfo,
      )
    }.onLeft {
      logcat(LogPriority.INFO, it.throwable) {
        "Insurance items failed to load: ${it.message}"
      }
    }
  }
}

private data class InsuranceData(
  val contracts: List<EstablishedInsuranceContract>,
  val pendingContracts: List<PendingInsuranceContract>,
  val crossSells: List<CrossSell>,
  val quantityOfCancelledInsurances: Int,
  val isEligibleToPerformMovingFlow: Boolean,
  val addonBannerInfoList: List<AddonBannerInfo>,
) {
  companion object {
    fun fromUiState(uiState: InsuranceUiState): InsuranceData {
      return InsuranceData(
        contracts = uiState.contracts,
        crossSells = uiState.crossSells,
        quantityOfCancelledInsurances = uiState.quantityOfCancelledInsurances,
        isEligibleToPerformMovingFlow = uiState.shouldSuggestMovingFlow,
        addonBannerInfoList = uiState.addonBannerInfoList,
        pendingContracts = uiState.pendingContracts,
      )
    }

    val Empty: InsuranceData = InsuranceData(
      contracts = listOf(),
      pendingContracts = listOf(),
      crossSells = listOf(),
      quantityOfCancelledInsurances = 0,
      isEligibleToPerformMovingFlow = false,
      addonBannerInfoList = emptyList(),
    )
  }
}

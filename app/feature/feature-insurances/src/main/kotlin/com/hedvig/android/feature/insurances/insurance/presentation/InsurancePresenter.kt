package com.hedvig.android.feature.insurances.insurance.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract.EstablishedInsuranceContract
import com.hedvig.android.feature.insurances.data.InsuranceContract.PendingInsuranceContract
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import octopus.type.CrossSellType

internal sealed interface InsuranceScreenEvent {
  data object RetryLoading : InsuranceScreenEvent

  data object MarkCardCrossSellsAsSeen : InsuranceScreenEvent
}

internal data class InsuranceUiState(
  val contracts: List<EstablishedInsuranceContract>,
  val pendingContracts: List<PendingInsuranceContract>,
  val crossSells: List<CrossSell>,
  val travelAddonBannerInfo: TravelAddonBannerInfo?,
  val showNotificationBadge: Boolean,
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
      showNotificationBadge = false,
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = false,
      hasError = false,
      isLoading = true,
      isRetrying = false,
      travelAddonBannerInfo = null,
    )
  }
}

internal class InsurancePresenter(
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
  private val getCrossSellsUseCaseProvider: Provider<GetCrossSellsUseCase>,
  private val crossSellCardNotificationBadgeServiceProvider: Provider<CrossSellCardNotificationBadgeService>,
  private val applicationScope: CoroutineScope,
  private val getTravelAddonBannerInfoUseCase: Provider<GetTravelAddonBannerInfoUseCase>,
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

    val showNotificationBadge by produceState(lastState.showNotificationBadge) {
      crossSellCardNotificationBadgeServiceProvider
        .provide()
        .showNotification()
        .collectLatest {
          value = it
        }
    }

    CollectEvents { event ->
      when (event) {
        InsuranceScreenEvent.RetryLoading -> loadIteration++
        InsuranceScreenEvent.MarkCardCrossSellsAsSeen -> {
          applicationScope.launch { crossSellCardNotificationBadgeServiceProvider.provide().markAsSeen() }
        }
      }
    }

    LaunchedEffect(loadIteration) {
      val isRetryingIteration = loadIteration != 0
      Snapshot.withMutableSnapshot {
        didFailToLoad = false
        isRetrying = isRetryingIteration
      }
      loadInsuranceData(
        getInsuranceContractsUseCase = getInsuranceContractsUseCaseProvider.provide(),
        getCrossSellsUseCase = getCrossSellsUseCaseProvider.provide(),
        getTravelAddonBannerInfoUseCase = getTravelAddonBannerInfoUseCase.provide(),
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
      showNotificationBadge = showNotificationBadge,
      quantityOfCancelledInsurances = insuranceData.quantityOfCancelledInsurances,
      shouldSuggestMovingFlow = insuranceData.isEligibleToPerformMovingFlow,
      hasError = didFailToLoad && !isLoading && !isRetrying,
      isLoading = isLoading,
      isRetrying = isRetrying,
      travelAddonBannerInfo = insuranceData.travelAddonBannerInfo,
    )
  }
}

private fun loadInsuranceData(
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
  getTravelAddonBannerInfoUseCase: GetTravelAddonBannerInfoUseCase,
): Flow<Either<ErrorMessage, InsuranceData>> {
  return combine(
    getInsuranceContractsUseCase.invoke(),
    flow { emit(getCrossSellsUseCase.invoke()) },
    getTravelAddonBannerInfoUseCase.invoke(TravelAddonBannerSource.INSURANCES_TAB),
  ) { contractsResult, crossSellsDataResult, travelAddonBannerInfoResult ->
    either {
      val result = contractsResult.bind()
      val contracts = result.filterIsInstance<EstablishedInsuranceContract>()
      val pendingContracts = result.filterIsInstance<PendingInsuranceContract>()
      val crossSellsData = crossSellsDataResult.bind()
      val travelAddonBannerInfo = travelAddonBannerInfoResult.bind()
      val insuranceCards = contracts.filterNot(EstablishedInsuranceContract::isTerminated)

      val crossSells = crossSellsData.map { crossSell ->
        CrossSell(
          id = crossSell.id,
          title = crossSell.title,
          subtitle = crossSell.description,
          storeUrl = crossSell.storeUrl,
          type = when (crossSell.type) {
            CrossSellType.CAR -> CrossSell.CrossSellType.CAR
            CrossSellType.HOME -> CrossSell.CrossSellType.HOME
            CrossSellType.ACCIDENT -> CrossSell.CrossSellType.ACCIDENT
            CrossSellType.PET -> CrossSell.CrossSellType.PET
            CrossSellType.UNKNOWN__ -> CrossSell.CrossSellType.UNKNOWN
          },
        )
      }
      InsuranceData(
        contracts = insuranceCards,
        pendingContracts = pendingContracts,
        crossSells = crossSells,
        quantityOfCancelledInsurances = contracts.count(EstablishedInsuranceContract::isTerminated),
        isEligibleToPerformMovingFlow = contracts.any {
          !it.isTerminated && it.upcomingInsuranceAgreement == null && it.supportsAddressChange
        },
        travelAddonBannerInfo = travelAddonBannerInfo,
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
  val travelAddonBannerInfo: TravelAddonBannerInfo?,
) {
  companion object {
    fun fromUiState(uiState: InsuranceUiState): InsuranceData {
      return InsuranceData(
        contracts = uiState.contracts,
        crossSells = uiState.crossSells,
        quantityOfCancelledInsurances = uiState.quantityOfCancelledInsurances,
        isEligibleToPerformMovingFlow = uiState.shouldSuggestMovingFlow,
        travelAddonBannerInfo = uiState.travelAddonBannerInfo,
        pendingContracts = uiState.pendingContracts,
      )
    }

    val Empty: InsuranceData = InsuranceData(
      contracts = listOf(),
      pendingContracts = listOf(),
      crossSells = listOf(),
      quantityOfCancelledInsurances = 0,
      isEligibleToPerformMovingFlow = false,
      travelAddonBannerInfo = null,
    )
  }
}

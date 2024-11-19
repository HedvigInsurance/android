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
import arrow.fx.coroutines.parZip
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import octopus.CrossSellsQuery
import octopus.type.CrossSellType

internal sealed interface InsuranceScreenEvent {
  data object RetryLoading : InsuranceScreenEvent

  data object MarkCardCrossSellsAsSeen : InsuranceScreenEvent
}

internal data class InsuranceUiState(
  val contracts: List<InsuranceContract>,
  val crossSells: List<CrossSell>,
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
      crossSells = listOf(),
      showNotificationBadge = false,
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = false,
      hasError = false,
      isLoading = true,
      isRetrying = false,
    )
  }
}

internal class InsurancePresenter(
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
  private val getCrossSellsUseCaseProvider: Provider<GetCrossSellsUseCase>,
  private val crossSellCardNotificationBadgeServiceProvider: Provider<CrossSellCardNotificationBadgeService>,
  private val applicationScope: CoroutineScope,
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
        forceNetworkFetch = true,
      ).fold(
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

    return InsuranceUiState(
      contracts = insuranceData.contracts,
      crossSells = insuranceData.crossSells,
      showNotificationBadge = showNotificationBadge,
      quantityOfCancelledInsurances = insuranceData.quantityOfCancelledInsurances,
      shouldSuggestMovingFlow = insuranceData.isEligibleToPerformMovingFlow,
      hasError = didFailToLoad && !isLoading && !isRetrying,
      isLoading = isLoading,
      isRetrying = isRetrying,
    )
  }
}

private suspend fun loadInsuranceData(
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
  forceNetworkFetch: Boolean,
): Either<ErrorMessage, InsuranceData> {
  return either {
    parZip(
      { getInsuranceContractsUseCase.invoke(forceNetworkFetch).first().bind() },
      { getCrossSellsUseCase.invoke().bind() },
    ) {
      contracts: List<InsuranceContract>,
      crossSellsData: List<CrossSellsQuery.Data.CurrentMember.CrossSell>,
      ->
      val insuranceCards = contracts
        .filterNot(InsuranceContract::isTerminated)

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
        crossSells = crossSells,
        quantityOfCancelledInsurances = contracts.count(InsuranceContract::isTerminated),
        isEligibleToPerformMovingFlow = contracts.any {
          !it.isTerminated && it.upcomingInsuranceAgreement == null && it.supportsAddressChange
        },
      )
    }
  }.onLeft {
    logcat(LogPriority.INFO, it.throwable) {
      "Insurance items failed to load: ${it.message}"
    }
  }
}

private data class InsuranceData(
  val contracts: List<InsuranceContract>,
  val crossSells: List<CrossSell>,
  val quantityOfCancelledInsurances: Int,
  val isEligibleToPerformMovingFlow: Boolean,
) {
  companion object {
    fun fromUiState(uiState: InsuranceUiState): InsuranceData {
      return InsuranceData(
        contracts = uiState.contracts,
        crossSells = uiState.crossSells,
        quantityOfCancelledInsurances = uiState.quantityOfCancelledInsurances,
        isEligibleToPerformMovingFlow = uiState.shouldSuggestMovingFlow,
      )
    }

    val Empty: InsuranceData = InsuranceData(
      contracts = listOf(),
      crossSells = listOf(),
      quantityOfCancelledInsurances = 0,
      isEligibleToPerformMovingFlow = false,
    )
  }
}

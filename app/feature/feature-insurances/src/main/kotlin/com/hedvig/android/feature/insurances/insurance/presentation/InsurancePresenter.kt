package com.hedvig.android.feature.insurances.insurance.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.ui.insurance.ContractType
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.data.toContractType
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import octopus.CrossSalesQuery
import octopus.type.CrossSellType

internal sealed interface InsuranceScreenEvent {
  data object RetryLoading : InsuranceScreenEvent
  data object MarkCardCrossSellsAsSeen : InsuranceScreenEvent
}

internal data class InsuranceUiState(
  val insuranceCards: ImmutableList<InsuranceCard>,
  val crossSells: ImmutableList<CrossSell>,
  val showNotificationBadge: Boolean,
  val quantityOfCancelledInsurances: Int,
  val hasError: Boolean,
  val isLoading: Boolean,
  val isRetrying: Boolean,
) {
  data class InsuranceCard(
    val contractId: String,
    val backgroundImageUrl: String?,
    val chips: ImmutableList<String>,
    val title: String,
    val subtitle: String,
    val contractType: ContractType,
  )

  data class CrossSell(
    val id: String,
    val title: String,
    val subtitle: String,
    val storeUrl: String,
    val type: CrossSellType,
  ) {
    enum class CrossSellType {
      PET, HOME, ACCIDENT, CAR, UNKNOWN
    }
  }

  companion object {
    val initialState = InsuranceUiState(
      insuranceCards = persistentListOf(),
      crossSells = persistentListOf(),
      showNotificationBadge = false,
      quantityOfCancelledInsurances = 0,
      hasError = false,
      isLoading = true,
      isRetrying = false,
    )
  }
}

internal class InsurancePresenter(
  private val getInsuranceContractsUseCaseProvider: Provider<GetInsuranceContractsUseCase>,
  private val getCrossSellsUseCaseProvider: Provider<GetCrossSellsUseCase>,
  private val crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
) : MoleculePresenter<InsuranceScreenEvent, InsuranceUiState> {
  @Composable
  override fun MoleculePresenterScope<InsuranceScreenEvent>.present(
    lastState: InsuranceUiState,
  ): InsuranceUiState {
    var insuranceData by remember {
      mutableStateOf<InsuranceData>(
        InsuranceData.fromUiState(lastState),
      )
    }
    var isLoading by remember { mutableStateOf(lastState.isLoading) }
    var isRetrying by remember { mutableStateOf(false) }
    var didFailToLoad by remember { mutableStateOf(false) }
    var loadIteration by remember { mutableIntStateOf(0) }

    val showNotificationBadge by crossSellCardNotificationBadgeService
      .showNotification()
      .collectAsState(lastState.showNotificationBadge)

    CollectEvents { event ->
      when (event) {
        InsuranceScreenEvent.RetryLoading -> loadIteration++
        InsuranceScreenEvent.MarkCardCrossSellsAsSeen -> {
          launch { crossSellCardNotificationBadgeService.markAsSeen() }
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
        getInsuranceContractsUseCaseProvider.provide(),
        getCrossSellsUseCaseProvider.provide(),
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
      insuranceCards = insuranceData.insuranceCards,
      crossSells = insuranceData.crossSells,
      showNotificationBadge = showNotificationBadge,
      quantityOfCancelledInsurances = insuranceData.quantityOfCancelledInsurances,
      hasError = didFailToLoad && !isLoading && !isRetrying,
      isLoading = isLoading,
      isRetrying = isRetrying,
    )
  }
}

private suspend fun loadInsuranceData(
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
): Either<ErrorMessage, InsuranceData> {
  return either {
    parZip(
      { getInsuranceContractsUseCase.invoke().bind() },
      { getCrossSellsUseCase.invoke().bind() },
    ) {
        contracts: List<InsuranceContract>,
        crossSellsData: List<CrossSalesQuery.Data.CurrentMember.CrossSell>,
      ->
      val insuranceCards = contracts
        .filterNot(InsuranceContract::isTerminated)
        .map { contract ->
          InsuranceUiState.InsuranceCard(
            contractId = contract.id,
            backgroundImageUrl = null, // Fill when we get image from backend
            chips = contract.statusPills.toPersistentList(),
            title = contract.displayName,
            subtitle = contract.detailPills.joinToString(" ∙ "),
            contractType = contract.typeOfContract.toContractType(),
          )
        }.toPersistentList()
      val crossSells = crossSellsData.map { crossSell ->
        InsuranceUiState.CrossSell(
          id = crossSell.id,
          title = crossSell.title,
          subtitle = crossSell.description,
          storeUrl = crossSell.storeUrl,
          type = when (crossSell.type) {
            CrossSellType.CAR -> InsuranceUiState.CrossSell.CrossSellType.CAR
            CrossSellType.HOME -> InsuranceUiState.CrossSell.CrossSellType.HOME
            CrossSellType.ACCIDENT -> InsuranceUiState.CrossSell.CrossSellType.ACCIDENT
            CrossSellType.PET -> InsuranceUiState.CrossSell.CrossSellType.PET
            CrossSellType.UNKNOWN__ -> InsuranceUiState.CrossSell.CrossSellType.UNKNOWN
            null -> InsuranceUiState.CrossSell.CrossSellType.UNKNOWN
          },
        )
      }.toPersistentList()
      InsuranceData(
        insuranceCards = insuranceCards,
        crossSells = crossSells,
        quantityOfCancelledInsurances = contracts.count(InsuranceContract::isTerminated),
      )
    }
  }.onLeft {
    logcat(LogPriority.INFO, it.throwable) {
      "Insurance items failed to load: ${it.message}"
    }
  }
}

private data class InsuranceData(
  val insuranceCards: ImmutableList<InsuranceUiState.InsuranceCard>,
  val crossSells: ImmutableList<InsuranceUiState.CrossSell>,
  val quantityOfCancelledInsurances: Int,
) {
  companion object {
    fun fromUiState(uiState: InsuranceUiState): InsuranceData {
      return InsuranceData(
        insuranceCards = uiState.insuranceCards,
        crossSells = uiState.crossSells,
        quantityOfCancelledInsurances = uiState.quantityOfCancelledInsurances,
      )
    }

    val Empty: InsuranceData = InsuranceData(
      insuranceCards = persistentListOf(),
      crossSells = persistentListOf(),
      quantityOfCancelledInsurances = 0,
    )
  }
}

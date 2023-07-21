package com.hedvig.android.feature.insurances

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.core.common.android.i
import com.hedvig.android.core.ui.MoleculeViewModel
import com.hedvig.android.feature.insurances.data.GetCrossSellsUseCase
import com.hedvig.android.feature.insurances.data.GetInsuranceContractsUseCase
import com.hedvig.android.feature.insurances.data.isTerminated
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import giraffe.InsuranceContractsQuery
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import octopus.CrossSalesQuery

internal class InsuranceViewModel(
  private val getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  private val getCrossSellsUseCase: GetCrossSellsUseCase,
  private val crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
) : MoleculeViewModel<InsuranceScreenEvent, InsuranceUiState>() {

  override val initialValue: InsuranceUiState
    get() = InsuranceUiState(
      insuranceCards = persistentListOf(),
      crossSells = persistentListOf(),
      showNotificationBadge = false,
      quantityOfCancelledInsurances = 0,
      hasError = false,
      loading = true,
    )

  @Composable
  override fun models(events: Flow<InsuranceScreenEvent>): InsuranceUiState {
    return InsuranceScreenPresenter(
      events = events,
      getInsuranceContractsUseCase = getInsuranceContractsUseCase,
      getCrossSellsUseCase = getCrossSellsUseCase,
      crossSellCardNotificationBadgeService = crossSellCardNotificationBadgeService,
    )
  }
}

@SuppressLint("ComposableNaming")
@Composable
internal fun InsuranceScreenPresenter(
  events: Flow<InsuranceScreenEvent>,
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
  crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
): InsuranceUiState {
  var insuranceData by remember { mutableStateOf<InsuranceData?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  val retryChannel = remember { RetryChannel() }

  LaunchedEffect(Unit) {
    events.collect { event: InsuranceScreenEvent ->
      when (event) {
        InsuranceScreenEvent.RetryLoading -> {
          retryChannel.retry()
        }
        InsuranceScreenEvent.MarkCardCrossSellsAsSeen -> {
          crossSellCardNotificationBadgeService.markAsSeen()
        }
      }
    }
  }
  LaunchedEffect(Unit) {
    retryChannel.mapLatest {
      isLoading = true
      val insuranceDataResult = loadInsuranceData(
        getInsuranceContractsUseCase,
        getCrossSellsUseCase,
        crossSellCardNotificationBadgeService,
      )
      Snapshot.withMutableSnapshot {
        isLoading = false
        insuranceData = insuranceDataResult
      }
    }.collect()
  }

  return InsuranceUiState(
    insuranceCards = insuranceData?.insuranceCards ?: persistentListOf(),
    crossSells = insuranceData?.crossSells ?: persistentListOf(),
    showNotificationBadge = insuranceData?.showNotificationBadge ?: false,
    quantityOfCancelledInsurances = insuranceData?.quantityOfCancelledInsurances ?: 0,
    hasError = insuranceData == null && isLoading == false,
    loading = isLoading,
  )
}

internal sealed interface InsuranceScreenEvent {
  object RetryLoading : InsuranceScreenEvent
  object MarkCardCrossSellsAsSeen : InsuranceScreenEvent
}

internal data class InsuranceUiState(
  val insuranceCards: ImmutableList<InsuranceCard>,
  val crossSells: ImmutableList<CrossSell>,
  val showNotificationBadge: Boolean,
  val quantityOfCancelledInsurances: Int,
  val hasError: Boolean = false,
  val loading: Boolean = false,
) {
  data class InsuranceCard(
    val contractId: String,
    val backgroundImageUrl: String?,
    val chips: ImmutableList<String>,
    val title: String,
    val subtitle: String,
  )

  data class CrossSell(
    val title: String,
    val subtitle: String,
    val uri: Uri,
  )

  companion object {
    val Loading = InsuranceUiState(
      insuranceCards = persistentListOf(),
      crossSells = persistentListOf(),
      showNotificationBadge = false,
      quantityOfCancelledInsurances = 0,
      hasError = false,
      loading = true,
    )
  }
}

private suspend fun loadInsuranceData(
  getInsuranceContractsUseCase: GetInsuranceContractsUseCase,
  getCrossSellsUseCase: GetCrossSellsUseCase,
  crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
): InsuranceData? {
  return either {
    parZip(
      { getInsuranceContractsUseCase.invoke().bind() },
      { getCrossSellsUseCase.invoke().bind() },
      { crossSellCardNotificationBadgeService.showNotification().first() },
    ) {
        contracts: List<InsuranceContractsQuery.Contract>,
        crossSellsData: List<CrossSalesQuery.Data.CurrentMember.CrossSell>,
        showNotificationBadge: Boolean,
      ->
      val insuranceCards = contracts
        .filterNot(InsuranceContractsQuery.Contract::isTerminated)
        .map { contract ->
          InsuranceUiState.InsuranceCard(
            contractId = contract.id,
            backgroundImageUrl = null, // Fill when we get image from backend
            chips = contract.statusPills.toPersistentList(),
            title = contract.displayName,
            subtitle = contract.detailPills.joinToString(" ∙ "),
          )
        }.toPersistentList()
      val crossSells = crossSellsData.map { crossSell ->
        InsuranceUiState.CrossSell(
          title = crossSell.title,
          subtitle = crossSell.description,
          uri = Uri.parse(crossSell.storeUrl),
        )
      }.toPersistentList()
      InsuranceData(
        insuranceCards = insuranceCards,
        crossSells = crossSells,
        showNotificationBadge = showNotificationBadge,
        quantityOfCancelledInsurances = contracts.count(InsuranceContractsQuery.Contract::isTerminated),
      )
    }
  }.fold(
    ifLeft = {
      i(it.throwable) { "Insurance items failed to load: ${it.message}" }
      null
    },
    ifRight = { insuranceData ->
      insuranceData
    },
  )
}

private data class InsuranceData(
  val insuranceCards: ImmutableList<InsuranceUiState.InsuranceCard>,
  val crossSells: ImmutableList<InsuranceUiState.CrossSell>,
  val showNotificationBadge: Boolean,
  val quantityOfCancelledInsurances: Int,
)

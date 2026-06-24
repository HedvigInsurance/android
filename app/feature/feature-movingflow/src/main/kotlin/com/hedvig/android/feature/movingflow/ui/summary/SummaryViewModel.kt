package com.hedvig.android.feature.movingflow.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.movingflow.HousingTypeKey
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.movingflow.SuccessfulMoveKey
import com.hedvig.android.feature.movingflow.SummaryKey
import com.hedvig.android.feature.movingflow.data.AddonId
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.AddonQuote.HomeAddonQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent.ConfirmChanges
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.CardContent.DisplayItem
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.ui.tiersandaddons.CostBreakdownEntry
import com.hedvig.ui.tiersandaddons.DisplayDocument
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2CommitMutation

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SummaryViewModel(
  @Assisted summaryRoute: SummaryKey,
  movingFlowRepository: MovingFlowRepository,
  apolloClient: ApolloClient,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
  getMoveIntentCostUseCase: GetMoveIntentCostUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SummaryEvent, SummaryUiState>(
    Loading,
    SummaryPresenter(
      summaryRoute = summaryRoute,
      movingFlowRepository = movingFlowRepository,
      apolloClient = apolloClient,
      crossSellAfterFlowRepository = crossSellAfterFlowRepository,
      getMoveIntentCostUseCase = getMoveIntentCostUseCase,
      backstack = backstack,
    ),
  )

internal class SummaryPresenter(
  private val summaryRoute: SummaryKey,
  private val movingFlowRepository: MovingFlowRepository,
  private val apolloClient: ApolloClient,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
  private val getMoveIntentCostUseCase: GetMoveIntentCostUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SummaryEvent, SummaryUiState> {
  @Composable
  override fun MoleculePresenterScope<SummaryEvent>.present(lastState: SummaryUiState): SummaryUiState {
    var summaryInfo: SummaryInfoState by remember { mutableStateOf(SummaryInfoState.Loading) }
    var moveIntentCost: MoveIntentCost? by remember { mutableStateOf(null) }
    var submitChangesError: SubmitError? by remember { mutableStateOf(null) }
    var submitChangesWithData: SubmitChangesData? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        ConfirmChanges -> {
          val moveHomeQuote =
            (summaryInfo as? SummaryInfoState.Content)?.summaryInfo?.moveHomeQuote ?: return@CollectEvents
          submitChangesWithData = SubmitChangesData(
            forDate = moveHomeQuote.startDate,
            excludedAddonIds = moveHomeQuote
              .relatedAddonQuotes
              .filter(HomeAddonQuote::isExcludedByUser)
              .map(HomeAddonQuote::addonId)
              .toNonEmptyListOrNull(),
          )
        }

        DismissSubmissionError -> {
          submitChangesError = null
        }
      }
    }

    LaunchedEffect(Unit) {
      movingFlowRepository.movingFlowState().collect { movingFlowState ->
        val movingFlowQuotes = movingFlowState?.movingFlowQuotes
        val contractId = movingFlowState?.moveFromAddressId
        if (movingFlowQuotes == null) {
          logcat(LogPriority.ERROR) { "In moving flow summary, no moving flow quotes found." }
          summaryInfo = SummaryInfoState.Error.MissingOngoingMovingFlow
        } else {
          val matchingMoveHomeQuote = movingFlowQuotes.homeQuotes.firstOrNull { it.id == summaryRoute.homeQuoteId }
          if (matchingMoveHomeQuote == null) {
            logcat(LogPriority.ERROR) { "In moving flow summary, no matching move home quote found." }
            summaryInfo = SummaryInfoState.Error.NoMatchingQuoteFound
            return@collect
          }
          val moveMtaQuotes = movingFlowQuotes.mtaQuotes
          summaryInfo = SummaryInfoState.Content(
            SummaryInfo(
              moveHomeQuote = matchingMoveHomeQuote,
              moveMtaQuotes = moveMtaQuotes,
              currentInsuranceId = contractId
            ),
          )
        }
      }
    }

    if (summaryInfo is SummaryInfoState.Content) {
      LaunchedEffect(summaryInfo) {
        val summaryInfo = (summaryInfo as SummaryInfoState.Content).summaryInfo
        getMoveIntentCostUseCase.invoke(
          intentId = summaryRoute.moveIntentId,
          selectedHomeQuoteId = summaryInfo.moveHomeQuote.id,
          selectedAddonIds = summaryInfo
            .moveHomeQuote
            .relatedAddonQuotes
            .filterNot(HomeAddonQuote::isExcludedByUser)
            .map(HomeAddonQuote::addonId)
            .map(AddonId::id),
        ).collect { result ->
          moveIntentCost = result.getOrNull()
        }
      }
    }

    val submitChangesDataValue = submitChangesWithData
    if (submitChangesDataValue != null) {
      LaunchedEffect(submitChangesDataValue) {
        logcat { "Submitting moving changes, with submitting data:$submitChangesDataValue" }
        apolloClient
          .mutation(
            MoveIntentV2CommitMutation(
              intentId = summaryRoute.moveIntentId,
              homeQuoteId = summaryRoute.homeQuoteId,
              excludedAddons = submitChangesDataValue.excludedAddonIds.orEmpty().map(AddonId::id),
            ),
          )
          .safeExecute()
          .map { it.moveIntentCommit }
          .fold(
            ifLeft = {
              Snapshot.withMutableSnapshot {
                submitChangesWithData = null
                submitChangesError = SubmitError.Generic
              }
            },
            ifRight = { moveIntentCommit ->
              val userErrorMessage = moveIntentCommit.userError?.message
              if (userErrorMessage != null) {
                Snapshot.withMutableSnapshot {
                  submitChangesWithData = null
                  submitChangesError = SubmitError.WithMessage(userErrorMessage)
                }
              } else {
                val currentContractId = (summaryInfo as? SummaryInfoState.Content)?.summaryInfo?.currentInsuranceId
                crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
                  CrossSellInfoType.MovingFlow(currentContractId),
                )
                submitChangesWithData = null
                backstack.popUpTo<SelectContractForMovingKey>(inclusive = true)
                backstack.navigateAndPopUpTo<HousingTypeKey>(
                  SuccessfulMoveKey(submitChangesDataValue.forDate),
                  inclusive = true,
                )
              }
            },
          )
      }
    }

    return when (
      val summaryInfoValue = summaryInfo
    ) {
      SummaryInfoState.Loading -> Loading

      SummaryInfoState.Error.MissingOngoingMovingFlow -> SummaryUiState.Error

      SummaryInfoState.Error.NoMatchingQuoteFound -> SummaryUiState.Error

      is SummaryInfoState.Content -> SummaryUiState.Content(
        summaryInfo = summaryInfoValue.summaryInfo,
        isSubmitting = submitChangesWithData != null,
        submitError = submitChangesError,
        moveIntentCost = moveIntentCost,
      )
    }
  }
}

private sealed interface SummaryInfoState {
  data object Loading : SummaryInfoState

  sealed interface Error : SummaryInfoState {
    data object MissingOngoingMovingFlow : Error

    data object NoMatchingQuoteFound : Error
  }

  data class Content(val summaryInfo: SummaryInfo) : SummaryInfoState
}

internal sealed interface SummaryUiState {
  data object Loading : SummaryUiState

  data object Error : SummaryUiState

  data class Content(
    private val summaryInfo: SummaryInfo,
    val isSubmitting: Boolean,
    val submitError: SubmitError?,
    private val moveIntentCost: MoveIntentCost?,
  ) : SummaryUiState {
    val cards: List<CardContent> = buildList {
      val moveHomeQuote = summaryInfo.moveHomeQuote
      val quoteCosts = moveIntentCost?.quoteCosts
      add(moveHomeQuote.toCardContent(quoteCosts?.firstOrNull { it.id == moveHomeQuote.id }))
      addAll(
        summaryInfo.moveMtaQuotes.map { moveMtaQuote ->
          moveMtaQuote.toCardContent(null)
        },
      )
    }
    val movingStartDate = summaryInfo.moveHomeQuote.startDate
    val hasMtaQuotes: Boolean = summaryInfo.moveMtaQuotes.isNotEmpty()

    val totalPremium: UiMoney? = moveIntentCost?.monthlyNet
    val grossPremium: UiMoney? = moveIntentCost?.monthlyGross
    val shouldDisableInput: Boolean = isSubmitting ||
      submitError != null

    sealed interface SubmitError {
      data object Generic : SubmitError

      data class WithMessage(val message: String) : SubmitError
    }

    data class CardContent(
      val displayName: String,
      val subtitle: String?,
      val contractGroup: ContractGroup?,
      val insurableLimits: List<InsurableLimit>,
      val documents: List<DisplayDocument>,
      val premium: UiMoney,
      val previousPremium: UiMoney?,
      val costBreakdown: List<CostBreakdownEntry>,
      val displayItems: List<DisplayItem>,
    ) {
      data class InsurableLimit(
        val label: String,
        val limit: String,
        val description: String,
      )

      data class DisplayItem(
        val title: String,
        val value: String,
        val subtitle: String?,
      )
    }
  }
}

private fun MovingFlowQuotes.Quote.toCardContent(
  quoteCost: MoveIntentCost.QuoteCost?,
): SummaryUiState.Content.CardContent = SummaryUiState.Content.CardContent(
  displayName = productVariant.displayName,
  subtitle = exposureName,
  contractGroup = productVariant.contractGroup,
  insurableLimits = productVariant.insurableLimits.map {
    SummaryUiState.Content.CardContent.InsurableLimit(
      label = it.label,
      limit = it.limit,
      description = it.description,
    )
  },
  documents = productVariant.documents.plus(
    includedRelatedAddonQuotes.flatMap { it.addonVariant.documents },
  ).map {
    DisplayDocument(
      displayName = it.displayName,
      url = it.url,
    )
  },
  premium = quoteCost?.monthlyNet ?: this.netPremiumWithAddons,
  previousPremium = quoteCost?.monthlyGross ?: this.grossPremiumWithAddons,
  costBreakdown = buildList {
    add(
      CostBreakdownEntry(
        productVariant.displayName,
        premium,
      ),
    )
    addAll(
      includedRelatedAddonQuotes.map { addonQuote ->
        CostBreakdownEntry(
          addonQuote.exposureName,
          addonQuote.premium,
        )
      },
    )
    val discounts = quoteCost?.discounts?.map { discount ->
      CostBreakdownEntry(
        discount.displayName,
        discount.displayValue,
      )
    } ?: discounts.map { discount ->
      CostBreakdownEntry(
        discount.displayName,
        discount.discountValue,
      )
    }
    addAll(discounts)
  },
  displayItems = displayItems.map {
    DisplayItem(
      title = it.title,
      value = it.value,
      subtitle = it.subtitle,
    )
  },
)

internal sealed interface SummaryEvent {
  data object ConfirmChanges : SummaryEvent

  data object DismissSubmissionError : SummaryEvent
}

internal data class SummaryInfo(
  val moveHomeQuote: MoveHomeQuote,
  val moveMtaQuotes: List<MoveMtaQuote>,
  val currentInsuranceId: String?
)

private data class SubmitChangesData(
  val forDate: LocalDate,
  val excludedAddonIds: NonEmptyList<AddonId>?,
)

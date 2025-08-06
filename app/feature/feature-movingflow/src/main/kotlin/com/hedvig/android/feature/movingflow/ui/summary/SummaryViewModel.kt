package com.hedvig.android.feature.movingflow.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.movingflow.MovingFlowDestinations.Summary
import com.hedvig.android.feature.movingflow.data.AddonId
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.AddonQuote.HomeAddonQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent.ConfirmChanges
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.summary.SummaryEvent.ToggleHomeAddonExclusion
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Content.SubmitError
import com.hedvig.android.feature.movingflow.ui.summary.SummaryUiState.Loading
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2CommitMutation

internal class SummaryViewModel(
  savedStateHandle: SavedStateHandle,
  movingFlowRepository: MovingFlowRepository,
  apolloClient: ApolloClient,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
  featureManager: FeatureManager,
) : MoleculeViewModel<SummaryEvent, SummaryUiState>(
    Loading,
    SummaryPresenter(
      summaryRoute = savedStateHandle.toRoute<Summary>(),
      movingFlowRepository = movingFlowRepository,
      apolloClient = apolloClient,
      crossSellAfterFlowRepository = crossSellAfterFlowRepository,
      featureManager = featureManager,
    ),
  )

internal class SummaryPresenter(
  private val summaryRoute: Summary,
  private val movingFlowRepository: MovingFlowRepository,
  private val apolloClient: ApolloClient,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
  private val featureManager: FeatureManager,
) : MoleculePresenter<SummaryEvent, SummaryUiState> {
  @Composable
  override fun MoleculePresenterScope<SummaryEvent>.present(lastState: SummaryUiState): SummaryUiState {
    var summaryInfo: SummaryInfoState by remember { mutableStateOf(SummaryInfoState.Loading) }
    var submitChangesError: SubmitError? by remember { mutableStateOf(null) }
    var submitChangesWithData: SubmitChangesData? by remember { mutableStateOf(null) }
    var navigateToFinishedScreenWithDate: LocalDate? by remember { mutableStateOf(null) }
    val canExcludeAddons by remember {
      featureManager.isFeatureEnabled(Feature.ENABLE_ADDONS_REMOVAL_FROM_MOVING_FLOW)
    }.collectAsState(false)

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

        is ToggleHomeAddonExclusion -> {
          launch { movingFlowRepository.toggleHomeAddonExclusion(event.addonId) }
        }
      }
    }

    LaunchedEffect(Unit) {
      movingFlowRepository.movingFlowState().collect { movingFlowState ->
        val movingFlowQuotes = movingFlowState?.movingFlowQuotes
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
            ),
          )
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
                crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
                  CrossSellInfoType.MovingFlow,
                )
                Snapshot.withMutableSnapshot {
                  submitChangesWithData = null
                  navigateToFinishedScreenWithDate = submitChangesDataValue.forDate
                }
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
      is SummaryInfoState.Content -> Content(
        summaryInfo = summaryInfoValue.summaryInfo,
        isSubmitting = submitChangesWithData != null,
        submitError = submitChangesError,
        navigateToFinishedScreenWithDate = navigateToFinishedScreenWithDate,
        canExcludeAddons = canExcludeAddons,
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
    val summaryInfo: SummaryInfo,
    val isSubmitting: Boolean,
    val submitError: SubmitError?,
    val navigateToFinishedScreenWithDate: LocalDate?,
    val canExcludeAddons: Boolean,
  ) : SummaryUiState {
    val shouldDisableInput: Boolean = isSubmitting ||
      submitError != null ||
      navigateToFinishedScreenWithDate != null

    sealed interface SubmitError {
      data object Generic : SubmitError

      data class WithMessage(val message: String) : SubmitError
    }
  }
}

internal sealed interface SummaryEvent {
  data object ConfirmChanges : SummaryEvent

  data object DismissSubmissionError : SummaryEvent

  data class ToggleHomeAddonExclusion(val addonId: AddonId) : SummaryEvent
}

internal data class SummaryInfo(
  val moveHomeQuote: MoveHomeQuote,
  val moveMtaQuotes: List<MoveMtaQuote>,
) {
  private val homeQuotePremium = moveHomeQuote.premium.amount
  private val homeQuoteAddonsPremium = moveHomeQuote
    .relatedAddonQuotes
    .filter { !it.isExcludedByUser }
    .sumOf { it.premium.amount }
  private val homeQuoteWithAddonsPremium = homeQuotePremium + homeQuoteAddonsPremium

  private val mtaQuotesPremium = moveMtaQuotes.sumOf { it.premium.amount }
  private val mtaQuotesAddonsPremium = moveMtaQuotes.flatMap { it.relatedAddonQuotes }.sumOf { it.premium.amount }

  val totalPremium: UiMoney = UiMoney(
    amount = homeQuoteWithAddonsPremium + mtaQuotesPremium + mtaQuotesAddonsPremium,
    currencyCode = moveHomeQuote.premium.currencyCode,
  )
  val grossPremium: UiMoney = UiMoney(
    amount =
      totalPremium.amount + moveHomeQuote.relatedAddonQuotes.filter { it.isExcludedByUser }.sumOf { it.premium.amount },
    currencyCode = moveHomeQuote.premium.currencyCode,
  )
}

private data class SubmitChangesData(
  val forDate: LocalDate,
  val excludedAddonIds: NonEmptyList<AddonId>?,
)

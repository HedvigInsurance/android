package com.hedvig.android.feature.home.home.ui

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
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class HomePresenter(
  private val getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
  private val seenImportantMessagesStorage: SeenImportantMessagesStorage,
  private val crossSellHomeNotificationServiceProvider: Provider<CrossSellHomeNotificationService>,
  private val applicationScope: CoroutineScope,
) : MoleculePresenter<HomeEvent, HomeUiState> {
  @Composable
  override fun MoleculePresenterScope<HomeEvent>.present(lastState: HomeUiState): HomeUiState {
    var hasError by remember { mutableStateOf(false) }
    var isReloading by remember { mutableStateOf(lastState.isReloading) }
    var successData: SuccessData? by remember { mutableStateOf(SuccessData.fromLastState(lastState)) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var crossSellToolTipShownEpochDay by remember { mutableStateOf<Long?>(null) }
    val alreadySeenImportantMessages: List<String>
      by seenImportantMessagesStorage.seenMessages.collectAsState()

    CollectEvents { homeEvent: HomeEvent ->
      when (homeEvent) {
        HomeEvent.RefreshData -> loadIteration++
        is HomeEvent.MarkMessageAsSeen -> {
          seenImportantMessagesStorage.markMessageAsSeen(homeEvent.messageId)
        }

        HomeEvent.MarkCardCrossSellsAsSeen -> {
          applicationScope.launch {
            crossSellHomeNotificationServiceProvider.provide().markAsSeen()
          }
        }

        is HomeEvent.CrossSellToolTipShown -> {
          crossSellToolTipShownEpochDay = homeEvent.epochDay
        }
      }
    }
    LaunchedEffect(crossSellToolTipShownEpochDay) {
      val epochDay = crossSellToolTipShownEpochDay
      if (epochDay != null) {
        crossSellHomeNotificationServiceProvider.provide().setLastEpochDayNewRecommendationNotificationWasShown(
          epochDay,
        )
      }
    }
    LaunchedEffect(loadIteration) {
      val forceNetworkFetch = loadIteration != 0
      Snapshot.withMutableSnapshot {
        isReloading = true
        hasError = false
      }
      combine(
        getHomeDataUseCaseProvider.provide().invoke(forceNetworkFetch),
        crossSellHomeNotificationServiceProvider.provide().showRedDotNotification(),
        crossSellHomeNotificationServiceProvider.provide().getLastEpochDayNewRecommendationNotificationWasShown(),
      ) { homeResult: Either<ApolloOperationError, HomeData>, showRedDot: Boolean, epochDay: Long? ->
        homeResult to CrossSellRecommendationNotification(
          showRedDot,
          epochDay,
        )
      }.collectLatest {
        (
          homeResult: Either<ApolloOperationError, HomeData>,
          crossSellNotification: CrossSellRecommendationNotification,
        ),
        ->
        homeResult.fold(
          ifLeft = {
            Snapshot.withMutableSnapshot {
              hasError = true
              isReloading = false
              successData = null
            }
          },
        ) { homeData: HomeData ->
          Snapshot.withMutableSnapshot {
            hasError = false
            isReloading = false
            successData = SuccessData.fromHomeData(homeData, crossSellNotification)
          }
        }
      }
    }

    return if (hasError) {
      HomeUiState.Error(null)
    } else {
      @Suppress("NAME_SHADOWING")
      val successData = successData
      return if (successData == null) {
        HomeUiState.Loading
      } else {
        HomeUiState.Success(
          isReloading = isReloading,
          homeText = successData.homeText,
          claimStatusCardsData = successData.claimStatusCardsData,
          memberReminders = successData.memberReminders,
          veryImportantMessages = successData.veryImportantMessages.filter {
            !alreadySeenImportantMessages.contains(it.id)
          },
          isHelpCenterEnabled = successData.showHelpCenter,
          hasUnseenChatMessages = successData.hasUnseenChatMessages,
          chatAction = successData.chatAction,
          firstVetAction = successData.firstVetAction,
          crossSellsAction = successData.crossSellsAction,
          travelAddonBannerInfo = successData.travelAddonBannerInfo,
        )
      }
    }
  }
}

internal sealed interface HomeEvent {
  data object RefreshData : HomeEvent

  data class MarkMessageAsSeen(val messageId: String) : HomeEvent

  data object MarkCardCrossSellsAsSeen : HomeEvent

  data class CrossSellToolTipShown(val epochDay: Long) : HomeEvent
}

internal sealed interface HomeUiState {
  val isReloading: Boolean
    get() = false

  val isHelpCenterEnabled: Boolean
    get() = false

  val hasUnseenChatMessages: Boolean
    get() = false

  data class Success(
    override val isReloading: Boolean = false,
    val homeText: HomeText,
    val claimStatusCardsData: HomeData.ClaimStatusCardsData?,
    val veryImportantMessages: List<HomeData.VeryImportantMessage>,
    val memberReminders: MemberReminders,
    val chatAction: HomeTopBarAction.ChatAction?,
    val firstVetAction: HomeTopBarAction.FirstVetAction?,
    val crossSellsAction: HomeTopBarAction.CrossSellsAction?,
    val travelAddonBannerInfo: TravelAddonBannerInfo?,
    override val isHelpCenterEnabled: Boolean,
    override val hasUnseenChatMessages: Boolean,
  ) : HomeUiState

  data class Error(val message: String?) : HomeUiState

  data object Loading : HomeUiState
}

private data class SuccessData(
  val homeText: HomeText,
  val claimStatusCardsData: HomeData.ClaimStatusCardsData?,
  val veryImportantMessages: List<HomeData.VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val showHelpCenter: Boolean,
  val chatAction: HomeTopBarAction.ChatAction?,
  val firstVetAction: HomeTopBarAction.FirstVetAction?,
  val crossSellsAction: HomeTopBarAction.CrossSellsAction?,
  val hasUnseenChatMessages: Boolean,
  val travelAddonBannerInfo: TravelAddonBannerInfo?,
) {
  companion object {
    fun fromLastState(lastState: HomeUiState): SuccessData? {
      if (lastState !is HomeUiState.Success) return null
      return SuccessData(
        homeText = lastState.homeText,
        claimStatusCardsData = lastState.claimStatusCardsData,
        veryImportantMessages = lastState.veryImportantMessages,
        memberReminders = lastState.memberReminders,
        showHelpCenter = lastState.isHelpCenterEnabled,
        chatAction = lastState.chatAction,
        crossSellsAction = lastState.crossSellsAction,
        firstVetAction = lastState.firstVetAction,
        hasUnseenChatMessages = lastState.hasUnseenChatMessages,
        travelAddonBannerInfo = lastState.travelAddonBannerInfo,
      )
    }

    fun fromHomeData(
      homeData: HomeData,
      crossSellRecommendationNotification: CrossSellRecommendationNotification,
    ): SuccessData {
      val crossSellsAction = if (homeData.crossSells.recommendedCrossSell != null ||
        homeData.crossSells.otherCrossSells.isNotEmpty()
      ) {
        HomeTopBarAction.CrossSellsAction(homeData.crossSells, crossSellRecommendationNotification)
      } else {
        null
      }

      val chatAction = if (homeData.showChatIcon) HomeTopBarAction.ChatAction else null
      val firstVetAction = if (homeData.firstVetSections.isNotEmpty()) {
        HomeTopBarAction.FirstVetAction(homeData.firstVetSections)
      } else {
        null
      }
      return SuccessData(
        homeText = when (homeData.contractStatus) {
          HomeData.ContractStatus.Active -> HomeText.Active
          is HomeData.ContractStatus.ActiveInFuture -> HomeText.ActiveInFuture(
            homeData.contractStatus.futureInceptionDate,
          )

          HomeData.ContractStatus.Terminated -> HomeText.Terminated
          HomeData.ContractStatus.Pending -> HomeText.Pending
          HomeData.ContractStatus.Switching -> HomeText.Switching
          HomeData.ContractStatus.Unknown -> HomeText.Active
        },
        claimStatusCardsData = homeData.claimStatusCardsData,
        veryImportantMessages = homeData.veryImportantMessages,
        memberReminders = homeData.memberReminders.copy(enableNotifications = null),
        showHelpCenter = homeData.showHelpCenter,
        chatAction = chatAction,
        firstVetAction = firstVetAction,
        crossSellsAction = crossSellsAction,
        hasUnseenChatMessages = homeData.hasUnseenChatMessages,
        travelAddonBannerInfo = homeData.travelBannerInfo,
      )
    }
  }
}

sealed interface HomeText {
  data object Active : HomeText

  data object Terminated : HomeText

  data class ActiveInFuture(val inception: LocalDate) : HomeText

  data object Pending : HomeText

  data object Switching : HomeText
}

sealed interface HomeTopBarAction {
  data object ChatAction : HomeTopBarAction

  data class FirstVetAction(
    val sections: List<FirstVetSection>,
  ) : HomeTopBarAction

  data class CrossSellsAction(
    val crossSells: CrossSellSheetData,
    val crossSellRecommendationNotification: CrossSellRecommendationNotification,
  ) : HomeTopBarAction
}

data class CrossSellRecommendationNotification(
  val hasUnreadRecommendation: Boolean, // show red dot
  private val epochDayWhenLastToolTipShown: Long?,
) {
  val showToolTip: Boolean =
    hasUnreadRecommendation &&
      epochDayWhenLastToolTipShown != Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault(),
      ).date.toEpochDays().toLong()
}

package com.hedvig.android.feature.home.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.chat.read.timestamp.ChatLastMessageReadRepository
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorage
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class HomePresenter(
  private val getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
  private val chatLastMessageReadRepository: ChatLastMessageReadRepository,
  private val seenImportantMessagesStorage: SeenImportantMessagesStorage,
  private val crossSellCardNotificationBadgeServiceProvider: Provider<CrossSellCardNotificationBadgeService>,
  private val applicationScope: CoroutineScope,
) : MoleculePresenter<HomeEvent, HomeUiState> {
  @Composable
  override fun MoleculePresenterScope<HomeEvent>.present(lastState: HomeUiState): HomeUiState {
    var hasError by remember { mutableStateOf(false) }
    var isReloading by remember { mutableStateOf(lastState.isReloading) }
    var successData: SuccessData? by remember { mutableStateOf(SuccessData.fromLastState(lastState)) }
    var loadIteration by remember { mutableIntStateOf(0) }
    val alreadySeenImportantMessages: List<String>
      by seenImportantMessagesStorage.seenMessages.collectAsState()

    val hasUnseenChatMessages by produceState(
      lastState.safeCast<HomeUiState.Success>()?.hasUnseenChatMessages ?: false,
    ) {
      while (isActive) {
        value = chatLastMessageReadRepository.isNewestMessageNewerThanLastReadTimestamp()
        delay(10.seconds)
      }
    }

    CollectEvents { homeEvent: HomeEvent ->
      when (homeEvent) {
        HomeEvent.RefreshData -> loadIteration++
        is HomeEvent.MarkMessageAsSeen -> {
          seenImportantMessagesStorage.markMessageAsSeen(homeEvent.messageId)
        }
        HomeEvent.MarkCardCrossSellsAsSeen -> {
          applicationScope.launch { crossSellCardNotificationBadgeServiceProvider.provide().markAsSeen() }
        }
      }
    }

    LaunchedEffect(loadIteration) {
      val forceNetworkFetch = loadIteration != 0
      Snapshot.withMutableSnapshot {
        isReloading = true
        hasError = false
      }
      getHomeDataUseCaseProvider.provide().invoke(forceNetworkFetch).collectLatest { homeResult ->
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
            successData = SuccessData.fromHomeData(
              homeData,
            )
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
          }.toPersistentList(),
          isHelpCenterEnabled = successData.showHelpCenter,
          hasUnseenChatMessages = hasUnseenChatMessages,
          chatAction = successData.chatAction,
          firstVetAction = successData.firstVetAction,
          crossSellsAction = successData.crossSellsAction,
        )
      }
    }
  }
}

internal sealed interface HomeEvent {
  data object RefreshData : HomeEvent

  data class MarkMessageAsSeen(val messageId: String) : HomeEvent

  data object MarkCardCrossSellsAsSeen : HomeEvent
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
    val veryImportantMessages: ImmutableList<HomeData.VeryImportantMessage>,
    val memberReminders: MemberReminders,
    val chatAction: HomeTopBarAction.ChatAction?,
    val firstVetAction: HomeTopBarAction.FirstVetAction?,
    val crossSellsAction: HomeTopBarAction.CrossSellsAction?,
    override val isHelpCenterEnabled: Boolean,
    override val hasUnseenChatMessages: Boolean,
  ) : HomeUiState

  data class Error(val message: String?) : HomeUiState

  data object Loading : HomeUiState
}

private data class SuccessData(
  val homeText: HomeText,
  val claimStatusCardsData: HomeData.ClaimStatusCardsData?,
  val veryImportantMessages: ImmutableList<HomeData.VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val showHelpCenter: Boolean,
  val chatAction: HomeTopBarAction.ChatAction?,
  val firstVetAction: HomeTopBarAction.FirstVetAction?,
  val crossSellsAction: HomeTopBarAction.CrossSellsAction?,
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
      )
    }

    fun fromHomeData(homeData: HomeData): SuccessData {
      val crossSellsAction = if (homeData.crossSells.isNotEmpty()) {
        HomeTopBarAction.CrossSellsAction(homeData.crossSells)
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
    val crossSells: ImmutableList<CrossSell>,
  ) : HomeTopBarAction
}

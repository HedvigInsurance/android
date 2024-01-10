package com.hedvig.android.feature.home.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.hedvig.android.feature.home.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.emergency.EmergencyData
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDate

internal class HomePresenter(
    private val getHomeDataUseCaseProvider: Provider<GetHomeDataUseCase>,
    private val chatLastMessageReadRepository: ChatLastMessageReadRepository,
    private val featureManager: FeatureManager,
) : MoleculePresenter<HomeEvent, HomeUiState> {
  @Composable
  override fun MoleculePresenterScope<HomeEvent>.present(lastState: HomeUiState): HomeUiState {
    var hasError by remember { mutableStateOf(false) }
    var isReloading by remember { mutableStateOf(lastState.isReloading) }
    var successData: SuccessData? by remember { mutableStateOf(SuccessData.fromLastState(lastState)) }
    var loadIteration by remember { mutableIntStateOf(0) }
    val showChatIcon by produceState(lastState.showChatIcon) {
      val showChatIcon = !featureManager.isFeatureEnabled(Feature.DISABLE_CHAT)
      value = showChatIcon
    }
    val isHelpCenterEnabled by produceState(lastState.isHelpCenterEnabled) {
      val isHelpCenterEnabled = featureManager.isFeatureEnabled(Feature.HELP_CENTER)
      value = isHelpCenterEnabled
    }
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
          ifRight = { homeData: HomeData ->
            Snapshot.withMutableSnapshot {
              hasError = false
              isReloading = false
              successData = SuccessData.fromHomeData(homeData)
            }
          },
        )
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
          veryImportantMessages = successData.veryImportantMessages,
          allowAddressChange = successData.allowAddressChange,
          isHelpCenterEnabled = isHelpCenterEnabled,
          allowGeneratingTravelCertificate = successData.allowGeneratingTravelCertificate,
          emergencyData = successData.emergencyData,
          commonClaimsData = successData.commonClaimsData,
          showChatIcon = showChatIcon,
          hasUnseenChatMessages = hasUnseenChatMessages,
        )
      }
    }
  }
}

internal sealed interface HomeEvent {
  data object RefreshData : HomeEvent
}

internal sealed interface HomeUiState {
  val isReloading: Boolean
    get() = false

  val showChatIcon: Boolean
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
    val allowAddressChange: Boolean,
    val allowGeneratingTravelCertificate: Boolean,
    val emergencyData: EmergencyData?,
    val commonClaimsData: ImmutableList<CommonClaimsData>,
    override val isHelpCenterEnabled: Boolean,
    override val showChatIcon: Boolean,
    override val hasUnseenChatMessages: Boolean,
  ) : HomeUiState

  data class Error(val message: String?) : HomeUiState

  object Loading : HomeUiState
}

private data class SuccessData(
  val homeText: HomeText,
  val claimStatusCardsData: HomeData.ClaimStatusCardsData?,
  val veryImportantMessages: ImmutableList<HomeData.VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val allowAddressChange: Boolean,
  val allowGeneratingTravelCertificate: Boolean,
  val emergencyData: EmergencyData?,
  val commonClaimsData: ImmutableList<CommonClaimsData>,
) {
  companion object {
    fun fromLastState(lastState: HomeUiState): SuccessData? {
      lastState as? HomeUiState.Success ?: return null
      return SuccessData(
        homeText = lastState.homeText,
        claimStatusCardsData = lastState.claimStatusCardsData,
        veryImportantMessages = lastState.veryImportantMessages,
        memberReminders = lastState.memberReminders,
        allowAddressChange = lastState.allowAddressChange,
        allowGeneratingTravelCertificate = lastState.allowGeneratingTravelCertificate,
        emergencyData = lastState.emergencyData,
        commonClaimsData = lastState.commonClaimsData,
      )
    }

    fun fromHomeData(homeData: HomeData): SuccessData {
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
        memberReminders = homeData.memberReminders.copy(enableNotifications = null),
        veryImportantMessages = homeData.veryImportantMessages,
        allowAddressChange = homeData.allowAddressChange,
        allowGeneratingTravelCertificate = homeData.allowGeneratingTravelCertificate,
        emergencyData = homeData.emergencyData,
        commonClaimsData = homeData.commonClaimsData,
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

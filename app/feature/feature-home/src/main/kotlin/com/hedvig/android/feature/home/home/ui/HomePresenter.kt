package com.hedvig.android.feature.home.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.data.HomeData
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate

internal class HomePresenter(
  private val getHomeDataUseCase: GetHomeDataUseCase,
) : MoleculePresenter<HomeEvent, HomeUiState> {
  @Composable
  override fun MoleculePresenterScope<HomeEvent>.present(lastState: HomeUiState): HomeUiState {
    var hasError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var successData: SuccessData? by remember { mutableStateOf(SuccessData.fromLastState(lastState)) }
    var loadIteration by remember { mutableStateOf(0) }

    CollectEvents { homeEvent: HomeEvent ->
      when (homeEvent) {
        HomeEvent.RefreshData -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      val forceNetworkFetch = loadIteration != 0
      Snapshot.withMutableSnapshot {
        isLoading = true
        hasError = false
      }
      getHomeDataUseCase.invoke(forceNetworkFetch).collectLatest { homeResult ->
        homeResult.fold(
          ifLeft = {
            Snapshot.withMutableSnapshot {
              hasError = true
              isLoading = false
              successData = null
            }
          },
          ifRight = { homeData: HomeData ->
            Snapshot.withMutableSnapshot {
              hasError = false
              isLoading = false
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
          isReloading = isLoading,
          homeText = successData.homeText,
          claimStatusCards = successData.claimStatusCards,
          upcomingRenewals = successData.upcomingRenewals,
          memberReminders = successData.memberReminders,
          veryImportantMessages = successData.veryImportantMessages,
          allowAddressChange = successData.allowAddressChange,
          allowGeneratingTravelCertificate = successData.allowGeneratingTravelCertificate,
          emergencyData = successData.emergencyData,
        )
      }
    }
  }
}

internal sealed interface HomeEvent {
  object RefreshData : HomeEvent
}

internal sealed interface HomeUiState {
  val isLoading: Boolean
    get() = this is Loading || (this is Success && isReloading)

  data class Success(
    val isReloading: Boolean = false,
    val homeText: HomeText,
    val claimStatusCards: HomeData.ClaimStatusCards?,
    val upcomingRenewals: ImmutableList<HomeData.UpcomingRenewal>,
    val veryImportantMessages: ImmutableList<HomeData.VeryImportantMessage>,
    val memberReminders: MemberReminders,
    val allowAddressChange: Boolean,
    val allowGeneratingTravelCertificate: Boolean,
    val emergencyData: EmergencyData?,
  ) : HomeUiState

  data class Error(val message: String?) : HomeUiState
  object Loading : HomeUiState
}

private data class SuccessData(
  val homeText: HomeText,
  val claimStatusCards: HomeData.ClaimStatusCards?,
  val upcomingRenewals: ImmutableList<HomeData.UpcomingRenewal>,
  val veryImportantMessages: ImmutableList<HomeData.VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val allowAddressChange: Boolean,
  val allowGeneratingTravelCertificate: Boolean,
  val emergencyData: EmergencyData?,
) {
  companion object {
    fun fromLastState(lastState: HomeUiState): SuccessData? {
      lastState as? HomeUiState.Success ?: return null
      return SuccessData(
        homeText = lastState.homeText,
        claimStatusCards = lastState.claimStatusCards,
        upcomingRenewals = lastState.upcomingRenewals,
        veryImportantMessages = lastState.veryImportantMessages,
        memberReminders = lastState.memberReminders,
        allowAddressChange = lastState.allowAddressChange,
        allowGeneratingTravelCertificate = lastState.allowGeneratingTravelCertificate,
        emergencyData = lastState.emergencyData,
      )
    }

    fun fromHomeData(homeData: HomeData): SuccessData {
      return SuccessData(
        homeText = when (homeData.contractStatus) {
          HomeData.ContractStatus.Active -> HomeText.Active(homeData.memberName)
          is HomeData.ContractStatus.ActiveInFuture -> HomeText.ActiveInFuture(
            homeData.memberName,
            homeData.contractStatus.futureInceptionDate,
          )
          HomeData.ContractStatus.Terminated -> HomeText.Terminated(homeData.memberName)
          HomeData.ContractStatus.Pending -> HomeText.Pending(homeData.memberName)
          HomeData.ContractStatus.Switching -> HomeText.Switching(homeData.memberName)
          HomeData.ContractStatus.Unknown -> HomeText.Active(homeData.memberName)
        },
        claimStatusCards = homeData.claimStatusCards,
        upcomingRenewals = homeData.upcomingRenewals,
        memberReminders = homeData.memberReminders,
        veryImportantMessages = homeData.veryImportantMessages,
        allowAddressChange = homeData.allowAddressChange,
        allowGeneratingTravelCertificate = homeData.allowGeneratingTravelCertificate,
        emergencyData = homeData.emergencyData,
      )
    }
  }
}

sealed interface HomeText {
  val name: String?

  data class Active(override val name: String?) : HomeText
  data class Terminated(override val name: String?) : HomeText
  data class ActiveInFuture(override val name: String?, val inception: LocalDate) : HomeText

  // Probably use generic text here. what does home_tab_pending_unknown_body mean otherwise?
  data class Pending(override val name: String?) : HomeText

  // Also use generic text, home_tab_pending_switchable_body doesn't make sense in new design probably
  data class Switching(override val name: String?) : HomeText // no need?
}

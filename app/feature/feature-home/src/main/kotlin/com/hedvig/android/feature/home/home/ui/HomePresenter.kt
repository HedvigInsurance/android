package com.hedvig.android.feature.home.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
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
    var isReloading by remember { mutableStateOf(false) }
    var successData: SuccessData? by remember { mutableStateOf(SuccessData.fromLastState(lastState)) }
    var loadIteration by remember { mutableIntStateOf(0) }

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
      getHomeDataUseCase.invoke(forceNetworkFetch).collectLatest { homeResult ->
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
          allowGeneratingTravelCertificate = successData.allowGeneratingTravelCertificate,
          emergencyData = successData.emergencyData,
          commonClaimsData = successData.commonClaimsData,
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
  val name: String?

  data class Active(override val name: String?) : HomeText
  data class Terminated(override val name: String?) : HomeText
  data class ActiveInFuture(override val name: String?, val inception: LocalDate) : HomeText
  data class Pending(override val name: String?) : HomeText
  data class Switching(override val name: String?) : HomeText
}

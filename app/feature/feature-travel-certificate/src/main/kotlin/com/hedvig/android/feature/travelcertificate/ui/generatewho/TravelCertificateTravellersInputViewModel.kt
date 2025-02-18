package com.hedvig.android.feature.travelcertificate.ui.generatewho

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetCoInsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal class TravelCertificateTravellersInputViewModel(
  primaryInput: TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  getCoInsuredForContractUseCase: GetCoInsuredForContractUseCase,
) : MoleculeViewModel<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState>(
    initialState = TravelCertificateTravellersInputUiState.Loading,
    presenter = TravelCertificateTravellersInputPresenter(
      primaryInput,
      createTravelCertificateUseCase,
      getCoInsuredForContractUseCase,
    ),
  )

internal class TravelCertificateTravellersInputPresenter(
  private val primaryInput: TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  private val getCoInsuredForContractUseCase: GetCoInsuredForContractUseCase,
) : MoleculePresenter<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateTravellersInputEvent>.present(
    lastState: TravelCertificateTravellersInputUiState,
  ): TravelCertificateTravellersInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var generateIteration by remember { mutableIntStateOf(0) }

    var screenContent by remember { mutableStateOf<TravelersInputScreenContent>(TravelersInputScreenContent.Loading) }

    var fetchedCoInsuredList by remember { mutableStateOf<List<CoInsured>>(listOf()) }
    val coInsuredIncludedMap = remember { mutableStateMapOf<CoInsured, Boolean>() }
    val coInsuredList by remember {
      derivedStateOf {
        fetchedCoInsuredList.map {
          it.copy(isIncluded = coInsuredIncludedMap.get(it) ?: it.isIncluded)
        }
      }
    }
    var isMemberIncluded by remember { mutableStateOf(true) }

    CollectEvents { event ->
      when (event) {
        TravelCertificateTravellersInputEvent.RetryLoadData -> loadIteration++
        TravelCertificateTravellersInputEvent.GenerateTravelCertificate -> generateIteration++
        is TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked -> {
          coInsuredIncludedMap.put(event.coInsured, !event.coInsured.isIncluded)
        }

        TravelCertificateTravellersInputEvent.ChangeMemberChecked -> {
          isMemberIncluded = !isMemberIncluded
        }
      }
    }

    LaunchedEffect(loadIteration) {
      getCoInsuredForContractUseCase.invoke(primaryInput.contractId).collectLatest { data ->
        data.fold(
          ifLeft = {
            if (screenContent !is TravelersInputScreenContent.Success) {
              screenContent = TravelersInputScreenContent.Failure
            }
          },
          ifRight = { data ->
            val coInsuredList = data.coInsuredList.filterNot {
              it.hasMissingInfo
            }.map { coInsured ->
              CoInsured(
                id = coInsured.id,
                name = "${coInsured.firstName} ${coInsured.lastName}",
                ssn = coInsured.ssn,
                dateOfBirth = coInsured.dateOfBirth,
                isIncluded = false,
              )
            }.sortedBy { it.name }
            val hasMissingInfo = data.coInsuredList.any { it.hasMissingInfo }
            Snapshot.withMutableSnapshot {
              fetchedCoInsuredList = coInsuredList
              screenContent = TravelersInputScreenContent.Success(
                coInsuredHasMissingInfo = hasMissingInfo,
                memberFullName = data.memberFullName,
                isButtonLoading = false,
              )
            }
          },
        )
      }
    }

    LaunchedEffect(generateIteration) {
      val currentContent = screenContent
      if (currentContent is TravelersInputScreenContent.Success) {
        screenContent = currentContent.copy(isButtonLoading = true)
        createTravelCertificateUseCase.invoke(
          contractId = primaryInput.contractId,
          startDate = primaryInput.travelDate,
          isMemberIncluded = isMemberIncluded,
          coInsured = coInsuredList.filter { it.isIncluded },
          email = primaryInput.email,
        ).fold(
          ifLeft = { _ ->
            TravelersInputScreenContent.Failure
          },
          ifRight = { url ->
            screenContent = TravelersInputScreenContent.UrlFetched(url)
          },
        )
      }
    }

    return when (val screenContentValue = screenContent) {
      TravelersInputScreenContent.Failure -> TravelCertificateTravellersInputUiState.Failure
      TravelersInputScreenContent.Loading -> TravelCertificateTravellersInputUiState.Loading
      is TravelersInputScreenContent.Success -> {
        TravelCertificateTravellersInputUiState.Success(
          coInsuredHasMissingInfo = screenContentValue.coInsuredHasMissingInfo,
          coInsuredList = coInsuredList,
          memberFullName = screenContentValue.memberFullName,
          isMemberIncluded = isMemberIncluded,
          isButtonLoading = screenContentValue.isButtonLoading,
        )
      }

      is TravelersInputScreenContent.UrlFetched -> TravelCertificateTravellersInputUiState.UrlFetched(
        screenContentValue.travelCertificateUrl,
      )
    }
  }
}

private sealed interface TravelersInputScreenContent {
  data object Loading : TravelersInputScreenContent

  data object Failure : TravelersInputScreenContent

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : TravelersInputScreenContent

  data class Success(
    val coInsuredHasMissingInfo: Boolean,
    val memberFullName: String,
    val isButtonLoading: Boolean,
  ) : TravelersInputScreenContent
}

internal sealed interface TravelCertificateTravellersInputUiState {
  data object Loading : TravelCertificateTravellersInputUiState

  data object Failure : TravelCertificateTravellersInputUiState

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : TravelCertificateTravellersInputUiState

  data class Success(
    val coInsuredHasMissingInfo: Boolean,
    val coInsuredList: List<CoInsured>,
    val memberFullName: String,
    val isMemberIncluded: Boolean,
    val isButtonLoading: Boolean,
  ) : TravelCertificateTravellersInputUiState {
    val hasAtLeastOneTraveler = isMemberIncluded || coInsuredList.any { it.isIncluded }
  }
}

internal sealed interface TravelCertificateTravellersInputEvent {
  data object RetryLoadData : TravelCertificateTravellersInputEvent

  data object GenerateTravelCertificate : TravelCertificateTravellersInputEvent

  data object ChangeMemberChecked : TravelCertificateTravellersInputEvent

  data class ChangeCoInsuredChecked(val coInsured: CoInsured) : TravelCertificateTravellersInputEvent
}

@Serializable
internal data class CoInsured(
  val id: String?,
  val name: String,
  val ssn: String?,
  val dateOfBirth: LocalDate?,
  val isIncluded: Boolean,
)

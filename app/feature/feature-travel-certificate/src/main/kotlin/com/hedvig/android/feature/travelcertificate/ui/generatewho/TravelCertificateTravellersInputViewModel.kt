package com.hedvig.android.feature.travelcertificate.ui.generatewho

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var screenContent by remember { mutableStateOf<TravelersInputScreenContent>(TravelersInputScreenContent.Loading) }

    var currentCoInsuredList by remember { mutableStateOf<List<CoInsured>>(listOf()) }

    var isMemberIncluded by remember { mutableStateOf(true) }

    var generateIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        TravelCertificateTravellersInputEvent.RetryLoadData -> loadIteration++
        TravelCertificateTravellersInputEvent.GenerateTravelCertificate -> generateIteration++
        is TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked -> {
          val changedTraveler = event.coInsured.copy(isIncluded = !event.coInsured.isIncluded)
          val newList = currentCoInsuredList.toMutableList()
          newList.remove(event.coInsured)
          newList.add(changedTraveler)
          val sorted = newList.sortedBy { it.name }
          currentCoInsuredList = sorted
        }

        TravelCertificateTravellersInputEvent.ChangeMemberChecked -> {
          val lastMemberState = isMemberIncluded
          isMemberIncluded = !lastMemberState
        }
      }
    }

    LaunchedEffect(loadIteration) {
      getCoInsuredForContractUseCase.invoke(primaryInput.contractId).collectLatest { data ->
        data.fold(
          ifLeft = {
            screenContent = TravelersInputScreenContent.Failure
          },
          ifRight = { data ->
            val resultList = data.coInsuredList.filterNot {
              it.hasMissingInfo
            }.map { coInsured ->
              CoInsured(
                coInsured.id,
                "${coInsured.firstName} ${coInsured.lastName}",
                coInsured.ssn,
                coInsured.dateOfBirth,
              )
            }.sortedBy { it.name }
            currentCoInsuredList = resultList
            val hasMissingInfo = data.coInsuredList.any { it.hasMissingInfo }
            screenContent = TravelersInputScreenContent.Success(
              hasMissingInfo,
              data.memberFullName,
            )
          },
        )
      }
    }

    LaunchedEffect(generateIteration) {
      val currentContent = screenContent
      if (currentContent is TravelersInputScreenContent.Success) {
        screenContent = TravelersInputScreenContent.Loading
        createTravelCertificateUseCase.invoke(
          contractId = primaryInput.contractId,
          startDate = primaryInput.travelDate,
          isMemberIncluded = isMemberIncluded,
          coInsured = currentCoInsuredList.filter { it.isIncluded },
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

    return when (val currentContent = screenContent) {
      TravelersInputScreenContent.Failure -> TravelCertificateTravellersInputUiState.Failure
      TravelersInputScreenContent.Loading -> TravelCertificateTravellersInputUiState.Loading
      is TravelersInputScreenContent.Success -> TravelCertificateTravellersInputUiState.Success(
        coInsuredHasMissingInfo = currentContent.coInsuredHasMissingInfo,
        coInsuredList = currentCoInsuredList,
        memberFullName = currentContent.memberFullName,
        isMemberIncluded = isMemberIncluded,
      )

      is TravelersInputScreenContent.UrlFetched -> TravelCertificateTravellersInputUiState.UrlFetched(
        currentContent.travelCertificateUrl,
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
  val isIncluded: Boolean = false,
)

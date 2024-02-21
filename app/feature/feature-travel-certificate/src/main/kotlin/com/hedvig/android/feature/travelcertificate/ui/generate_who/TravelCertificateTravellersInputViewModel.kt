package com.hedvig.android.feature.travelcertificate.ui.generate_who

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.travelcertificate.GetCoEnsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generate_when.TravelCertificatePrimaryInput
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal class TravelCertificateTravellersInputViewModel(
  primaryInput: TravelCertificatePrimaryInput,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  getCoEnsuredForContractUseCase: GetCoEnsuredForContractUseCase,
) : MoleculeViewModel<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState>(
    initialState = TravelCertificateTravellersInputUiState.Loading,
    presenter = TravelCertificateTravellersInputPresenter(
      primaryInput,
      createTravelCertificateUseCase,
      getCoEnsuredForContractUseCase,
    ),
  )

internal class TravelCertificateTravellersInputPresenter(
  private val primaryInput: TravelCertificatePrimaryInput,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  private val getCoEnsuredForContractUseCase: GetCoEnsuredForContractUseCase,
) : MoleculePresenter<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateTravellersInputEvent>.present(
    lastState: TravelCertificateTravellersInputUiState,
  ): TravelCertificateTravellersInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }

    var screenContent by remember { mutableStateOf<TravelersInputScreenContent>(TravelersInputScreenContent.Loading) }

    var currentCoEnsuredList by remember { mutableStateOf<List<CoInsured>>(listOf()) }

    var isMemberIncluded by remember { mutableStateOf(true) }

    var generateIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        TravelCertificateTravellersInputEvent.RetryLoadData -> loadIteration++
        TravelCertificateTravellersInputEvent.GenerateTravelCertificate -> generateIteration++
        is TravelCertificateTravellersInputEvent.ChangeCoEnsuredChecked -> {
          val changedTraveler = event.coInsured.copy(isIncluded = !event.coInsured.isIncluded)
          val newList = currentCoEnsuredList.toMutableList()
          newList.remove(event.coInsured)
          newList.add(changedTraveler)
          val sorted = newList.sortedBy { it.name }
          currentCoEnsuredList = sorted
        }
        TravelCertificateTravellersInputEvent.ChangeMemberChecked -> {
          val lastMemberState = isMemberIncluded
          isMemberIncluded = !lastMemberState
        }
      }
    }

    LaunchedEffect(loadIteration) {
      getCoEnsuredForContractUseCase.invoke(primaryInput.contractId).fold(
        ifLeft = {
          screenContent = TravelersInputScreenContent.Failure
        },
        ifRight = { data ->
          val resultList = data.coEnsuredList.filterNot {
            it.hasMissingInfo
          }.map { coEnsured ->
            CoInsured(
              coEnsured.id,
              "${coEnsured.firstName} ${coEnsured.lastName}",
              coEnsured.ssn,
              coEnsured.dateOfBirth,
            )
          }.sortedBy { it.name }
          currentCoEnsuredList = resultList
          val hasMissingInfo = data.coEnsuredList.any { it.hasMissingInfo }
          screenContent = TravelersInputScreenContent.Success(
            hasMissingInfo,
            data.memberFullName,
          )
        },
      )
    }

    LaunchedEffect(generateIteration) {
      val currentContent = screenContent
      if (currentContent is TravelersInputScreenContent.Success) {
        screenContent = TravelersInputScreenContent.Loading
        createTravelCertificateUseCase.invoke(
          contractId = primaryInput.contractId,
          startDate = primaryInput.travelDate,
          isMemberIncluded = isMemberIncluded,
          coInsured = currentCoEnsuredList.filter { it.isIncluded },
          email = primaryInput.email,
        ).fold(
          ifLeft = { errorMessage ->
            val message = errorMessage.message
            screenContent = if (message != null && message.contains("Invalid email")) {
              TravelersInputScreenContent.FailureWithInvalidEmail
            } else {
              TravelersInputScreenContent.Failure
            }
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
        coEnsuredHasMissingInfo = currentContent.coEnsuredHasMissingInfo,
        coEnsuredList = currentCoEnsuredList,
        memberFullName = currentContent.memberFullName,
        isMemberIncluded = isMemberIncluded,
        hasAtLeastOneTraveler = isMemberIncluded || currentCoEnsuredList.any { it.isIncluded },
      )
      is TravelersInputScreenContent.UrlFetched -> TravelCertificateTravellersInputUiState.UrlFetched(
        currentContent.travelCertificateUrl,
      )
      TravelersInputScreenContent.FailureWithInvalidEmail -> TravelCertificateTravellersInputUiState.FailureWithInvalidEmail
    }
  }
}

private sealed interface TravelersInputScreenContent {
  data object Loading : TravelersInputScreenContent

  data object Failure : TravelersInputScreenContent

  data object FailureWithInvalidEmail : TravelersInputScreenContent

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : TravelersInputScreenContent

  data class Success(
    val coEnsuredHasMissingInfo: Boolean,
    val memberFullName: String,
  ) : TravelersInputScreenContent
}

internal sealed interface TravelCertificateTravellersInputUiState {
  data object Loading : TravelCertificateTravellersInputUiState

  data object Failure : TravelCertificateTravellersInputUiState

  data object FailureWithInvalidEmail : TravelCertificateTravellersInputUiState

  data class UrlFetched(val travelCertificateUrl: TravelCertificateUrl) : TravelCertificateTravellersInputUiState

  data class Success(
    val coEnsuredHasMissingInfo: Boolean,
    val coEnsuredList: List<CoInsured>,
    val memberFullName: String,
    val isMemberIncluded: Boolean,
    val hasAtLeastOneTraveler: Boolean,
  ) : TravelCertificateTravellersInputUiState
}

internal sealed interface TravelCertificateTravellersInputEvent {
  data object RetryLoadData : TravelCertificateTravellersInputEvent

  data object GenerateTravelCertificate : TravelCertificateTravellersInputEvent

  data object ChangeMemberChecked : TravelCertificateTravellersInputEvent

  data class ChangeCoEnsuredChecked(val coInsured: CoInsured) : TravelCertificateTravellersInputEvent
}

@Serializable
internal data class CoInsured(
  val id: String?,
  val name: String,
  val ssn: String?,
  val dateOfBirth: LocalDate?,
  val isIncluded: Boolean = false,
)

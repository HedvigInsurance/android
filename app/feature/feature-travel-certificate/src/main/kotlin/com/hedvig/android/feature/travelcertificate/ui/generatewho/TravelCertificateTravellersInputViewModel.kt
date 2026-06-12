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
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.travelcertificate.data.CoInsuredData
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetCoInsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@AssistedInject
@HedvigViewModel
internal class TravelCertificateTravellersInputViewModel(
  @Assisted primaryInput: TravelCertificateTravellersInputKey.TravelCertificatePrimaryInput,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  getCoInsuredForContractUseCase: GetCoInsuredForContractUseCase,
  backstack: Backstack,
) : MoleculeViewModel<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState>(
    initialState = TravelCertificateTravellersInputUiState.Loading,
    presenter = TravelCertificateTravellersInputPresenter(
      primaryInput,
      createTravelCertificateUseCase,
      getCoInsuredForContractUseCase,
      backstack,
    ),
  )

internal class TravelCertificateTravellersInputPresenter(
  private val primaryInput: TravelCertificateTravellersInputKey.TravelCertificatePrimaryInput,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  private val getCoInsuredForContractUseCase: GetCoInsuredForContractUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateTravellersInputEvent>.present(
    lastState: TravelCertificateTravellersInputUiState,
  ): TravelCertificateTravellersInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var generateIteration by remember { mutableIntStateOf(0) }

    var screenContent by remember { mutableStateOf<TravelersInputScreenContent>(TravelersInputScreenContent.Loading) }

    var fetchedCoInsuredList by remember { mutableStateOf<List<CoInsured>>(listOf()) }
    val coInsuredIncludedMap = remember { mutableStateMapOf<CoInsuredId, Boolean>() }
    val coInsuredList by remember {
      derivedStateOf {
        fetchedCoInsuredList.map {
          it.copy(isIncluded = coInsuredIncludedMap.get(it.id) ?: it.isIncluded)
        }
      }
    }
    var isMemberIncluded by remember { mutableStateOf(true) }

    CollectEvents { event ->
      when (event) {
        TravelCertificateTravellersInputEvent.RetryLoadData -> {
          loadIteration++
        }

        TravelCertificateTravellersInputEvent.GenerateTravelCertificate -> {
          generateIteration++
        }

        is TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked -> {
          coInsuredIncludedMap.put(event.coInsured.id, !event.coInsured.isIncluded)
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
            }.map { coInsuredData ->
              CoInsured(
                id = CoInsuredId(coInsuredData),
                name = "${coInsuredData.firstName} ${coInsuredData.lastName}",
                ssn = coInsuredData.ssn,
                dateOfBirth = coInsuredData.dateOfBirth,
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
            backstack.navigateAndPopUpTo<TravelCertificateKey>(ShowCertificateKey(url), inclusive = false)
          },
        )
      }
    }

    return when (val screenContentValue = screenContent) {
      TravelersInputScreenContent.Failure -> {
        TravelCertificateTravellersInputUiState.Failure
      }

      TravelersInputScreenContent.Loading -> {
        TravelCertificateTravellersInputUiState.Loading
      }

      is TravelersInputScreenContent.Success -> {
        TravelCertificateTravellersInputUiState.Success(
          coInsuredHasMissingInfo = screenContentValue.coInsuredHasMissingInfo,
          coInsuredList = coInsuredList,
          memberFullName = screenContentValue.memberFullName,
          isMemberIncluded = isMemberIncluded,
          isButtonLoading = screenContentValue.isButtonLoading,
        )
      }
    }
  }
}

private sealed interface TravelersInputScreenContent {
  data object Loading : TravelersInputScreenContent

  data object Failure : TravelersInputScreenContent

  data class Success(
    val coInsuredHasMissingInfo: Boolean,
    val memberFullName: String,
    val isButtonLoading: Boolean,
  ) : TravelersInputScreenContent
}

internal sealed interface TravelCertificateTravellersInputUiState {
  data object Loading : TravelCertificateTravellersInputUiState

  data object Failure : TravelCertificateTravellersInputUiState

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
  val id: CoInsuredId,
  val name: String,
  val ssn: String?,
  val dateOfBirth: LocalDate?,
  val isIncluded: Boolean,
) {
  @Serializable
  @JvmInline
  value class CoInsuredId(val value: String) {
    companion object {
      operator fun invoke(coInsuredData: CoInsuredData) = CoInsuredId(
        // Backend does not return a non-null ID here, so to make sure we don't accidentally consider two entries as
        // duplicate since they'd have the same key ("null"), we make our own key based on the data available to us.
        coInsuredData.id
          ?: (coInsuredData.firstName + coInsuredData.lastName + coInsuredData.ssn + coInsuredData.dateOfBirth),
      )
    }
  }
}

package com.hedvig.android.feature.purchase.house.ui.house

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.house.data.CreateHouseSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.house.data.HouseOffers
import com.hedvig.android.feature.purchase.house.data.SessionAndIntent
import com.hedvig.android.feature.purchase.house.data.SubmitHouseFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.house.ui.extrabuildings.ExtraBuildingInfo
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import java.time.LocalDate

internal class HouseFormViewModel(
  productName: String,
  createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  submitHouseFormAndGetOffersUseCase: SubmitHouseFormAndGetOffersUseCase,
) : MoleculeViewModel<HouseFormEvent, HouseFormState>(
    initialState = HouseFormState(),
    presenter = HouseFormPresenter(
      productName,
      createHouseSessionAndPriceIntentUseCase,
      submitHouseFormAndGetOffersUseCase,
    ),
  )

internal sealed interface HouseFormEvent {
  data class UpdateIsSubleted(val value: Boolean) : HouseFormEvent

  data class AddExtraBuilding(val building: ExtraBuildingInfo) : HouseFormEvent

  data class RemoveExtraBuilding(val building: ExtraBuildingInfo) : HouseFormEvent

  data class SubmitForm(
    val street: String,
    val zipCode: String,
    val livingSpace: String,
    val ancillaryArea: String,
    val numberCoInsured: Int,
    val yearOfConstruction: String,
    val numberOfBathrooms: Int,
  ) : HouseFormEvent

  data object ClearNavigation : HouseFormEvent

  data object Retry : HouseFormEvent

  data object DismissError : HouseFormEvent
}

internal data class HouseFormState(
  val isSubleted: Boolean? = null,
  val extraBuildings: List<ExtraBuildingInfo> = emptyList(),
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val livingSpaceError: String? = null,
  val ancillaryAreaError: String? = null,
  val yearOfConstructionError: String? = null,
  val isSubletedError: String? = null,
  val isSubmitting: Boolean = false,
  val isLoadingSession: Boolean = true,
  val loadSessionError: Boolean = false,
  val submitError: String? = null,
  val offersToNavigate: OffersNavigationData? = null,
)

internal data class OffersNavigationData(
  val shopSessionId: String,
  val offers: HouseOffers,
)

private class HouseFormPresenter(
  private val productName: String,
  private val createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  private val submitHouseFormAndGetOffersUseCase: SubmitHouseFormAndGetOffersUseCase,
) : MoleculePresenter<HouseFormEvent, HouseFormState> {
  @Composable
  override fun MoleculePresenterScope<HouseFormEvent>.present(lastState: HouseFormState): HouseFormState {
    var currentState by remember { mutableStateOf(lastState) }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var sessionLoadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: HouseFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is HouseFormEvent.UpdateIsSubleted -> {
          currentState = currentState.copy(isSubleted = event.value, isSubletedError = null)
        }

        is HouseFormEvent.AddExtraBuilding -> {
          currentState = currentState.copy(extraBuildings = currentState.extraBuildings + event.building)
        }

        is HouseFormEvent.RemoveExtraBuilding -> {
          currentState = currentState.copy(
            extraBuildings = currentState.extraBuildings.filterNot { it == event.building },
          )
        }

        is HouseFormEvent.SubmitForm -> {
          val errors = validate(event, currentState)
          currentState = currentState.copy(
            streetError = errors.streetError,
            zipCodeError = errors.zipCodeError,
            livingSpaceError = errors.livingSpaceError,
            ancillaryAreaError = errors.ancillaryAreaError,
            yearOfConstructionError = errors.yearOfConstructionError,
            isSubletedError = errors.isSubletedError,
          )
          if (!errors.hasErrors()) {
            pendingSubmit = event
            submitIteration++
          }
        }

        HouseFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }

        HouseFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            sessionLoadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }

        HouseFormEvent.DismissError -> {
          currentState = currentState.copy(submitError = null)
        }
      }
    }

    LaunchedEffect(sessionLoadIteration) {
      currentState = currentState.copy(isLoadingSession = true, loadSessionError = false)
      createHouseSessionAndPriceIntentUseCase.invoke(productName).fold(
        ifLeft = {
          currentState = currentState.copy(isLoadingSession = false, loadSessionError = true)
        },
        ifRight = { result ->
          sessionAndIntent = result
          currentState = currentState.copy(isLoadingSession = false, loadSessionError = false)
        },
      )
    }

    LaunchedEffect(submitIteration) {
      val submit = pendingSubmit ?: return@LaunchedEffect
      val session = sessionAndIntent ?: return@LaunchedEffect
      val livingSpace = submit.livingSpace.toIntOrNull() ?: return@LaunchedEffect
      val ancillaryArea = submit.ancillaryArea.toIntOrNull() ?: return@LaunchedEffect
      val yearOfConstruction = submit.yearOfConstruction.toIntOrNull() ?: return@LaunchedEffect
      val isSubleted = currentState.isSubleted ?: return@LaunchedEffect
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)
      submitHouseFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        ssn = session.ssn,
        email = session.email,
        street = submit.street,
        zipCode = submit.zipCode,
        livingSpace = livingSpace,
        ancillaryArea = ancillaryArea,
        numberCoInsured = submit.numberCoInsured,
        yearOfConstruction = yearOfConstruction,
        numberOfBathrooms = submit.numberOfBathrooms,
        isSubleted = isSubleted,
        extraBuildings = currentState.extraBuildings,
      ).fold(
        ifLeft = { error ->
          currentState = currentState.copy(
            isSubmitting = false,
            submitError = error.message ?: "Something went wrong",
          )
        },
        ifRight = { offers ->
          currentState = currentState.copy(
            isSubmitting = false,
            offersToNavigate = OffersNavigationData(
              shopSessionId = session.shopSessionId,
              offers = offers,
            ),
          )
        },
      )
    }

    return currentState
  }
}

private data class ValidationErrors(
  val streetError: String?,
  val zipCodeError: String?,
  val livingSpaceError: String?,
  val ancillaryAreaError: String?,
  val yearOfConstructionError: String?,
  val isSubletedError: String?,
) {
  fun hasErrors(): Boolean = streetError != null ||
    zipCodeError != null ||
    livingSpaceError != null ||
    ancillaryAreaError != null ||
    yearOfConstructionError != null ||
    isSubletedError != null
}

private fun validate(event: HouseFormEvent.SubmitForm, state: HouseFormState): ValidationErrors {
  val currentYear = LocalDate.now().year
  return ValidationErrors(
    // TODO: Add "Enter an address" / "Ange en adress" to Lokalise
    streetError = if (event.street.isBlank()) "Enter an address" else null,
    zipCodeError = when {
      // TODO: Add "Enter a valid zip code (5 digits)" / "Ange ett giltigt postnummer (5 siffror)" to Lokalise
      event.zipCode.length != 5 -> "Enter a valid zip code (5 digits)"

      // TODO: Add "Zip code must contain only digits" / "Postnumret får bara innehålla siffror" to Lokalise
      !event.zipCode.all { it.isDigit() } -> "Zip code must contain only digits"

      else -> null
    },
    livingSpaceError = when (val space = event.livingSpace.toIntOrNull()) {
      // TODO: Add "Enter living space" / "Ange boyta" to Lokalise
      null -> "Enter living space"

      // TODO: Add "Enter a valid living space" / "Ange en giltig boyta" to Lokalise
      !in 1..Int.MAX_VALUE -> "Enter a valid living space"

      else -> null
    },
    ancillaryAreaError = when (val area = event.ancillaryArea.toIntOrNull()) {
      // TODO: Add "Enter ancillary area" / "Ange biyta" to Lokalise
      null -> "Enter ancillary area"

      // TODO: Add "Enter a valid ancillary area" / "Ange en giltig biyta" to Lokalise
      !in 0..Int.MAX_VALUE -> "Enter a valid ancillary area"

      else -> null
    },
    yearOfConstructionError = when (val year = event.yearOfConstruction.toIntOrNull()) {
      // TODO: Add "Enter year of construction" / "Ange byggår" to Lokalise
      null -> "Enter year of construction"

      // TODO: Add "Enter a valid year of construction" / "Ange ett giltigt byggår" to Lokalise
      !in 1700..currentYear -> "Enter a valid year of construction"

      else -> null
    },
    // TODO: Add "Choose an option" / "Välj ett alternativ" to Lokalise
    isSubletedError = if (state.isSubleted == null) "Choose an option" else null,
  )
}

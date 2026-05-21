package com.hedvig.android.feature.purchase.house.ui.vacationhome

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
import com.hedvig.android.feature.purchase.house.data.SubmitVacationHomeFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import java.time.LocalDate

internal class VacationHomeFormViewModel(
  productName: String,
  createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  submitVacationHomeFormAndGetOffersUseCase: SubmitVacationHomeFormAndGetOffersUseCase,
) : MoleculeViewModel<VacationHomeFormEvent, VacationHomeFormState>(
    initialState = VacationHomeFormState(),
    presenter = VacationHomeFormPresenter(
      productName,
      createHouseSessionAndPriceIntentUseCase,
      submitVacationHomeFormAndGetOffersUseCase,
    ),
  )

internal sealed interface VacationHomeFormEvent {
  data class UpdateMultipleOwners(val value: Boolean) : VacationHomeFormEvent

  data class UpdateHasWaterConnected(val value: Boolean) : VacationHomeFormEvent

  data class UpdateIsSubleted(val value: Boolean) : VacationHomeFormEvent

  data class SubmitForm(
    val street: String,
    val zipCode: String,
    val yearOfConstruction: String,
    val livingSpace: String,
    val numberOfBathrooms: Int,
  ) : VacationHomeFormEvent

  data object ClearNavigation : VacationHomeFormEvent

  data object Retry : VacationHomeFormEvent

  data object DismissError : VacationHomeFormEvent
}

internal data class VacationHomeFormState(
  val multipleOwners: Boolean? = null,
  val hasWaterConnected: Boolean? = null,
  val isSubleted: Boolean? = null,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val multipleOwnersError: String? = null,
  val yearOfConstructionError: String? = null,
  val livingSpaceError: String? = null,
  val hasWaterConnectedError: String? = null,
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

private class VacationHomeFormPresenter(
  private val productName: String,
  private val createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  private val submitVacationHomeFormAndGetOffersUseCase: SubmitVacationHomeFormAndGetOffersUseCase,
) : MoleculePresenter<VacationHomeFormEvent, VacationHomeFormState> {
  @Composable
  override fun MoleculePresenterScope<VacationHomeFormEvent>.present(
    lastState: VacationHomeFormState,
  ): VacationHomeFormState {
    var currentState by remember { mutableStateOf(lastState) }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var sessionLoadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: VacationHomeFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is VacationHomeFormEvent.UpdateMultipleOwners -> {
          currentState = currentState.copy(multipleOwners = event.value, multipleOwnersError = null)
        }

        is VacationHomeFormEvent.UpdateHasWaterConnected -> {
          currentState = currentState.copy(hasWaterConnected = event.value, hasWaterConnectedError = null)
        }

        is VacationHomeFormEvent.UpdateIsSubleted -> {
          currentState = currentState.copy(isSubleted = event.value, isSubletedError = null)
        }

        is VacationHomeFormEvent.SubmitForm -> {
          val errors = validate(event, currentState)
          currentState = currentState.copy(
            streetError = errors.streetError,
            zipCodeError = errors.zipCodeError,
            multipleOwnersError = errors.multipleOwnersError,
            yearOfConstructionError = errors.yearOfConstructionError,
            livingSpaceError = errors.livingSpaceError,
            hasWaterConnectedError = errors.hasWaterConnectedError,
            isSubletedError = errors.isSubletedError,
          )
          if (!errors.hasErrors()) {
            pendingSubmit = event
            submitIteration++
          }
        }

        VacationHomeFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }

        VacationHomeFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            sessionLoadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }

        VacationHomeFormEvent.DismissError -> {
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
      val multipleOwners = currentState.multipleOwners ?: return@LaunchedEffect
      val yearOfConstruction = submit.yearOfConstruction.toIntOrNull() ?: return@LaunchedEffect
      val livingSpace = submit.livingSpace.toIntOrNull() ?: return@LaunchedEffect
      val hasWaterConnected = currentState.hasWaterConnected ?: return@LaunchedEffect
      val isSubleted = currentState.isSubleted ?: return@LaunchedEffect
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)
      submitVacationHomeFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        ssn = session.ssn,
        email = session.email,
        street = submit.street,
        zipCode = submit.zipCode,
        multipleOwners = multipleOwners,
        yearOfConstruction = yearOfConstruction,
        livingSpace = livingSpace,
        hasWaterConnected = hasWaterConnected,
        numberOfBathrooms = submit.numberOfBathrooms,
        isSubleted = isSubleted,
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
  val multipleOwnersError: String?,
  val yearOfConstructionError: String?,
  val livingSpaceError: String?,
  val hasWaterConnectedError: String?,
  val isSubletedError: String?,
) {
  fun hasErrors(): Boolean = streetError != null ||
    zipCodeError != null ||
    multipleOwnersError != null ||
    yearOfConstructionError != null ||
    livingSpaceError != null ||
    hasWaterConnectedError != null ||
    isSubletedError != null
}

private fun validate(event: VacationHomeFormEvent.SubmitForm, state: VacationHomeFormState): ValidationErrors {
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
    // TODO: Add "Choose an option" / "Välj ett alternativ" to Lokalise
    multipleOwnersError = if (state.multipleOwners == null) "Choose an option" else null,
    yearOfConstructionError = when (val year = event.yearOfConstruction.toIntOrNull()) {
      // TODO: Add "Enter year of construction" / "Ange byggår" to Lokalise
      null -> "Enter year of construction"

      // TODO: Add "Enter a valid year of construction" / "Ange ett giltigt byggår" to Lokalise
      !in 1700..currentYear -> "Enter a valid year of construction"

      else -> null
    },
    livingSpaceError = when (val space = event.livingSpace.toIntOrNull()) {
      // TODO: Add "Enter living space" / "Ange boyta" to Lokalise
      null -> "Enter living space"

      // TODO: Add "Enter a valid living space" / "Ange en giltig boyta" to Lokalise
      !in 1..Int.MAX_VALUE -> "Enter a valid living space"

      else -> null
    },
    hasWaterConnectedError = if (state.hasWaterConnected == null) "Choose an option" else null,
    isSubletedError = if (state.isSubleted == null) "Choose an option" else null,
  )
}

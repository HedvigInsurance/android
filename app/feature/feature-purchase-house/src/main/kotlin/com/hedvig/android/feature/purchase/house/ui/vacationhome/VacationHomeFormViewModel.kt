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
  data class SubmitForm(
    val street: String,
    val zipCode: String,
    val multipleOwners: Boolean?,
    val yearOfConstruction: String,
    val livingSpace: String,
    val hasWaterConnected: Boolean?,
    val numberOfBathrooms: Int,
    val isSubleted: Boolean?,
  ) : VacationHomeFormEvent

  data object ClearNavigation : VacationHomeFormEvent

  data object Retry : VacationHomeFormEvent

  data object DismissError : VacationHomeFormEvent
}

internal data class VacationHomeFormState(
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
        is VacationHomeFormEvent.SubmitForm -> {
          val errors = validate(
            street = event.street,
            zipCode = event.zipCode,
            multipleOwners = event.multipleOwners,
            yearOfConstruction = event.yearOfConstruction,
            livingSpace = event.livingSpace,
            hasWaterConnected = event.hasWaterConnected,
            isSubleted = event.isSubleted,
          )
          if (errors.hasErrors()) {
            currentState = currentState.copy(
              streetError = errors.streetError,
              zipCodeError = errors.zipCodeError,
              multipleOwnersError = errors.multipleOwnersError,
              yearOfConstructionError = errors.yearOfConstructionError,
              livingSpaceError = errors.livingSpaceError,
              hasWaterConnectedError = errors.hasWaterConnectedError,
              isSubletedError = errors.isSubletedError,
            )
          } else {
            currentState = currentState.copy(
              streetError = null,
              zipCodeError = null,
              multipleOwnersError = null,
              yearOfConstructionError = null,
              livingSpaceError = null,
              hasWaterConnectedError = null,
              isSubletedError = null,
            )
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
      val multipleOwners = submit.multipleOwners ?: return@LaunchedEffect
      val yearOfConstruction = submit.yearOfConstruction.toIntOrNull() ?: return@LaunchedEffect
      val livingSpace = submit.livingSpace.toIntOrNull() ?: return@LaunchedEffect
      val hasWaterConnected = submit.hasWaterConnected ?: return@LaunchedEffect
      val isSubleted = submit.isSubleted ?: return@LaunchedEffect
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

private fun validate(
  street: String,
  zipCode: String,
  multipleOwners: Boolean?,
  yearOfConstruction: String,
  livingSpace: String,
  hasWaterConnected: Boolean?,
  isSubleted: Boolean?,
): ValidationErrors {
  val currentYear = LocalDate.now().year
  return ValidationErrors(
    streetError = if (street.isBlank()) "Ange en adress" else null,
    zipCodeError = when {
      zipCode.length != 5 -> "Ange ett giltigt postnummer (5 siffror)"
      !zipCode.all { it.isDigit() } -> "Postnumret får bara innehålla siffror"
      else -> null
    },
    multipleOwnersError = if (multipleOwners == null) "Välj ett alternativ" else null,
    yearOfConstructionError = when (val year = yearOfConstruction.toIntOrNull()) {
      null -> "Ange byggår"
      !in 1700..currentYear -> "Ange ett giltigt byggår"
      else -> null
    },
    livingSpaceError = when (val space = livingSpace.toIntOrNull()) {
      null -> "Ange boyta"
      !in 1..Int.MAX_VALUE -> "Ange en giltig boyta"
      else -> null
    },
    hasWaterConnectedError = if (hasWaterConnected == null) "Välj ett alternativ" else null,
    isSubletedError = if (isSubleted == null) "Välj ett alternativ" else null,
  )
}

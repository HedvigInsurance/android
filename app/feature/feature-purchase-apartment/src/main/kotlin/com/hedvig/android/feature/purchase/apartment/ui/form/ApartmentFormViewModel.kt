package com.hedvig.android.feature.purchase.apartment.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.apartment.data.ApartmentOffers
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.apartment.data.SessionAndIntent
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class ApartmentFormViewModel(
  productName: String,
  createSessionAndPriceIntentUseCase: CreateSessionAndPriceIntentUseCase,
  submitFormAndGetOffersUseCase: SubmitFormAndGetOffersUseCase,
) : MoleculeViewModel<ApartmentFormEvent, ApartmentFormState>(
    initialState = ApartmentFormState(),
    presenter = ApartmentFormPresenter(productName, createSessionAndPriceIntentUseCase, submitFormAndGetOffersUseCase),
  )

internal sealed interface ApartmentFormEvent {
  data class UpdateStreet(val value: String) : ApartmentFormEvent
  data class UpdateZipCode(val value: String) : ApartmentFormEvent
  data class UpdateLivingSpace(val value: String) : ApartmentFormEvent
  data class UpdateNumberCoInsured(val value: Int) : ApartmentFormEvent
  data object Submit : ApartmentFormEvent
  data object ClearNavigation : ApartmentFormEvent
  data object Retry : ApartmentFormEvent
}

internal data class ApartmentFormState(
  val street: String = "",
  val zipCode: String = "",
  val livingSpace: String = "",
  val numberCoInsured: Int = 0,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val livingSpaceError: String? = null,
  val isSubmitting: Boolean = false,
  val isLoadingSession: Boolean = true,
  val loadSessionError: Boolean = false,
  val submitError: String? = null,
  val offersToNavigate: OffersNavigationData? = null,
)

internal data class OffersNavigationData(
  val shopSessionId: String,
  val offers: ApartmentOffers,
)

private class ApartmentFormPresenter(
  private val productName: String,
  private val createSessionAndPriceIntentUseCase: CreateSessionAndPriceIntentUseCase,
  private val submitFormAndGetOffersUseCase: SubmitFormAndGetOffersUseCase,
) : MoleculePresenter<ApartmentFormEvent, ApartmentFormState> {
  @Composable
  override fun MoleculePresenterScope<ApartmentFormEvent>.present(
    lastState: ApartmentFormState,
  ): ApartmentFormState {
    var currentState by remember { mutableStateOf(lastState) }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var sessionLoadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var shouldSubmit by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        is ApartmentFormEvent.UpdateStreet -> {
          currentState = currentState.copy(street = event.value, streetError = null)
        }
        is ApartmentFormEvent.UpdateZipCode -> {
          currentState = currentState.copy(zipCode = event.value, zipCodeError = null)
        }
        is ApartmentFormEvent.UpdateLivingSpace -> {
          currentState = currentState.copy(livingSpace = event.value, livingSpaceError = null)
        }
        is ApartmentFormEvent.UpdateNumberCoInsured -> {
          currentState = currentState.copy(numberCoInsured = event.value.coerceAtLeast(0))
        }
        ApartmentFormEvent.Submit -> {
          val errors = validate(currentState)
          if (errors.hasErrors()) {
            currentState = currentState.copy(
              streetError = errors.streetError,
              zipCodeError = errors.zipCodeError,
              livingSpaceError = errors.livingSpaceError,
            )
          } else {
            shouldSubmit = true
            submitIteration++
          }
        }
        ApartmentFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }
        ApartmentFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            sessionLoadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }
      }
    }

    LaunchedEffect(sessionLoadIteration) {
      currentState = currentState.copy(isLoadingSession = true, loadSessionError = false)
      createSessionAndPriceIntentUseCase.invoke(productName).fold(
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
      if (!shouldSubmit) return@LaunchedEffect
      val session = sessionAndIntent ?: return@LaunchedEffect
      shouldSubmit = false
      currentState = currentState.copy(isSubmitting = true, submitError = null)
      submitFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        street = currentState.street,
        zipCode = currentState.zipCode,
        livingSpace = currentState.livingSpace.toInt(),
        numberCoInsured = currentState.numberCoInsured,
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
) {
  fun hasErrors(): Boolean = streetError != null || zipCodeError != null || livingSpaceError != null
}

private fun validate(state: ApartmentFormState): ValidationErrors {
  return ValidationErrors(
    streetError = if (state.street.isBlank()) "Ange en adress" else null,
    zipCodeError = when {
      state.zipCode.length != 5 -> "Ange ett giltigt postnummer (5 siffror)"
      !state.zipCode.all { it.isDigit() } -> "Postnumret får bara innehålla siffror"
      else -> null
    },
    livingSpaceError = when {
      state.livingSpace.isBlank() -> "Ange boyta"
      state.livingSpace.toIntOrNull() == null -> "Ange ett giltigt tal"
      state.livingSpace.toInt() <= 0 -> "Boytan måste vara större än 0"
      else -> null
    },
  )
}

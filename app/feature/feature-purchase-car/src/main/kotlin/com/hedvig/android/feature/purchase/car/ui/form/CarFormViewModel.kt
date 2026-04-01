package com.hedvig.android.feature.purchase.car.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.car.data.CarOffers
import com.hedvig.android.feature.purchase.car.data.CreateCarSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.car.data.SessionAndIntent
import com.hedvig.android.feature.purchase.car.data.SubmitCarFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class CarFormViewModel(
  productName: String,
  createCarSessionAndPriceIntentUseCase: CreateCarSessionAndPriceIntentUseCase,
  submitCarFormAndGetOffersUseCase: SubmitCarFormAndGetOffersUseCase,
) : MoleculeViewModel<CarFormEvent, CarFormState>(
    initialState = CarFormState(),
    presenter = CarFormPresenter(productName, createCarSessionAndPriceIntentUseCase, submitCarFormAndGetOffersUseCase),
  )

internal sealed interface CarFormEvent {
  data class SubmitForm(
    val ssn: String,
    val registrationNumber: String,
    val mileage: Int?,
    val street: String,
    val zipCode: String,
    val email: String,
  ) : CarFormEvent

  data object ClearNavigation : CarFormEvent

  data object Retry : CarFormEvent

  data object DismissError : CarFormEvent
}

internal data class CarFormState(
  val ssnError: String? = null,
  val registrationNumberError: String? = null,
  val mileageError: String? = null,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val emailError: String? = null,
  val isSubmitting: Boolean = false,
  val isLoadingSession: Boolean = true,
  val loadSessionError: Boolean = false,
  val submitError: String? = null,
  val offersToNavigate: OffersNavigationData? = null,
)

internal data class OffersNavigationData(
  val shopSessionId: String,
  val offers: CarOffers,
)

private class CarFormPresenter(
  private val productName: String,
  private val createCarSessionAndPriceIntentUseCase: CreateCarSessionAndPriceIntentUseCase,
  private val submitCarFormAndGetOffersUseCase: SubmitCarFormAndGetOffersUseCase,
) : MoleculePresenter<CarFormEvent, CarFormState> {
  @Composable
  override fun MoleculePresenterScope<CarFormEvent>.present(lastState: CarFormState): CarFormState {
    var currentState by remember { mutableStateOf(lastState) }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var sessionLoadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: CarFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is CarFormEvent.SubmitForm -> {
          val errors = validate(
            event.ssn,
            event.registrationNumber,
            event.mileage,
            event.street,
            event.zipCode,
            event.email,
          )
          if (errors.hasErrors()) {
            currentState = currentState.copy(
              ssnError = errors.ssnError,
              registrationNumberError = errors.registrationNumberError,
              mileageError = errors.mileageError,
              streetError = errors.streetError,
              zipCodeError = errors.zipCodeError,
              emailError = errors.emailError,
            )
          } else {
            currentState = currentState.copy(
              ssnError = null,
              registrationNumberError = null,
              mileageError = null,
              streetError = null,
              zipCodeError = null,
              emailError = null,
            )
            pendingSubmit = event
            submitIteration++
          }
        }

        CarFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }

        CarFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            sessionLoadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }

        CarFormEvent.DismissError -> {
          currentState = currentState.copy(submitError = null)
        }
      }
    }

    LaunchedEffect(sessionLoadIteration) {
      currentState = currentState.copy(isLoadingSession = true, loadSessionError = false)
      createCarSessionAndPriceIntentUseCase.invoke(productName).fold(
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
      val mileage = submit.mileage ?: return@LaunchedEffect
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)
      submitCarFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        ssn = submit.ssn,
        registrationNumber = submit.registrationNumber,
        mileage = mileage,
        street = submit.street,
        zipCode = submit.zipCode,
        email = submit.email,
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
  val ssnError: String?,
  val registrationNumberError: String?,
  val mileageError: String?,
  val streetError: String?,
  val zipCodeError: String?,
  val emailError: String?,
) {
  fun hasErrors(): Boolean = ssnError != null ||
    registrationNumberError != null ||
    mileageError != null ||
    streetError != null ||
    zipCodeError != null ||
    emailError != null
}

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

private fun validate(
  ssn: String,
  registrationNumber: String,
  mileage: Int?,
  street: String,
  zipCode: String,
  email: String,
): ValidationErrors {
  return ValidationErrors(
    ssnError = when {
      ssn.length != 12 -> "Ange ett giltigt personnummer (12 siffror)"
      !ssn.all { it.isDigit() } -> "Personnumret f\u00e5r bara inneh\u00e5lla siffror"
      else -> null
    },
    registrationNumberError = when {
      registrationNumber.isBlank() -> "Ange registreringsnummer"
      registrationNumber.replace(" ", "").length != 6 -> "Ange ett giltigt registreringsnummer (t.ex. ABC 123)"
      else -> null
    },
    mileageError = if (mileage == null) "V\u00e4lj miltal" else null,
    streetError = if (street.isBlank()) "Ange en adress" else null,
    zipCodeError = when {
      zipCode.length != 5 -> "Ange ett giltigt postnummer (5 siffror)"
      !zipCode.all { it.isDigit() } -> "Postnumret f\u00e5r bara inneh\u00e5lla siffror"
      else -> null
    },
    emailError = when {
      email.isBlank() -> "Ange en e-postadress"
      !EMAIL_REGEX.matches(email) -> "Ange en giltig e-postadress"
      else -> null
    },
  )
}

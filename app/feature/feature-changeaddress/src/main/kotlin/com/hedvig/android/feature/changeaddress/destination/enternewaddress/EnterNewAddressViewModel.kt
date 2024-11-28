package com.hedvig.android.feature.changeaddress.destination.enternewaddress

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.HousingType.VILLA
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeIsStudent
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeMoveDate
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangePostalCode
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeSquareMeters
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ChangeStreet
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ClearNavParams
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.DismissErrorDialog
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.OnCoInsuredDecreased
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.OnCoInsuredIncreased
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressEvent.ValidateInput
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.feature.changeaddress.navigation.NewAddressParameters
import com.hedvig.android.feature.changeaddress.navigation.SelectHousingTypeParameters
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal class EnterNewAddressViewModel(
  previousParameters: SelectHousingTypeParameters,
  languageService: LanguageService,
) : MoleculeViewModel<EnterNewAddressEvent, EnterNewAddressUiState>(
    initialState = EnterNewAddressUiState(
      datePickerUiState = DatePickerUiState(
        locale = languageService.getLocale(),
        initiallySelectedDate = null,
        minDate = previousParameters.minDate,
        maxDate = previousParameters.maxDate,
      ),
      maxNumberCoInsured = previousParameters.maxNumberCoInsured,
      maxSquareMeters = previousParameters.maxSquareMeters,
      numberInsured = ValidatedInput(previousParameters.suggestedNumberInsured),
      oldAddressCoverageDurationDays = previousParameters.oldAddressCoverageDurationDays,
    ),
    presenter = EnterNewAddressPresenter(
      previousParameters,
    ),
  )

internal class EnterNewAddressPresenter(
  private val previousParameters: SelectHousingTypeParameters,
) : MoleculePresenter<EnterNewAddressEvent, EnterNewAddressUiState> {
  @Composable
  override fun MoleculePresenterScope<EnterNewAddressEvent>.present(
    lastState: EnterNewAddressUiState,
  ): EnterNewAddressUiState {
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        is ChangeIsStudent -> {
          currentState = currentState.copy(isStudent = event.isStudent)
        }

        is ChangeMoveDate -> {
          currentState = currentState.copy(movingDate = ValidatedInput(event.movingDate))
        }

        is ChangePostalCode -> {
          currentState = currentState.copy(postalCode = ValidatedInput(event.postalCode))
        }

        is ChangeSquareMeters -> {
          currentState = currentState.copy(squareMeters = ValidatedInput(event.squareMeters))
        }

        is ChangeStreet -> {
          currentState = currentState.copy(street = ValidatedInput(event.street))
        }

        DismissErrorDialog -> {
          currentState = currentState.copy(errorMessage = null)
        }

        ValidateInput -> {
          currentState = currentState.validateAddressInput()
          val stateSnapShot = currentState
          val isInputValid = stateSnapShot.isAddressInputValid
          if (isInputValid) {
            val newAddressParams = NewAddressParameters(
              street = stateSnapShot.street.input!!,
              postalCode = stateSnapShot.postalCode.input!!,
              squareMeters = stateSnapShot.squareMeters.input!!,
              numberInsured = stateSnapShot.numberInsured.input,
              isStudent = stateSnapShot.isStudent,
              movingDate = stateSnapShot.movingDate.input!!,
            )
            if (previousParameters.housingType == VILLA) {
              currentState = currentState.copy(
                navParamsForVillaDestination = MovingParameters(
                  villaOnlyParameters = null,
                  selectHousingTypeParameters = previousParameters,
                  newAddressParameters = newAddressParams,
                ),
              )
            } else {
              val params = MovingParameters(
                selectHousingTypeParameters = previousParameters,
                newAddressParameters = newAddressParams,
                villaOnlyParameters = null,
              )
              currentState = currentState.copy(navParamsForOfferDestination = params)
            }
          }
        }

        ClearNavParams -> {
          currentState = currentState.copy(
            navParamsForVillaDestination = null,
            navParamsForOfferDestination = null,
          )
        }

        OnCoInsuredDecreased -> {
          val currentInsured = currentState.numberInsured.input.toInt()
          if (currentInsured > 1) {
            val insuredNumberDecreased = currentInsured - 1
            currentState = currentState.copy(numberInsured = ValidatedInput(insuredNumberDecreased.toString()))
          }
        }

        OnCoInsuredIncreased -> {
          val currentInsured = currentState.numberInsured.input.toInt()
          val currentCoInsuredMax = currentState.maxNumberCoInsured
          if (currentCoInsuredMax == null || (currentInsured < (currentCoInsuredMax + 1))) {
            val insuredNumberIncreased = currentInsured + 1
            currentState = currentState.copy(numberInsured = ValidatedInput(insuredNumberIncreased.toString()))
          }
        }
      }
    }

    return currentState
  }
}

internal data class EnterNewAddressUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val street: ValidatedInput<String?> = ValidatedInput(null),
  val postalCode: ValidatedInput<String?> = ValidatedInput(null),
  val squareMeters: ValidatedInput<String?> = ValidatedInput(null),
  val numberInsured: ValidatedInput<String> = ValidatedInput("1"),
  val datePickerUiState: DatePickerUiState? = null,
  val isEligibleForStudent: Boolean = false,
  val isStudent: Boolean = false,
  val movingDate: ValidatedInput<LocalDate?> = ValidatedInput(null),
  val maxNumberCoInsured: Int? = null,
  val maxSquareMeters: Int? = null,
  val oldAddressCoverageDurationDays: Int? = null,
  val navParamsForVillaDestination: MovingParameters? = null,
  val navParamsForOfferDestination: MovingParameters? = null,
) {
  val isAddressInputValid: Boolean
    get() {
      return street.errorMessageRes == null &&
        postalCode.errorMessageRes == null &&
        squareMeters.errorMessageRes == null &&
        isSquareMetersWithinBounds(squareMeters.input?.toIntOrNull()) &&
        movingDate.errorMessageRes == null &&
        numberInsured.errorMessageRes == null
    }

  fun validateAddressInput(): EnterNewAddressUiState {
    return copy(
      street = street.copy(
        errorMessageRes = if (!street.isPresent || street.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_STREET_ERROR
        } else {
          null
        },
      ),
      postalCode = postalCode.copy(
        errorMessageRes = if (!postalCode.isPresent || postalCode.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_POSTAL_CODE_ERROR
        } else {
          null
        },
      ),
      squareMeters = squareMeters.copy(
        errorMessageRes = if (!squareMeters.isPresent || squareMeters.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
        } else if (squareMeters.isPresent && !isSquareMetersWithinBounds(squareMeters.input!!.toIntOrNull())) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_OVER_LIMIT_ERROR
        } else {
          null
        },
      ),
      movingDate = movingDate.copy(
        errorMessageRes = if (!movingDate.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_MOVING_DATE_ERROR
        } else {
          null
        },
      ),
      numberInsured = numberInsured.copy(
        errorMessageRes = if (!numberInsured.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_ERROR
        } else if (numberInsured.isPresent && !isNumberInsuredWithinBounds(numberInsured.input.toIntOrNull())) {
          hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_MAX_ERROR_ALTERNATIVE
        } else {
          null
        },
      ),
    )
  }

  private fun isSquareMetersWithinBounds(squareMeters: Int?): Boolean {
    if (maxSquareMeters == null) {
      return true
    }
    if (squareMeters == null) {
      return false
    }
    return squareMeters <= maxSquareMeters
  }

  private fun isNumberInsuredWithinBounds(numberInsured: Int?): Boolean {
    if (maxNumberCoInsured == null) {
      return true
    }
    if (numberInsured == null) {
      return false
    }
    return numberInsured <= maxNumberCoInsured + 1
  }
}

internal sealed interface EnterNewAddressEvent {
  data object DismissErrorDialog : EnterNewAddressEvent

  data object OnCoInsuredDecreased : EnterNewAddressEvent

  data object OnCoInsuredIncreased : EnterNewAddressEvent

  data class ChangeSquareMeters(val squareMeters: String) : EnterNewAddressEvent

  data class ChangePostalCode(val postalCode: String) : EnterNewAddressEvent

  data class ChangeStreet(val street: String) : EnterNewAddressEvent

  data class ChangeIsStudent(val isStudent: Boolean) : EnterNewAddressEvent

  data class ChangeMoveDate(val movingDate: LocalDate) : EnterNewAddressEvent

  data object ValidateInput : EnterNewAddressEvent

  data object ClearNavParams : EnterNewAddressEvent
}

@Stable
internal class DatePickerUiState(
  locale: Locale,
  initiallySelectedDate: LocalDate?,
  minDate: LocalDate = LocalDate(1900, 1, 1),
  maxDate: LocalDate = LocalDate(2100, 1, 1),
) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearRange = minDate.year..maxDate.year

  val datePickerState = DatePickerState(
    locale = locale,
    initialSelectedDateMillis = initiallySelectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    initialDisplayedMonthMillis = null,
    yearRange = yearRange,
    initialDisplayMode = DisplayMode.Picker,
    selectableDates = object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in minDateInMillis..maxDateInMillis

      override fun isSelectableYear(year: Int) = year in yearRange
    },
  )
}

package com.hedvig.android.feature.movingflow.ui.enternewaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.feature.movingflow.MovingFlowDestinations
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType.BRF
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType.RENT
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.Available
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.NotAvailable
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent.NavigatedToChoseCoverage
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent.Submit
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType.Apartment
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType.House
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.SubmittingInfoFailure
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Loading
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.EmptyAddress
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidMovingDate
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidNumberCoInsured
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.InvalidLength
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.Missing
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.MustBeOnlyDigits
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidSquareMeters
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.type.MoveApartmentSubType
import octopus.type.MoveApiVersion
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToApartmentInput

internal class EnterNewAddressViewModel(
  savedStateHandle: SavedStateHandle,
  movingFlowStorage: MovingFlowStorage,
  apolloClient: ApolloClient,
) : MoleculeViewModel<EnterNewAddressEvent, EnterNewAddressUiState>(
    EnterNewAddressUiState.Loading,
    EnterNewAddressPresenter(
      savedStateHandle.toRoute<MovingFlowDestinations.EnterNewAddress>().moveIntentId,
      movingFlowStorage,
      apolloClient,
    ),
  )

private class EnterNewAddressPresenter(
  private val moveIntentId: String,
  private val movingFlowStorage: MovingFlowStorage,
  private val apolloClient: ApolloClient,
) : MoleculePresenter<EnterNewAddressEvent, EnterNewAddressUiState> {
  @Composable
  override fun MoleculePresenterScope<EnterNewAddressEvent>.present(
    lastState: EnterNewAddressUiState,
  ): EnterNewAddressUiState {
    var content: Option<EnterNewAddressUiState.Content?> by remember {
      mutableStateOf(
        when (lastState) {
          Loading -> None
          MissingOngoingMovingFlow -> Option(null)
          is Content -> Option(lastState)
        },
      )
    }
    var submittingInfoFailure: SubmittingInfoFailure? by remember { mutableStateOf(null) }
    var navigateToChoseCoverage by remember { mutableStateOf(false) }
    var navigateToAddHouseInformation by remember { mutableStateOf(false) }
    var inputForSubmission: InputForSubmission? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
      movingFlowStorage
        .movingFlowState()
        .collectLatest {
          content = Option(it?.toContent())
        }
    }

    CollectEvents { event ->
      when (event) {
        NavigatedToChoseCoverage -> {
          navigateToChoseCoverage = false
        }

        DismissSubmissionError -> {
          submittingInfoFailure = null
        }

        Submit -> {
          @Suppress("NAME_SHADOWING")
          val content = content.getOrNull() ?: return@CollectEvents
          val allAreValid = listOf(
            content.movingDate,
            content.address,
            content.postalCode,
            content.squareMeters,
            content.numberCoInsured,
          ).onEach { validatedInput ->
            validatedInput.validate()
          }.all { validatedInput ->
            validatedInput.isValid
          }
          if (!allAreValid) return@CollectEvents
          // todo 1. store ephemeral data into the DataStore here for both apartment and house
          when (content.propertyType) {
            House -> {
              navigateToAddHouseInformation = true
            }

            is Apartment -> {
              inputForSubmission = content.toInputForSubmission(content.propertyType.apartmentType)
            }
          }
        }
      }
    }

    if (inputForSubmission != null) {
      LaunchedEffect(inputForSubmission) {
        @Suppress("NAME_SHADOWING")
        val inputForSubmission = inputForSubmission ?: return@LaunchedEffect
        apolloClient
          .mutation(MoveIntentV2RequestMutation(moveIntentId, inputForSubmission.moveIntentRequestInput))
          .safeExecute()
          .map { it.moveIntentRequest }
          .fold(
            ifLeft = {
              submittingInfoFailure = SubmittingInfoFailure.NetworkFailure
            },
            ifRight = { request ->
              when (val moveIntent = request.moveIntent) {
                null -> submittingInfoFailure = when (val errorMessage = request.userError?.message) {
                  null -> SubmittingInfoFailure.NetworkFailure
                  else -> SubmittingInfoFailure.UserError(errorMessage)
                }

                else -> {
                  // todo 2. otherwise pehraps store them here instead.
                  movingFlowStorage.updateMoveIntentAfterRequest(
                    moveIntent,
                    moveIntent,
                    when (inputForSubmission.apartmentType) {
                      RENT -> HousingType.ApartmentRent
                      BRF -> HousingType.ApartmentOwn
                    },
                  )
                  navigateToChoseCoverage = true
                }
              }
            },
          )
      }
    }

    return when (val contentValue = content) {
      None -> EnterNewAddressUiState.Loading
      is Some -> {
        when (val state = contentValue.value) {
          null -> EnterNewAddressUiState.MissingOngoingMovingFlow
          else -> state.copy(
            submittingInfoFailure = submittingInfoFailure,
            navigateToChoseCoverage = navigateToChoseCoverage,
            navigateToAddHouseInformation = navigateToAddHouseInformation,
            isLoadingNextStep = inputForSubmission != null,
          )
        }
      }
    }
  }
}

// TODO consider validate() and convert to a null-safe new object instead of `!!`
private fun Content.toInputForSubmission(apartmentType: ApartmentType): InputForSubmission {
  return InputForSubmission(
    apartmentType = apartmentType,
    moveIntentRequestInput = MoveIntentRequestInput(
      apiVersion = com.apollographql.apollo.api.Optional.present(MoveApiVersion.V2_TIERS_AND_DEDUCTIBLES),
      moveToAddress = MoveToAddressInput(
        street = address.value!!,
        postalCode = postalCode.value!!,
        city = com.apollographql.apollo.api.Optional.absent(),
      ),
      moveFromAddressId = moveFromAddressId,
      movingDate = movingDate.value,
      numberCoInsured = numberCoInsured.value,
      squareMeters = squareMeters.value!!,
      apartment = com.apollographql.apollo.api.Optional.present(
        when (propertyType) {
          House -> error("Should have navigated to house input instead of submitting here")
          is Apartment -> {
            when (propertyType.apartmentType) {
              RENT -> MoveToApartmentInput(
                subType = MoveApartmentSubType.RENT,
                isStudent = propertyType.isStudentSelected,
              )

              BRF -> MoveToApartmentInput(
                subType = MoveApartmentSubType.OWN,
                isStudent = propertyType.isStudentSelected,
              )
            }
          }
        },
      ),
      house = com.apollographql.apollo.api.Optional.absent(),
    ),
  )
}

private data class InputForSubmission(
  val apartmentType: ApartmentType,
  val moveIntentRequestInput: MoveIntentRequestInput,
)

internal sealed interface EnterNewAddressEvent {
  data object Submit : EnterNewAddressEvent

  data object NavigatedToChoseCoverage : EnterNewAddressEvent

  data object DismissSubmissionError : EnterNewAddressEvent
}

@Stable
internal sealed interface EnterNewAddressUiState {
  data object MissingOngoingMovingFlow : EnterNewAddressUiState

  data object Loading : EnterNewAddressUiState

  data class Content(
    val moveIntentId: String,
    val moveFromAddressId: String,
    val movingDate: ValidatedInput<LocalDate, EnterNewAddressValidationError>,
    val address: ValidatedInput<String?, EnterNewAddressValidationError>,
    val postalCode: ValidatedInput<String?, EnterNewAddressValidationError>,
    val squareMeters: ValidatedInput<Int?, EnterNewAddressValidationError>,
    val numberCoInsured: ValidatedInput<Int, EnterNewAddressValidationError>,
    val propertyType: PropertyType,
    val submittingInfoFailure: SubmittingInfoFailure?,
    val isLoadingNextStep: Boolean,
    val navigateToChoseCoverage: Boolean,
    val navigateToAddHouseInformation: Boolean,
  ) : EnterNewAddressUiState {
    val shouldDisableInput: Boolean = submittingInfoFailure != null ||
      isLoadingNextStep == true ||
      navigateToChoseCoverage == true ||
      navigateToAddHouseInformation == true

    sealed interface PropertyType {
      data object House : PropertyType

      sealed interface Apartment : PropertyType {
        val apartmentType: ApartmentType
        val isStudentSelected: Boolean

        data class WithoutStudentOption(override val apartmentType: ApartmentType) : Apartment {
          override val isStudentSelected: Boolean = false
        }

        data class WithStudentOption(
          override val apartmentType: ApartmentType,
          val selectedIsStudent: ValidatedInput<Boolean, EnterNewAddressValidationError>,
        ) : Apartment {
          override val isStudentSelected: Boolean
            get() = selectedIsStudent.value
        }
      }
    }

    sealed interface SubmittingInfoFailure {
      data object NetworkFailure : SubmittingInfoFailure

      data class UserError(val message: String) : SubmittingInfoFailure
    }
  }
}

private fun MovingFlowState.toContent(): EnterNewAddressUiState.Content {
  return EnterNewAddressUiState.Content(
    moveIntentId = id,
    moveFromAddressId = moveFromAddressId,
    movingDate = ValidatedInput(
      initialValue = movingDateState.selectedMovingDate,
      validators = listOf(
        { movingDate ->
          InvalidMovingDate(movingDateState.allowedMovingDateRange).takeIf {
            movingDate !in movingDateState.allowedMovingDateRange
          }
        },
      ),
    ),
    address = ValidatedInput(
      initialValue = addressInfo.street,
      validators = listOf(
        { address ->
          EmptyAddress.takeIf { address.isNullOrBlank() }
        },
      ),
    ),
    postalCode = ValidatedInput(
      initialValue = addressInfo.postalCode,
      validators = listOf(
        { postalCode ->
          when {
            postalCode == null -> Missing
            postalCode.length != 5 -> InvalidLength
            !postalCode.isDigitsOnly() -> MustBeOnlyDigits
            else -> null
          }
        },
      ),
    ),
    squareMeters = ValidatedInput(
      propertyState.squareMetersState.selectedSquareMeters,
      listOf(
        { squareMeters ->
          InvalidSquareMeters(propertyState.squareMetersState.allowedSquareMetersRange).takeIf {
            squareMeters == null || squareMeters !in propertyState.squareMetersState.allowedSquareMetersRange
          }
        },
      ),
    ),
    numberCoInsured = ValidatedInput(
      propertyState.numberCoInsuredState.selectedNumberCoInsured,
      listOf(
        { numberCoInsured ->
          InvalidNumberCoInsured(propertyState.numberCoInsuredState.allowedNumberCoInsuredRange)
            .takeIf {
              numberCoInsured !in propertyState.numberCoInsuredState.allowedNumberCoInsuredRange
            }
        },
      ),
    ),
    propertyType = when (propertyState) {
      is HouseState -> PropertyType.House
      is ApartmentState -> {
        when (propertyState.isAvailableForStudentState) {
          NotAvailable -> PropertyType.Apartment.WithoutStudentOption(
            propertyState.apartmentType,
          )

          is Available -> PropertyType.Apartment.WithStudentOption(
            propertyState.apartmentType,
            ValidatedInput<Boolean, EnterNewAddressValidationError>(
              propertyState.isAvailableForStudentState.selectedIsStudent,
              emptyList(),
            ),
          )
        }
      }
    },
    submittingInfoFailure = null,
    isLoadingNextStep = false,
    navigateToChoseCoverage = false,
    navigateToAddHouseInformation = false,
  )
}

internal sealed interface EnterNewAddressValidationError {
  data class InvalidMovingDate(
    val allowedMovingDateRange: ClosedRange<LocalDate>,
  ) : EnterNewAddressValidationError

  data class InvalidSquareMeters(
    val allowedSquareMetersRange: ClosedRange<Int>,
  ) : EnterNewAddressValidationError

  data class InvalidNumberCoInsured(
    val allowedNumberCoInsuredRange: ClosedRange<Int>,
  ) : EnterNewAddressValidationError

  sealed interface InvalidPostalCode : EnterNewAddressValidationError {
    data object InvalidLength : InvalidPostalCode

    data object Missing : InvalidPostalCode

    data object MustBeOnlyDigits : InvalidPostalCode
  }

  data object EmptyAddress : EnterNewAddressValidationError
}

package com.hedvig.android.feature.movingflow.ui.enternewaddress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.merge
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.feature.movingflow.MovingFlowDestinations
import com.hedvig.android.feature.movingflow.compose.BooleanInput
import com.hedvig.android.feature.movingflow.compose.ConstrainedNumberInput
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType.BRF
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType.RENT
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.Available
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.NotAvailable
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent.NavigatedToAddHouseInformation
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent.NavigatedToChoseCoverage
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressEvent.Submit
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType.Apartment
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType.Apartment.WithStudentOption
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType.Apartment.WithoutStudentOption
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.PropertyType.House
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Content.SubmittingInfoFailure
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.Loading
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressUiState.MissingOngoingMovingFlow
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.EmptyAddress
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidMovingDate
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.InvalidLength
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.Missing
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidPostalCode.MustBeOnlyDigits
import com.hedvig.android.feature.movingflow.ui.enternewaddress.EnterNewAddressValidationError.InvalidSquareMeters
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.type.MoveApartmentSubType
import octopus.type.MoveApartmentSubType.OWN
import octopus.type.MoveApiVersion
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToApartmentInput

internal class EnterNewAddressViewModel(
  savedStateHandle: SavedStateHandle,
  movingFlowRepository: MovingFlowRepository,
  apolloClient: ApolloClient,
  featureManager: FeatureManager,
) : MoleculeViewModel<EnterNewAddressEvent, EnterNewAddressUiState>(
    Loading,
    EnterNewAddressPresenter(
      savedStateHandle.toRoute<MovingFlowDestinations.EnterNewAddress>().moveIntentId,
      movingFlowRepository,
      apolloClient,
      featureManager,
    ),
  )

private class EnterNewAddressPresenter(
  private val moveIntentId: String,
  private val movingFlowRepository: MovingFlowRepository,
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : MoleculePresenter<EnterNewAddressEvent, EnterNewAddressUiState> {
  @Composable
  override fun MoleculePresenterScope<EnterNewAddressEvent>.present(
    lastState: EnterNewAddressUiState,
  ): EnterNewAddressUiState {
    var content: Option<Content?> by remember {
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
      movingFlowRepository
        .movingFlowState()
        .collectLatest {
          content = Option(it?.toContent())
        }
    }

    val coroutineScope = rememberCoroutineScope()
    CollectEvents { event ->
      when (event) {
        NavigatedToChoseCoverage -> {
          navigateToChoseCoverage = false
        }

        NavigatedToAddHouseInformation -> {
          navigateToAddHouseInformation = false
        }

        DismissSubmissionError -> {
          submittingInfoFailure = null
        }

        Submit -> {
          @Suppress("NAME_SHADOWING")
          val content = content.getOrNull() ?: return@CollectEvents
          val validContent = content.validate()
          if (validContent == null) return@CollectEvents
          coroutineScope.launch {
            movingFlowRepository.updateWithPropertyInput(
              movingDate = validContent.movingDate,
              address = validContent.address,
              postalCode = validContent.postalCode,
              squareMeters = validContent.squareMeters,
              numberCoInsured = validContent.numberCoInsured,
              isStudent = validContent.propertyType.isStudent,
            )
            when (content.propertyType) {
              House -> {
                navigateToAddHouseInformation = true
              }

              is Apartment -> {
                inputForSubmission = validContent.toInputForSubmission()
              }
            }
          }
        }
      }
    }

    if (inputForSubmission != null) {
      LaunchedEffect(inputForSubmission) {
        @Suppress("NAME_SHADOWING")
        val inputForSubmissionValue = inputForSubmission ?: return@LaunchedEffect
        val isAddonFlagEnabled = featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON).first()
        apolloClient
          .mutation(
            MoveIntentV2RequestMutation(
              intentId = moveIntentId,
              moveIntentRequestInput = inputForSubmissionValue.moveIntentRequestInput,
              addonsFlagOn = isAddonFlagEnabled,
            ),
          )
          .safeExecute()
          .map { it.moveIntentRequest }
          .fold(
            ifLeft = {
              submittingInfoFailure = SubmittingInfoFailure.NetworkFailure
            },
            ifRight = { request ->
              when (val moveIntentQuotesFragment = request.moveIntent) {
                null -> submittingInfoFailure = when (val errorMessage = request.userError?.message) {
                  null -> SubmittingInfoFailure.NetworkFailure
                  else -> SubmittingInfoFailure.UserError(errorMessage)
                }

                else -> {
                  movingFlowRepository.updateWithMoveIntentQuotes(moveIntentQuotesFragment)
                  navigateToChoseCoverage = true
                }
              }
            },
          )
        @Suppress("KotlinUnreachableCode")
        inputForSubmission = null
      }
    }

    return when (val contentValue = content) {
      None -> Loading
      is Some -> {
        when (val state = contentValue.value) {
          null -> MissingOngoingMovingFlow
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

private fun ValidContent.toInputForSubmission(): InputForSubmission {
  return InputForSubmission(
    moveIntentRequestInput = MoveIntentRequestInput(
      apiVersion = Optional.present(MoveApiVersion.V2_TIERS_AND_DEDUCTIBLES),
      moveToAddress = MoveToAddressInput(
        street = address,
        postalCode = postalCode,
        city = Optional.absent(),
      ),
      moveFromAddressId = moveFromAddressId,
      movingDate = movingDate,
      numberCoInsured = numberCoInsured,
      squareMeters = squareMeters,
      apartment = Optional.present(
        when (propertyType) {
          ValidContent.PropertyType.House -> error("Should have navigated to house input instead of submitting here")
          is ValidContent.PropertyType.Apartment -> {
            when (propertyType.apartmentType) {
              RENT -> MoveToApartmentInput(
                subType = MoveApartmentSubType.RENT,
                isStudent = propertyType.isStudent,
              )

              BRF -> MoveToApartmentInput(
                subType = OWN,
                isStudent = propertyType.isStudent,
              )
            }
          }
        },
      ),
      house = Optional.absent(),
    ),
  )
}

private data class InputForSubmission(
  val moveIntentRequestInput: MoveIntentRequestInput,
)

internal sealed interface EnterNewAddressEvent {
  data object Submit : EnterNewAddressEvent

  data object NavigatedToChoseCoverage : EnterNewAddressEvent

  data object NavigatedToAddHouseInformation : EnterNewAddressEvent

  data object DismissSubmissionError : EnterNewAddressEvent
}

@Stable
internal sealed interface EnterNewAddressUiState {
  data object MissingOngoingMovingFlow : EnterNewAddressUiState

  data object Loading : EnterNewAddressUiState

  data class Content(
    val moveFromAddressId: String,
    val movingDate: ValidatedInput<LocalDate?, LocalDate, EnterNewAddressValidationError>,
    val allowedMovingDateRange: ClosedRange<LocalDate>,
    val address: ValidatedInput<String?, String, EnterNewAddressValidationError>,
    val postalCode: ValidatedInput<String?, String, EnterNewAddressValidationError>,
    val squareMeters: ValidatedInput<Int?, Int, EnterNewAddressValidationError>,
    val numberCoInsured: ConstrainedNumberInput,
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
          val selectedIsStudent: BooleanInput,
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

private class ValidContent(
  val moveFromAddressId: String,
  val movingDate: LocalDate,
  val address: String,
  val postalCode: String,
  val squareMeters: Int,
  val numberCoInsured: Int,
  val propertyType: PropertyType,
) {
  sealed interface PropertyType {
    val isStudent: Boolean

    data object House : PropertyType {
      override val isStudent: Boolean = false
    }

    data class Apartment(override val isStudent: Boolean, val apartmentType: ApartmentType) : PropertyType
  }
}

private fun Content.validate(): ValidContent? {
  val movingDate = this.movingDate.validate()
  val address = this.address.validate()
  val postalCode = this.postalCode.validate()
  val squareMeters = this.squareMeters.validate()
  return either {
    ValidContent(
      moveFromAddressId = moveFromAddressId,
      movingDate = movingDate.bind(),
      address = address.bind(),
      postalCode = postalCode.bind(),
      squareMeters = squareMeters.bind(),
      numberCoInsured = numberCoInsured.value,
      propertyType = when (propertyType) {
        House -> ValidContent.PropertyType.House
        is WithStudentOption -> ValidContent.PropertyType.Apartment(
          isStudent = propertyType.isStudentSelected,
          apartmentType = propertyType.apartmentType,
        )

        is WithoutStudentOption -> ValidContent.PropertyType.Apartment(
          isStudent = propertyType.isStudentSelected,
          apartmentType = propertyType.apartmentType,
        )
      },
    )
  }.mapLeft { null }.merge()
}

private fun MovingFlowState.toContent(): Content {
  return Content(
    moveFromAddressId = moveFromAddressId,
    movingDate = ValidatedInput(
      initialValue = movingDateState.selectedMovingDate,
      validator = { movingDate ->
        either {
          ensure(movingDate != null) {
            InvalidMovingDate.MustSelectDate
          }
          ensure(movingDate in movingDateState.allowedMovingDateRange) {
            InvalidMovingDate.InvalidChoice(movingDateState.allowedMovingDateRange)
          }
          movingDate
        }
      },
    ),
    allowedMovingDateRange = movingDateState.allowedMovingDateRange,
    address = ValidatedInput(
      initialValue = addressInfo.street,
      validator = { address ->
        either {
          ensure(address != null && address.isNotBlank()) {
            EmptyAddress
          }
          address
        }
      },
    ),
    postalCode = ValidatedInput(
      initialValue = addressInfo.postalCode,
      validator = { postalCode ->
        either {
          ensureNotNull(postalCode) { Missing }
          ensure(postalCode.length == 5) { InvalidLength }
          ensure(postalCode.isDigitsOnly()) { MustBeOnlyDigits }
          postalCode
        }
      },
    ),
    squareMeters = ValidatedInput(
      initialValue = propertyState?.squareMetersState?.selectedSquareMeters,
      validator = { squareMeters ->
        either {
          val invalidSquareMetersError = InvalidSquareMeters(
            propertyState?.squareMetersState?.allowedSquareMetersRange!!,
          )
          ensureNotNull(squareMeters) { invalidSquareMetersError }
          ensure(squareMeters in propertyState.squareMetersState.allowedSquareMetersRange) {
            invalidSquareMetersError
          }
          squareMeters
        }
      },
    ),
    numberCoInsured = ConstrainedNumberInput(
      // property state should be set before (HousingType step)
      initialValue = propertyState!!.numberCoInsuredState.selectedNumberCoInsured,
      validRange = propertyState.numberCoInsuredState.allowedNumberCoInsuredRange,
    ),
    propertyType = when (propertyState) {
      is HouseState -> House
      is ApartmentState -> {
        when (propertyState.isAvailableForStudentState) {
          NotAvailable -> WithoutStudentOption(
            propertyState.apartmentType,
          )

          is Available -> WithStudentOption(
            propertyState.apartmentType,
            BooleanInput(propertyState.isAvailableForStudentState.selectedIsStudent),
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
  sealed interface InvalidMovingDate : EnterNewAddressValidationError {
    data object MustSelectDate : InvalidMovingDate

    data class InvalidChoice(val allowedMovingDateRange: ClosedRange<LocalDate>) : InvalidMovingDate
  }

  data class InvalidSquareMeters(
    val allowedSquareMetersRange: ClosedRange<Int>,
  ) : EnterNewAddressValidationError

  sealed interface InvalidPostalCode : EnterNewAddressValidationError {
    data object InvalidLength : InvalidPostalCode

    data object Missing : InvalidPostalCode

    data object MustBeOnlyDigits : InvalidPostalCode
  }

  data object EmptyAddress : EnterNewAddressValidationError
}

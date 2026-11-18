package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.merge
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.movingflow.compose.BooleanInput
import com.hedvig.android.feature.movingflow.compose.ConstrainedNumberInput
import com.hedvig.android.feature.movingflow.compose.ListInput
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.ExtraBuildingTypesState.ExtraBuildingInfo
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType
import com.hedvig.android.feature.movingflow.data.toMoveIntentSourceInput
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.Submit
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure.NetworkFailure
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Loading
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.MissingOngoingMovingFlow
import com.hedvig.android.molecule.public.AttemptStatus
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.public.rememberAttempts
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.type.MoveExtraBuildingInput
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToHouseInput

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class AddHouseInformationViewModel(
  @Assisted moveIntentId: String,
  movingFlowRepository: MovingFlowRepository,
  apolloClient: ApolloClient,
) : MoleculeViewModel<AddHouseInformationEvent, AddHouseInformationUiState>(
    Loading,
    AddHouseInformationPresenter(
      moveIntentId,
      movingFlowRepository,
      apolloClient,
    ),
  )

internal class AddHouseInformationPresenter(
  private val moveIntentId: String,
  private val movingFlowRepository: MovingFlowRepository,
  private val apolloClient: ApolloClient,
) : MoleculePresenter<AddHouseInformationEvent, AddHouseInformationUiState> {
  @Composable
  override fun MoleculePresenterScope<AddHouseInformationEvent>.present(
    lastState: AddHouseInformationUiState,
  ): AddHouseInformationUiState {
    var addressInput: Option<AddressInput?> by remember {
      mutableStateOf(
        when (lastState) {
          Loading -> None
          MissingOngoingMovingFlow -> Option(null)
          is Content -> Option(lastState.addressInput)
        },
      )
    }
    var submittingInfoFailure: SubmittingInfoFailure? by remember { mutableStateOf(null) }
    // A pending step is kept across a restart: its request already succeeded and stored the quotes, so the member
    // lands on the coverage step on return instead of having to request the quotes again.
    var next: NavigationRequest<ChoseCoverageLevelAndDeductibleStep>? by remember {
      mutableStateOf((lastState as? Content)?.next)
    }
    // The input is stored inside the run, so the run starts in the event handler that passed the guard.
    val requestQuotes = rememberAttempts<ValidAddressInput>(
      restoredWhileRunning = (lastState as? Content)?.isLoadingNextStep == true,
    ) { validContent ->
      val movingFlowState = movingFlowRepository.updateWithHouseInput(
        validContent.yearOfConstruction,
        validContent.ancillaryArea,
        validContent.numberOfBathrooms,
        validContent.isSublet,
        validContent.extraBuildings,
      )
      if (movingFlowState == null) {
        submittingInfoFailure = NetworkFailure
        return@rememberAttempts false
      }
      apolloClient
        .mutation(
          MoveIntentV2RequestMutation(
            intentId = moveIntentId,
            moveIntentRequestInput = movingFlowState.toInputForSubmission(validContent).moveIntentRequestInput,
            addonsFlagOn = true,
          ),
        )
        .safeExecute()
        .map { it.moveIntentRequest }
        .fold(
          ifLeft = {
            submittingInfoFailure = NetworkFailure
            false
          },
          ifRight = { request ->
            when (val moveIntentQuotesFragment = request.moveIntent) {
              null -> {
                submittingInfoFailure = when (val errorMessage = request.userError?.message) {
                  null -> NetworkFailure
                  else -> SubmittingInfoFailure.UserError(errorMessage)
                }
                false
              }

              else -> {
                movingFlowRepository.updateWithMoveIntentQuotes(moveIntentQuotesFragment)
                next = NavigationRequest.of(ChoseCoverageLevelAndDeductibleStep(moveIntentId))
                true
              }
            }
          },
        )
    }

    LaunchedEffect(Unit) {
      movingFlowRepository.movingFlowState().collect {
        addressInput = Option(it?.toAddressInputOrNull())
      }
    }

    CollectEvents { event ->
      when (event) {
        Submit -> {
          if (requestQuotes.status == AttemptStatus.Running || next?.isPending == true) return@CollectEvents
          @Suppress("NAME_SHADOWING")
          val addressInput = addressInput.getOrNull() ?: return@CollectEvents
          val validContent = addressInput.validate()
          if (validContent == null) return@CollectEvents
          requestQuotes.start(validContent)
        }

        DismissSubmissionError -> {
          submittingInfoFailure = null
        }
      }
    }

    return when (val addressInputValue = addressInput) {
      None -> {
        Loading
      }

      is Some -> {
        when (val value = addressInputValue.value) {
          null -> MissingOngoingMovingFlow

          else -> Content(
            moveFromAddressId = moveIntentId,
            addressInput = value,
            isLoadingNextStep = when (requestQuotes.status) {
              AttemptStatus.Running -> true

              // The request stopped with the last presenter run and may have reached the server. Its quotes were
              // never stored, so the only way on is to request them again with the same input, and the form is shown
              // ready for that rather than with a failure that may not have happened.
              AttemptStatus.Interrupted -> false

              AttemptStatus.Idle -> false
            },
            submittingInfoFailure = submittingInfoFailure,
            next = next,
          )
        }
      }
    }
  }
}

private fun MovingFlowState.toInputForSubmission(validContent: ValidAddressInput): InputForSubmission {
  return InputForSubmission(
    moveIntentRequestInput = MoveIntentRequestInput(
      moveToAddress = MoveToAddressInput(
        street = this.addressInfo.street!!,
        postalCode = this.addressInfo.postalCode!!,
        city = com.apollographql.apollo.api.Optional.absent(),
      ),
      moveFromAddressId = moveFromAddressId,
      movingDate = this.movingDateState.selectedMovingDate!!,
      // property state should be set before
      numberCoInsured = this.propertyState!!.numberCoInsuredState.selectedNumberCoInsured,
      squareMeters = this.propertyState.squareMetersState.selectedSquareMeters!!,
      apartment = com.apollographql.apollo.api.Optional.absent(),
      house = com.apollographql.apollo.api.Optional.present(
        MoveToHouseInput(
          ancillaryArea = validContent.ancillaryArea,
          yearOfConstruction = validContent.yearOfConstruction,
          numberOfBathrooms = validContent.numberOfBathrooms,
          isSubleted = validContent.isSublet,
          extraBuildings = validContent.extraBuildings.map { extraBuildingInfo ->
            MoveExtraBuildingInput(
              area = extraBuildingInfo.area,
              hasWaterConnected = extraBuildingInfo.hasWaterConnected,
              type = extraBuildingInfo.type,
            )
          },
        ),
      ),
      source = movingSource.toMoveIntentSourceInput(),
    ),
  )
}

internal sealed interface AddHouseInformationEvent {
  data object Submit : AddHouseInformationEvent

  data object DismissSubmissionError : AddHouseInformationEvent
}

@Stable
internal sealed interface AddHouseInformationUiState {
  data object MissingOngoingMovingFlow : AddHouseInformationUiState

  data object Loading : AddHouseInformationUiState

  data class Content(
    val moveFromAddressId: String,
    val addressInput: AddressInput,
    val isLoadingNextStep: Boolean,
    val submittingInfoFailure: SubmittingInfoFailure?,
    val next: NavigationRequest<ChoseCoverageLevelAndDeductibleStep>?,
  ) : AddHouseInformationUiState {
    val shouldDisableInput: Boolean = submittingInfoFailure != null ||
      isLoadingNextStep == true

    sealed interface SubmittingInfoFailure {
      data object NetworkFailure : SubmittingInfoFailure

      data class UserError(val message: String) : SubmittingInfoFailure
    }
  }
}

internal sealed interface AddHouseInformationValidationError {
  sealed interface InvalidYearOfConstruction : AddHouseInformationValidationError {
    data object TooEarly : InvalidYearOfConstruction

    data object Missing : InvalidYearOfConstruction
  }

  data object MissingAncillaryArea : AddHouseInformationValidationError
}

/** The coverage level and deductible step, for the move intent whose quotes were just stored. */
internal data class ChoseCoverageLevelAndDeductibleStep(val moveIntentId: String)

private data class InputForSubmission(
  val moveIntentRequestInput: MoveIntentRequestInput,
)

internal data class AddressInput(
  val yearOfConstruction: ValidatedInput<Int?, Int, AddHouseInformationValidationError>,
  val ancillaryArea: ValidatedInput<Int?, Int, AddHouseInformationValidationError>,
  val numberOfBathrooms: ConstrainedNumberInput,
  val isSublet: BooleanInput,
  val possibleExtraBuildingTypes: List<MoveExtraBuildingType>,
  val extraBuildings: ListInput<ExtraBuildingInfo>,
)

private data class ValidAddressInput(
  val yearOfConstruction: Int,
  val ancillaryArea: Int,
  val numberOfBathrooms: Int,
  val isSublet: Boolean,
  val extraBuildings: List<ExtraBuildingInfo>,
)

private fun AddressInput.validate(): ValidAddressInput? {
  val yearOfConstruction = yearOfConstruction.validate()
  val ancillaryArea = ancillaryArea.validate()
  return either {
    ValidAddressInput(
      yearOfConstruction = yearOfConstruction.bind(),
      ancillaryArea = ancillaryArea.bind(),
      numberOfBathrooms = numberOfBathrooms.value,
      isSublet = isSublet.value,
      extraBuildings = extraBuildings.value,
    )
  }.mapLeft { null }.merge()
}

private fun MovingFlowState.toAddressInputOrNull(): AddressInput? {
  val houseState = propertyState as? MovingFlowState.PropertyState.HouseState ?: return null
  return AddressInput(
    yearOfConstruction = ValidatedInput(
      initialValue = houseState.yearOfConstruction,
      validator = { yearOfConstruction ->
        either {
          ensureNotNull(yearOfConstruction) { AddHouseInformationValidationError.InvalidYearOfConstruction.Missing }
          ensure(yearOfConstruction >= 1900) { AddHouseInformationValidationError.InvalidYearOfConstruction.TooEarly }
          yearOfConstruction
        }
      },
    ),
    ancillaryArea = ValidatedInput(
      initialValue = houseState.ancillaryArea,
      validator = { ancillaryArea ->
        either {
          ensureNotNull(ancillaryArea) { AddHouseInformationValidationError.MissingAncillaryArea }
          ancillaryArea
        }
      },
    ),
    numberOfBathrooms = ConstrainedNumberInput(
      initialValue = houseState.numberOfBathrooms ?: 1,
      validRange = 1..10,
    ),
    isSublet = BooleanInput(houseState.isSublet),
    possibleExtraBuildingTypes = houseState.extraBuildingTypesState.allowedExtraBuildingTypes,
    extraBuildings = ListInput(
      initialList = houseState.extraBuildingTypesState.selectedExtraBuildingTypes,
    ),
  )
}

package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.feature.movingflow.MovingFlowDestinations
import com.hedvig.android.feature.movingflow.compose.BooleanInput
import com.hedvig.android.feature.movingflow.compose.ConstrainedNumberInput
import com.hedvig.android.feature.movingflow.compose.ListInput
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.ExtraBuildingTypesState.ExtraBuildingInfo
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Attefall
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Barn
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Boathouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Carport
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Friggebod
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Garage
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Gazebo
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Greenhouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Guesthouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Other
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Outhouse
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Sauna
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Shed
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType.Storehouse
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.DismissSubmissionError
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.NavigatedToChoseCoverage
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationEvent.Submit
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure.NetworkFailure
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Loading
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.MissingOngoingMovingFlow
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.type.MoveApiVersion
import octopus.type.MoveExtraBuildingInput
import octopus.type.MoveExtraBuildingType.ATTEFALL
import octopus.type.MoveExtraBuildingType.BARN
import octopus.type.MoveExtraBuildingType.BOATHOUSE
import octopus.type.MoveExtraBuildingType.CARPORT
import octopus.type.MoveExtraBuildingType.FRIGGEBOD
import octopus.type.MoveExtraBuildingType.GARAGE
import octopus.type.MoveExtraBuildingType.GAZEBO
import octopus.type.MoveExtraBuildingType.GREENHOUSE
import octopus.type.MoveExtraBuildingType.GUESTHOUSE
import octopus.type.MoveExtraBuildingType.OTHER
import octopus.type.MoveExtraBuildingType.OUTHOUSE
import octopus.type.MoveExtraBuildingType.SAUNA
import octopus.type.MoveExtraBuildingType.SHED
import octopus.type.MoveExtraBuildingType.STOREHOUSE
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToHouseInput

internal class AddHouseInformationViewModel(
  savedStateHandle: SavedStateHandle,
  movingFlowRepository: MovingFlowRepository,
  apolloClient: ApolloClient,
  featureManager: FeatureManager,
) : MoleculeViewModel<AddHouseInformationEvent, AddHouseInformationUiState>(
    AddHouseInformationUiState.Loading,
    AddHouseInformationPresenter(
      savedStateHandle.toRoute<MovingFlowDestinations.AddHouseInformation>().moveIntentId,
      movingFlowRepository,
      apolloClient,
      featureManager,
    ),
  )

internal class AddHouseInformationPresenter(
  private val moveIntentId: String,
  private val movingFlowRepository: MovingFlowRepository,
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
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
    var oldAddressCoverageDurationDays by remember { mutableStateOf<Int?>(null) }
    var submittingInfoFailure: SubmittingInfoFailure? by remember { mutableStateOf(null) }
    var navigateToChoseCoverage by remember { mutableStateOf(false) }
    var inputForSubmission: InputForSubmission? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
      movingFlowRepository.movingFlowState().collect {
        addressInput = Option(it?.toAddressInputOrNull())
        oldAddressCoverageDurationDays = it?.oldAddressCoverageDurationDays
      }
    }

    val coroutineScope = rememberCoroutineScope()
    CollectEvents { event ->
      when (event) {
        Submit -> {
          @Suppress("NAME_SHADOWING")
          val addressInput = addressInput.getOrNull() ?: return@CollectEvents
          val validContent = addressInput.validate()
          if (validContent == null) return@CollectEvents
          coroutineScope.launch {
            val movingFlowState = movingFlowRepository.updateWithHouseInput(
              validContent.yearOfConstruction,
              validContent.ancillaryArea,
              validContent.numberOfBathrooms,
              validContent.isSublet,
              validContent.extraBuildings,
            )
            if (movingFlowState != null) {
              inputForSubmission = movingFlowState.toInputForSubmission(validContent)
            } else {
              submittingInfoFailure = NetworkFailure
            }
          }
        }

        NavigatedToChoseCoverage -> {
          navigateToChoseCoverage = false
        }

        DismissSubmissionError -> {
          submittingInfoFailure = null
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
        inputForSubmission = null
      }
    }

    return when (val addressInputValue = addressInput) {
      None -> AddHouseInformationUiState.Loading
      is Some -> {
        when (val value = addressInputValue.value) {
          null -> AddHouseInformationUiState.MissingOngoingMovingFlow
          else -> Content(
            moveFromAddressId = moveIntentId,
            addressInput = value,
            isLoadingNextStep = inputForSubmission != null,
            submittingInfoFailure = submittingInfoFailure,
            navigateToChoseCoverage = navigateToChoseCoverage,
            oldAddressCoverageDurationDays = oldAddressCoverageDurationDays,
          )
        }
      }
    }
  }
}

private fun MovingFlowState.toInputForSubmission(validContent: ValidAddressInput): InputForSubmission {
  return InputForSubmission(
    moveIntentRequestInput = MoveIntentRequestInput(
      apiVersion = com.apollographql.apollo.api.Optional.present(MoveApiVersion.V2_TIERS_AND_DEDUCTIBLES),
      moveToAddress = MoveToAddressInput(
        street = this.addressInfo.street!!,
        postalCode = this.addressInfo.postalCode!!,
        city = com.apollographql.apollo.api.Optional.absent(),
      ),
      moveFromAddressId = moveFromAddressId,
      movingDate = this.movingDateState.selectedMovingDate!!,
      numberCoInsured = this.propertyState.numberCoInsuredState.selectedNumberCoInsured,
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
              type = when (extraBuildingInfo.type) {
                Garage -> GARAGE
                Carport -> CARPORT
                Shed -> SHED
                Storehouse -> STOREHOUSE
                Friggebod -> FRIGGEBOD
                Attefall -> ATTEFALL
                Outhouse -> OUTHOUSE
                Guesthouse -> GUESTHOUSE
                Gazebo -> GAZEBO
                Greenhouse -> GREENHOUSE
                Sauna -> SAUNA
                Barn -> BARN
                Boathouse -> BOATHOUSE
                Other -> OTHER
              },
            )
          },
        ),
      ),
    ),
  )
}

internal sealed interface AddHouseInformationEvent {
  data object Submit : AddHouseInformationEvent

  data object DismissSubmissionError : AddHouseInformationEvent

  data object NavigatedToChoseCoverage : AddHouseInformationEvent
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
    val navigateToChoseCoverage: Boolean,
    val oldAddressCoverageDurationDays: Int?,
  ) : AddHouseInformationUiState {
    val shouldDisableInput: Boolean = submittingInfoFailure != null ||
      isLoadingNextStep == true ||
      navigateToChoseCoverage == true

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

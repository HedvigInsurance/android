package com.hedvig.android.feature.movingflow.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.type.MoveExtraBuildingType
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
import octopus.type.MoveExtraBuildingType.UNKNOWN__

@Serializable
internal data class MovingFlowState(
  val id: String,
  val housingType: HousingType,
  val movingDateState: MovingDateState,
  val houseState: HouseState,
  val apartmentState: ApartmentState,
) {
  // todo do we need these two
  private val isHouse = housingType == HousingType.Villa
  private val isApartment = housingType == HousingType.ApartmentOwn || housingType == HousingType.ApartmentRent

  @Serializable
  data class MovingDateState(
    val allowedMovingDateMin: LocalDate,
    val allowedMovingDateMax: LocalDate,
    val selectedMovingDate: LocalDate = allowedMovingDateMin,
  ) {
    val allowedMovingDateRange: ClosedRange<LocalDate>
      get() = allowedMovingDateMin..allowedMovingDateMax
  }

  @Serializable
  data class NumberCoInsuredState private constructor(
    val allowedNumberCoInsuredMin: Int,
    val allowedNumberCoInsuredMax: Int,
    val selectedNumberCoInsured: Int,
  ) {
    val allowedNumberCoInsuredRange: ClosedRange<Int>
      get() = allowedNumberCoInsuredMin..allowedNumberCoInsuredMax

    companion object {
      operator fun invoke(maxNumberCoInsured: Int?, suggestedNumberCoInsured: Int): NumberCoInsuredState {
        @Suppress("NAME_SHADOWING")
        val maxNumberCoInsured = maxNumberCoInsured ?: SaneMaxNumberCoInsured
        return NumberCoInsuredState(
          allowedNumberCoInsuredMin = SaneMinNumberCoInsured,
          allowedNumberCoInsuredMax = maxNumberCoInsured,
          selectedNumberCoInsured = suggestedNumberCoInsured.coerceAtMost(maxNumberCoInsured),
        )
      }
    }
  }

  @Serializable
  data class SquareMetersState(
    val allowedSquareMetersMin: Int,
    val allowedSquareMetersMax: Int,
    val selectedSquareMeters: Int?,
  ) {
    val allowedSquareMetersRange: ClosedRange<Int>
      get() = allowedSquareMetersMin..allowedSquareMetersMax

    companion object {
      operator fun invoke(maxSquareMeters: Int?) = SquareMetersState(
        allowedSquareMetersMin = SaneMinSquareMeters,
        allowedSquareMetersMax = maxSquareMeters ?: SaneMaxSquareMeters,
        selectedSquareMeters = null,
      )
    }
  }

  @Serializable
  data class ApartmentState(
    val numberCoInsuredState: NumberCoInsuredState,
    val squareMetersState: SquareMetersState,
    val isAvailableForStudentState: IsAvailableForStudentState,
    //
  ) {
    @Serializable
    sealed interface IsAvailableForStudentState {
      object NotAvailable : IsAvailableForStudentState

      data class Available(val selectedIsStudent: Boolean) : IsAvailableForStudentState
    }
  }

  @Serializable
  data class HouseState(
    val numberCoInsuredState: NumberCoInsuredState,
    val squareMetersState: SquareMetersState,
    val extraBuildingTypesState: ExtraBuildingTypesState,
    val ancillaryArea: Int?,
    val yearOfConstruction: Int?,
    val numberOfBathrooms: Int?,
    val isSublet: Boolean,
  ) {
    @Serializable
    enum class MoveExtraBuildingType {
      Garage,
      Carport,
      Shed,
      Storehouse,
      Friggebod,
      Attefall,
      Outhouse,
      Guesthouse,
      Gazebo,
      Greenhouse,
      Sauna,
      Barn,
      Boathouse,
      Other,
      Unknown,
    }

    @Serializable
    data class ExtraBuildingTypesState(
      val allowedExtraBuildingTypes: List<MoveExtraBuildingType>,
      val selectedExtraBuildingTypes: List<ExtraBuildingInput>,
    ) {
      @Serializable
      data class ExtraBuildingInput(
        val area: Int,
        val type: MoveExtraBuildingType,
        val hasWaterConnected: Boolean,
      )
    }
  }
}

internal fun MoveIntentFragment.toMovingFlowState(housingType: HousingType): MovingFlowState {
  return MovingFlowState(
    id = id,
    housingType = housingType,
    movingDateState = MovingFlowState.MovingDateState(
      allowedMovingDateMin = minMovingDate,
      allowedMovingDateMax = maxMovingDate,
    ),
    houseState = MovingFlowState.HouseState(
      numberCoInsuredState = MovingFlowState.NumberCoInsuredState(
        maxNumberCoInsured = maxHouseNumberCoInsured,
        suggestedNumberCoInsured = suggestedNumberCoInsured,
      ),
      squareMetersState = MovingFlowState.SquareMetersState(
        maxSquareMeters = maxHouseSquareMeters,
      ),
      extraBuildingTypesState = MovingFlowState.HouseState.ExtraBuildingTypesState(
        allowedExtraBuildingTypes = extraBuildingTypes.map { it.toMoveExtraBuildingType() },
        selectedExtraBuildingTypes = emptyList(),
      ),
      ancillaryArea = null,
      yearOfConstruction = null,
      numberOfBathrooms = null,
      isSublet = false,
    ),
    apartmentState = MovingFlowState.ApartmentState(
      numberCoInsuredState = MovingFlowState.NumberCoInsuredState(
        maxNumberCoInsured = maxApartmentNumberCoInsured,
        suggestedNumberCoInsured = suggestedNumberCoInsured,
      ),
      squareMetersState = MovingFlowState.SquareMetersState(
        maxSquareMeters = maxApartmentSquareMeters,
      ),
      isAvailableForStudentState = if (isApartmentAvailableforStudent == true) {
        MovingFlowState.ApartmentState.IsAvailableForStudentState.Available(false)
      } else {
        MovingFlowState.ApartmentState.IsAvailableForStudentState.NotAvailable
      },
    ),
  )
}

private fun MoveExtraBuildingType.toMoveExtraBuildingType(): MovingFlowState.HouseState.MoveExtraBuildingType {
  return when (this) {
    GARAGE -> MovingFlowState.HouseState.MoveExtraBuildingType.Garage
    CARPORT -> MovingFlowState.HouseState.MoveExtraBuildingType.Carport
    SHED -> MovingFlowState.HouseState.MoveExtraBuildingType.Shed
    STOREHOUSE -> MovingFlowState.HouseState.MoveExtraBuildingType.Storehouse
    FRIGGEBOD -> MovingFlowState.HouseState.MoveExtraBuildingType.Friggebod
    ATTEFALL -> MovingFlowState.HouseState.MoveExtraBuildingType.Attefall
    OUTHOUSE -> MovingFlowState.HouseState.MoveExtraBuildingType.Outhouse
    GUESTHOUSE -> MovingFlowState.HouseState.MoveExtraBuildingType.Guesthouse
    GAZEBO -> MovingFlowState.HouseState.MoveExtraBuildingType.Gazebo
    GREENHOUSE -> MovingFlowState.HouseState.MoveExtraBuildingType.Greenhouse
    SAUNA -> MovingFlowState.HouseState.MoveExtraBuildingType.Sauna
    BARN -> MovingFlowState.HouseState.MoveExtraBuildingType.Barn
    BOATHOUSE -> MovingFlowState.HouseState.MoveExtraBuildingType.Boathouse
    OTHER -> MovingFlowState.HouseState.MoveExtraBuildingType.Other
    UNKNOWN__ -> MovingFlowState.HouseState.MoveExtraBuildingType.Unknown
  }
}

// Null guideline values should not be possible, so default to some high max value in order to make the UI not look
//  comically wrong
private const val SaneMinNumberCoInsured = 0
private const val SaneMaxNumberCoInsured = 99
private const val SaneMinSquareMeters = 1
private const val SaneMaxSquareMeters = 999

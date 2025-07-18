package com.hedvig.android.feature.movingflow.data

import com.hedvig.android.feature.movingflow.data.MovingFlowState.AddressInfo
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment
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
  val moveFromAddressId: String,
  val housingType: HousingType?,
  val addressInfo: AddressInfo,
  val movingDateState: MovingDateState,
  val propertyState: PropertyState?,
  val movingFlowQuotes: MovingFlowQuotes?,
  // If in the flow there was a quote selected once, we persist that selection so that it's pre-selected when going
  //  back to that step again
  val lastSelectedHomeQuoteId: String?,
  val mapOfPropertyStates: Map<HousingType, MovingFlowState.PropertyState>,
) {
  @Serializable
  data class AddressInfo(
    val street: String?,
    val postalCode: String?,
  )

  @Serializable
  data class MovingDateState(
    val selectedMovingDate: LocalDate?,
    @Serializable(with = ClosedRangeSerializer::class)
    val allowedMovingDateRange: ClosedRange<LocalDate>,
  )

  @Serializable
  data class NumberCoInsuredState(
    @Serializable(with = ClosedRangeSerializer::class)
    val allowedNumberCoInsuredRange: ClosedRange<Int>,
    val selectedNumberCoInsured: Int,
  ) {
    companion object {
      operator fun invoke(maxNumberCoInsured: Int?, suggestedNumberCoInsured: Int): NumberCoInsuredState {
        @Suppress("NAME_SHADOWING")
        val maxNumberCoInsured = maxNumberCoInsured ?: SaneMaxNumberCoInsured
        return NumberCoInsuredState(
          allowedNumberCoInsuredRange = SaneMinNumberCoInsured..maxNumberCoInsured,
          selectedNumberCoInsured = suggestedNumberCoInsured.coerceAtMost(maxNumberCoInsured),
        )
      }
    }
  }

  @Serializable
  data class SquareMetersState(
    @Serializable(with = ClosedRangeSerializer::class)
    val allowedSquareMetersRange: ClosedRange<Int>,
    val selectedSquareMeters: Int?,
  ) {
    companion object {
      operator fun invoke(maxSquareMeters: Int?) = SquareMetersState(
        allowedSquareMetersRange = SaneMinSquareMeters..(maxSquareMeters ?: SaneMaxSquareMeters),
        selectedSquareMeters = null,
      )
    }
  }

  @Serializable
  sealed interface PropertyState {
    val numberCoInsuredState: NumberCoInsuredState
    val squareMetersState: SquareMetersState

    @Serializable
    data class ApartmentState(
      override val numberCoInsuredState: NumberCoInsuredState,
      override val squareMetersState: SquareMetersState,
      val apartmentType: ApartmentType,
      val isAvailableForStudentState: IsAvailableForStudentState,
    ) : PropertyState {
      enum class ApartmentType {
        RENT,
        BRF,
      }

      @Serializable
      sealed interface IsAvailableForStudentState {
        @Serializable
        object NotAvailable : IsAvailableForStudentState

        @Serializable
        data class Available(val selectedIsStudent: Boolean) : IsAvailableForStudentState
      }
    }

    @Serializable
    data class HouseState(
      override val numberCoInsuredState: NumberCoInsuredState,
      override val squareMetersState: SquareMetersState,
      val extraBuildingTypesState: ExtraBuildingTypesState,
      val ancillaryArea: Int?,
      val yearOfConstruction: Int?,
      val numberOfBathrooms: Int?,
      val isSublet: Boolean,
    ) : PropertyState {
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
      }

      @Serializable
      data class ExtraBuildingTypesState(
        val allowedExtraBuildingTypes: List<MoveExtraBuildingType>,
        val selectedExtraBuildingTypes: List<ExtraBuildingInfo>,
      ) {
        @Serializable
        data class ExtraBuildingInfo(
          val area: Int,
          val type: MoveExtraBuildingType,
          val hasWaterConnected: Boolean,
        )
      }
    }
  }

  companion object
}

internal fun MovingFlowState.Companion.fromFragments(
  moveIntentFragment: MoveIntentFragment,
  moveIntentQuotesFragment: MoveIntentQuotesFragment?,
  moveFromAddressId: String,
): MovingFlowState {
  val currentHomeAddress = moveIntentFragment.currentHomeAddresses.firstOrNull { it.id == moveFromAddressId }
    ?: moveIntentFragment.currentHomeAddresses.first()
  val suggestedCoInsured = currentHomeAddress.suggestedNumberCoInsured
  val houseState = with(moveIntentFragment) {
    MovingFlowState.PropertyState.HouseState(
      numberCoInsuredState = MovingFlowState.NumberCoInsuredState(
        maxNumberCoInsured = maxHouseNumberCoInsured,
        suggestedNumberCoInsured = suggestedCoInsured,
      ),
      squareMetersState = MovingFlowState.SquareMetersState(
        maxSquareMeters = maxHouseSquareMeters,
      ),
      extraBuildingTypesState = MovingFlowState.PropertyState.HouseState.ExtraBuildingTypesState(
        allowedExtraBuildingTypes = extraBuildingTypes.map { it.toMoveExtraBuildingType() }.filterNotNull(),
        selectedExtraBuildingTypes = emptyList(),
      ),
      ancillaryArea = null,
      yearOfConstruction = null,
      numberOfBathrooms = null,
      isSublet = false,
    )
  }
  val apartmentState: (ApartmentType) -> MovingFlowState.PropertyState.ApartmentState = { apartmentType ->
    with(moveIntentFragment) {
      MovingFlowState.PropertyState.ApartmentState(
        numberCoInsuredState = MovingFlowState.NumberCoInsuredState(
          maxNumberCoInsured = maxApartmentNumberCoInsured,
          suggestedNumberCoInsured = suggestedCoInsured,
        ),
        squareMetersState = MovingFlowState.SquareMetersState(
          maxSquareMeters = maxApartmentSquareMeters,
        ),
        apartmentType = apartmentType,
        isAvailableForStudentState = if (isApartmentAvailableforStudent == true) {
          MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.Available(false)
        } else {
          MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.NotAvailable
        },
      )
    }
  }
  val mapOfPropertyStates = mapOf(
    HousingType.ApartmentOwn to apartmentState(ApartmentState.ApartmentType.BRF),
    HousingType.ApartmentRent to apartmentState(ApartmentState.ApartmentType.RENT),
    HousingType.Villa to houseState,
  )
  return MovingFlowState(
    id = moveIntentFragment.id,
    moveFromAddressId = moveFromAddressId,
    housingType = null,
    addressInfo = AddressInfo(null, null),
    movingDateState = MovingFlowState.MovingDateState(
      selectedMovingDate = null,
      allowedMovingDateRange = currentHomeAddress.minMovingDate..currentHomeAddress.maxMovingDate,
    ),
    propertyState = null,
    mapOfPropertyStates = mapOfPropertyStates,
    movingFlowQuotes = moveIntentQuotesFragment?.toMovingFlowQuotes(),
    lastSelectedHomeQuoteId = null,
  )
}

private fun MoveExtraBuildingType.toMoveExtraBuildingType(): HouseState.MoveExtraBuildingType? {
  return when (this) {
    GARAGE -> HouseState.MoveExtraBuildingType.Garage
    CARPORT -> HouseState.MoveExtraBuildingType.Carport
    SHED -> HouseState.MoveExtraBuildingType.Shed
    STOREHOUSE -> HouseState.MoveExtraBuildingType.Storehouse
    FRIGGEBOD -> HouseState.MoveExtraBuildingType.Friggebod
    ATTEFALL -> HouseState.MoveExtraBuildingType.Attefall
    OUTHOUSE -> HouseState.MoveExtraBuildingType.Outhouse
    GUESTHOUSE -> HouseState.MoveExtraBuildingType.Guesthouse
    GAZEBO -> HouseState.MoveExtraBuildingType.Gazebo
    GREENHOUSE -> HouseState.MoveExtraBuildingType.Greenhouse
    SAUNA -> HouseState.MoveExtraBuildingType.Sauna
    BARN -> HouseState.MoveExtraBuildingType.Barn
    BOATHOUSE -> HouseState.MoveExtraBuildingType.Boathouse
    OTHER -> HouseState.MoveExtraBuildingType.Other
    UNKNOWN__ -> {
      logcat(ERROR) { "Mapping unknown MoveExtraBuildingType:{$this} to MoveExtraBuildingType.Unknown" }
      null
    }
  }
}

// Null guideline values should not be possible, so default to some high max value in order to make the UI not look
//  comically wrong
private const val SaneMinNumberCoInsured = 0
private const val SaneMaxNumberCoInsured = 99
private const val SaneMinSquareMeters = 1
private const val SaneMaxSquareMeters = 999

package com.hedvig.android.feature.movingflow.data

import com.hedvig.android.feature.movingflow.data.MovingFlowState.AddressInfo
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment

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
      data class MoveExtraBuildingType(
        val type: String,
        val displayName: String,
      )

      @Serializable
      data class ExtraBuildingTypesState(
        val allowedExtraBuildingTypes: List<MoveExtraBuildingType>,
        val selectedExtraBuildingTypes: List<ExtraBuildingInfo>,
      ) {
        @Serializable
        data class ExtraBuildingInfo(
          val area: Int,
          val type: String,
          val displayName: String,
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
        allowedExtraBuildingTypes = extraBuildingTypesV2.map { it.toMoveExtraBuildingType() }.filterNotNull(),
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

private fun MoveIntentFragment.ExtraBuildingTypesV2.toMoveExtraBuildingType():
  MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType {
  return MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType(
    type = this.type,
    displayName = this.displayName,
  )
}

// Null guideline values should not be possible, so default to some high max value in order to make the UI not look
//  comically wrong
private const val SaneMinNumberCoInsured = 0
private const val SaneMaxNumberCoInsured = 99
private const val SaneMinSquareMeters = 1
private const val SaneMaxSquareMeters = 999

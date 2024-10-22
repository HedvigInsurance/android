package com.hedvig.android.feature.movingflow.storage

import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.Available
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.NotAvailable
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState
import com.hedvig.android.feature.movingflow.data.fromFragments
import com.hedvig.android.feature.movingflow.data.toMovingFlowQuotes
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment

internal class MovingFlowRepository(
  private val movingFlowStorage: MovingFlowStorage,
) {
  fun movingFlowState(): Flow<MovingFlowState?> {
    return movingFlowStorage.getMovingFlowState()
  }

  suspend fun initiateNewMovingFlow(moveIntent: MoveIntentFragment, housingType: HousingType) {
    movingFlowStorage.setMovingFlowState(MovingFlowState.fromFragments(moveIntent, null, housingType))
  }

  suspend fun updateWithPropertyInput(
    movingDate: LocalDate,
    address: String,
    postalCode: String,
    squareMeters: Int,
    numberCoInsured: Int,
    isStudent: Boolean,
  ) {
    movingFlowStorage.editMovingFlowState { existingState ->
      if (existingState == null) {
        logcat(LogPriority.ERROR) { "Trying to `updateWithPropertyInput` a non-existing moving flow state" }
        return@editMovingFlowState null
      }
      val updatedState = existingState.copy(
        addressInfo = existingState.addressInfo.copy(
          street = address,
          postalCode = postalCode,
        ),
        movingDateState = existingState.movingDateState.copy(
          selectedMovingDate = movingDate,
        ),
        propertyState = when (val propertyState = existingState.propertyState) {
          is HouseState -> {
            propertyState.copy(
              numberCoInsuredState = propertyState.numberCoInsuredState.copy(
                selectedNumberCoInsured = numberCoInsured,
              ),
              squareMetersState = propertyState.squareMetersState.copy(
                selectedSquareMeters = squareMeters,
              ),
            )
          }

          is ApartmentState -> {
            propertyState.copy(
              numberCoInsuredState = propertyState.numberCoInsuredState.copy(
                selectedNumberCoInsured = numberCoInsured,
              ),
              squareMetersState = propertyState.squareMetersState.copy(
                selectedSquareMeters = squareMeters,
              ),
              isAvailableForStudentState = when (propertyState.isAvailableForStudentState) {
                NotAvailable -> NotAvailable
                is Available -> propertyState.isAvailableForStudentState.copy(
                  selectedIsStudent = isStudent,
                )
              },
            )
          }
        },
      )
      updatedState
    }
  }

  suspend fun updateWithMoveIntentQuotes(moveIntentQuotesFragment: MoveIntentQuotesFragment) {
    movingFlowStorage.editMovingFlowState { existingState ->
      if (existingState == null) {
        logcat(LogPriority.ERROR) { "Trying to `updateWithPropertyInput` a non-existing moving flow state" }
        return@editMovingFlowState null
      }
      existingState.copy(movingFlowQuotes = moveIntentQuotesFragment.toMovingFlowQuotes())
    }
  }
}

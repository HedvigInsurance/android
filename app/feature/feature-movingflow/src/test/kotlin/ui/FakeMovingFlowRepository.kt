package ui

import app.cash.turbine.Turbine
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment

internal class FakeMovingFlowRepository : MovingFlowRepository {
  val movingFlowStateTurbine = Turbine<MovingFlowState?>(name = "movingFlowStateTurbine")
  var selectedQuoteIdParameterThatWasSentIn = "start"
  val movingFlowInitiatedTurbine = Turbine<Boolean>(name = "movingFlowInitiatedTurbine")

  override fun movingFlowState(): Flow<MovingFlowState?> {
    return movingFlowStateTurbine.asChannel().receiveAsFlow()
  }

  override suspend fun initiateNewMovingFlow(moveIntent: MoveIntentFragment, housingType: HousingType) {
    delay(300) //to imitate button loading
    movingFlowInitiatedTurbine.add(true)
  }

  override suspend fun updateWithPropertyInput(
    movingDate: LocalDate,
    address: String,
    postalCode: String,
    squareMeters: Int,
    numberCoInsured: Int,
    isStudent: Boolean,
  ) {}

  override suspend fun updateWithHouseInput(
    yearOfConstruction: Int,
    ancillaryArea: Int,
    numberOfBathrooms: Int,
    isSublet: Boolean,
    extraBuildings: List<MovingFlowState.PropertyState.HouseState.ExtraBuildingTypesState.ExtraBuildingInfo>,
  ): MovingFlowState? {
    return movingFlowStateTurbine.awaitItem()
  }

  override suspend fun updateWithMoveIntentQuotes(moveIntentQuotesFragment: MoveIntentQuotesFragment) {}

  override suspend fun updatePreselectedHomeQuoteId(selectedHomeQuoteId: String) {
    selectedQuoteIdParameterThatWasSentIn = selectedHomeQuoteId
  }
}

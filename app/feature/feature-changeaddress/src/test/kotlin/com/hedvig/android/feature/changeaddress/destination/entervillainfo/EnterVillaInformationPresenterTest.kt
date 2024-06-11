package com.hedvig.android.feature.changeaddress.destination.entervillainfo

import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType.GARAGE
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType.SAUNA
import com.hedvig.android.feature.changeaddress.destination.fakeMovingParametersForVilla
import com.hedvig.android.feature.changeaddress.navigation.VillaOnlyParameters
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EnterVillaInformationPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when the values are changed by the user it shows in the uiState`() = runTest {
    val presenter = EnterVillaInformationPresenter(fakeMovingParametersForVilla)
    presenter.test(
      EnterVillaInformationUiState(extraBuildingTypes = listOf(GARAGE, SAUNA)),
    ) {
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterVillaInformationUiState>()
        .apply {
          prop(EnterVillaInformationUiState::extraBuildingTypes).isEqualTo(listOf(GARAGE, SAUNA))
          prop(EnterVillaInformationUiState::isSublet).isEqualTo(ValidatedInput(false))
          prop(EnterVillaInformationUiState::ancillaryArea).isEqualTo(ValidatedInput(null))
          prop(EnterVillaInformationUiState::numberOfBathrooms).isEqualTo(ValidatedInput(null))
          prop(EnterVillaInformationUiState::yearOfConstruction).isEqualTo(ValidatedInput(null))
          prop(EnterVillaInformationUiState::extraBuildings).isEqualTo(listOf())
        }
      sendEvent(EnterVillaInformationEvent.ChangeIsSublet(true))
      sendEvent(EnterVillaInformationEvent.ChangeAncillaryArea("15"))
      sendEvent(EnterVillaInformationEvent.ChangeNumberOfBathrooms("2"))
      sendEvent(EnterVillaInformationEvent.ChangeYearOfConstruction("1999"))
      sendEvent(
        EnterVillaInformationEvent.AddExtraBuilding(
          ExtraBuilding(
            "iddd",
            3,
            type = GARAGE,
            hasWaterConnected = false,
          ),
        ),
      )
      skipItems(4)
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterVillaInformationUiState>()
        .apply {
          prop(EnterVillaInformationUiState::extraBuildingTypes).isEqualTo(listOf(GARAGE, SAUNA))
          prop(EnterVillaInformationUiState::isSublet).isEqualTo(ValidatedInput(true))
          prop(EnterVillaInformationUiState::ancillaryArea).isEqualTo(ValidatedInput("15"))
          prop(EnterVillaInformationUiState::numberOfBathrooms).isEqualTo(ValidatedInput("2"))
          prop(EnterVillaInformationUiState::yearOfConstruction).isEqualTo(ValidatedInput("1999"))
          prop(EnterVillaInformationUiState::extraBuildings).isEqualTo(
            listOf(
              ExtraBuilding(
                "iddd",
                3,
                type = GARAGE,
                hasWaterConnected = false,
              ),
            ),
          )
        }
    }
  }

  @Test
  fun `when user removes an extra building it gets removed from the uiState`() = runTest {
    val presenter = EnterVillaInformationPresenter(fakeMovingParametersForVilla)
    presenter.test(
      EnterVillaInformationUiState(
        extraBuildingTypes = listOf(GARAGE, SAUNA),
        isSublet = ValidatedInput(true),
        ancillaryArea = ValidatedInput("15"),
        numberOfBathrooms = ValidatedInput("2"),
        yearOfConstruction = ValidatedInput("1999"),
        extraBuildings = listOf(
          ExtraBuilding(
            "iddd",
            3,
            type = GARAGE,
            hasWaterConnected = false,
          ),
        ),
      ),
    ) {
      skipItems(1)
      sendEvent(
        EnterVillaInformationEvent.RemoveExtraBuildingClicked(
          ExtraBuilding(
            "iddd",
            3,
            type = GARAGE,
            hasWaterConnected = false,
          ),
        ),
      )
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterVillaInformationUiState>()
        .apply {
          prop(EnterVillaInformationUiState::extraBuildings).isEqualTo(
            listOf(),
          )
        }
    }
  }

  @Test
  fun `when continue the parameters received from previous destination are passed further to next destination along with new values`() =
    runTest {
      val presenter = EnterVillaInformationPresenter(fakeMovingParametersForVilla)
      presenter.test(
        EnterVillaInformationUiState(extraBuildingTypes = listOf(GARAGE, SAUNA)),
      ) {
        sendEvent(EnterVillaInformationEvent.ChangeIsSublet(true))
        sendEvent(EnterVillaInformationEvent.ChangeAncillaryArea("15"))
        sendEvent(EnterVillaInformationEvent.ChangeNumberOfBathrooms("2"))
        sendEvent(EnterVillaInformationEvent.ChangeYearOfConstruction("1999"))
        sendEvent(
          EnterVillaInformationEvent.AddExtraBuilding(
            ExtraBuilding(
              "iddd",
              3,
              type = GARAGE,
              hasWaterConnected = false,
            ),
          ),
        )
        skipItems(6)
        sendEvent(EnterVillaInformationEvent.SubmitNewAddress)
        assertk.assertThat(awaitItem())
          .isInstanceOf<EnterVillaInformationUiState>()
          .prop(EnterVillaInformationUiState::movingParameters).isEqualTo(
            fakeMovingParametersForVilla.copy(
              villaOnlyParameters = VillaOnlyParameters(
                isSublet = true,
                ancillaryArea = "15",
                numberOfBathrooms = "2",
                yearOfConstruction = "1999",
                extraBuildings = listOf(
                  ExtraBuilding(
                    "iddd",
                    3,
                    type = GARAGE,
                    hasWaterConnected = false,
                  ),
                ),
              ),
            ),
          )
      }
    }
}

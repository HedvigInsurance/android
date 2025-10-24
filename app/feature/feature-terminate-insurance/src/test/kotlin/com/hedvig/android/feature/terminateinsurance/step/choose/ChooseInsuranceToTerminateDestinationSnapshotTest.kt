package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.termination.data.TerminatableInsurance
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import org.junit.Rule
import org.junit.Test

/**
 * Paparazzi Snapshot Testing Commands:
 *
 * Record/Update snapshots:
 * `./gradlew :feature-terminate-insurance:recordPaparazziDebug`
 *
 * Verify snapshots:
 * `./gradlew :feature-terminate-insurance:verifyPaparazziDebug`
 *
 * Snapshot location: app/feature/feature-terminate-insurance/src/test/snapshots/images/
 */
class ChooseInsuranceToTerminateDestinationSnapshotTest {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5,
    renderingMode = RenderingMode.SHRINK,
    showSystemUi = false,
  )

  @Test
  fun loadingState() {
    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.Loading,
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Test
  fun failureState() {
    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.Failure,
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Test
  fun notAllowedState() {
    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.NotAllowed,
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Test
  fun successStateWithNoSelection() {
    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.Success(
            nextStepWithInsurance = null,
            insuranceList = listOf(
              TerminatableInsurance(
                id = "1",
                displayName = "Homeowner Insurance",
                contractExposure = "Opulullegatan 19",
                contractGroup = ContractGroup.HOUSE,
              ),
              TerminatableInsurance(
                id = "2",
                displayName = "Tenant Insurance",
                contractExposure = "Bullegatan 23",
                contractGroup = ContractGroup.RENTAL,
              ),
            ),
            selectedInsurance = null,
            isNavigationStepLoading = false,
            navigationStepFailedToLoad = false,
          ),
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Test
  fun successStateWithSelection() {
    val selectedInsurance = TerminatableInsurance(
      id = "2",
      displayName = "Tenant Insurance",
      contractExposure = "Bullegatan 23",
      contractGroup = ContractGroup.RENTAL,
    )

    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.Success(
            nextStepWithInsurance = null,
            insuranceList = listOf(
              TerminatableInsurance(
                id = "1",
                displayName = "Homeowner Insurance",
                contractExposure = "Opulullegatan 19",
                contractGroup = ContractGroup.HOUSE,
              ),
              selectedInsurance,
            ),
            selectedInsurance = selectedInsurance,
            isNavigationStepLoading = false,
            navigationStepFailedToLoad = false,
          ),
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Test
  fun successStateWithLoadingNextStep() {
    val selectedInsurance = TerminatableInsurance(
      id = "1",
      displayName = "Homeowner Insurance",
      contractExposure = "Opulullegatan 19",
      contractGroup = ContractGroup.HOUSE,
    )

    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.Success(
            nextStepWithInsurance = null,
            insuranceList = listOf(selectedInsurance),
            selectedInsurance = selectedInsurance,
            isNavigationStepLoading = true,
            navigationStepFailedToLoad = false,
          ),
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Test
  fun successStateWithNavigationError() {
    val selectedInsurance = TerminatableInsurance(
      id = "1",
      displayName = "Homeowner Insurance",
      contractExposure = "Opulullegatan 19",
      contractGroup = ContractGroup.HOUSE,
    )

    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.Success(
            nextStepWithInsurance = null,
            insuranceList = listOf(selectedInsurance),
            selectedInsurance = selectedInsurance,
            isNavigationStepLoading = false,
            navigationStepFailedToLoad = true,
          ),
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Test
  fun successStateWithMultipleInsurances() {
    paparazzi.snapshot {
      TestWrapper {
        ChooseInsuranceToTerminateScreen(
          uiState = ChooseInsuranceToTerminateStepUiState.Success(
            nextStepWithInsurance = null,
            insuranceList = listOf(
              TerminatableInsurance(
                id = "1",
                displayName = "Homeowner Insurance",
                contractExposure = "Opulullegatan 19",
                contractGroup = ContractGroup.HOUSE,
              ),
              TerminatableInsurance(
                id = "2",
                displayName = "Tenant Insurance",
                contractExposure = "Bullegatan 23",
                contractGroup = ContractGroup.RENTAL,
              ),
              TerminatableInsurance(
                id = "3",
                displayName = "Car Insurance",
                contractExposure = "Tesla Model 3, ABC123",
                contractGroup = ContractGroup.CAR,
              ),
              TerminatableInsurance(
                id = "4",
                displayName = "Travel Insurance",
                contractExposure = "Annual worldwide coverage",
                contractGroup = ContractGroup.ACCIDENT,
              ),
            ),
            selectedInsurance = null,
            isNavigationStepLoading = false,
            navigationStepFailedToLoad = false,
          ),
          navigateUp = {},
          reload = {},
          openChat = {},
          closeTerminationFlow = {},
          fetchTerminationStep = {},
          selectInsurance = {},
        )
      }
    }
  }

  @Composable
  private fun TestWrapper(content: @Composable () -> Unit) {
    HedvigTheme {
      Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
        content()
      }
    }
  }
}

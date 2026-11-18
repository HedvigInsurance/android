package com.hedvig.android.feature.change.mileage

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.change.mileage.data.MileageContract
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageChooseInsuranceKey
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageKey
import com.hedvig.android.feature.change.mileage.ui.choose.ChooseInsuranceForMileageEvent
import com.hedvig.android.feature.change.mileage.ui.choose.ChooseInsuranceForMileagePresenter
import com.hedvig.android.feature.change.mileage.ui.choose.ChooseInsuranceForMileageUiState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChooseInsuranceForMileagePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val getContracts = FakeGetMileageContractsUseCase()
  private val backstack = TestBackstack(mutableListOf(ChangeMileageChooseInsuranceKey))
  private val presenter = ChooseInsuranceForMileagePresenter(getContracts, backstack)

  @Test
  fun `a single eligible contract skips the choice and replaces this screen with the flow`() = runTest {
    presenter.test(ChooseInsuranceForMileageUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChooseInsuranceForMileageUiState.Loading)
      getContracts.turbine.add(listOf(testContract("only")).right())
      expectNoEvents()
      assertThat(backstack.entries).isEqualTo(listOf(ChangeMileageKey("only")))
    }
  }

  @Test
  fun `no eligible contracts shows the empty state`() = runTest {
    presenter.test(ChooseInsuranceForMileageUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChooseInsuranceForMileageUiState.Loading)
      getContracts.turbine.add(emptyList<MileageContract>().right())
      assertThat(awaitItem()).isEqualTo(ChooseInsuranceForMileageUiState.NoEligibleInsurances)
    }
  }

  @Test
  fun `picking one of several contracts and continuing replaces this screen with the flow`() = runTest {
    val contracts = listOf(testContract("first"), testContract("second"))
    presenter.test(ChooseInsuranceForMileageUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChooseInsuranceForMileageUiState.Loading)
      getContracts.turbine.add(contracts.right())
      assertThat(awaitItem()).isEqualTo(ChooseInsuranceForMileageUiState.Success(contracts, null))
      sendEvent(ChooseInsuranceForMileageEvent.SelectInsurance("second"))
      assertThat(awaitItem()).isEqualTo(ChooseInsuranceForMileageUiState.Success(contracts, "second"))
      sendEvent(ChooseInsuranceForMileageEvent.Continue)
      expectNoEvents()
      assertThat(backstack.entries).isEqualTo(listOf(ChangeMileageKey("second")))
    }
  }
}

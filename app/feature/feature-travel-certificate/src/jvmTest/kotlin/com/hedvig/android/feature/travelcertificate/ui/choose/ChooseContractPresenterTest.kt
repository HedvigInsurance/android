package com.hedvig.android.feature.travelcertificate.ui.choose

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.travelcertificate.FakeGetEligibleContractsWithAddressUseCase
import com.hedvig.android.feature.travelcertificate.data.ContractEligibleWithAddress
import com.hedvig.android.feature.travelcertificate.expectNoStateChange
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChooseContractPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val eligibleContracts = FakeGetEligibleContractsWithAddressUseCase()
  private val presenter = ChooseContractPresenter(eligibleContracts)

  private val contracts = listOf(
    ContractEligibleWithAddress(address = "Street 1", contractId = "home"),
    ContractEligibleWithAddress(address = "Lake 2", contractId = "summerHouse"),
  )

  @Test
  fun `loading then the eligible contracts`() = runTest {
    presenter.test(ChooseContractUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Loading)
      eligibleContracts.responses.trySend(contracts.right())
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Success(contracts))
    }
  }

  @Test
  fun `a failure can be reloaded, and the failure stays on screen until the reload answers`() = runTest {
    presenter.test(ChooseContractUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Loading)
      eligibleContracts.responses.trySend(ErrorMessage("offline").left())
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Failure)

      sendEvent(ChooseContractEvent.RetryLoadData)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(ChooseContractUiState.Failure)
      assertThat(eligibleContracts.calls).isEqualTo(2)

      eligibleContracts.responses.trySend(contracts.right())
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Success(contracts))
    }
  }

  @Test
  fun `a reload after content does not fetch again`() = runTest {
    presenter.test(ChooseContractUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Loading)
      eligibleContracts.responses.trySend(contracts.right())
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Success(contracts))

      sendEvent(ChooseContractEvent.RetryLoadData)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(ChooseContractUiState.Success(contracts))
      assertThat(eligibleContracts.calls).isEqualTo(1)
    }
  }

  @Test
  fun `starting from earlier content shows it without fetching`() = runTest {
    presenter.test(ChooseContractUiState.Success(contracts)) {
      assertThat(awaitItem()).isEqualTo(ChooseContractUiState.Success(contracts))
      testScheduler.advanceUntilIdle()
      expectNoStateChange(ChooseContractUiState.Success(contracts))
      assertThat(eligibleContracts.calls).isEqualTo(0)
    }
  }
}

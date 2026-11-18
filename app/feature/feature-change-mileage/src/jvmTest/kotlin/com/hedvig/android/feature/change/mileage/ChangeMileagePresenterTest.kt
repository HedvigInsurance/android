package com.hedvig.android.feature.change.mileage

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.change.mileage.data.CreateMileageIntentError
import com.hedvig.android.feature.change.mileage.navigation.ChangeMileageSummaryKey
import com.hedvig.android.feature.change.mileage.ui.start.ChangeMileageEvent
import com.hedvig.android.feature.change.mileage.ui.start.ChangeMileagePresenter
import com.hedvig.android.feature.change.mileage.ui.start.ChangeMileageUiState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChangeMileagePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val contractId = "contractId"
  private val getContracts = FakeGetMileageContractsUseCase()
  private val createIntent = FakeCreateMileageIntentUseCase()
  private val backstack = TestBackstack()
  private val presenter = ChangeMileagePresenter(contractId, getContracts, createIntent, backstack)

  @Test
  fun `the current mileage is preselected and continuing is not possible until another one is picked`() = runTest {
    presenter.test(ChangeMileageUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.Loading)
      getContracts.turbine.add(listOf(testContract(contractId)).right())
      createIntent.turbine.add(testIntent.right())
      val state = awaitItem()
      assertThat(state).isInstanceOf(ChangeMileageUiState.Success::class).apply {
        prop(ChangeMileageUiState.Success::selectedQuote).isEqualTo(currentQuote)
        prop(ChangeMileageUiState.Success::isCurrentSelected).isTrue()
      }
      sendEvent(ChangeMileageEvent.Continue)
      sendEvent(ChangeMileageEvent.SelectQuote(lowerQuote.id))
      assertThat(awaitItem()).isInstanceOf(ChangeMileageUiState.Success::class)
        .prop(ChangeMileageUiState.Success::selectedQuote)
        .isEqualTo(lowerQuote)
      assertThat(backstack.entries).isEqualTo(emptyList())
      sendEvent(ChangeMileageEvent.Continue)
      expectNoEvents()
      assertThat(backstack.entries.single()).isInstanceOf(ChangeMileageSummaryKey::class)
        .prop(ChangeMileageSummaryKey::params)
        .transform { it.quote to it.currentMonthlyCost }
        .isEqualTo(lowerQuote to testIntent.currentTotalCost.monthlyNet)
    }
  }

  @Test
  fun `a contract that does not support changing mileage shows the failure state`() = runTest {
    presenter.test(ChangeMileageUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.Loading)
      getContracts.turbine.add(listOf(testContract("someOtherContract")).right())
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.Failure)
    }
  }

  @Test
  fun `failing to load contracts shows the failure state and retrying loads again`() = runTest {
    presenter.test(ChangeMileageUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.Loading)
      getContracts.turbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.Failure)
      sendEvent(ChangeMileageEvent.Reload)
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.Loading)
      getContracts.turbine.add(listOf(testContract(contractId)).right())
      createIntent.turbine.add(testIntent.right())
      assertThat(awaitItem()).isInstanceOf(ChangeMileageUiState.Success::class)
    }
  }

  @Test
  fun `a user error from the backend is shown with its message`() = runTest {
    presenter.test(ChangeMileageUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.Loading)
      getContracts.turbine.add(listOf(testContract(contractId)).right())
      createIntent.turbine.add(CreateMileageIntentError.UserError("Not now").left())
      assertThat(awaitItem()).isEqualTo(ChangeMileageUiState.UserError("Not now"))
    }
  }
}

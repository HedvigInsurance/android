package ui.stepstart

import FakeChangeTierRepository
import TestBackstack
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.DeflectOutput
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.feature.change.tier.navigation.ChooseTierKey
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.GENERAL
import com.hedvig.android.feature.change.tier.ui.stepstart.FailureReason.QUOTES_ARE_EMPTY
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangePresenter
import com.hedvig.android.feature.change.tier.ui.stepstart.StartTierChangeState
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import testQuote
import testQuote2

class StartTierChangePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val insuranceId = "testId"

  @Test
  fun `if the quote list comes empty show empty quotes screen`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val presenter = StartTierChangePresenter(
      tierRepository = tierRepo,
      insuranceID = insuranceId,
      backstack = TestBackstack(),
    )
    presenter.test(StartTierChangeState.Loading) {
      tierRepo.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          IntentOutput(
            activationDate = LocalDate(2024, 11, 11),
            quotes = emptyList(),
          ),
          null,
        ).right(),
      )
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(StartTierChangeState.Failure::class)
        .prop(StartTierChangeState.Failure::reason)
        .isEqualTo(QUOTES_ARE_EMPTY)
    }
  }

  @Test
  fun `if gor error show general error screen`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val presenter = StartTierChangePresenter(
      tierRepository = tierRepo,
      insuranceID = insuranceId,
      backstack = TestBackstack(),
    )
    presenter.test(StartTierChangeState.Loading) {
      tierRepo.changeTierIntentTurbine.add(com.hedvig.android.core.common.ErrorMessage().left())
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(StartTierChangeState.Failure::class)
        .prop(StartTierChangeState.Failure::reason)
        .isEqualTo(GENERAL)
    }
  }

  @Test
  fun `if the quote list comes not empty redirect to select tier destination`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val backstack = TestBackstack()
    val scheduler = testScheduler
    val presenter = StartTierChangePresenter(
      tierRepository = tierRepo,
      insuranceID = insuranceId,
      backstack = backstack,
    )
    presenter.test(StartTierChangeState.Loading) {
      tierRepo.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          IntentOutput(
            activationDate = LocalDate(2024, 11, 11),
            quotes = listOf(testQuote, testQuote2),
          ),
          null,
        ).right(),
      )
      scheduler.advanceUntilIdle()
      assertThat(backstack.entries.last()).isInstanceOf(ChooseTierKey::class)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `if deflectOutput is returned show deflect screen`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val presenter = StartTierChangePresenter(
      tierRepository = tierRepo,
      insuranceID = insuranceId,
      backstack = TestBackstack(),
    )
    presenter.test(StartTierChangeState.Loading) {
      tierRepo.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          intentOutput = null,
          deflectOutput = DeflectOutput(
            title = "Deflect title",
            message = "Deflect message",
          ),
        ).right(),
      )
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(StartTierChangeState.Deflect::class)
        .prop(StartTierChangeState.Deflect::title)
        .isEqualTo("Deflect title")
    }
  }

  @Test
  fun `if intent is returned with both intentOutput and deflectOutput deflect takes precedence`() = runTest {
    val tierRepo = FakeChangeTierRepository()
    val presenter = StartTierChangePresenter(
      tierRepository = tierRepo,
      insuranceID = insuranceId,
      backstack = TestBackstack(),
    )
    presenter.test(StartTierChangeState.Loading) {
      tierRepo.changeTierIntentTurbine.add(
        ChangeTierDeductibleIntent(
          intentOutput = IntentOutput(
            activationDate = LocalDate(2024, 11, 11),
            quotes = listOf(testQuote, testQuote2),
          ),
          deflectOutput = DeflectOutput(
            title = "Deflect title",
            message = "Deflect message",
          ),
        ).right(),
      )
      skipItems(1)
      val state = awaitItem()
      assertThat(state).isInstanceOf(StartTierChangeState.Deflect::class)
        .prop(StartTierChangeState.Deflect::title)
        .isEqualTo("Deflect title")
    }
  }
}

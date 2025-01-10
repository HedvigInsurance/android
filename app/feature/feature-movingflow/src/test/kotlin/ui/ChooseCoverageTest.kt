package ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.size
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleEvent
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductiblePresenter
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.ChoseCoverageLevelAndDeductibleUiState
import com.hedvig.android.feature.movingflow.ui.chosecoveragelevelanddeductible.TiersInfo
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChooseCoverageTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `should send to repo quote with the new coverage and if there is any same deductible when new tier is selected`() =
    runTest {
      val repo = FakeMovingFlowRepository()
      val presenter = ChoseCoverageLevelAndDeductiblePresenter(
        movingFlowRepository = repo,
      )
      presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
        repo.movingFlowStateTurbine.add(fakeMovingStateWithTiersAndDeductibles)
        skipItems(2)
        sendEvent(ChoseCoverageLevelAndDeductibleEvent.SelectCoverage(fakeHomeQuoteWithTiersDeductibles2.id))
        assertThat(repo.selectedQuoteIdParameterThatWasSentIn)
          .isEqualTo(fakeHomeQuoteWithTiersDeductibles1.id)
      }
    }

  @Test
  fun `should send to repo newly selected quote itself if there is no same deductible when new tier is selected`() =
    runTest {
      val repo = FakeMovingFlowRepository()
      val presenter = ChoseCoverageLevelAndDeductiblePresenter(
        movingFlowRepository = repo,
      )
      presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
        repo.movingFlowStateTurbine.add(fakeMovingStateWithTiersAndDeductibles)
        skipItems(2)
        sendEvent(ChoseCoverageLevelAndDeductibleEvent.SelectCoverage(fakeHomeQuoteWithTiersDeductibles5.id))
        assertThat(repo.selectedQuoteIdParameterThatWasSentIn)
          .isEqualTo(fakeHomeQuoteWithTiersDeductibles5.id)
      }
    }

  @Test
  fun `should send to repo newly selected quote itself when new deductible is selected`() = runTest {
    val repo = FakeMovingFlowRepository()
    val presenter = ChoseCoverageLevelAndDeductiblePresenter(
      movingFlowRepository = repo,
    )
    presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTiersAndDeductibles)
      skipItems(2)
      sendEvent(ChoseCoverageLevelAndDeductibleEvent.SelectDeductible(fakeHomeQuoteWithTiersDeductibles4.id))
      assertThat(repo.selectedQuoteIdParameterThatWasSentIn)
        .isEqualTo(fakeHomeQuoteWithTiersDeductibles4.id)
    }
  }

  @Test
  fun `should show button loading and navigate to summary with selected home quote ID when selected quote is submitted`() =
    runTest {
      val repo = FakeMovingFlowRepository()
      val presenter = ChoseCoverageLevelAndDeductiblePresenter(
        movingFlowRepository = repo,
      )
      presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
        repo.movingFlowStateTurbine.add(
          fakeMovingStateWithTiersAndDeductibles.copy(
            lastSelectedHomeQuoteId = fakeHomeQuoteWithTiersDeductibles4.id,
          ),
        )
        skipItems(2)
        sendEvent(ChoseCoverageLevelAndDeductibleEvent.SubmitSelectedHomeQuoteId)
        val result = awaitItem()
        assertThat(result).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.Content::class)
          .prop(ChoseCoverageLevelAndDeductibleUiState.Content::isSubmitting)
          .isTrue()
        val result2 = awaitItem()
        assertThat(result2).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.Content::class)
          .prop(ChoseCoverageLevelAndDeductibleUiState.Content::navigateToSummaryScreenWithHomeQuoteId)
          .isEqualTo(fakeHomeQuoteWithTiersDeductibles4.id)
      }
    }

  @Test
  fun `should clear navigation parameter after navigating to summary`() = runTest {
    val repo = FakeMovingFlowRepository()
    val presenter = ChoseCoverageLevelAndDeductiblePresenter(
      movingFlowRepository = repo,
    )
    presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
      repo.movingFlowStateTurbine.add(
        fakeMovingStateWithTiersAndDeductibles.copy(
          lastSelectedHomeQuoteId = fakeHomeQuoteWithTiersDeductibles4.id,
        ),
      )
      sendEvent(ChoseCoverageLevelAndDeductibleEvent.SubmitSelectedHomeQuoteId)
      skipItems(4)
      sendEvent(ChoseCoverageLevelAndDeductibleEvent.NavigatedToSummary)
      val result2 = awaitItem()
      assertThat(result2).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.Content::class)
        .prop(ChoseCoverageLevelAndDeductibleUiState.Content::navigateToSummaryScreenWithHomeQuoteId)
        .isNull()
    }
  }

  @Test
  fun `should clear navigation parameter after navigating to comparison`() = runTest {
    val repo = FakeMovingFlowRepository()
    val presenter = ChoseCoverageLevelAndDeductiblePresenter(
      movingFlowRepository = repo,
    )
    presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTiersAndDeductibles)
      repo.movingFlowStateTurbine.add(
        fakeMovingStateWithTiersAndDeductibles.copy(
          lastSelectedHomeQuoteId = fakeHomeQuoteWithTiersDeductibles4.id,
        ),
      )
      sendEvent(ChoseCoverageLevelAndDeductibleEvent.LaunchComparison)
      skipItems(4)
      sendEvent(ChoseCoverageLevelAndDeductibleEvent.ClearNavigateToComparison)
      val result2 = awaitItem()
      assertThat(result2).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.Content::class)
        .prop(ChoseCoverageLevelAndDeductibleUiState.Content::comparisonParameters)
        .isNull()
    }
  }

  @Test
  fun `should navigate to comparison when LaunchComparison event is received`() = runTest {
    val repo = FakeMovingFlowRepository()
    val presenter = ChoseCoverageLevelAndDeductiblePresenter(
      movingFlowRepository = repo,
    )
    presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
      repo.movingFlowStateTurbine.add(
        fakeMovingStateWithTiersAndDeductibles.copy(
          lastSelectedHomeQuoteId = fakeHomeQuoteWithTiersDeductibles4.id,
        ),
      )
      sendEvent(ChoseCoverageLevelAndDeductibleEvent.LaunchComparison)
      skipItems(2)
      val result1 = awaitItem()
      assertThat(result1).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.Content::class)
        .prop(ChoseCoverageLevelAndDeductibleUiState.Content::comparisonParameters)
        .isNotNull()
    }
  }

  @Test
  fun `should correctly show tiers info`() = runTest {
    val repo = FakeMovingFlowRepository()
    val presenter = ChoseCoverageLevelAndDeductiblePresenter(
      movingFlowRepository = repo,
    )
    presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTiersAndDeductibles)
      skipItems(1)
      val result1 = awaitItem()
      assertThat(result1).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.Content::class)
        .prop(ChoseCoverageLevelAndDeductibleUiState.Content::tiersInfo)
        .isInstanceOf(TiersInfo::class)
        .prop(TiersInfo::allOptions)
        .size().isEqualTo(5)
    }
  }

  @Test
  fun `show error section when home quotes are empty`() = runTest {
    val repo = FakeMovingFlowRepository()
    val presenter = ChoseCoverageLevelAndDeductiblePresenter(
      movingFlowRepository = repo,
    )
    presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithNoQuotes)
      skipItems(1)
      val result1 = awaitItem()
      assertThat(result1).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.MissingOngoingMovingFlow::class)
    }
  }

  @Test
  fun `when no home quote is selected choose the default option`() = runTest {
    val repo = FakeMovingFlowRepository()
    val presenter = ChoseCoverageLevelAndDeductiblePresenter(
      movingFlowRepository = repo,
    )
    presenter.test(ChoseCoverageLevelAndDeductibleUiState.Loading) {
      repo.movingFlowStateTurbine.add(fakeMovingStateWithTiersAndDeductibles)
      skipItems(1)
      val result1 = awaitItem()
      assertThat(result1).isInstanceOf(ChoseCoverageLevelAndDeductibleUiState.Content::class)
        .prop(ChoseCoverageLevelAndDeductibleUiState.Content::tiersInfo)
        .isInstanceOf(TiersInfo::class)
        .prop(TiersInfo::selectedCoverage)
        .prop(MovingFlowQuotes.MoveHomeQuote::id)
        .isEqualTo(fakeHomeQuoteWithTiersDeductibles3.id)
    }
  }
}

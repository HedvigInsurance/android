package com.hedvig.android.feature.home.yearinreview.ui

import app.cash.turbine.Event
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.apollographql.cache.normalized.FetchPolicy
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.home.yearinreview.data.GetYearInReviewUseCase
import com.hedvig.android.feature.home.yearinreview.data.YearInReview
import com.hedvig.android.feature.home.yearinreview.data.YearInReviewStory
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class YearInReviewPresenterTest {
  private val enabledFeatureManager = FakeFeatureManager(mapOf(Feature.YEAR_IN_REVIEW to true))

  @Test
  fun `with the feature turned off the screen is unavailable and nothing is fetched`() = runTest {
    val useCase = FakeGetYearInReviewUseCase(YearInReview(stories).right())
    YearInReviewPresenter(useCase, FakeFeatureManager(mapOf(Feature.YEAR_IN_REVIEW to false)))
      .test(YearInReviewUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(YearInReviewUiState.Loading)
        assertThat(awaitItem()).isEqualTo(YearInReviewUiState.Unavailable)
        assertThat(useCase.invocations).isEqualTo(0)
        cancelAndIgnoreRemainingEvents()
      }
  }

  private val stories = List(3) { index ->
    YearInReviewStory(
      id = "$index",
      title = "title $index",
      description = "description $index",
      media = null,
      action = null,
    )
  }

  @Test
  fun `loading the stories starts on the first one`() = runTest {
    YearInReviewPresenter(FakeGetYearInReviewUseCase(YearInReview(stories).right()), enabledFeatureManager)
      .test(YearInReviewUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(YearInReviewUiState.Loading)
        assertThat(awaitContent().currentIndex).isEqualTo(0)
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `a failed load shows the failure and retrying recovers`() = runTest {
    val useCase = FakeGetYearInReviewUseCase(ErrorMessage().left())
    YearInReviewPresenter(useCase, enabledFeatureManager).test(YearInReviewUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(YearInReviewUiState.Loading)
      assertThat(awaitItem()).isEqualTo(YearInReviewUiState.Failure)

      useCase.result = YearInReview(stories).right()
      sendEvent(YearInReviewEvent.Retry)
      assertThat(awaitContent().currentIndex).isEqualTo(0)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `tapping forward and back moves between stories without leaving the ends`() = runTest {
    YearInReviewPresenter(FakeGetYearInReviewUseCase(YearInReview(stories).right()), enabledFeatureManager)
      .test(YearInReviewUiState.Loading) {
        awaitContent()

        sendEvent(YearInReviewEvent.Previous)
        sendEvent(YearInReviewEvent.Next)
        awaitContent { it.currentIndex == 1 }

        sendEvent(YearInReviewEvent.Next)
        sendEvent(YearInReviewEvent.Next)
        awaitContent { it.currentIndex == stories.lastIndex }

        sendEvent(YearInReviewEvent.Previous)
        awaitContent { it.currentIndex == 1 }
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `a story advances to the next one once its timer runs out`() = runTest {
    YearInReviewPresenter(FakeGetYearInReviewUseCase(YearInReview(stories).right()), enabledFeatureManager)
      .test(YearInReviewUiState.Loading) {
        val first = awaitContent()
        assertThat(first.currentIndex).isEqualTo(0)
        sendEvent(YearInReviewEvent.Resume)

        val advanced = awaitContent { it.currentIndex == 1 }
        assertThat(advanced.progress).isEqualTo(0f)
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `with an infinite story duration a story waits for the member to move on`() = runTest {
    YearInReviewPresenter(FakeGetYearInReviewUseCase(YearInReview(stories).right()), enabledFeatureManager)
      .test(YearInReviewUiState.Loading) {
        awaitContent()
        sendEvent(YearInReviewEvent.SetStoryDuration(Duration.INFINITE))
        sendEvent(YearInReviewEvent.Resume)
        testScheduler.advanceTimeBy(1.hours)

        val indexes = cancelAndConsumeRemainingEvents()
          .mapNotNull { ((it as? Event.Item)?.value as? YearInReviewUiState.Content)?.currentIndex }
        assertThat(indexes.all { it == 0 }).isTrue()
      }
  }
}

private suspend fun MoleculePresenterTestContext<YearInReviewEvent, YearInReviewUiState>.awaitContent(
  predicate: (YearInReviewUiState.Content) -> Boolean = { true },
): YearInReviewUiState.Content {
  while (true) {
    val item = awaitItem()
    if (item is YearInReviewUiState.Content && predicate(item)) return item
  }
}

private class FakeGetYearInReviewUseCase(
  var result: Either<ErrorMessage, YearInReview>,
) : GetYearInReviewUseCase {
  var invocations = 0

  override suspend fun invoke(fetchPolicy: FetchPolicy): Either<ErrorMessage, YearInReview> {
    invocations++
    return result
  }
}

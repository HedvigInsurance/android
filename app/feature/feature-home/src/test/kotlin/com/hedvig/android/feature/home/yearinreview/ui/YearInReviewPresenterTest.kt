package com.hedvig.android.feature.home.yearinreview.ui

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.home.yearinreview.data.GetYearInReviewUseCase
import com.hedvig.android.feature.home.yearinreview.data.YearInReview
import com.hedvig.android.feature.home.yearinreview.data.YearInReviewStory
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class YearInReviewPresenterTest {
  private val stories = List(3) { index ->
    YearInReviewStory(
      id = "$index",
      title = "title $index",
      leadText = "lead $index",
      text = "text $index",
      imageUrl = "https://example.com/$index.png",
    )
  }

  @Test
  fun `loading the stories starts on the first one`() = runTest {
    YearInReviewPresenter(FakeGetYearInReviewUseCase(YearInReview(stories).right()))
      .test(YearInReviewUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(YearInReviewUiState.Loading)
        assertThat(awaitContent().currentIndex).isEqualTo(0)
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `a failed load shows the failure and retrying recovers`() = runTest {
    val useCase = FakeGetYearInReviewUseCase(ErrorMessage().left())
    YearInReviewPresenter(useCase).test(YearInReviewUiState.Loading) {
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
    YearInReviewPresenter(FakeGetYearInReviewUseCase(YearInReview(stories).right()))
      .test(YearInReviewUiState.Loading) {
        awaitContent()

        sendEvent(YearInReviewEvent.Previous)
        sendEvent(YearInReviewEvent.Next)
        awaitContent { it.currentIndex == 1 }

        sendEvent(YearInReviewEvent.Next)
        sendEvent(YearInReviewEvent.Next)
        awaitContent { it.currentIndex == 2 && it.isLastStory }

        sendEvent(YearInReviewEvent.Previous)
        awaitContent { it.currentIndex == 1 }
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `a story advances to the next one once its timer runs out`() = runTest {
    YearInReviewPresenter(FakeGetYearInReviewUseCase(YearInReview(stories).right()))
      .test(YearInReviewUiState.Loading) {
        val first = awaitContent()
        assertThat(first.currentIndex).isEqualTo(0)
        sendEvent(YearInReviewEvent.Resume)

        val advanced = awaitContent { it.currentIndex == 1 }
        assertThat(advanced.progress).isEqualTo(0f)
        cancelAndIgnoreRemainingEvents()
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
  override suspend fun invoke(): Either<ErrorMessage, YearInReview> = result
}

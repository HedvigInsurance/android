package com.hedvig.android.feature.home.yearinreview.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.apollographql.cache.normalized.FetchPolicy
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class IsYearInReviewAvailableUseCaseTest {
  private val yearInReview = YearInReview(
    listOf(YearInReviewStory("0", "title", "description", null, null)),
  )

  @Test
  fun `with the feature turned off it is unavailable and nothing is fetched`() = runTest {
    val getYearInReviewUseCase = FakeGetYearInReviewUseCase(yearInReview.right())
    val useCase = IsYearInReviewAvailableUseCaseImpl(
      FakeFeatureManager(mapOf(Feature.YEAR_IN_REVIEW to false)),
      getYearInReviewUseCase,
    )

    assertThat(useCase.invoke().first()).isFalse()
    assertThat(getYearInReviewUseCase.fetchPolicies).isEqualTo(emptyList<FetchPolicy>())
  }

  @Test
  fun `with the feature turned on it is available when there are stories, read from the cache first`() = runTest {
    val getYearInReviewUseCase = FakeGetYearInReviewUseCase(yearInReview.right())
    val useCase = IsYearInReviewAvailableUseCaseImpl(
      FakeFeatureManager(mapOf(Feature.YEAR_IN_REVIEW to true)),
      getYearInReviewUseCase,
    )

    assertThat(useCase.invoke().first()).isTrue()
    assertThat(getYearInReviewUseCase.fetchPolicies).isEqualTo(listOf(FetchPolicy.CacheFirst))
  }

  @Test
  fun `with the feature turned on it is unavailable when there are no stories or they cannot be fetched`() = runTest {
    val useCase = IsYearInReviewAvailableUseCaseImpl(
      FakeFeatureManager(mapOf(Feature.YEAR_IN_REVIEW to true)),
      FakeGetYearInReviewUseCase(ErrorMessage().left()),
    )

    assertThat(useCase.invoke().first()).isFalse()
  }
}

private class FakeGetYearInReviewUseCase(
  private val result: Either<ErrorMessage, YearInReview>,
) : GetYearInReviewUseCase {
  val fetchPolicies = mutableListOf<FetchPolicy>()

  override suspend fun invoke(fetchPolicy: FetchPolicy): Either<ErrorMessage, YearInReview> {
    fetchPolicies += fetchPolicy
    return result
  }
}

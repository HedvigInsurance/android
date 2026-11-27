package com.hedvig.android.feature.home.yearinreview.data

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestNetworkError
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.YearInReviewQuery
import octopus.builder.Data
import octopus.builder.buildMember
import octopus.builder.buildYearInReviewAction
import octopus.builder.buildYearInReviewArticle
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
internal class GetYearInReviewUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `the articles become stories in order, keeping their image and action`() = runTest {
    apolloClient.registerTestResponse(
      YearInReviewQuery(),
      YearInReviewQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          yearInReview = listOf(
            buildYearInReviewArticle {
              title = "first"
              description = "first description"
              fileUrl = "https://example.com/first.png"
              action = null
            },
            buildYearInReviewArticle {
              title = "second"
              description = "second description"
              fileUrl = null
              action = buildYearInReviewAction {
                title = "Read more"
                url = "https://example.com"
              }
            },
          )
        }
      },
    )

    val result = GetYearInReviewUseCaseImpl(apolloClient).invoke()

    assertThat(result).isEqualTo(
      YearInReview(
        listOf(
          YearInReviewStory(
            "0",
            "first",
            "first description",
            YearInReviewMedia.Image("https://example.com/first.png"),
            null,
          ),
          YearInReviewStory(
            "1",
            "second",
            "second description",
            null,
            YearInReviewAction("Read more", "https://example.com"),
          ),
        ),
      ).right(),
    )
  }

  @Test
  fun `no articles is an error, so there is nothing to open`() = runTest {
    apolloClient.registerTestResponse(
      YearInReviewQuery(),
      YearInReviewQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember { yearInReview = emptyList() }
      },
    )

    assertThat(GetYearInReviewUseCaseImpl(apolloClient).invoke().isLeft()).isTrue()
  }

  @Test
  fun `a failed request is an error`() = runTest {
    apolloClient.registerTestNetworkError(YearInReviewQuery())

    assertThat(GetYearInReviewUseCaseImpl(apolloClient).invoke().isLeft()).isTrue()
  }

  @Test
  fun `a file is a video when its path ends in a video extension, ignoring case and query`() {
    listOf(
      "https://example.com/a.mp4",
      "https://example.com/a.MOV",
      "https://example.com/a.webm?width=400",
      "https://example.com/a.mp4#t=2",
    ).forEach { url ->
      assertThat(yearInReviewMedia(url)).isEqualTo(YearInReviewMedia.Video(url))
    }
  }

  @Test
  fun `any other file is an image`() {
    listOf(
      "https://example.com/a.gif",
      "https://example.com/a.jpg",
      "https://example.com/mp4/a.png",
      "https://example.com/a",
      "https://example.com/image?format=mp4",
    ).forEach { url ->
      assertThat(yearInReviewMedia(url)).isEqualTo(YearInReviewMedia.Image(url))
    }
  }
}

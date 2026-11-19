package com.hedvig.android.memberreminders

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import octopus.ClaimRatingRemindersQuery
import octopus.builder.Data
import octopus.builder.buildClaim
import octopus.builder.buildMember
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetClaimRatingRemindersUseCaseTest {
  @get:Rule
  val testApolloClientRule = TestApolloClientRule()

  private fun useCase(isClaimRatingEnabled: Boolean) = GetClaimRatingRemindersUseCaseImpl(
    testApolloClientRule.apolloClient,
    FakeFeatureManager(mapOf(Feature.ENABLE_CLAIM_RATING to isClaimRatingEnabled)),
  )

  @Test
  fun `with the flag off there are no reminders and no query is made`() = runTest {
    // Nothing is enqueued, so a query would fail and surface as a Left.
    assertThat(useCase(isClaimRatingEnabled = false).invoke().first())
      .isEqualTo(emptyList<MemberReminder.ClaimRatingPending>().right())
  }

  @Test
  fun `with the flag on each claim pending a rating becomes a reminder`() = runTest {
    testApolloClientRule.apolloClient.enqueueTestResponse(
      ClaimRatingRemindersQuery(),
      ClaimRatingRemindersQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          claimsPendingRating = listOf(
            buildClaim {
              id = "claim-1"
              hasSubmittedRating = false
            },
            buildClaim {
              id = "claim-2"
              hasSubmittedRating = true
            },
          )
        }
      },
    )

    assertThat(useCase(isClaimRatingEnabled = true).invoke().first())
      .isEqualTo(listOf(MemberReminder.ClaimRatingPending("claim-1")).right())
  }
}

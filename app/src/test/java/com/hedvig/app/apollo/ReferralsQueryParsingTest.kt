@file:OptIn(ApolloExperimental::class)

package com.hedvig.app.apollo

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.enqueue
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
import org.junit.Test

class ReferralsQueryParsingTest {

  @Test
  fun `apollo parses a referral query with multiple referrals in different states`() =
    runApolloTest { mockServer, apolloClient ->
      val originalData = REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
      val jsonData = originalData.toJsonStringWithData()
      mockServer.enqueue(jsonData)

      val response = apolloClient
        .query(ReferralsQuery())
        .execute()

      assertThat(response.data).isNotNull()
      assertThat(response.data!!).isEqualTo(originalData)
    }
}

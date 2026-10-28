package com.hedvig.android.feature.editcoinsured.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.PersonalInformationQuery
import org.junit.Rule
import org.junit.Test

class FetchCoInsuredPersonalInformationUseCaseImplTest {
  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val ssn = "199001011234"

  @OptIn(ApolloExperimental::class)
  private suspend fun fetchWithNames(firstName: String, lastName: String): CoInsuredPersonalInformation? {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PersonalInformationQuery(ssn),
        data = PersonalInformationQuery.Data(
          PersonalInformationQuery.Data.PersonalInformation(
            __typename = "PersonalInformation",
            firstName = firstName,
            lastName = lastName,
          ),
        ),
      )
    }
    return FetchCoInsuredPersonalInformationUseCaseImpl(apolloClient).invoke(ssn).getOrNull()
  }

  @Test
  fun `a lookup with both names returns them`() = runTest {
    assertThat(fetchWithNames("First", "Last"))
      .isEqualTo(CoInsuredPersonalInformation.FullInfo("First", "Last"))
  }

  @Test
  fun `a lookup with a blank name falls back to manual entry with the birth date from the ssn`() = runTest {
    val expected = CoInsuredPersonalInformation.EmptyInfo(LocalDate(1990, 1, 1))

    assertThat(fetchWithNames("", "Last")).isEqualTo(expected)
    assertThat(fetchWithNames("First", "  ")).isEqualTo(expected)
  }
}

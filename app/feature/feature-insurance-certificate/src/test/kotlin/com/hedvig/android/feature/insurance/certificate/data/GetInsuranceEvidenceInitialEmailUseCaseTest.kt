package com.hedvig.android.feature.insurance.certificate.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.InsuranceEvidenceInitialDataQuery
import octopus.type.buildMember
import octopus.type.buildMemberActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ApolloExperimental::class)
@RunWith(JUnit4::class)
internal class GetInsuranceEvidenceInitialEmailUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when network request fails, we get Error response`() = runTest {
    val getInsuranceEvidenceInitialEmailUseCase = GetInsuranceEvidenceInitialEmailUseCaseImpl(apolloClient)

    apolloClient.enqueueTestNetworkError()
    val result = getInsuranceEvidenceInitialEmailUseCase.invoke()

    assertThat(result).isLeft().isInstanceOf<ErrorMessage>()
  }

  @Test
  fun `when isCreatingOfInsuranceEvidenceEnabled is false, we get Error response`() = runTest {
    val getInsuranceEvidenceInitialEmailUseCase = GetInsuranceEvidenceInitialEmailUseCaseImpl(apolloClient)

    apolloClient.enqueueTestResponse(
      InsuranceEvidenceInitialDataQuery(),
      InsuranceEvidenceInitialDataQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          email = "test@email.com"
          memberActions = buildMemberActions {
            isCreatingOfInsuranceEvidenceEnabled = false
          }
        }
      },
    )

    val result = getInsuranceEvidenceInitialEmailUseCase.invoke()

    assertThat(result).isLeft().transform { it.message }.isEqualTo("isCreatingOfInsuranceEvidenceEnabled is false")
  }

  @Test
  fun `when memberActions is null, we get Error response`() = runTest {
    val getInsuranceEvidenceInitialEmailUseCase = GetInsuranceEvidenceInitialEmailUseCaseImpl(apolloClient)

    apolloClient.enqueueTestResponse(
      InsuranceEvidenceInitialDataQuery(),
      InsuranceEvidenceInitialDataQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          email = "test@email.com"
          memberActions = null
        }
      },
    )

    val result = getInsuranceEvidenceInitialEmailUseCase.invoke()

    assertThat(result).isLeft().transform { it.message }.isEqualTo("isCreatingOfInsuranceEvidenceEnabled is false")
  }

  @Test
  fun `when isCreatingOfInsuranceEvidenceEnabled is true, we get success response with email`() = runTest {
    val getInsuranceEvidenceInitialEmailUseCase = GetInsuranceEvidenceInitialEmailUseCaseImpl(apolloClient)
    val email = "test@email.com"

    apolloClient.enqueueTestResponse(
      InsuranceEvidenceInitialDataQuery(),
      InsuranceEvidenceInitialDataQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          this.email = email
          memberActions = buildMemberActions {
            isCreatingOfInsuranceEvidenceEnabled = true
          }
        }
      },
    )

    val result = getInsuranceEvidenceInitialEmailUseCase.invoke()

    assertThat(result).isRight().isEqualTo(email)
  }
}

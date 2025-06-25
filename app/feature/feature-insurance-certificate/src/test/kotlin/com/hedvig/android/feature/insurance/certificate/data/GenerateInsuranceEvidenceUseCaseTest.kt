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
import octopus.InsuranceEvidenceCreateMutation
import octopus.type.InsuranceEvidenceInput
import octopus.type.buildInsuranceEvidenceInformation
import octopus.type.buildInsuranceEvidenceOutput
import octopus.type.buildUserError
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ApolloExperimental::class)
@RunWith(JUnit4::class)
internal class GenerateInsuranceEvidenceUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when network request fails, we get Error response`() = runTest {
    val generateInsuranceEvidenceUseCase = GenerateInsuranceEvidenceUseCaseImpl(apolloClient)

    apolloClient.enqueueTestNetworkError()
    val result = generateInsuranceEvidenceUseCase.invoke("test@email.com")

    assertThat(result).isLeft().isInstanceOf<ErrorMessage>()
  }

  @Test
  fun `when backend returns user error, we get Error response with the error message`() = runTest {
    val generateInsuranceEvidenceUseCase = GenerateInsuranceEvidenceUseCaseImpl(apolloClient)
    val errorMessage = "Something went wrong"

    apolloClient.enqueueTestResponse(
      InsuranceEvidenceCreateMutation(input = InsuranceEvidenceInput("test@email.com")),
      InsuranceEvidenceCreateMutation.Companion.Data(OctopusFakeResolver) {
        insuranceEvidenceCreate = buildInsuranceEvidenceOutput {
          userError = buildUserError {
            message = errorMessage
          }
        }
      },
    )

    val result = generateInsuranceEvidenceUseCase.invoke("test@email.com")

    assertThat(result).isLeft().transform { it.message }.isEqualTo(errorMessage)
  }

  @Test
  fun `when backend returns null insuranceEvidenceInformation, we get Error response`() = runTest {
    val generateInsuranceEvidenceUseCase = GenerateInsuranceEvidenceUseCaseImpl(apolloClient)

    apolloClient.enqueueTestResponse(
      InsuranceEvidenceCreateMutation(input = InsuranceEvidenceInput("test@email.com")),
      InsuranceEvidenceCreateMutation.Companion.Data(OctopusFakeResolver) {
        insuranceEvidenceCreate = buildInsuranceEvidenceOutput {
          insuranceEvidenceInformation = null
        }
      },
    )

    val result = generateInsuranceEvidenceUseCase.invoke("test@email.com")

    assertThat(result).isLeft().isInstanceOf<ErrorMessage>()
  }

  @Test
  fun `when backend returns valid signed url, we get success response with the url`() = runTest {
    val generateInsuranceEvidenceUseCase = GenerateInsuranceEvidenceUseCaseImpl(apolloClient)
    val signedUrl = "https://example.com/signed-url"

    apolloClient.enqueueTestResponse(
      InsuranceEvidenceCreateMutation(input = InsuranceEvidenceInput("test@email.com")),
      InsuranceEvidenceCreateMutation.Companion.Data(OctopusFakeResolver) {
        insuranceEvidenceCreate = buildInsuranceEvidenceOutput {
          insuranceEvidenceInformation = buildInsuranceEvidenceInformation {
            this.signedUrl = signedUrl
          }
          userError = null
        }
      },
    )

    val result = generateInsuranceEvidenceUseCase.invoke("test@email.com")

    assertThat(result).isRight().isEqualTo(signedUrl)
  }
}

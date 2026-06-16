package com.hedvig.android.memberreminders

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.memberreminders.PaymentReminder.ShowConnectPaymentReminder
import com.hedvig.android.memberreminders.PaymentReminder.ShowConnectPayoutReminder
import com.hedvig.android.memberreminders.PaymentReminder.ShowMissingPaymentsReminder
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.GetPayinMethodStatusQuery
import octopus.type.MissingPaymentConnection
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildMemberPaymentMethods
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
class GetConnectPaymentReminderUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `when payin connection is missing, show the connect payment reminder`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentMethods = buildMemberPaymentMethods {
            missingConnection = MissingPaymentConnection.PAYIN
          }
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result).isRight().isEqualTo(ShowConnectPaymentReminder)
  }

  @Test
  fun `when payout connection is missing, show the connect payout reminder`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentMethods = buildMemberPaymentMethods {
            missingConnection = MissingPaymentConnection.PAYOUT
          }
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result).isRight().isEqualTo(ShowConnectPayoutReminder)
  }

  @Test
  fun `when a contract is being terminated due to missed payments, show the missing payment reminder`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(apolloClient)
    val terminationDate = LocalDate.parse("2030-01-01")
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          this.activeContracts = listOf(
            this.buildContract {
              this.terminationDueToMissedPayments = true
              this.terminationDate = terminationDate
            },
          )
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result).isRight().isEqualTo(ShowMissingPaymentsReminder(terminationDate))
  }

  @Test
  fun `when no payment connection is missing, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(apolloClient)
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentMethods = buildMemberPaymentMethods {
            missingConnection = null
          }
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.DomainError.AlreadySetup)
  }

  @Test
  fun `on network failure, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(apolloClient)
    apolloClient.enqueueTestNetworkError()

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.NetworkError>()
  }
}
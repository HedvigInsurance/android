package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.testing.enqueueTestNetworkError
import com.apollographql.apollo3.testing.enqueueTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.GetPayinMethodStatusQuery
import octopus.type.MemberPaymentConnectionStatus
import octopus.type.buildMember
import octopus.type.buildMemberPaymentInformation
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetConnectPaymentReminderUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule()
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `with the feature flag on but payment already connected, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      doesHavePayingContractsUseCaseProvider,
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentInformation = buildMemberPaymentInformation {
            status = MemberPaymentConnectionStatus.ACTIVE
          }
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.AlreadySetup)
  }

  @Test
  fun `with the feature flag on but network failure, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      doesHavePayingContractsUseCaseProvider,
    )
    apolloClient.enqueueTestNetworkError()

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.NetworkError>()
      .prop(ConnectPaymentReminderError.NetworkError::message)
      .isEqualTo("Network error queued in QueueTestNetworkTransport")
  }

  @Test
  fun `for a non paying member, don't remind them to connect payment`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      onlyHasNonPayingContractsUseCaseProvider,
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.NonPayingMember>()
  }

  private val doesHavePayingContractsUseCaseProvider: Provider<GetOnlyHasNonPayingContractsUseCase> = Provider {
    object : GetOnlyHasNonPayingContractsUseCase {
      override suspend fun invoke(): Either<ErrorMessage, Boolean> {
        return false.right()
      }
    }
  }

  private val onlyHasNonPayingContractsUseCaseProvider: Provider<GetOnlyHasNonPayingContractsUseCase> = Provider {
    object : GetOnlyHasNonPayingContractsUseCase {
      override suspend fun invoke(): Either<ErrorMessage, Boolean> {
        return true.right()
      }
    }
  }
}

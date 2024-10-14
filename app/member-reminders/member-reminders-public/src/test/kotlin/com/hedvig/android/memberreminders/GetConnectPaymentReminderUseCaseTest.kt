package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.enqueueTestNetworkError
import com.apollographql.apollo.testing.enqueueTestResponse
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.market.Market
import com.hedvig.android.market.Market.SE
import com.hedvig.android.market.test.FakeMarketManager
import com.hedvig.android.memberreminders.PaymentReminder.ShowConnectPaymentReminder
import com.hedvig.android.memberreminders.PaymentReminder.ShowMissingPaymentsReminder
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import octopus.GetPayinMethodStatusQuery
import octopus.type.MemberPaymentConnectionStatus
import octopus.type.buildContract
import octopus.type.buildMember
import octopus.type.buildMemberPaymentInformation
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
  fun `when payin method needs setup, show the reminder`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      doesHavePayingContractsUseCaseProvider,
      FakeMarketManager(Market.SE),
    )
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentInformation = buildMemberPaymentInformation {
            status = MemberPaymentConnectionStatus.NEEDS_SETUP
          }
        }
      },
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result).isRight().isEqualTo(ShowConnectPaymentReminder)
  }

  @Test
  fun `when payin method does not need setup but is missing payments, show the missing payment reminder`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      doesHavePayingContractsUseCaseProvider,
      FakeMarketManager(Market.SE),
    )
    val terminationDate = LocalDate.parse("2030-01-01")
    apolloClient.enqueueTestResponse(
      GetPayinMethodStatusQuery(),
      GetPayinMethodStatusQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          paymentInformation = buildMemberPaymentInformation {
            status = MemberPaymentConnectionStatus.ACTIVE
          }
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
  fun `with the feature flag on but payment already connected, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      doesHavePayingContractsUseCaseProvider,
      FakeMarketManager(SE),
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

    assertThat(result).isLeft().isEqualTo(ConnectPaymentReminderError.DomainError.AlreadySetup)
  }

  @Test
  fun `with the feature flag on but network failure, don't get a 'ShowReminder' response`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      doesHavePayingContractsUseCaseProvider,
      FakeMarketManager(SE),
    )
    apolloClient.enqueueTestNetworkError()

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.NetworkError>()
  }

  @Test
  fun `for a non paying member, don't remind them to connect payment`() = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      onlyHasNonPayingContractsUseCaseProvider,
      FakeMarketManager(SE),
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.DomainError.NonPayingMember>()
  }

  @Test
  fun `for a non Swedish market, don't remind them to connect payment`(
    @TestParameter isDk: Boolean,
  ) = runTest {
    val getConnectPaymentReminderUseCase = GetConnectPaymentReminderUseCaseImpl(
      apolloClient,
      onlyHasNonPayingContractsUseCaseProvider,
      FakeMarketManager(if (isDk) Market.DK else Market.NO),
    )

    val result = getConnectPaymentReminderUseCase.invoke()

    assertThat(result)
      .isLeft()
      .isInstanceOf<ConnectPaymentReminderError.DomainError.NotSwedishMarket>()
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

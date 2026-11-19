package com.hedvig.android.feature.payments.overview.data

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.DataBuilderScope
import com.apollographql.apollo.testing.registerTestNetworkError
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.GetMemberTypeUseCase
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.feature.payments.data.ManualChargeToPrompt
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlin.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.UpcomingPaymentQuery
import octopus.builder.ContractMap
import octopus.builder.Data
import octopus.builder.MemberChargeMap
import octopus.builder.MemberPaymentMethodMap
import octopus.builder.buildContract
import octopus.builder.buildMember
import octopus.builder.buildMemberCharge
import octopus.builder.buildMemberPaymentMethod
import octopus.builder.buildMemberPaymentMethods
import octopus.builder.buildMoney
import octopus.type.CurrencyCode
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import octopus.type.MissingPaymentConnection
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class GetUpcomingPaymentUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `a default payin method still being activated is pending, even while the backend asks for payin`() = runTest {
    registerMember(missingConnection = MissingPaymentConnection.PAYIN) {
      buildPayinMethod(MemberPaymentMethodStatus.PENDING)
    }

    assertThat(useCase().invoke()).isRightWith { it.paymentConnection }.isEqualTo(PaymentConnection.Pending)
  }

  @Test
  fun `a missing payin connection carries the earliest termination caused by missed payments`() = runTest {
    registerMember(
      missingConnection = MissingPaymentConnection.PAYIN,
      activeContracts = {
        listOf(
          buildContract {
            terminationDueToMissedPayments = true
            terminationDate = LocalDate(2026, 12, 31)
          },
          buildContract {
            terminationDueToMissedPayments = true
            terminationDate = LocalDate(2026, 11, 30)
          },
          buildContract {
            terminationDueToMissedPayments = false
            terminationDate = LocalDate(2026, 10, 31)
          },
        )
      },
    ) { null }

    assertThat(useCase().invoke()).isRightWith { it.paymentConnection }
      .isEqualTo(PaymentConnection.NeedsPayinSetup(terminationDateIfNotConnected = LocalDate(2026, 11, 30)))
  }

  @Test
  fun `a missing payin connection without any termination has no due date`() = runTest {
    registerMember(missingConnection = MissingPaymentConnection.PAYIN) { null }

    assertThat(useCase().invoke()).isRightWith { it.paymentConnection }
      .isEqualTo(PaymentConnection.NeedsPayinSetup(terminationDateIfNotConnected = null))
  }

  @Test
  fun `a missing payout connection asks for payout setup`() = runTest {
    registerMember(missingConnection = MissingPaymentConnection.PAYOUT) {
      buildPayinMethod(MemberPaymentMethodStatus.ACTIVE)
    }

    assertThat(useCase().invoke()).isRightWith { it.paymentConnection }
      .isEqualTo(PaymentConnection.NeedsPayoutSetup)
  }

  @Test
  fun `nothing missing with an active default method is an active connection`() = runTest {
    registerMember(missingConnection = null) {
      buildPayinMethod(MemberPaymentMethodStatus.ACTIVE)
    }

    assertThat(useCase().invoke()).isRightWith { it.paymentConnection }.isEqualTo(PaymentConnection.Active)
  }

  @Test
  fun `a missed charge named by the backend is offered for manual charging at its net amount`() = runTest {
    registerMember(
      missedChargeIdToChargeManually = "missed",
      pastCharges = {
        listOf(
          buildMemberCharge {
            id = "older"
            net = buildMoney {
              amount = 99.0
              currencyCode = CurrencyCode.SEK
            }
          },
          buildMemberCharge {
            id = "missed"
            net = buildMoney {
              amount = 233.0
              currencyCode = CurrencyCode.SEK
            }
          },
        )
      },
    ) { null }

    assertThat(useCase().invoke()).isRightWith { it.isManualChargeAllowed }
      .isEqualTo(ManualChargeToPrompt(UiMoney(233.0, UiCurrencyCode.SEK)))
  }

  @Test
  fun `a missed charge the backend did not return is not offered for manual charging`() = runTest {
    registerMember(
      missedChargeIdToChargeManually = "missed",
      pastCharges = { listOf(buildMemberCharge { id = "someOtherCharge" }) },
    ) { null }

    assertThat(useCase().invoke()).isRightWith { it.isManualChargeAllowed }.isNull()
  }

  @Test
  fun `ongoing charges without an id are left out`() = runTest {
    registerMember(
      ongoingCharges = {
        listOf(
          buildMemberCharge {
            id = "ongoing"
            date = LocalDate(2026, 10, 27)
            net = buildMoney {
              amount = 150.0
              currencyCode = CurrencyCode.SEK
            }
          },
          buildMemberCharge { id = null },
        )
      },
    ) { null }

    assertThat(useCase().invoke()).isRightWith { it.ongoingCharges }.containsExactly(
      PaymentOverview.OngoingCharge("ongoing", LocalDate(2026, 10, 27), UiMoney(150.0, UiCurrencyCode.SEK)),
    )
  }

  @Test
  fun `a network failure is returned as an error`() = runTest {
    apolloClient.registerTestNetworkError(UpcomingPaymentQuery())

    assertThat(useCase().invoke()).isInstanceOf<Either.Left<ErrorMessage>>()
  }

  private fun useCase() = GetUpcomingPaymentUseCaseImpl(
    apolloClient = apolloClient,
    clock = Clock.System,
    getMemberTypeUseCase = object : GetMemberTypeUseCase {
      override suspend fun invoke(): Either<ErrorMessage, MemberType> = MemberType.STANDARD_MEMBER.right()
    },
  )

  private fun DataBuilderScope.buildPayinMethod(status: MemberPaymentMethodStatus) = buildMemberPaymentMethod {
    provider = MemberPaymentProvider.TRUSTLY
    this.status = status
    isDefault = true
    details = null
  }

  private fun registerMember(
    missingConnection: MissingPaymentConnection? = null,
    missedChargeIdToChargeManually: String? = null,
    pastCharges: DataBuilderScope.() -> List<MemberChargeMap> = { emptyList() },
    ongoingCharges: DataBuilderScope.() -> List<MemberChargeMap> = { emptyList() },
    activeContracts: DataBuilderScope.() -> List<ContractMap> = { emptyList() },
    defaultPayinMethod: DataBuilderScope.() -> MemberPaymentMethodMap?,
  ) {
    apolloClient.registerTestResponse(
      UpcomingPaymentQuery(),
      UpcomingPaymentQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          this.missedChargeIdToChargeManually = missedChargeIdToChargeManually
          showRetryChargeNotice = null
          this.pastCharges = pastCharges()
          this.ongoingCharges = ongoingCharges()
          this.activeContracts = activeContracts()
          futureCharge = null
          val defaultMethod = defaultPayinMethod()
          paymentMethods = buildMemberPaymentMethods {
            this.missingConnection = missingConnection
            this.defaultPayinMethod = defaultMethod
            payinMethods = listOfNotNull(defaultMethod)
            payoutMethods = emptyList()
            defaultPayoutMethod = null
          }
        }
      },
    )
  }
}

private fun <T> assertk.Assert<Either<ErrorMessage, PaymentOverview>>.isRightWith(
  extract: (PaymentOverview) -> T,
): assertk.Assert<T> = isInstanceOf<Either.Right<PaymentOverview>>()
  .prop("value") { extract(it.value) }

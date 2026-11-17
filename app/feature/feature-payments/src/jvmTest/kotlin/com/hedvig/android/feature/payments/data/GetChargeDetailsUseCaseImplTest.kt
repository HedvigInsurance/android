package com.hedvig.android.feature.payments.data

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
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
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payments.data.MemberCharge.MemberChargeStatus
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.PaymentHistoryWithDetailsQuery
import octopus.builder.Data
import octopus.builder.MemberChargeBuilder
import octopus.builder.MemberChargeMap
import octopus.builder.MemberPaymentMethodMap
import octopus.builder.buildMember
import octopus.builder.buildMemberCharge
import octopus.builder.buildMemberChargeBreakdownItem
import octopus.builder.buildMemberChargeBreakdownItemPeriod
import octopus.builder.buildMemberChargePriceBreakdownItem
import octopus.builder.buildMemberPaymentMethod
import octopus.builder.buildMemberPaymentMethods
import octopus.builder.buildMemberReferralInformation
import octopus.builder.buildMoney
import octopus.builder.buildPaymentMethodBankAccountDetails
import octopus.builder.buildPaymentMethodSwishDetails
import octopus.type.CurrencyCode
import octopus.type.MemberChargeStatus as OctopusChargeStatus
import octopus.type.MemberPaymentMethodStatus
import octopus.type.MemberPaymentProvider
import org.junit.Rule
import org.junit.Test

/**
 * The backend sends past charges newest first, as the history shows them. The details keep them oldest first, so the
 * charge after a past charge is the next one in [PaymentDetails.pastCharges].
 */
@OptIn(ApolloExperimental::class)
class GetChargeDetailsUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)
  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val january = expectedCharge("charge-jan", MemberChargeStatus.SUCCESS, LocalDate(2026, 1, 27))
  private val march = expectedCharge("charge-mar", MemberChargeStatus.SUCCESS, LocalDate(2026, 3, 27))
  private val upcoming = expectedCharge("upcoming", MemberChargeStatus.UPCOMING, LocalDate(2026, 4, 27))

  @Test
  fun `a failed past charge carries every field and is followed by the newer past charge`() = runTest {
    registerMember(
      pastCharges = {
        listOf(
          charge("charge-mar", OctopusChargeStatus.SUCCESS, LocalDate(2026, 3, 27)),
          failedFebruaryCharge(),
          charge("charge-jan", OctopusChargeStatus.SUCCESS, LocalDate(2026, 1, 27)),
        )
      },
      futureCharge = { charge("upcoming", OctopusChargeStatus.UPCOMING, LocalDate(2026, 4, 27)) },
    )

    val details = (useCase().invoke("charge-feb") as Either.Right).value

    val february = MemberCharge(
      grossAmount = sek(500.0),
      netAmount = sek(449.5),
      id = "charge-feb",
      status = MemberChargeStatus.FAILED,
      dueDate = LocalDate(2026, 2, 27),
      failedCharge = MemberCharge.FailedCharge(LocalDate(2026, 1, 1), LocalDate(2026, 1, 31), sek(300.0)),
      chargeBreakdowns = listOf(
        MemberCharge.ChargeBreakdown(
          contractDisplayName = "Home insurance",
          contractDetails = "Street 1",
          grossAmount = sek(500.0),
          netAmount = sek(449.5),
          periods = listOf(
            MemberCharge.ChargeBreakdown.Period(
              amount = sek(300.0),
              fromDate = LocalDate(2026, 1, 1),
              toDate = LocalDate(2026, 1, 31),
              isPreviouslyFailedCharge = true,
            ),
            MemberCharge.ChargeBreakdown.Period(
              amount = sek(149.5),
              fromDate = LocalDate(2026, 2, 1),
              toDate = LocalDate(2026, 2, 16),
              isPreviouslyFailedCharge = false,
            ),
          ),
          priceBreakdown = listOf("Base price" to sek(500.0), "Bundle discount" to sek(-50.5)),
        ),
        MemberCharge.ChargeBreakdown(
          contractDisplayName = "Car insurance",
          contractDetails = "",
          grossAmount = sek(0.0),
          netAmount = sek(0.0),
          periods = emptyList(),
          priceBreakdown = emptyList(),
        ),
      ),
      referralDiscount = Discount(
        code = "FRIEND",
        description = null,
        status = Discount.DiscountStatus.ACTIVE,
        statusDescription = null,
        amount = sek(-10.0),
        isReferral = true,
      ),
      carriedAdjustment = sek(12.25),
      settlementAdjustment = sek(3.0),
      chargeMethod = MemberPaymentChargeMethod.TRUSTLY,
    )
    assertThat(details).isEqualTo(
      PaymentDetails(
        memberCharge = february,
        pastCharges = listOf(january, february, march),
        paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
        upComingCharge = upcoming,
      ),
    )
    assertThat(details.getNextCharge(details.memberCharge)).isEqualTo(march)
  }

  @Test
  fun `the newest past charge has no later past charge, so its next charge is the upcoming one`() = runTest {
    registerMember(
      pastCharges = {
        listOf(
          charge("charge-mar", OctopusChargeStatus.FAILED, LocalDate(2026, 3, 27)),
          charge("charge-jan", OctopusChargeStatus.SUCCESS, LocalDate(2026, 1, 27)),
        )
      },
      futureCharge = { charge("upcoming", OctopusChargeStatus.UPCOMING, LocalDate(2026, 4, 27)) },
    )

    val details = (useCase().invoke("charge-mar") as Either.Right).value

    assertThat(details.memberCharge.status).isEqualTo(MemberChargeStatus.FAILED)
    assertThat(details.getNextCharge(details.memberCharge)).isNull()
    assertThat(details.upComingCharge).isEqualTo(upcoming)
  }

  @Test
  fun `an upcoming charge goes out on the default method and shows its account`() = runTest {
    registerMember(
      futureCharge = { charge("upcoming", OctopusChargeStatus.UPCOMING, LocalDate(2026, 4, 27), "trustly") },
      payinMethods = { listOf(swishMethod(MemberPaymentMethodStatus.ACTIVE)) },
    )

    val details = (useCase().invoke("upcoming") as Either.Right).value

    assertThat(details).isEqualTo(
      PaymentDetails(
        memberCharge = upcoming.copy(chargeMethod = MemberPaymentChargeMethod.SWISH),
        pastCharges = emptyList(),
        paymentsInfo = PaymentDetails.PaymentsInfo.Active(
          PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = true),
        ),
        upComingCharge = expectedCharge(
          "upcoming",
          MemberChargeStatus.UPCOMING,
          LocalDate(2026, 4, 27),
          method = MemberPaymentChargeMethod.TRUSTLY,
        ),
      ),
    )
  }

  @Test
  fun `an upcoming charge without an active default method keeps its own method and shows no account`() = runTest {
    registerMember(
      futureCharge = { charge("upcoming", OctopusChargeStatus.UPCOMING, LocalDate(2026, 4, 27), "trustly") },
      payinMethods = { listOf(swishMethod(MemberPaymentMethodStatus.PENDING)) },
    )

    val details = (useCase().invoke("upcoming") as Either.Right).value

    assertThat(details.memberCharge.chargeMethod).isEqualTo(MemberPaymentChargeMethod.TRUSTLY)
    assertThat(details.paymentsInfo).isEqualTo(PaymentDetails.PaymentsInfo.NoPresentableInfo)
  }

  @Test
  fun `an ongoing pending charge made with the default method shows its account`() = runTest {
    registerMember(
      ongoingCharges = { listOf(charge("ongoing", OctopusChargeStatus.PENDING, LocalDate(2026, 3, 27), "trustly")) },
      payinMethods = { listOf(trustlyMethod()) },
    )

    val details = (useCase().invoke("ongoing") as Either.Right).value

    assertThat(details).isEqualTo(
      PaymentDetails(
        memberCharge = expectedCharge(
          "ongoing",
          MemberChargeStatus.PENDING,
          LocalDate(2026, 3, 27),
          method = MemberPaymentChargeMethod.TRUSTLY,
        ),
        pastCharges = emptyList(),
        paymentsInfo = PaymentDetails.PaymentsInfo.Active(
          PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = false, isDefault = true),
        ),
        upComingCharge = null,
      ),
    )
  }

  @Test
  fun `an ongoing charge made with another method than the default shows no account`() = runTest {
    registerMember(
      ongoingCharges = { listOf(charge("ongoing", OctopusChargeStatus.PENDING, LocalDate(2026, 3, 27), "swish")) },
      payinMethods = { listOf(trustlyMethod()) },
    )

    val details = (useCase().invoke("ongoing") as Either.Right).value

    assertThat(details.memberCharge.chargeMethod).isEqualTo(MemberPaymentChargeMethod.SWISH)
    assertThat(details.paymentsInfo).isEqualTo(PaymentDetails.PaymentsInfo.NoPresentableInfo)
  }

  @Test
  fun `a successful past charge shows no account even when it was made with the default method`() = runTest {
    registerMember(
      pastCharges = { listOf(charge("charge-jan", OctopusChargeStatus.SUCCESS, LocalDate(2026, 1, 27), "trustly")) },
      payinMethods = { listOf(trustlyMethod()) },
    )

    val details = (useCase().invoke("charge-jan") as Either.Right).value

    assertThat(details).isEqualTo(
      PaymentDetails(
        memberCharge = january.copy(chargeMethod = MemberPaymentChargeMethod.TRUSTLY),
        pastCharges = listOf(january.copy(chargeMethod = MemberPaymentChargeMethod.TRUSTLY)),
        paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
        upComingCharge = null,
      ),
    )
  }

  @Test
  fun `a null id selects the upcoming charge without an id first`() = runTest {
    registerMember(
      pastCharges = { listOf(charge(null, OctopusChargeStatus.SUCCESS, LocalDate(2026, 1, 27))) },
      futureCharge = { charge(null, OctopusChargeStatus.UPCOMING, LocalDate(2026, 4, 27)) },
    )

    val details = (useCase().invoke(null) as Either.Right).value

    assertThat(details.memberCharge).isEqualTo(upcoming.copy(id = null))
  }

  @Test
  fun `a null id selects the first past charge without an id when the upcoming charge has one`() = runTest {
    registerMember(
      pastCharges = {
        listOf(
          charge(null, OctopusChargeStatus.FAILED, LocalDate(2026, 3, 27)),
          charge("charge-feb", OctopusChargeStatus.SUCCESS, LocalDate(2026, 2, 27)),
          charge(null, OctopusChargeStatus.SUCCESS, LocalDate(2026, 1, 27)),
        )
      },
      futureCharge = { charge("upcoming", OctopusChargeStatus.UPCOMING, LocalDate(2026, 4, 27)) },
    )

    val details = (useCase().invoke(null) as Either.Right).value

    assertThat(details.memberCharge).isEqualTo(january.copy(id = null))
  }

  @Test
  fun `an empty id or an id that matches no charge fails`() = runTest {
    registerMember(
      pastCharges = { listOf(charge(null, OctopusChargeStatus.SUCCESS, LocalDate(2026, 1, 27))) },
      futureCharge = { charge("upcoming", OctopusChargeStatus.UPCOMING, LocalDate(2026, 4, 27)) },
    )

    assertThat(useCase().invoke("")).isInstanceOf<Either.Left<ErrorMessage>>()
    assertThat(useCase().invoke("unknown")).isInstanceOf<Either.Left<ErrorMessage>>()
  }

  @Test
  fun `a network failure fails`() = runTest {
    apolloClient.registerTestNetworkError(PaymentHistoryWithDetailsQuery())

    assertThat(useCase().invoke("charge-jan")).isInstanceOf<Either.Left<ErrorMessage>>()
  }

  private fun useCase() = GetChargeDetailsUseCaseImpl(apolloClient)

  private fun registerMember(
    pastCharges: DataBuilderScope.() -> List<MemberChargeMap> = { emptyList() },
    futureCharge: DataBuilderScope.() -> MemberChargeMap? = { null },
    ongoingCharges: DataBuilderScope.() -> List<MemberChargeMap> = { emptyList() },
    payinMethods: DataBuilderScope.() -> List<MemberPaymentMethodMap> = { emptyList() },
  ) {
    apolloClient.registerTestResponse(
      PaymentHistoryWithDetailsQuery(),
      PaymentHistoryWithDetailsQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          this.pastCharges = pastCharges()
          this.futureCharge = futureCharge()
          this.ongoingCharges = ongoingCharges()
          referralInformation = buildMemberReferralInformation { code = "FRIEND" }
          paymentMethods = buildMemberPaymentMethods { this.payinMethods = payinMethods() }
        }
      },
    )
  }

  private fun DataBuilderScope.failedFebruaryCharge() = charge(
    "charge-feb",
    OctopusChargeStatus.FAILED,
    LocalDate(2026, 2, 27),
    provider = "trustly",
  ) {
    gross = money(500.0)
    net = money(449.5)
    chargeBreakdown = listOf(
      buildMemberChargeBreakdownItem {
        displayTitle = "Home insurance"
        displaySubtitle = "Street 1"
        gross = money(500.0)
        net = money(449.5)
        periods = listOf(
          buildMemberChargeBreakdownItemPeriod {
            fromDate = LocalDate(2026, 1, 1)
            toDate = LocalDate(2026, 1, 31)
            amount = money(300.0)
            isPreviouslyFailedCharge = true
          },
          buildMemberChargeBreakdownItemPeriod {
            fromDate = LocalDate(2026, 2, 1)
            toDate = LocalDate(2026, 2, 16)
            amount = money(149.5)
            isPreviouslyFailedCharge = false
          },
        )
        insurancePriceBreakdown = listOf(
          buildMemberChargePriceBreakdownItem {
            displayTitle = "Base price"
            amount = money(500.0)
          },
          buildMemberChargePriceBreakdownItem {
            displayTitle = "Bundle discount"
            amount = money(-50.5)
          },
        )
      },
      buildMemberChargeBreakdownItem {
        displayTitle = "Car insurance"
        displaySubtitle = null
        gross = money(0.0)
        net = money(0.0)
        periods = emptyList()
        insurancePriceBreakdown = emptyList()
      },
    )
    referralDiscount = money(10.0)
    carriedAdjustment = money(12.25)
    settlementAdjustment = money(3.0)
  }

  private fun DataBuilderScope.charge(
    id: String?,
    status: OctopusChargeStatus,
    date: LocalDate,
    provider: String? = "swish",
    block: MemberChargeBuilder.() -> Unit = {},
  ) = buildMemberCharge {
    this.id = id
    this.status = status
    this.date = date
    gross = money(100.0)
    net = money(100.0)
    paymentProvider = provider
    chargeBreakdown = emptyList()
    referralDiscount = null
    carriedAdjustment = null
    settlementAdjustment = null
    block()
  }

  private fun DataBuilderScope.money(amount: Double) = buildMoney {
    this.amount = amount
    currencyCode = CurrencyCode.SEK
  }

  private fun DataBuilderScope.swishMethod(status: MemberPaymentMethodStatus) = buildMemberPaymentMethod {
    provider = MemberPaymentProvider.SWISH
    this.status = status
    isDefault = true
    details = buildPaymentMethodSwishDetails { phoneNumber = "0701234567" }
  }

  private fun DataBuilderScope.trustlyMethod() = buildMemberPaymentMethod {
    provider = MemberPaymentProvider.TRUSTLY
    status = MemberPaymentMethodStatus.ACTIVE
    isDefault = true
    details = buildPaymentMethodBankAccountDetails {
      account = "8327-91234124"
      bank = "Swedbank"
    }
  }
}

private fun sek(amount: Double) = UiMoney(amount, UiCurrencyCode.SEK)

private fun expectedCharge(
  id: String?,
  status: MemberChargeStatus,
  dueDate: LocalDate,
  method: MemberPaymentChargeMethod = MemberPaymentChargeMethod.SWISH,
) = MemberCharge(
  grossAmount = sek(100.0),
  netAmount = sek(100.0),
  id = id,
  status = status,
  dueDate = dueDate,
  failedCharge = null,
  chargeBreakdowns = emptyList(),
  referralDiscount = null,
  carriedAdjustment = null,
  settlementAdjustment = null,
  chargeMethod = method,
)

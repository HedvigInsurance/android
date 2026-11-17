package com.hedvig.android.feature.payments.ios

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.InvoiceDelivery
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.MemberPaymentChargeMethod
import com.hedvig.android.feature.payments.data.PaymentDetails
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsUiState
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class PaymentDetailsScreenStateTest {
  @Test
  fun `Loading maps to Loading`() {
    assertThat(PaymentDetailsUiState.Loading.toScreenState()).isEqualTo(PaymentDetailsScreenState.Loading)
  }

  @Test
  fun `Failure maps to Failed`() {
    assertThat(PaymentDetailsUiState.Failure.toScreenState()).isEqualTo(PaymentDetailsScreenState.Failed)
  }

  @Test
  fun `a success maps every field the details screen shows`() {
    val failed = charge(
      id = "charge-1",
      status = MemberCharge.MemberChargeStatus.FAILED,
      dueDate = LocalDate(2026, 9, 27),
      gross = 500.0,
      net = 449.5,
      chargeBreakdowns = listOf(
        MemberCharge.ChargeBreakdown(
          contractDisplayName = "Home insurance",
          contractDetails = "Street 1",
          grossAmount = sek(500.0),
          netAmount = sek(449.5),
          periods = listOf(
            MemberCharge.ChargeBreakdown.Period(
              amount = sek(300.0),
              fromDate = LocalDate(2026, 8, 1),
              toDate = LocalDate(2026, 8, 31),
              isPreviouslyFailedCharge = true,
            ),
            MemberCharge.ChargeBreakdown.Period(
              amount = sek(149.5),
              fromDate = LocalDate(2026, 9, 1),
              toDate = LocalDate(2026, 9, 16),
              isPreviouslyFailedCharge = false,
            ),
          ),
          priceBreakdown = listOf("Base price" to sek(500.0), "Bundle discount" to sek(-50.5)),
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
    val next = charge(id = "charge-2", dueDate = LocalDate(2026, 10, 27))
    val details = PaymentDetails(
      memberCharge = failed,
      pastCharges = listOf(failed, next),
      paymentsInfo = PaymentDetails.PaymentsInfo.Active(
        PayinAccount.Trustly("8327", "91234124", "Swedbank", isPending = false, isDefault = true),
      ),
      upComingCharge = charge(id = "upcoming", dueDate = LocalDate(2026, 11, 27)),
    )

    assertThat(PaymentDetailsUiState.Success(details).toScreenState()).isEqualTo(
      PaymentDetailsScreenState.Content(
        id = "charge-1",
        status = PaymentChargeStatus.FAILED,
        dueDate = "2026-09-27",
        grossAmount = PaymentMoney(500.0, "SEK"),
        netAmount = PaymentMoney(449.5, "SEK"),
        chargeBreakdowns = listOf(
          PaymentChargeBreakdown(
            contractDisplayName = "Home insurance",
            contractDetails = "Street 1",
            grossAmount = PaymentMoney(500.0, "SEK"),
            netAmount = PaymentMoney(449.5, "SEK"),
            periods = listOf(
              PaymentChargePeriod(
                amount = PaymentMoney(300.0, "SEK"),
                fromDate = "2026-08-01",
                toDate = "2026-08-31",
                isPreviouslyFailedCharge = true,
                periodDescription = PaymentPeriodDescription.FullPeriod,
              ),
              PaymentChargePeriod(
                amount = PaymentMoney(149.5, "SEK"),
                fromDate = "2026-09-01",
                toDate = "2026-09-16",
                isPreviouslyFailedCharge = false,
                periodDescription = PaymentPeriodDescription.BetweenDays(15),
              ),
            ),
            priceBreakdown = listOf(
              PaymentPriceBreakdownItem("Base price", PaymentMoney(500.0, "SEK")),
              PaymentPriceBreakdownItem("Bundle discount", PaymentMoney(-50.5, "SEK")),
            ),
          ),
        ),
        carriedAdjustment = PaymentMoney(12.25, "SEK"),
        settlementAdjustment = PaymentMoney(3.0, "SEK"),
        referralDiscount = PaymentReferralDiscount("FRIEND", PaymentMoney(-10.0, "SEK")),
        chargeMethod = PaymentChargeMethod.TRUSTLY,
        payinAccount = PaymentPayinAccount.Trustly("8327", "91234124", "Swedbank"),
        nextChargeId = "charge-2",
        nextChargeDueDate = "2026-10-27",
      ),
    )
  }

  @Test
  fun `adjustments of zero or less and a missing referral are null`() {
    val content = contentFor(
      charge(carriedAdjustment = sek(0.0), settlementAdjustment = sek(-4.0), referralDiscount = null),
    )

    assertThat(content.carriedAdjustment).isNull()
    assertThat(content.settlementAdjustment).isNull()
    assertThat(content.referralDiscount).isNull()
  }

  @Test
  fun `a referral discount without an amount keeps its code`() {
    val referral = Discount("FRIEND", null, Discount.DiscountStatus.ACTIVE, null, amount = null, isReferral = true)

    val content = contentFor(charge(referralDiscount = referral))

    assertThat(content.referralDiscount).isEqualTo(PaymentReferralDiscount("FRIEND", null))
  }

  @Test
  fun `each charge method maps to its own entry`() {
    val mapped = MemberPaymentChargeMethod.entries.map { contentFor(charge(chargeMethod = it)).chargeMethod }

    assertThat(mapped).containsExactly(
      PaymentChargeMethod.TRUSTLY,
      PaymentChargeMethod.SWISH,
      PaymentChargeMethod.INVOICE,
      PaymentChargeMethod.UNKNOWN,
    )
  }

  @Test
  fun `no presentable payment info is a null payin account`() {
    val content = contentFor(charge(), paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo)

    assertThat(content.payinAccount).isNull()
  }

  @Test
  fun `each payin account kind keeps its own fields`() {
    val accounts = listOf(
      PayinAccount.Trustly(null, null, null, isPending = false, isDefault = true),
      PayinAccount.SwishPayin("0701234567", isPending = false, isDefault = true),
      PayinAccount.SwishPayin(null, isPending = false, isDefault = true),
      PayinAccount.Invoice(InvoiceDelivery.Kivra, null, isPending = false, isDefault = true),
      PayinAccount.Invoice(InvoiceDelivery.Mail, "member@example.test", isPending = false, isDefault = true),
      PayinAccount.Invoice(null, null, isPending = false, isDefault = true),
    )

    val mapped = accounts.map {
      contentFor(charge(), paymentsInfo = PaymentDetails.PaymentsInfo.Active(it)).payinAccount
    }

    assertThat(mapped).containsExactly(
      PaymentPayinAccount.Trustly(null, null, null),
      PaymentPayinAccount.Swish("0701234567"),
      PaymentPayinAccount.Swish(null),
      PaymentPayinAccount.Invoice(PaymentInvoiceDelivery.KIVRA, null),
      PaymentPayinAccount.Invoice(PaymentInvoiceDelivery.MAIL, "member@example.test"),
      PaymentPayinAccount.Invoice(null, null),
    )
  }

  @Test
  fun `the last past charge points at the upcoming charge`() {
    val last = charge(
      id = "charge-2",
      status = MemberCharge.MemberChargeStatus.FAILED,
      dueDate = LocalDate(2026, 10, 27),
    )
    val details = PaymentDetails(
      memberCharge = last,
      pastCharges = listOf(charge(id = "charge-1"), last),
      paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
      upComingCharge = charge(id = "upcoming", dueDate = LocalDate(2026, 11, 27)),
    )

    val content = PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content

    assertThat(content.nextChargeId).isEqualTo("upcoming")
    assertThat(content.nextChargeDueDate).isEqualTo("2026-11-27")
  }

  @Test
  fun `without a later or upcoming charge there is no next charge`() {
    val last = charge(id = "charge-2", status = MemberCharge.MemberChargeStatus.FAILED)
    val details = PaymentDetails(
      memberCharge = last,
      pastCharges = listOf(charge(id = "charge-1"), last),
      paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
      upComingCharge = null,
    )

    val content = PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content

    assertThat(content.nextChargeId).isNull()
    assertThat(content.nextChargeDueDate).isNull()
  }

  @Test
  fun `a next charge without an id keeps its due date`() {
    val first = charge(id = "charge-1", status = MemberCharge.MemberChargeStatus.FAILED)
    val details = PaymentDetails(
      memberCharge = first,
      pastCharges = listOf(first, charge(id = null, dueDate = LocalDate(2026, 3, 27))),
      paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
      upComingCharge = null,
    )

    val content = PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content

    assertThat(content.nextChargeId).isNull()
    assertThat(content.nextChargeDueDate).isEqualTo("2026-03-27")
  }

  @Test
  fun `a past charge that did not fail has no next charge`() {
    val first = charge(id = "charge-1", status = MemberCharge.MemberChargeStatus.SUCCESS)
    val details = PaymentDetails(
      memberCharge = first,
      pastCharges = listOf(first, charge(id = "charge-2")),
      paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
      upComingCharge = charge(id = "upcoming"),
    )

    val content = PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content

    assertThat(content.nextChargeId).isNull()
    assertThat(content.nextChargeDueDate).isNull()
  }

  @Test
  fun `an upcoming charge has no next charge though past charges exist`() {
    val upcoming = charge(id = "upcoming", status = MemberCharge.MemberChargeStatus.UPCOMING)
    val details = PaymentDetails(
      memberCharge = upcoming,
      pastCharges = listOf(charge(id = "charge-1"), charge(id = "charge-2")),
      paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
      upComingCharge = upcoming,
    )

    val content = PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content

    assertThat(content.nextChargeId).isNull()
    assertThat(content.nextChargeDueDate).isNull()
  }

  @Test
  fun `an upcoming charge without past charges does not point at itself`() {
    val upcoming = charge(id = "upcoming", status = MemberCharge.MemberChargeStatus.UPCOMING)
    val details = PaymentDetails(
      memberCharge = upcoming,
      pastCharges = emptyList(),
      paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
      upComingCharge = upcoming,
    )

    val content = PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content

    assertThat(content.nextChargeId).isNull()
    assertThat(content.nextChargeDueDate).isNull()
  }

  @Test
  fun `an ongoing pending charge has no next charge`() {
    val ongoing = charge(id = "ongoing", status = MemberCharge.MemberChargeStatus.PENDING)
    val details = PaymentDetails(
      memberCharge = ongoing,
      pastCharges = listOf(charge(id = "charge-1")),
      paymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
      upComingCharge = charge(id = "upcoming"),
    )

    val content = PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content

    assertThat(content.nextChargeId).isNull()
    assertThat(content.nextChargeDueDate).isNull()
  }

  @Test
  fun `an empty or null charge id is null`() {
    assertThat(contentFor(charge(id = "")).id).isNull()
    assertThat(contentFor(charge(id = null)).id).isNull()
  }

  @Test
  fun `handle calls the closure of each state`() {
    val states = listOf(
      PaymentDetailsScreenState.Loading,
      PaymentDetailsScreenState.Failed,
      contentFor(charge(id = "charge-1")),
    )

    val handled = states.map { state ->
      val calls = mutableListOf<String>()
      state.handle(
        onLoading = { calls += "loading" },
        onFailed = { calls += "failed" },
        onContent = { calls += "content:${it.id}" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("loading"), listOf("failed"), listOf("content:charge-1"))
  }

  @Test
  fun `handle calls the closure of each charge method`() {
    val handled = PaymentChargeMethod.entries.map { method ->
      val calls = mutableListOf<String>()
      method.handle(
        onTrustly = { calls += "trustly" },
        onSwish = { calls += "swish" },
        onInvoice = { calls += "invoice" },
        onUnknown = { calls += "unknown" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("trustly"), listOf("swish"), listOf("invoice"), listOf("unknown"))
  }

  @Test
  fun `a breakdown's and a referral's text leave out the address and the code`() {
    val breakdown = PaymentChargeBreakdown(
      contractDisplayName = "Home insurance",
      contractDetails = "Street 1",
      grossAmount = PaymentMoney(500.0, "SEK"),
      netAmount = PaymentMoney(449.5, "SEK"),
      periods = emptyList(),
      priceBreakdown = emptyList(),
    )
    val referral = PaymentReferralDiscount("FRIEND", PaymentMoney(-10.0, "SEK"))

    assertThat(breakdown.toString()).isEqualTo(
      "PaymentChargeBreakdown(contractDisplayName=Home insurance, grossAmount=PaymentMoney(amount=500.0, " +
        "currencyCode=SEK), netAmount=PaymentMoney(amount=449.5, currencyCode=SEK), periods=[], priceBreakdown=[])",
    )
    assertThat(referral.toString()).isEqualTo(
      "PaymentReferralDiscount(amount=PaymentMoney(amount=-10.0, currencyCode=SEK))",
    )
  }

  @Test
  fun `a payin account's text leaves out the member's details`() {
    val texts = listOf(
      PaymentPayinAccount.Trustly("8327", "91234124", "Swedbank"),
      PaymentPayinAccount.Swish("0701234567"),
      PaymentPayinAccount.Invoice(PaymentInvoiceDelivery.MAIL, "member@example.test"),
    ).map { it.toString() }

    assertThat(texts).containsExactly("Trustly(bankName=Swedbank)", "Swish", "Invoice(delivery=MAIL)")
  }

  @Test
  fun `handle calls the closure of each payin account`() {
    val accounts = listOf(
      PaymentPayinAccount.Trustly("1", "2", "Bank"),
      PaymentPayinAccount.Swish("070"),
      PaymentPayinAccount.Invoice(PaymentInvoiceDelivery.KIVRA, null),
    )

    val handled = accounts.map { account ->
      val calls = mutableListOf<String>()
      account.handle(
        onTrustly = { calls += "trustly:${it.bankName}" },
        onSwish = { calls += "swish:${it.phoneNumber}" },
        onInvoice = { calls += "invoice:${it.delivery}" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("trustly:Bank"), listOf("swish:070"), listOf("invoice:KIVRA"))
  }

  @Test
  fun `handle calls the closure of each invoice delivery`() {
    val handled = PaymentInvoiceDelivery.entries.map { delivery ->
      val calls = mutableListOf<String>()
      delivery.handle(onKivra = { calls += "kivra" }, onMail = { calls += "mail" })
      calls
    }

    assertThat(handled).containsExactly(listOf("kivra"), listOf("mail"))
  }

  @Test
  fun `handle calls the closure of each period description`() {
    val descriptions = listOf(PaymentPeriodDescription.FullPeriod, PaymentPeriodDescription.BetweenDays(15))

    val handled = descriptions.map { description ->
      val calls = mutableListOf<String>()
      description.handle(
        onFullPeriod = { calls += "full" },
        onBetweenDays = { calls += "between:${it.daysBetween}" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("full"), listOf("between:15"))
  }
}

private fun contentFor(
  memberCharge: MemberCharge,
  paymentsInfo: PaymentDetails.PaymentsInfo = PaymentDetails.PaymentsInfo.NoPresentableInfo,
): PaymentDetailsScreenState.Content {
  val details = PaymentDetails(
    memberCharge = memberCharge,
    pastCharges = listOf(memberCharge),
    paymentsInfo = paymentsInfo,
    upComingCharge = null,
  )
  return PaymentDetailsUiState.Success(details).toScreenState() as PaymentDetailsScreenState.Content
}

private fun sek(amount: Double) = UiMoney(amount, UiCurrencyCode.SEK)

private fun charge(
  id: String? = "charge-1",
  status: MemberCharge.MemberChargeStatus = MemberCharge.MemberChargeStatus.SUCCESS,
  dueDate: LocalDate = LocalDate(2026, 1, 27),
  gross: Double = 100.0,
  net: Double = 100.0,
  chargeBreakdowns: List<MemberCharge.ChargeBreakdown> = emptyList(),
  referralDiscount: Discount? = null,
  carriedAdjustment: UiMoney? = null,
  settlementAdjustment: UiMoney? = null,
  chargeMethod: MemberPaymentChargeMethod = MemberPaymentChargeMethod.SWISH,
) = MemberCharge(
  grossAmount = sek(gross),
  netAmount = sek(net),
  id = id,
  status = status,
  dueDate = dueDate,
  failedCharge = null,
  chargeBreakdowns = chargeBreakdowns,
  referralDiscount = referralDiscount,
  carriedAdjustment = carriedAdjustment,
  settlementAdjustment = settlementAdjustment,
  chargeMethod = chargeMethod,
)

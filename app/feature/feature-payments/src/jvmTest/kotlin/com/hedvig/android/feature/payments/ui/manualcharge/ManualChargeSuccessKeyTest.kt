package com.hedvig.android.feature.payments.ui.manualcharge

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.navigation.ManualChargeSuccessKey
import kotlinx.datetime.LocalDate
import org.junit.Test

class ManualChargeSuccessKeyTest {
  private val trustly = PayinAccount.Trustly("8327", "1234567", "Swedbank", isPending = false, isDefault = true)
  private val info = ManualChargeInfo(
    chargeId = "missedChargeId",
    missedDueDate = LocalDate(2026, 9, 27),
    amountDue = UiMoney(399.0, UiCurrencyCode.SEK),
    currentMethods = listOf(trustly),
    primaryPayinMethod = trustly,
    showCancellationWarning = true,
  )

  @Test
  fun `the success key carries the cancellation warning and the primary method's provider`() {
    assertThat(info.toManualChargeSuccessKey()).isEqualTo(
      ManualChargeSuccessKey(showCancellationWarning = true, primaryPayinProvider = PaymentProvider.Trustly),
    )
  }

  @Test
  fun `without a primary method the success key has no provider`() {
    val noPrimary = info.copy(primaryPayinMethod = null, showCancellationWarning = false)
    assertThat(noPrimary.toManualChargeSuccessKey()).isEqualTo(
      ManualChargeSuccessKey(showCancellationWarning = false, primaryPayinProvider = null),
    )
  }
}

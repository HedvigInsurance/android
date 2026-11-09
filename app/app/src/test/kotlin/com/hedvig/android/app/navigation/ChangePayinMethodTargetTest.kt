package com.hedvig.android.app.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.payin.account.navigation.PayinAccountKey
import com.hedvig.android.feature.payin.account.navigation.SetupSwishPayinKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable
import org.junit.Test

internal class ChangePayinMethodTargetTest {
  @Serializable
  private data object FakeOnboardingPayinKey : HedvigNavKey

  @Test
  fun `overview below the flow is popped back onto`() {
    val stack = listOf(HomeKey, PaymentsKey, PayinAccountKey, TrustlyKey())

    val target = changePayinMethodTarget(stack)

    assertThat(target).isEqualTo(ChangePayinMethodTarget.PopTo(2))
  }

  @Test
  fun `overview reached through another tab is still popped back onto`() {
    val stack = listOf(HomeKey, ProfileKey, PayinAccountKey, TrustlyKey())

    val target = changePayinMethodTarget(stack)

    assertThat(target).isEqualTo(ChangePayinMethodTarget.PopTo(2))
  }

  @Test
  fun `swish setup over the overview is popped back onto it`() {
    val stack = listOf(HomeKey, PaymentsKey, PayinAccountKey, SetupSwishPayinKey())

    val target = changePayinMethodTarget(stack)

    assertThat(target).isEqualTo(ChangePayinMethodTarget.PopTo(2))
  }

  @Test
  fun `onboarding keeps a plain pop back to its own payin step`() {
    val stack = listOf(HomeKey, FakeOnboardingPayinKey, TrustlyKey())

    val target = changePayinMethodTarget(stack)

    assertThat(target).isEqualTo(ChangePayinMethodTarget.PopOne)
  }

  @Test
  fun `payout account keeps a plain pop back to its own screen`() {
    val stack = listOf(HomeKey, PaymentsKey, PayoutAccountKey, TrustlyKey())

    val target = changePayinMethodTarget(stack)

    assertThat(target).isEqualTo(ChangePayinMethodTarget.PopOne)
  }
}

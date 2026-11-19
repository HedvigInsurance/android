package com.hedvig.android.feature.payin.account.ui.setupswish

import assertk.assertThat
import assertk.assertions.isFalse
import org.junit.Test

class DesktopSwishAppHandoverTest {
  @Test
  fun `reports Swish as not installed, so the approval screen shows only the QR code`() {
    assertThat(DesktopSwishAppHandover.isSwishInstalled).isFalse()
  }
}

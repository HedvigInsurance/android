package com.hedvig.app.feature.insurance.ui.detail

import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_UPCOMING_AGREEMENT
import org.junit.Test

class YourInfoItemsTest {

  @Test
  fun `when there is no upcoming agreement, should show no upcoming agreement card`() {
    val viewState = INSURANCE_DATA.contracts[0].toMemberDetailsViewState()
    assertThat(viewState.pendingAddressChange).isNull()
  }

  @Test
  fun `when there is an upcoming agreement, should show an upcoming agreement card`() {
    val viewState = INSURANCE_DATA_UPCOMING_AGREEMENT.contracts[0].toMemberDetailsViewState()
    assertThat(viewState.pendingAddressChange).isNotNull()
  }

  @Test
  fun `when termination flow is enabled, it should be part of the items`() {
    val viewState = INSURANCE_DATA_UPCOMING_AGREEMENT.contracts[0].toDocumentsViewState(true)
    assertThat(viewState.cancelInsurance).isNotNull()
  }

  @Test
  fun `when termination flow is disabled, it should not be part of the items`() {
    val viewState = INSURANCE_DATA_UPCOMING_AGREEMENT.contracts[0].toDocumentsViewState(false)
    assertThat(viewState.cancelInsurance).isNull()
  }
}

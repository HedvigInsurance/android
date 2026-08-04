package com.hedvig.android.feature.cross.sell.sheet

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.api.Optional
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import octopus.type.FlowSource
import octopus.type.UserFlow
import org.junit.Test

class CrossSellInfoTypeToCrossSellInputTest {
  @Test
  fun `a flow that changed a contract passes that contract id to the cross sell input`() {
    val input = CrossSellInfoType.ChangeTier("contractId").toCrossSellSource()

    assertThat(input.userFlow).isEqualTo(UserFlow.SMART_X_SELL)
    assertThat(input.flowSource).isEqualTo(Optional.present(FlowSource.CHANGE_TIER))
    assertThat(input.contractId).isEqualTo(Optional.present("contractId"))
  }

  @Test
  fun `a flow without an associated contract leaves the contract id absent in the cross sell input`() {
    val input = CrossSellInfoType.Addon.toCrossSellSource()

    assertThat(input.flowSource).isEqualTo(Optional.present(FlowSource.ADDON))
    assertThat(input.contractId).isEqualTo(Optional.absent())
  }

  @Test
  fun `a moving flow which did not report a new contract id leaves the contract id absent`() {
    val input = CrossSellInfoType.MovingFlow(null).toCrossSellSource()

    assertThat(input.flowSource).isEqualTo(Optional.present(FlowSource.MOVING))
    assertThat(input.contractId).isEqualTo(Optional.absent())
  }

  @Test
  fun `a closed claim flow passes the claim id to the cross sell input`() {
    val input = CrossSellInfoType.ClosedClaim(
      info = CrossSellInfoType.ClosedClaim.ClaimInfo(
        id = "claimId",
        status = null,
        type = null,
        typeOfContract = null,
      ),
      contractId = "contractId",
    ).toCrossSellSource()

    assertThat(input.flowSource).isEqualTo(Optional.present(FlowSource.CLOSED_CLAIM))
    assertThat(input.claimId).isEqualTo(Optional.present("claimId"))
    assertThat(input.contractId).isEqualTo(Optional.present("contractId"))
  }

  @Test
  fun `a flow that is not a closed claim leaves the claim id absent in the cross sell input`() {
    val input = CrossSellInfoType.ChangeTier("contractId").toCrossSellSource()

    assertThat(input.claimId).isEqualTo(Optional.absent())
  }
}

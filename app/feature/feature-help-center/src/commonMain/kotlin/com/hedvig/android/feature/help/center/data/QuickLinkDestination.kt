package com.hedvig.android.feature.help.center.data

import com.hedvig.android.ui.emergency.FirstVetSection

sealed interface QuickLinkDestination {
  sealed interface OuterDestination : QuickLinkDestination {
    data class QuickLinkCoInsuredAddInfo(val contractId: String) : OuterDestination

    data class QuickLinkCoInsuredAddOrRemove(val contractId: String) : OuterDestination

    data object QuickLinkTermination : OuterDestination

    data object QuickLinkTravelCertificate : OuterDestination

    data object QuickLinkChangeAddress : OuterDestination

    data object QuickLinkConnectPayment : OuterDestination

    data object QuickLinkChangeTier : OuterDestination

    data object ChooseInsuranceForEditCoInsured : OuterDestination
  }
}

internal sealed interface InnerHelpCenterDestination : QuickLinkDestination {
  data class QuickLinkSickAbroad(
    val emergencyNumber: String?,
    val emergencyUrl: String?,
    val preferredPartnerImageHeight: Int?,
  ) : InnerHelpCenterDestination

  data class FirstVet(
    val sections: List<FirstVetSection>,
  ) : InnerHelpCenterDestination
}

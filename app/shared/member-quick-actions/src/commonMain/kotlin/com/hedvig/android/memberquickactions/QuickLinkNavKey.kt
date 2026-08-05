package com.hedvig.android.memberquickactions

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowChooseInsuranceKey
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoInsured
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoOwners
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkChangeAddress
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkChangeTier
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddInfo
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddOrRemove
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkConnectPayment
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkTermination
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate
import com.hedvig.android.navigation.common.HedvigNavKey

fun QuickLinkDestination.toNavKey(): HedvigNavKey = when (this) {
  is InnerHelpCenterDestination.FirstVet -> {
    error("InnerHelpCenterDestination is navigated by the consuming feature, not via toNavKey()")
  }

  is InnerHelpCenterDestination.QuickLinkSickAbroad -> {
    error("InnerHelpCenterDestination is navigated by the consuming feature, not via toNavKey()")
  }

  QuickLinkChangeAddress -> {
    SelectContractForMovingKey(MovingSource.OTHER)
  }

  is QuickLinkCoInsuredAddInfo -> {
    CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured)
  }

  is QuickLinkCoInsuredAddOrRemove -> {
    CoInsuredAddOrRemoveKey(contractId, CoInsuredFlowType.CoInsured)
  }

  is QuickLinkCoOwnerAddInfo -> {
    CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoOwners)
  }

  is QuickLinkCoOwnerAddOrRemove -> {
    CoInsuredAddOrRemoveKey(contractId, CoInsuredFlowType.CoOwners)
  }

  QuickLinkConnectPayment -> {
    TrustlyKey
  }

  QuickLinkTermination -> {
    TerminateInsuranceKey(null)
  }

  QuickLinkTravelCertificate -> {
    TravelCertificateKey
  }

  QuickLinkChangeTier -> {
    StartTierFlowChooseInsuranceKey
  }

  ChooseInsuranceForEditCoInsured -> {
    EditCoInsuredTriageKey()
  }

  ChooseInsuranceForEditCoOwners -> {
    EditCoInsuredTriageKey(type = CoInsuredFlowType.CoOwners)
  }
}

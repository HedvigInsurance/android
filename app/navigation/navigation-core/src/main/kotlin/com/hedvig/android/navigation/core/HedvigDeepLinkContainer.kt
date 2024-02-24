package com.hedvig.android.navigation.core

import com.hedvig.android.core.buildconstants.HedvigBuildConstants

interface HedvigDeepLinkContainer {
  val home: String // Home destination, the start destination of the app
  val helpCenter: String // The help center root screen
  val helpCenterCommonTopic: String // A common topic inside the help center
  val helpCenterQuestion: String // A specific question inside the help center

  val insurances: String // The insurances destination, which also shows cross sells

  val forever: String // The forever/referrals destination, showing the existing discount and the unique code

  val profile: String // The profile screen, which acts as a gateway to several app settings
  val connectPayment: String // Screen where the member can connect their payment method to Hedvig to pay for insurance
  val directDebit: String // Same as connectPayment but to support an old link to it
  val eurobonus: String // The destination allowing to edit your current Eurobonus (SAS) number
  val payments: String // The payments screen, showing the payments history and the upcoming payment information
  val deleteAccount: String // The screen where the member may request for their account data to be GDPR wiped

  val chat: String // Hedvig Chat
}

internal class HedvigDeepLinkContainerImpl(
  hedvigBuildConstants: HedvigBuildConstants,
) : HedvigDeepLinkContainer {
  private val baseDeepLinkDomain = "https://${hedvigBuildConstants.deepLinkHost}"

  // Home does not have some special text, acts as the fallback to all unknown deep links
  override val home: String = baseDeepLinkDomain
  override val helpCenter: String = "$baseDeepLinkDomain/help-center"

  // Sample url: https://hedvigdevelop.page.link/help-center/topic?id=1
  override val helpCenterCommonTopic: String = "$baseDeepLinkDomain/help-center/topic&id={id}"

  // Sample url: https://hedvigdevelop.page.link/help-center/question?id=2
  override val helpCenterQuestion: String = "$baseDeepLinkDomain/help-center/question&id={id}"

  override val insurances: String = "$baseDeepLinkDomain/insurances"

  override val forever: String = "$baseDeepLinkDomain/forever"

  override val profile: String = "$baseDeepLinkDomain/profile"
  override val connectPayment: String = "$baseDeepLinkDomain/connect-payment"
  override val directDebit: String = "$baseDeepLinkDomain/direct-debit"
  override val eurobonus: String = "$baseDeepLinkDomain/eurobonus"
  override val payments: String = "$baseDeepLinkDomain/payments"
  override val deleteAccount: String = "$baseDeepLinkDomain/delete-account"

  override val chat: String = "$baseDeepLinkDomain/chat"
}

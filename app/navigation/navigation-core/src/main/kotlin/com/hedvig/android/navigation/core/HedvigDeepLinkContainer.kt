package com.hedvig.android.navigation.core

import com.hedvig.android.core.buildconstants.HedvigBuildConstants

interface HedvigDeepLinkContainer {
  val home: List<String> // Home destination, the start destination of the app
  val helpCenter: List<String> // The help center root screen
  val helpCenterCommonTopic: List<String> // A common topic inside the help center
  val helpCenterQuestion: List<String> // A specific question inside the help center

  val insurances: List<String> // The insurances destination, which also shows cross sells

  /**
   * A link to a contract without an id, which should fall back to the same behavior that [insurances] gives us.
   * This is required so that it takes over the [contract] link in scenarios where the ID is not provided. Otherwise
   * the deep link is still matched but the contractId is not present, which results in a crash.
   */
  val contractWithoutContractId: List<String>

  // A specific contract destination with a contractId. If none match, an empty screen is shown
  val contract: List<String>

  val terminateInsurance: List<String> // The screen with a list of insurances eligible for self-service cancellation

  val forever: List<String> // The forever/referrals destination, showing the existing discount and the unique code

  val profile: List<String> // The profile screen, which acts as a gateway to several app settings

  // Screen where the member can connect their payment method to Hedvig to pay for insurance
  val connectPayment: List<String>
  val directDebit: List<String> // Same as connectPayment but to support an old link to it
  val eurobonus: List<String> // The destination allowing to edit your current Eurobonus (SAS) number
  val payments: List<String> // The payments screen, showing the payments history and the upcoming payment information
  val deleteAccount: List<String> // The screen where the member may request for their account data to be GDPR wiped

  // The screen where one can change their contact information, like their email and phone.
  val contactInfo: List<String>

  val chat: List<String> // Hedvig Chat
  val inbox: List<String> // Hedvig CBM inbox
  val conversation: List<String> // Hedvig specific CBM conversation
}

internal class HedvigDeepLinkContainerImpl(
  hedvigBuildConstants: HedvigBuildConstants,
) : HedvigDeepLinkContainer {
  private val baseDeepLinkDomains = hedvigBuildConstants.deepLinkHosts.map { "https://$it" }

  // Home does not have some special text, acts as the fallback to all unknown deep links
  override val home: List<String> = baseDeepLinkDomains
  override val helpCenter: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/help-center"
  }

  // Sample url: https://hedvigdevelop.page.link/help-center/topic?id=1
  override val helpCenterCommonTopic: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/help-center/topic?id={id}"
  }

  // Sample url: https://hedvigdevelop.page.link/help-center/question?id=2
  override val helpCenterQuestion: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/help-center/question?id={id}"
  }

  override val insurances: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/insurances"
  }
  override val contractWithoutContractId: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/contract"
  }
  override val contract: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/contract?contractId={contractId}"
  }

  override val terminateInsurance: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/terminate-contract?contractId={contractId}"
  }

  override val forever: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain -> "$baseDeepLinkDomain/forever" }

  override val profile: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain -> "$baseDeepLinkDomain/profile" }
  override val connectPayment: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/connect-payment"
  }
  override val directDebit: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/direct-debit"
  }
  override val eurobonus: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/eurobonus"
  }
  override val payments: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain -> "$baseDeepLinkDomain/payments" }
  override val deleteAccount: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/delete-account"
  }
  override val contactInfo: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/contact-info"
  }

  override val chat: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain -> "$baseDeepLinkDomain/chat" }
  override val inbox: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain -> "$baseDeepLinkDomain/inbox" }
  override val conversation: List<String> = baseDeepLinkDomains.map { baseDeepLinkDomain ->
    "$baseDeepLinkDomain/conversation/{conversationId}"
  }
}

val HedvigDeepLinkContainer.allDeepLinkUriPatterns: List<String>
  get() = listOf(
    home.first(),
    helpCenter.first(),
    helpCenterCommonTopic.first(),
    helpCenterQuestion.first(),
    insurances.first(),
    contract.first(),
    contractWithoutContractId.first(),
    terminateInsurance.first(),
    forever.first(),
    profile.first(),
    connectPayment.first(),
    directDebit.first(),
    eurobonus.first(),
    payments.first(),
    deleteAccount.first(),
    contactInfo.first(),
    chat.first(),
    inbox.first(),
    conversation.first(),
  )

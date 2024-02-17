package com.hedvig.android.navigation.core

import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import kotlinx.serialization.descriptors.elementNames

interface HedvigDeepLinkContainer {
  val home: String // Home destination, the start destination of the app
  val insurances: String // The insurances destination, which also shows cross sells
  val forever: String // The forever/referrals destination, showing the existing discount and the unique code
  val profile: String // The profile screen, which acts as a gateway to several app settings
  val eurobonus: String // The destination allowing to edit your current Eurobonus (SAS) number
  val chat: HedvigDeepLinkWithParameters<AppDestination.Chat.ChatContext?> // Hedvig Chat
  val connectPayment: String // Screen where the member can connect their payment method to Hedvig to pay for insurance
  val directDebit: String // Same as connectPayment but to support an old link to it
  val payments: String // The payments screen, showing the payments history and the upcoming payment information
  val helpCenter: String // The help center root screen
  val helpCenterCommonTopic: String // A common topic inside the help center
  val helpCenterQuestion: String // A specific question inside the help center
}

interface HedvigDeepLinkWithParameters<Params> {
  val uriPattern: String
  fun createDeepLinkRoute(params: Params): String
}

internal class HedvigDeepLinkContainerImpl(
  hedvigBuildConstants: HedvigBuildConstants,
) : HedvigDeepLinkContainer {
  private val baseDeepLinkDomain = "https://${hedvigBuildConstants.deepLinkHost}"

  // Home does not have some special text, acts as the fallback to all unknown deep links
  override val home: String = baseDeepLinkDomain
  override val insurances: String = "$baseDeepLinkDomain/insurances"
  override val forever: String = "$baseDeepLinkDomain/forever"
  override val profile: String = "$baseDeepLinkDomain/profile"
  override val eurobonus: String = "$baseDeepLinkDomain/eurobonus"
  override val chat: HedvigDeepLinkWithParameters<AppDestination.Chat.ChatContext?> =
    object : HedvigDeepLinkWithParameters<AppDestination.Chat.ChatContext?> {
      val chatContextParameterName = run {
        val elements = AppDestination.Chat.serializer().descriptor.elementNames.toList()
        val chatContextName = elements.first()
        require(elements.size == 1 && chatContextName == "chatContext") {
          "If the Chat destination object changes, the way the deep link is created needs to be updated along with it"
        }
        elements
      }
      override val uriPattern: String = "$baseDeepLinkDomain/chat?$chatContextParameterName={$chatContextParameterName}"
      override fun createDeepLinkRoute(params: AppDestination.Chat.ChatContext?): String {
        val chatContext = params
        if (chatContext == null) {
          return "$baseDeepLinkDomain/chat"
        } else {
          val enumIndex = chatContext.ordinal
          return "$baseDeepLinkDomain/chat?$chatContextParameterName=$enumIndex\""
        }
      }
    }
  override val connectPayment: String = "$baseDeepLinkDomain/connect-payment"
  override val directDebit: String = "$baseDeepLinkDomain/direct-debit"
  override val payments: String = "$baseDeepLinkDomain/payments"
  override val helpCenter: String = "$baseDeepLinkDomain/help-center"

  // Sample url: https://hedvigdevelop.page.link/help-center/topic?id=1
  override val helpCenterCommonTopic: String = "$baseDeepLinkDomain/help-center/topic?id={id}"

  // Sample url: https://hedvigdevelop.page.link/help-center/question?id=2
  override val helpCenterQuestion: String = "$baseDeepLinkDomain/help-center/question?id={id}"
}

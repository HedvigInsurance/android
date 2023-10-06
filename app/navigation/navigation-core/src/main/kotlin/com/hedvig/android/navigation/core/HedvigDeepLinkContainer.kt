package com.hedvig.android.navigation.core

interface HedvigDeepLinkContainer {
  val home: String // Home destination, the start destination of the app
  val insurances: String // The insurances destination, which also shows cross sells
  val forever: String // The forever/referrals destination, showing the existing discount and the unique code
  val profile: String // The profile screen, which acts as a gateway to several app settings
  val eurobonus: String // The destination allowing to edit your current Eurobonus (SAS) number
  val chat: String // Hedvig Chat
}

internal class HedvigDeepLinkContainerImpl(
  isProduction: Boolean,
) : HedvigDeepLinkContainer {
  private val baseFirebaseLink = if (isProduction) {
    "https://hedvig.page.link"
  } else {
    "https://hedvigtest.page.link"
  }

  // Home does not have some special text, acts as the fallback to all unknown deep links
  override val home: String = baseFirebaseLink
  override val insurances: String = "$baseFirebaseLink/insurances"
  override val forever: String = "$baseFirebaseLink/forever"
  override val profile: String = "$baseFirebaseLink/profile"
  override val eurobonus: String = "$baseFirebaseLink/eurobonus"
  override val chat: String = "$baseFirebaseLink/chat"
}

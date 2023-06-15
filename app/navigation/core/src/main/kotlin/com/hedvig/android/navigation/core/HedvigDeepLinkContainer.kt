package com.hedvig.android.navigation.core

interface HedvigDeepLinkContainer {
  val home: String // Home screen, the start destination of the app
  val insurances: String // The insurances destination, which also shows cross sells
  val forever: String // The forever screen, showing the existing discount and the unique code
  val eurobonus: String // The destination allowing to edit your current Eurobonus (SAS) number
}

internal class HedvigDeepLinkContainerImpl(
  isProduction: Boolean,
) : HedvigDeepLinkContainer {
  private val baseFirebaseLink = if (isProduction) {
    "https://hedvig.page.link"
  } else {
    "https://hedvigtest.page.link"
  }

  override val home: String = baseFirebaseLink
  override val insurances: String = "$baseFirebaseLink/insurances"
  override val forever: String = "$baseFirebaseLink/forever"
  override val eurobonus: String = "$baseFirebaseLink/eurobonus"
}

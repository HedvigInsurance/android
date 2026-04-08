package com.hedvig.android.shared.partners.deflect

import kotlinx.serialization.Serializable

@Serializable
data class DeflectData(
  val title: String?,
  val infoText: String?,
  val warningText: String?,
  val partnersContainer: DeflectPartnerContainer?,
  val partnersInfo: InfoBlock?,
  val content: InfoBlock,
  val faq: List<InfoBlock>,
  val buttonText: String,
) {

  @Serializable
  sealed interface DeflectPartnerContainer {
    @Serializable
    data class ExtendedPartnerContainer(
      val partners: List<ExtendedPartner>,
    ) : DeflectPartnerContainer

    @Serializable
    data class SimplePartnerContainer(
      val partners: List<SimplePartner>,
    ) : DeflectPartnerContainer

    @Serializable
    data class ExtendedPartner(
      val id: String,
      val imageUrl: String?,
      val phoneNumber: String?,
      val title: String?,
      val description: String?,
      val info: String?,
      val url: String?,
      val urlButtonTitle: String?,
    )

    @Serializable
    data class SimplePartner(
      val url: String?,
      val urlButtonTitle: String?,
    )
  }

  @Serializable
  data class InfoBlock(
    val title: String,
    val description: String,
  )
}

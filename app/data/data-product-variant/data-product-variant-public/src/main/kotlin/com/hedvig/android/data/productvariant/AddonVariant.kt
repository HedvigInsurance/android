package com.hedvig.android.data.productvariant

import kotlinx.serialization.Serializable
import octopus.fragment.AddonVariantFragment

@Serializable
data class AddonVariant(
  val termsVersion: String,
  val displayName: String,
  val product: String,
  val documents: List<InsuranceVariantDocument>,
  val perils: List<ProductVariantPeril>
)

fun AddonVariantFragment.toAddonVariant() = AddonVariant(
  termsVersion = this.termsVersion,
  displayName = this.displayName,
  product = this.product,
  documents = this.documents.map {
    InsuranceVariantDocument(
      displayName = it.displayName,
      url = it.url,
      type = it.type.toDocumentType(),
    )
  },
  perils = this.perils.map { peril ->
    ProductVariantPeril(
      id = peril.id,
      title = peril.title,
      description = peril.description,
      covered = peril.covered,
      exceptions = peril.exceptions,
      colorCode = peril.colorCode,
    )
  }
)

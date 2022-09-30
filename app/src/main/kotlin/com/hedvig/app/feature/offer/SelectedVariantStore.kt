package com.hedvig.app.feature.offer

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Storage for the offer variant from a quote bundle
 */
class SelectedVariantStore {
  val selectedVariantId = MutableStateFlow<String?>(null)

  fun selectVariant(variantId: String) {
    selectedVariantId.value = variantId
  }
}

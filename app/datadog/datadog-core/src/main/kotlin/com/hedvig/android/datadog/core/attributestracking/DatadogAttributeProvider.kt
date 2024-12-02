package com.hedvig.android.datadog.core.attributestracking

import kotlinx.coroutines.flow.Flow

interface DatadogAttributeProvider {
  /**
   * The implementation must return something, even null, immediatelly. To avoid blocking the rest of the attributes
   * from being read in the [combine] which combines all of them together
   */
  fun provide(): Flow<Pair<String, Any?>>
}

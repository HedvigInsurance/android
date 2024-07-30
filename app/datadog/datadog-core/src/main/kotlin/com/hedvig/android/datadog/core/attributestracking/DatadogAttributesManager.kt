package com.hedvig.android.datadog.core.attributestracking

import com.datadog.android.Datadog
import com.datadog.android.rum.GlobalRumMonitor
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

interface DatadogMemberIdProvider {
  /**
   * The implementation must return something, even null, immediatelly. To avoid blocking the rest of the attributes
   * from being read in the [combine] which combines all of them together
   */
  fun provide(): Flow<Pair<String, String?>>
}

interface DatadogAttributeProvider {
  /**
   * The implementation must return something, even null, immediatelly. To avoid blocking the rest of the attributes
   * from being read in the [combine] which combines all of them together
   */
  fun provide(): Flow<Pair<String, Any?>>
}

internal class DatadogAttributesManager(
  private val applicationScope: ApplicationScope,
  private val memberIdProvider: DatadogMemberIdProvider,
  private val attributeProviders: Set<DatadogAttributeProvider>,
) : Initializable {
  override fun initialize() {
    val sdkCore = Datadog.getInstance()
    val rumMonitor = GlobalRumMonitor.get(sdkCore)
    applicationScope.launch {
      combine(
        memberIdProvider.provide(),
        combine(attributeProviders.map { it.provide() }) { it.toMap() },
      ) { memberId: Pair<String, String?>, attributes: Map<String, Any?> ->
        memberId to attributes
      }.collect { (memberIdAttribute, attributes): Pair<Pair<String, String?>, Map<String, Any?>> ->
        logcat(LogPriority.VERBOSE) { "DatadogAttributesManager storing: $memberIdAttribute, $attributes" }
        sdkCore.setUserInfo(id = memberIdAttribute.second, name = null, email = null, extraInfo = attributes)
        rumMonitor.addAttribute(memberIdAttribute.first, memberIdAttribute.second)
        for (attribute in attributes) {
          rumMonitor.addAttribute(attribute.key, attribute.value)
        }
      }
    }
  }
}

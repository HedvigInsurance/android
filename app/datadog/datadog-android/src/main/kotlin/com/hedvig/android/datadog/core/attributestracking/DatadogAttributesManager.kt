package com.hedvig.android.datadog.core.attributestracking

import com.datadog.android.Datadog
import com.datadog.android.rum.GlobalRumMonitor
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
      ) { memberIdInfo, attributes ->
        logcat(LogPriority.VERBOSE) { "DatadogAttributesManager storing: $memberIdInfo, $attributes" }
        val memberId = memberIdInfo.memberId
        if (memberId != null) {
          sdkCore.setUserInfo(id = memberId, name = null, email = null, extraInfo = attributes)
        } else {
          sdkCore.setUserInfo(id = LoggedOutMemberId, name = null, email = null, extraInfo = attributes)
        }
        rumMonitor.addAttribute(memberIdInfo.trackingKey, memberIdInfo.memberId)
        for (attribute in attributes) {
          rumMonitor.addAttribute(attribute.key, attribute.value)
        }
      }.collect()
    }
  }
}

/**
 * When we do not possess a member ID, we still want to maintain the list of `extraInfo` instead of simply calling
 * [com.datadog.android.api.SdkCore.clearUserInfo] which would make us lose this piece of valuable debugging
 * information
 */
private const val LoggedOutMemberId: String = "-1"

package com.hedvig.android.datadog.core.memberid

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class DatadogMemberIdUpdater(
  private val datadogAttributesManager: DatadogAttributesManager,
  private val memberIdService: MemberIdService,
  private val applicationScope: ApplicationScope,
) : Initializable {
  override fun initialize() {
    applicationScope.launch {
      memberIdService.getMemberId().collectLatest { memberId ->
        if (memberId == null) {
          logcat(LogPriority.INFO) { "Removing from global RUM attribute:$MEMBER_ID_TRACKING_KEY" }
          datadogAttributesManager.deleteAttribute(MEMBER_ID_TRACKING_KEY)
        } else {
          logcat(LogPriority.INFO) { "Appending to global RUM attribute:$MEMBER_ID_TRACKING_KEY = $memberId" }
          datadogAttributesManager.storeAttribute(MEMBER_ID_TRACKING_KEY, memberId)
        }
      }
    }
  }
}

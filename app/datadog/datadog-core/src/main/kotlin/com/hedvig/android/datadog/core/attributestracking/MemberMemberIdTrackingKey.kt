package com.hedvig.android.datadog.core.attributestracking

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class DatadogMemberIdProviderImpl(
  private val memberIdService: MemberIdService,
) : DatadogMemberIdProvider {
  override fun provide(): Flow<Pair<String, String?>> {
    return memberIdService
      .getMemberId()
      .map { MEMBER_ID_TRACKING_KEY to it }
      .onEach { (key, memberId) ->
        logcat(LogPriority.INFO) {
          if (memberId == null) {
            "Removing from global RUM attribute:$MEMBER_ID_TRACKING_KEY"
          } else {
            "Appending to global RUM attribute:$MEMBER_ID_TRACKING_KEY = $memberId"
          }
        }
      }
  }

  companion object {
    /**
     * Key to be passed to [com.hedvig.android.datadog.core.attributestracking.DatadogAttributesManager] to track the
     * member ID after we've logged into the app
     */
    private const val MEMBER_ID_TRACKING_KEY = "member_id"
  }
}

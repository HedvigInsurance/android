package com.hedvig.android.logger

import kotlin.concurrent.Volatile

/**
 * Logs leave the device through Crashlytics and Datadog, so member data, tokens and signed URLs may be logged only in
 * builds that no production member uses. Off until the host turns it on.
 */
object MemberDataLogging {
  @Volatile
  var isAllowed: Boolean = false
}

/**
 * [value] as text for a log message, or [replacement] where [MemberDataLogging] is not allowed. Wrap anything that can
 * hold member data, a token or a response body.
 */
fun memberData(value: Any?, replacement: String = "<redacted>"): String {
  return if (MemberDataLogging.isAllowed) value.toString() else replacement
}

/**
 * [url] as text for a log message. Where [MemberDataLogging] is not allowed the query and fragment are dropped, since
 * that is where signatures and tokens travel.
 */
fun urlForLog(url: Any?): String {
  return memberData(url, replacement = url.toString().substringBefore('?').substringBefore('#'))
}

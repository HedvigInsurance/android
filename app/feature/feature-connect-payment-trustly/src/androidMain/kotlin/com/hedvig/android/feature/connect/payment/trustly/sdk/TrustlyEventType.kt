package com.hedvig.android.feature.connect.payment.trustly.sdk

enum class TrustlyEventType(
  val eventTypeLabel: String,
) {
  SUCCESS("onTrustlyCheckoutSuccess"),
  REDIRECT("onTrustlyCheckoutRedirect"),
  ABORT("onTrustlyCheckoutAbort"),
  ERROR("onTrustlyCheckoutError"),
  ;

  companion object {
    fun valueForEventTypeLabel(eventName: String?): TrustlyEventType? {
      if (eventName == null) return null
      for (type in TrustlyEventType.entries) {
        if (type.eventTypeLabel == eventName) {
          return type
        }
      }
      return null
    }
  }
}

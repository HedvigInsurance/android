package com.hedvig.android.app.notification.senders

// region From customerIO https://www.customer.io/docs/send-push/#standard-payload
internal fun Map<String, String>.titleFromCustomerIoData(): String? {
  return get("title")
}

internal fun Map<String, String>.bodyFromCustomerIoData(): String? {
  return get("body")
}
// endregion

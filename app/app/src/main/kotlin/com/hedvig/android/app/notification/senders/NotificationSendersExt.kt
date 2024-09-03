package com.hedvig.android.app.notification.senders

// Used to refer to the MainActivity class fully qualified name when trying to make a notification deep link to
// specific screen of it
internal const val MainActivityFullyQualifiedName = "com.hedvig.android.app.MainActivity"

// region From customerIO https://www.customer.io/docs/send-push/#standard-payload
internal fun Map<String, String>.titleFromCustomerIoData(): String? {
  return get("title")
}

internal fun Map<String, String>.bodyFromCustomerIoData(): String? {
  return get("body")
}
// endregion

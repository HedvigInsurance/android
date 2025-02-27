package com.hedvig.android.app.notification.senders

import com.google.firebase.messaging.RemoteMessage

// region From customerIO https://www.customer.io/docs/send-push/#standard-payload
internal fun RemoteMessage?.titleFromCustomerIoData(): String? {
  return this?.data?.get("title")
}

internal fun RemoteMessage.bodyFromCustomerIoData(): String? {
  return this?.data?.get("body")
}
// endregion

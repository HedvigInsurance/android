package com.hedvig.android.datadog.core.network

/**
 * RUM attribute set on a network error when the request was cancelled rather than failed, for example because the
 * screen that started it was left. The RUM error event mapper drops errors carrying it, since abandoning such a request
 * is intended behavior and not an error.
 */
const val REQUEST_CANCELLED_RUM_ATTRIBUTE = "hedvig.request_cancelled"

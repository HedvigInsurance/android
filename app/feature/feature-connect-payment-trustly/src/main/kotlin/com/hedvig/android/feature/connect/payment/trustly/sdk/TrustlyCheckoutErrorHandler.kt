package com.hedvig.android.feature.connect.payment.trustly.sdk

/**
 * Invoked when the TrustlyWebView has encountered an error
 *
 * @implNote The webview will not autoclose and you can use this lambda to dismiss the web view yourself.
 */
fun interface TrustlyCheckoutErrorHandler {
  fun onTrustlyCheckoutError()
}

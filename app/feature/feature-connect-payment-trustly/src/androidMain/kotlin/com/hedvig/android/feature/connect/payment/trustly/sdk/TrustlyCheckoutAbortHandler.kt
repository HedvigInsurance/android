package com.hedvig.android.feature.connect.payment.trustly.sdk

/**
 * Invoked when the TrustlyWebView order was aborted by the end user
 *
 * @implNote The webview will not autoclose and you can use this lambda to dismiss the web view yourself.
 */
fun interface TrustlyCheckoutAbortHandler {
  fun onTrustlyCheckoutAbort()
}

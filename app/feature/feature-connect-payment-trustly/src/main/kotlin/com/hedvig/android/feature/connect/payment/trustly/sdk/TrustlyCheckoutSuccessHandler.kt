package com.hedvig.android.feature.connect.payment.trustly.sdk

/**
 * Invoked when the TrustlyWebView has successfully completed an order.
 *
 * @implNote The webview will not autoclose and you can use this lambda to dismiss the web view yourself.
 */
fun interface TrustlyCheckoutSuccessHandler {
  fun onTrustlyCheckoutSuccess()
}

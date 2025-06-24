package com.hedvig.android.feature.connect.payment.trustly.data

import com.hedvig.android.core.buildconstants.HedvigBuildConstants

/**
 * Trustly asks for two urls to redirect to when they are done processing the payment connection.
 * We need to pass these when we start the process, and we can then listen in on them to know when the process is done
 * and what the outcome of it was.
 */
internal interface TrustlyCallback {
  val successUrl: String
  val failureUrl: String
}

internal class TrustlyCallbackImpl(
  hedvigBuildConstants: HedvigBuildConstants,
) : TrustlyCallback {
  override val successUrl: String = "https://${hedvigBuildConstants.deepLinkHosts.first()}/payment-success"
  override val failureUrl: String = "https://${hedvigBuildConstants.deepLinkHosts.first()}/payment-failure"
}

internal class PreviewTrustlyCallback(override val successUrl: String, override val failureUrl: String) : TrustlyCallback

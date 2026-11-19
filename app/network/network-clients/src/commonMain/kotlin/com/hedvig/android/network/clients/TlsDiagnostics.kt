package com.hedvig.android.network.clients

const val TLS_DIAG_TAG = "TlsDiag"

/**
 * Evidence for why a TLS handshake was refused, gathered at the point of failure.
 *
 * A trust failure means a certificate did arrive and we declined it, so the questions worth answering
 * are where the hostname pointed, what the platform makes of the network, and whether this device
 * trusts anything unusual. An address outside our hosting means something other than us answered the
 * name; a network the platform never validated means something on the path answered for us; a
 * non-zero count of added CAs means traffic is being intercepted on the device itself.
 */
interface TlsDiagnostics {
  /** Null when [throwable] is not a certificate-trust failure, so callers can fall through. */
  suspend fun describe(throwable: Throwable?): String?
}

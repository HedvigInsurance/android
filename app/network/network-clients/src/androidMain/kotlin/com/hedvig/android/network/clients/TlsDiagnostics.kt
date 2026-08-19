package com.hedvig.android.network.clients

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.net.InetAddress
import java.net.URI
import java.security.KeyStore
import java.security.cert.CertPathBuilderException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLHandshakeException
import javax.security.auth.x500.X500Principal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val TLS_DIAG_TAG = "TlsDiag"

/**
 * Evidence for why a TLS handshake was refused, gathered at the point of failure.
 *
 * A trust failure means a certificate did arrive and we declined it, so the questions worth answering
 * are where the hostname pointed and whether this device trusts anything unusual. An address outside
 * our hosting means something other than us answered the name; a non-zero count of added CAs means
 * traffic is being intercepted on the device itself.
 */
interface TlsDiagnostics {
  /** Null when [throwable] is not a certificate-trust failure, so callers can fall through. */
  suspend fun describe(throwable: Throwable?): String?
}

@Inject
@ContributesBinding(AppScope::class)
internal class AndroidTlsDiagnostics(
  private val context: Context,
  private val buildConstants: HedvigBuildConstants,
) : TlsDiagnostics {
  override suspend fun describe(throwable: Throwable?): String? {
    val causes = generateSequence(throwable) { it.cause }.take(MAX_CAUSE_DEPTH).toList()
    val trustFailure = causes.firstOrNull { it.isTrustFailure() } ?: return null
    return withContext(Dispatchers.IO) {
      buildString {
        append("failure=").append(trustFailure::class.java.name)
        append(" chain=").append(trustFailure.servedChain())
        append(" resolvedIps=").append(resolvedAuthHostIps())
        append(" transport=").append(activeTransports())
        append(" userCaCount=").append(userInstalledCaCount())
        append(" api=").append(Build.VERSION.SDK_INT)
      }
    }
  }

  /**
   * Which exception carries a trust failure varies by platform and provider — Android reports
   * [CertPathValidatorException], the desktop JVM a [CertPathBuilderException] — so match on the
   * message too rather than on one type.
   */
  private fun Throwable.isTrustFailure(): Boolean {
    if (this is CertPathValidatorException || this is CertPathBuilderException) return true
    if (this is SSLHandshakeException || this is CertificateException) {
      val message = message ?: return false
      return message.contains("certification path", ignoreCase = true) ||
        message.contains("trust anchor", ignoreCase = true)
    }
    return false
  }

  /**
   * Only [CertPathValidatorException] can carry the chain, and even then it may be absent, so the rest
   * of the report has to stand on its own.
   */
  private fun Throwable.servedChain(): String {
    val certificates = (this as? CertPathValidatorException)?.certPath?.certificates.orEmpty()
      .filterIsInstance<X509Certificate>()
    if (certificates.isEmpty()) return "unavailable"
    return certificates.joinToString(
      separator = " | ",
      prefix = "[",
      postfix = "]",
    ) { "${it.subjectX500Principal.commonName()} issuedBy ${it.issuerX500Principal.commonName()}" }
  }

  /** An address outside our hosting means the name was answered by something that isn't us. */
  private fun resolvedAuthHostIps(): String = runCatching {
    val host = URI(buildConstants.urlAuthService).host ?: return@runCatching "unknown-host"
    InetAddress.getAllByName(host).joinToString(",") { it.hostAddress ?: "?" }
  }.getOrElse { "unresolved(${it::class.simpleName})" }

  private fun activeTransports(): String = runCatching {
    val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
      ?: return@runCatching "none"
    listOfNotNull(
      "vpn".takeIf { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) },
      "wifi".takeIf { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) },
      "cellular".takeIf { capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) },
    ).joinToString("+").ifEmpty { "other" }
  }.getOrElse { "unknown" }

  /**
   * How many CAs the member or an MDM profile added. Count only — an alias can name an employer.
   * A non-zero count is the strongest single signal that traffic is intercepted on the device.
   */
  private fun userInstalledCaCount(): Int = runCatching {
    KeyStore.getInstance("AndroidCAStore").apply { load(null) }
      .aliases()
      .asSequence()
      .count { it.startsWith("user:") }
  }.getOrElse { -1 }

  private fun X500Principal.commonName(): String = COMMON_NAME.find(name)?.groupValues?.get(1) ?: "?"

  private companion object {
    const val MAX_CAUSE_DEPTH = 10
    val COMMON_NAME = Regex("CN=([^,]+)")
  }
}

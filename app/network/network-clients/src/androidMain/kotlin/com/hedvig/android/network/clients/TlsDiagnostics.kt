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
import javax.net.ssl.SSLHandshakeException
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
        append(" authHost=").append(resolvedAuthHost())
        append(" transport=").append(activeTransports())
        append(" userCaCount=").append(userInstalledCaCount())
        append(" api=").append(Build.VERSION.SDK_INT)
        // Last, and quoted: it is the only field containing spaces, so the rest stay parseable.
        append(" msg=\"").append(trustFailure.messageForLog()).append('"')
      }
    }
  }

  /**
   * On Android the trust failure arrives as an [SSLHandshakeException] whose message merely names
   * `CertPathValidatorException`, so the type checks below never match there and the message check is
   * what identifies it. The type checks still hold on other providers, which report a
   * [CertPathValidatorException] or [CertPathBuilderException] directly.
   *
   * Should the wording ever change, these stop being recognised and fall back to the pre-existing
   * error log rather than disappearing, so the regression is visible in Datadog.
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
   * The wording is what identifies a trust failure on Android, so record it verbatim: it is the only
   * way to notice the provider changing it, or differing between OEMs and API levels.
   */
  private fun Throwable.messageForLog(): String {
    val message = message ?: return "none"
    return message.replace('\n', ' ').take(MAX_MESSAGE_CHARS)
  }

  /**
   * Reported as a category rather than raw addresses: our hosting answers from a rotating public pool,
   * so a specific address proves nothing, whereas a loopback, unspecified or private answer means a
   * filter on the device or the local network answered the name instead of us.
   */
  private fun resolvedAuthHost(): String = runCatching {
    val host = URI(buildConstants.urlAuthService).host ?: return@runCatching "unknown-host"
    val addresses = InetAddress.getAllByName(host)
    if (addresses.isEmpty()) return@runCatching "no-addresses"
    addresses
      .map { address ->
        when {
          address.isAnyLocalAddress -> "unspecified"
          address.isLoopbackAddress -> "loopback"
          address.isSiteLocalAddress || address.isLinkLocalAddress -> "private"
          else -> "public"
        }
      }
      .distinct()
      .sorted()
      .joinToString("+")
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

  private companion object {
    const val MAX_CAUSE_DEPTH = 10
    const val MAX_MESSAGE_CHARS = 200
  }
}

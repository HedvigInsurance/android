package com.hedvig.android.desktop

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.buildconstants.deepLinkHostsOf
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.payments.navigation.PaymentsKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.navigation.compose.uriDeepLinkMatchers
import java.net.URI
import org.junit.Rule
import org.junit.Test

class DesktopUnmatchedLinkRouterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(RecordingLogcatLogger())

  private val browsed = mutableListOf<URI>()
  private val desktop = object : DesktopIntegration {
    override fun browse(uri: URI) {
      browsed += uri
    }

    override fun mail() {}
  }

  @Test
  fun `a link a matcher handles navigates in the app`() {
    val backstack = loggedInOn(HomeKey, InsurancesKey)

    backstack.open("https://link.dev.hedvigit.com/payments")

    assertThat(backstack.entries.last()).isEqualTo(PaymentsKey)
    assertThat(browsed).isEmpty()
  }

  @Test
  fun `a production link under the develop flavor navigates to the same destination`() {
    val backstack = loggedInOn(HomeKey, InsurancesKey)

    backstack.open("https://hedvig.page.link/payments")

    assertThat(backstack.entries.last()).isEqualTo(PaymentsKey)
    assertThat(browsed).isEmpty()
  }

  @Test
  fun `the help center's production payments link under the develop flavor opens payments in the app`() {
    val backstack = loggedInOn(HomeKey)

    backstack.open("https://link.hedvig.com/payments")

    assertThat(backstack.entries.last()).isEqualTo(PaymentsKey)
    assertThat(browsed).isEmpty()
  }

  @Test
  fun `a production link with a path prefix keeps its query when it moves to the develop host`() {
    val backstack = loggedInOn(HomeKey)

    backstack.open("https://www.hedvig.com/deeplink/payments?utm_source=faq")

    assertThat(backstack.entries.last()).isEqualTo(PaymentsKey)
    assertThat(browsed).isEmpty()
  }

  @Test
  fun `an unknown path on a develop host lands on Home`() {
    val backstack = loggedInOn(HomeKey, InsurancesKey)

    backstack.open("https://dev.hedvigit.com/deeplink/a-path-the-app-does-not-know")

    assertThat(backstack.entries.toList()).containsExactly(HomeKey)
    assertThat(browsed).isEmpty()
  }

  @Test
  fun `an unknown path on a production host lands on Home`() {
    val backstack = loggedInOn(HomeKey, InsurancesKey)

    backstack.open("https://link.hedvig.com/a-path-the-app-does-not-know")

    assertThat(backstack.entries.toList()).containsExactly(HomeKey)
    assertThat(browsed).isEmpty()
  }

  @Test
  fun `a link on another host opens in the browser`() {
    val backstack = loggedInOn(HomeKey)

    backstack.open("https://www.hedvig.com/se/forsakringar")
    backstack.open("https://example.com/payments")

    assertThat(
      browsed,
    ).containsExactly(URI("https://www.hedvig.com/se/forsakringar"), URI("https://example.com/payments"))
    assertThat(backstack.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `a deep link host without its path prefix is not a deep link`() {
    val backstack = loggedInOn(HomeKey)

    backstack.open("https://dev.hedvigit.com/deeplinks-are-not-this")

    assertThat(browsed).containsExactly(URI("https://dev.hedvigit.com/deeplinks-are-not-this"))
  }

  @Test
  fun `a production link under the production flavor is left to the browser when no matcher handled it`() {
    val router = DesktopUnmatchedLinkRouter(Flavor.Production, matcherFor(Flavor.Production), loggedInOn(HomeKey))

    assertThat(router.route("https://example.com/payments")).isFalse()
    assertThat(router.route("https://link.hedvig.com/a-path-the-app-does-not-know")).isTrue()
  }

  @Test
  fun `logged out, a production link is held until login and an unknown Hedvig path changes nothing`() {
    val backstack = newBackstackController().apply { seedIfEmpty(listOf(LoginKey)) }

    backstack.open("https://hedvig.page.link/payments")
    backstack.open("https://link.dev.hedvigit.com/a-path-the-app-does-not-know")

    assertThat(backstack.pendingDeepLink).isEqualTo(PaymentsKey)
    assertThat(backstack.entries.toList()).containsExactly(LoginKey)
    assertThat(browsed).isEmpty()
  }

  /**
   * Opens [uri] the way the shell does: the deep link matcher first, as an in-app link, and only an unmatched link
   * reaches the desktop host.
   */
  private fun BackstackController.open(uri: String) {
    val destination = matcherFor(Flavor.Develop).match(uri)
    if (destination != null) {
      navigateToInAppLink(destination)
      return
    }
    val router = DesktopUnmatchedLinkRouter(Flavor.Develop, matcherFor(Flavor.Develop), this)
    DesktopAppHost(desktop, showNotice = {}, routeUnmatchedLink = router::route).uriHandler.openUri(uri)
  }

  private fun loggedInOn(vararg keys: HedvigNavKey): BackstackController =
    newBackstackController().apply { seedIfEmpty(keys.toList()) }

  private fun matcherFor(flavor: Flavor): HedvigDeepLinkMatcher = HedvigDeepLinkMatcher(
    uriDeepLinkMatchers(deepLinkHostsOf(flavor).map { "https://$it/payments" }, PaymentsKey.serializer()),
  )
}

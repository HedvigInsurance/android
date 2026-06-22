package com.hedvig.android.app.urihandler

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class ExternalDeepLinkHandlerTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  // Production-shaped hosts: a bare host, a host carrying a path prefix, and a legacy bare host.
  private val deepLinkHosts = listOf("link.hedvig.com", "www.hedvig.com/deeplink", "hedvig.page.link")

  // Empty matcher always returns null, so every uri exercises the host/path fallback rather than a match.
  private fun handlerWith(controller: BackstackController) = ExternalDeepLinkHandler(
    matcher = HedvigDeepLinkMatcher(emptyList()),
    backstackController = controller,
    readySignal = MutableStateFlow(true),
    deepLinkHosts = deepLinkHosts,
  )

  private fun loggedInController(vararg keys: HedvigNavKey) = BackstackController(
    mutableStateListOf(*keys),
    mutableStateMapOf(),
    mutableStateOf(null),
    mutableStateOf(null),
  )

  @Test
  fun `unmatched link on the path-prefixed own host falls back to Home alone`() = runTest {
    val controller = loggedInController(HomeKey, InsurancesKey)
    handlerWith(controller).handle("https://www.hedvig.com/deeplink/something-new")
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `unmatched link on a bare own host falls back to Home alone`() = runTest {
    val controller = loggedInController(HomeKey, InsurancesKey)
    handlerWith(controller).handle("https://link.hedvig.com/something-new")
    assertThat(controller.entries.toList()).containsExactly(HomeKey)
  }

  @Test
  fun `own host but outside the path prefix is ignored`() = runTest {
    val controller = loggedInController(HomeKey, InsurancesKey)
    handlerWith(controller).handle("https://www.hedvig.com/not-a-deeplink")
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
  }

  @Test
  fun `foreign host is ignored`() = runTest {
    val controller = loggedInController(HomeKey, InsurancesKey)
    handlerWith(controller).handle("https://evil.example.com/whatever")
    assertThat(controller.entries.toList()).containsExactly(HomeKey, InsurancesKey)
  }
}

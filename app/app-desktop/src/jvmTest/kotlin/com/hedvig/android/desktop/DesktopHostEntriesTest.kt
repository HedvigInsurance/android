package com.hedvig.android.desktop

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import assertk.assertThat
import assertk.assertions.isTrue
import assertk.assertions.startsWith
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.feature.login.navigation.LoginKey
import com.hedvig.android.feature.onboarding.navigation.OnboardingKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import org.junit.Rule
import org.junit.Test

class DesktopHostEntriesTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val backstack = newBackstackController()

  private val strictProvider: (HedvigNavKey) -> NavEntry<HedvigNavKey> = entryProvider {
    desktopHostEntries(backstack)
  }

  @Test
  fun `every key the shared provider can push and leaves to the host has a desktop entry`() {
    val keys = listOf(
      LoginKey,
      HomeKey,
      InsurancesKey,
      OnboardingKey,
      ChatKey("conversation-id"),
      InboxKey,
      TrustlyKey(),
      ImageViewerKey("https://example.com/image.png", "cache-key"),
      ClaimDetailsKey("claim-id"),
      TravelCertificateKey,
      InsuranceEvidenceKey,
    )

    keys.forEach { key -> assertThat(strictProvider(key).contentKey.toString()).startsWith(key.toString()) }
  }

  @Test
  fun `the tab root placeholders keep the navigation bar`() {
    val showNavBar = NavSuiteSceneDecoratorStrategy.showNavBar()

    listOf(HomeKey, InsurancesKey).forEach { key ->
      assertThat(strictProvider(key).metadata.entries.containsAll(showNavBar.entries)).isTrue()
    }
  }

  @Test
  fun `a key without an entry is rendered by the fallback instead of throwing`() {
    val provider = entryProvider(fallback = desktopFallbackEntry(backstack)) {
      desktopHostEntries(backstack)
    }

    assertThat(provider(UnregisteredKey).contentKey.toString()).startsWith(UnregisteredKey.toString())
  }
}

private data object UnregisteredKey : HedvigNavKey

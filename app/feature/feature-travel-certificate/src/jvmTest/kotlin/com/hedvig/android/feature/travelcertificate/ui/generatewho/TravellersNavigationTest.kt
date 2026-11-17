package com.hedvig.android.feature.travelcertificate.ui.generatewho

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import com.hedvig.android.feature.travelcertificate.TestBackstack
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDateInputKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.feature.travelcertificate.navigation.navigateToCreatedCertificate
import kotlinx.datetime.LocalDate
import org.junit.Test

/**
 * [TravellersNavigationEffect] wired to the same backstack call as the travellers entry, on a stack shaped like the
 * one the flow runs on.
 */
@OptIn(ExperimentalTestApi::class)
class TravellersNavigationTest {
  private val dateInputKey = TravelCertificateDateInputKey(null)
  private val travellersKey = TravelCertificateTravellersInputKey(
    TravelCertificateTravellersStep("member@hedvig.com", LocalDate(2026, 12, 24), "contractId"),
  )
  private val backstack = TestBackstack(TravelCertificateKey, dateInputKey, travellersKey)
  private val content = TravelCertificateTravellersInputUiState.Success(
    memberFullName = "Member Name",
    isMemberIncluded = true,
    coInsuredList = emptyList(),
    coInsuredHasMissingInfo = false,
    isCreating = false,
    createFailed = false,
  )
  private var uiState by mutableStateOf<TravelCertificateTravellersInputUiState>(content)
  private var recompositionTrigger by mutableIntStateOf(0)

  private fun ComposeUiTest.showEffect() {
    setContent {
      recompositionTrigger
      TravellersNavigationEffect(
        uiState = uiState,
        onCertificateCreated = { backstack.navigateToCreatedCertificate(it) },
      )
    }
    waitForIdle()
  }

  @Test
  fun `created replaces the flow above the history with the certificate`() = runComposeUiTest {
    showEffect()
    val url = TravelCertificateUrl("https://example.com/new.pdf")
    uiState = TravelCertificateTravellersInputUiState.Created(url)
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, ShowCertificateKey(url))
  }

  @Test
  fun `loading, failure, content, creating and a failed create navigate nowhere`() = runComposeUiTest {
    showEffect()
    for (state in listOf(
      TravelCertificateTravellersInputUiState.Loading,
      TravelCertificateTravellersInputUiState.Failure,
      content,
      content.copy(isCreating = true),
      content.copy(createFailed = true),
    )) {
      uiState = state
      waitForIdle()
    }
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey, travellersKey)
  }
}

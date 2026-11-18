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
import assertk.assertions.isEmpty
import com.hedvig.android.feature.travelcertificate.TestBackstack
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDateInputKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.feature.travelcertificate.navigation.navigateToCreatedCertificate
import com.hedvig.android.molecule.public.NavigationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
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

  @Before
  fun setUp() {
    // The effect waits for the resumed lifecycle on Dispatchers.Main, which the JVM has no platform implementation of.
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** The [recompositionTrigger] value of the composition whose callback navigated, once per navigation. */
  private val navigatedFrom = mutableListOf<Int>()

  private fun ComposeUiTest.showEffect() {
    setContent {
      val composition = recompositionTrigger
      TravellersNavigationEffect(
        uiState = uiState,
        // A new callback on every recomposition, so the effect is recomposed rather than skipped.
        onCertificateCreated = {
          navigatedFrom += composition
          backstack.navigateToCreatedCertificate(it)
        },
      )
    }
    waitForIdle()
  }

  @Test
  fun `created replaces the flow above the history with the certificate, once`() = runComposeUiTest {
    showEffect()
    val url = TravelCertificateUrl("https://example.com/new.pdf")
    uiState = TravelCertificateTravellersInputUiState.Created(NavigationRequest.of(url))
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, ShowCertificateKey(url))
    // Navigating again would leave the same entries, so the calls are counted.
    assertThat(navigatedFrom).containsExactly(0)
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
    assertThat(navigatedFrom).isEmpty()
  }
}

package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.travelcertificate.TestBackstack
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDateInputKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.feature.travelcertificate.navigation.navigateToCreatedCertificate
import com.hedvig.android.feature.travelcertificate.navigation.navigateToTravellersStep
import kotlinx.datetime.LocalDate
import org.junit.Test

/**
 * [DateInputNavigationEffect] wired to the same backstack calls as the date input entry, on a stack shaped like the
 * one the flow runs on.
 */
@OptIn(ExperimentalTestApi::class)
class DateInputNavigationTest {
  private val dateInputKey = TravelCertificateDateInputKey(null)
  private val backstack = TestBackstack(TravelCertificateKey, dateInputKey)
  private val step = TravelCertificateTravellersStep("member@hedvig.com", LocalDate(2026, 12, 24), "contractId")
  private val content = TravelCertificateDateInputUiState.Success(
    contractId = "contractId",
    email = "member@hedvig.com",
    emailRevision = 1,
    emailError = null,
    minDate = LocalDate(2026, 11, 10),
    maxDate = LocalDate(2027, 1, 16),
    pickedDate = LocalDate(2026, 12, 24),
    selectedDate = LocalDate(2026, 12, 24),
    daysValid = 45,
    hasCoInsured = true,
    isCreating = false,
    createFailed = false,
    next = null,
  )
  private var uiState by mutableStateOf<TravelCertificateDateInputUiState>(content)
  private var recompositionTrigger by mutableIntStateOf(0)
  private var stepShownCalls = 0

  private fun ComposeUiTest.showEffect() {
    setContent {
      recompositionTrigger
      DateInputNavigationEffect(
        uiState = uiState,
        onNavigateToTravellers = { backstack.navigateToTravellersStep(it) },
        onStepShown = { stepShownCalls++ },
        onCertificateCreated = { backstack.navigateToCreatedCertificate(it) },
      )
    }
    waitForIdle()
  }

  @Test
  fun `content without a next step navigates nowhere`() = runComposeUiTest {
    showEffect()
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey)
  }

  @Test
  fun `a next step is pushed once and acknowledged, and pushed again when offered again`() = runComposeUiTest {
    showEffect()
    uiState = content.copy(next = step)
    waitForIdle()
    // The acknowledgement reaches the presenter later, so states that keep the step arrive before it is cleared.
    uiState = content.copy(next = step, pickedDate = LocalDate(2026, 12, 25), selectedDate = LocalDate(2026, 12, 25))
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    val travellersKey = TravelCertificateTravellersInputKey(step)
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey, travellersKey)
    assertThat(stepShownCalls).isEqualTo(1)

    uiState = content
    waitForIdle()
    backstack.entries.removeLast()
    uiState = content.copy(next = step)
    waitForIdle()
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey, travellersKey)
    assertThat(stepShownCalls).isEqualTo(2)
  }

  @Test
  fun `created replaces the flow above the history with the certificate`() = runComposeUiTest {
    showEffect()
    val url = TravelCertificateUrl("https://example.com/new.pdf")
    uiState = TravelCertificateDateInputUiState.Created(url)
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, ShowCertificateKey(url))
    assertThat(stepShownCalls).isEqualTo(0)
  }

  @Test
  fun `loading, failure and creating navigate nowhere`() = runComposeUiTest {
    showEffect()
    for (state in listOf(
      TravelCertificateDateInputUiState.Loading,
      TravelCertificateDateInputUiState.Failure,
      content.copy(isCreating = true),
      content.copy(createFailed = true),
    )) {
      uiState = state
      waitForIdle()
    }
    assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey)
    assertThat(stepShownCalls).isEqualTo(0)
  }
}

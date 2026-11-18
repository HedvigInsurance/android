package com.hedvig.android.feature.travelcertificate

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputUiState
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState
import com.hedvig.android.molecule.public.NavigationRequest
import kotlinx.datetime.LocalDate
import org.junit.Test

/** Strings that can reach logs, crash reports and Swift leave out SSNs, birth dates, names, emails and signed urls. */
class PersonalDataToStringTest {
  private val ssn = "201503011234"
  private val signedUrl = "https://example.com/certificate.pdf?signature=secret"
  private val coInsured = CoInsured(
    id = CoInsuredId("backend-id"),
    name = "Anna Berg",
    ssn = ssn,
    dateOfBirth = LocalDate(2015, 3, 1),
    isIncluded = true,
  )

  private fun assertHasNoPersonalData(text: String) {
    for (personalData in listOf(ssn, "backend-id", "Anna", "2015-03-01", signedUrl, "example.com", "member@hedvig")) {
      assertThat(text).doesNotContain(personalData)
    }
  }

  @Test
  fun `a co-insured, its id and the event that carries it`() {
    assertThat(coInsured.toString()).isEqualTo("CoInsured(isIncluded=true)")
    assertThat(coInsured.id.toString()).isEqualTo("CoInsuredId(redacted)")
    val event = TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(coInsured.id, included = true)
    assertThat(event.toString()).isEqualTo("SetCoInsuredIncluded(included=true)")
  }

  @Test
  fun `the travellers content lists its co-insured without their data`() {
    val content = TravelCertificateTravellersInputUiState.Success(
      memberFullName = "Member Name",
      isMemberIncluded = true,
      coInsuredList = listOf(coInsured),
      coInsuredHasMissingInfo = false,
      isCreating = false,
      createFailed = false,
    )
    assertHasNoPersonalData(content.toString())
  }

  @Test
  fun `the travellers content leaves out the member's name`() {
    val content = TravelCertificateTravellersInputUiState.Success(
      memberFullName = "Member Name",
      isMemberIncluded = true,
      coInsuredList = listOf(coInsured),
      coInsuredHasMissingInfo = false,
      isCreating = true,
      createFailed = false,
    )
    assertThat(content.toString()).isEqualTo(
      "Success(isMemberIncluded=true, coInsuredList=[CoInsured(isIncluded=true)], coInsuredHasMissingInfo=false, " +
        "isCreating=true, createFailed=false)",
    )
  }

  @Test
  fun `an email change leaves out the email`() {
    val event = TravelCertificateDateInputEvent.ChangeEmailInput("member@hedvig.com")
    assertThat(event.toString()).isEqualTo("ChangeEmailInput(redacted)")
  }

  @Test
  fun `both created states and the certificate key leave out the signed url`() {
    val url = TravelCertificateUrl(signedUrl)
    assertThat(url.toString()).isEqualTo("TravelCertificateUrl(redacted)")
    assertHasNoPersonalData(TravelCertificateTravellersInputUiState.Created(NavigationRequest.of(url)).toString())
    assertHasNoPersonalData(TravelCertificateDateInputUiState.Created(NavigationRequest.of(url)).toString())
    assertHasNoPersonalData(ShowCertificateKey(url).toString())
  }

  @Test
  fun `the travellers step and its key leave out the email`() {
    val step = TravelCertificateTravellersStep("member@hedvig.com", LocalDate(2026, 12, 24), "contractId")
    val expected = "TravelCertificateTravellersStep(travelDate=2026-12-24, contractId=contractId)"
    assertThat(step.toString()).isEqualTo(expected)
    assertHasNoPersonalData(TravelCertificateTravellersInputKey(step).toString())
  }
}

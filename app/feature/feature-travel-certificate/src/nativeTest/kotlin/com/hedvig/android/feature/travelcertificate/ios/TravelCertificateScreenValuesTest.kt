package com.hedvig.android.feature.travelcertificate.ios

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TravelCertificateScreenValuesTest {
  @Test
  fun `dates leave as yyyy-MM-dd with padded month and day`() {
    assertThat(LocalDate(2026, 1, 5).toIsoDateString()).isEqualTo("2026-01-05")
    assertThat(LocalDate(2026, 12, 31).toIsoDateString()).isEqualTo("2026-12-31")
  }

  @Test
  fun `a yyyy-MM-dd day is parsed`() {
    assertThat(parseIsoDateOrNull("2026-01-05")).isEqualTo(LocalDate(2026, 1, 5))
    assertThat(parseIsoDateOrNull("2028-02-29")).isEqualTo(LocalDate(2028, 2, 29))
  }

  @Test
  fun `any other text and a day that does not exist parse to null`() {
    val rejected = listOf(
      "",
      "2026-1-5",
      "2026/01/05",
      "20260105",
      " 2026-01-05",
      "2026-01-05 ",
      "2026-01-05T00:00",
      "05-01-2026",
      "2026-02-29",
      "2026-02-30",
      "2026-13-01",
      "2026-00-10",
      "not a date",
    )
    for (text in rejected) {
      assertThat(parseIsoDateOrNull(text), text).isNull()
    }
  }

  @Test
  fun `selecting a date sends the parsed day and nothing for text that is not a day`() {
    assertThat(selectTravelDateEvent("2026-12-24"))
      .isEqualTo(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 12, 24)))
    assertThat(selectTravelDateEvent("2026-12-32")).isNull()
    assertThat(selectTravelDateEvent("24/12/2026")).isNull()
  }

  @Test
  fun `a co-insured id leaves as its plain value and comes back as the same presenter id`() {
    val coInsured = CoInsured(
      id = CoInsuredId("generated-3"),
      name = "Anna Berg",
      ssn = "201503011234",
      dateOfBirth = LocalDate(2015, 3, 1),
      isIncluded = false,
    )
    val content = TravelCertificateTravellersInputUiState.Success(
      memberFullName = "Member Name",
      isMemberIncluded = true,
      coInsuredList = listOf(coInsured),
      coInsuredHasMissingInfo = false,
      isCreating = false,
      createFailed = false,
    ).toScreenState() as TravelCertificateTravellersScreenState.Content
    val swiftId = content.coInsured.single().id

    assertThat(swiftId).isEqualTo("generated-3")
    assertThat(setCoInsuredIncludedEvent(swiftId, included = true))
      .isEqualTo(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(coInsured.id, included = true))
    assertThat(setCoInsuredIncludedEvent("backend-id", included = false))
      .isEqualTo(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(CoInsuredId("backend-id"), false))
  }

  @Test
  fun `a created certificate carries the signed url and leaves it out of its text`() {
    val created = TravelCertificateUrl("https://example.com/certificate.pdf?signature=secret").toCreated()

    assertThat(created.certificateUrl).isEqualTo("https://example.com/certificate.pdf?signature=secret")
    assertThat(created.toString()).isEqualTo("TravelCertificateCreated(certificateUrl=redacted)")
  }
}

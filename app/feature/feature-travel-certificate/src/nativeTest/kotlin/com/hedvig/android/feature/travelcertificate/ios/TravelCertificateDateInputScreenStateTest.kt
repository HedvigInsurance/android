package com.hedvig.android.feature.travelcertificate.ios

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.EmailError
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputUiState
import com.hedvig.android.molecule.public.NavigationRequest
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TravelCertificateDateInputScreenStateTest {
  private val form = TravelCertificateDateInputUiState.Success(
    contractId = "contract-1",
    email = "member@hedvig.com",
    emailRevision = 1,
    emailError = null,
    minDate = LocalDate(2026, 11, 17),
    maxDate = LocalDate(2027, 2, 5),
    pickedDate = null,
    selectedDate = LocalDate(2026, 11, 18),
    daysValid = 45,
    hasCoInsured = true,
    isCreating = false,
    createFailed = false,
    next = null,
  )

  private val content = TravelCertificateDateInputScreenState.Content(
    email = "member@hedvig.com",
    emailRevision = 1,
    emailError = null,
    minDate = "2026-11-17",
    maxDate = "2027-02-05",
    selectedDate = "2026-11-18",
    daysValid = 45,
    hasCoInsured = true,
    isCreating = false,
    createFailed = false,
    next = null,
  )

  @Test
  fun `loading and failure`() {
    assertThat(TravelCertificateDateInputUiState.Loading.toScreenState())
      .isEqualTo(TravelCertificateDateInputScreenState.Loading)
    assertThat(TravelCertificateDateInputUiState.Failure.toScreenState())
      .isEqualTo(TravelCertificateDateInputScreenState.Failed)
  }

  @Test
  fun `the form carries its fields with dates as yyyy-MM-dd`() {
    assertThat(form.toScreenState()).isEqualTo(content)
  }

  @Test
  fun `a picked date is the selected date`() {
    val picked = form.copy(pickedDate = LocalDate(2027, 1, 2), selectedDate = LocalDate(2027, 1, 2), emailRevision = 3)

    assertThat(picked.toScreenState()).isEqualTo(content.copy(selectedDate = "2027-01-02", emailRevision = 3))
  }

  @Test
  fun `each email error`() {
    assertThat(form.copy(emailError = EmailError.Empty).toScreenState())
      .isEqualTo(content.copy(emailError = TravelCertificateEmailError.EMPTY))
    assertThat(form.copy(email = "member@", emailError = EmailError.Invalid).toScreenState())
      .isEqualTo(content.copy(email = "member@", emailError = TravelCertificateEmailError.INVALID))
  }

  @Test
  fun `a contract without co-insured`() {
    assertThat(form.copy(hasCoInsured = false).toScreenState()).isEqualTo(content.copy(hasCoInsured = false))
  }

  @Test
  fun `creating and a failed create`() {
    assertThat(form.copy(isCreating = true).toScreenState()).isEqualTo(content.copy(isCreating = true))
    assertThat(form.copy(createFailed = true).toScreenState()).isEqualTo(content.copy(createFailed = true))
  }

  @Test
  fun `the travellers step offered`() {
    val step = NavigationRequest.of(
      TravelCertificateTravellersStep("member@hedvig.com", LocalDate(2026, 12, 1), "contract-1"),
    )

    val mapped = form.copy(next = step).toScreenState() as TravelCertificateDateInputScreenState.Content
    assertThat(mapped).isEqualTo(content.copy(next = step))
    assertThat(mapped.next).isSameInstanceAs(step)
  }

  @Test
  fun `created carries the signed url`() {
    val created = TravelCertificateDateInputUiState.Created(
      NavigationRequest.of(TravelCertificateUrl("https://example.com/c.pdf?s=1")),
    )

    val mapped = created.toScreenState() as TravelCertificateDateInputScreenState.Created
    assertThat(mapped.certificate.target).isEqualTo(TravelCertificateCreated("https://example.com/c.pdf?s=1"))
    // Swift taking the request it is shown takes the presenter's, so Android cannot perform it again.
    assertThat(mapped.certificate.tryConsume()).isTrue()
    assertThat(created.exit.isPending).isFalse()
  }

  @Test
  fun `the form and created leave out the email and the signed url from their text`() {
    val step = NavigationRequest.of(
      TravelCertificateTravellersStep("member@hedvig.com", LocalDate(2026, 12, 1), "contract-1"),
    )
    val text = content.copy(next = step).toString()

    assertThat(text).isEqualTo(
      "Content(emailRevision=1, emailError=null, minDate=2026-11-17, maxDate=2027-02-05, selectedDate=2026-11-18, " +
        "daysValid=45, hasCoInsured=true, isCreating=false, createFailed=false, " +
        "next=NavigationRequest(id=${step.id}, isPending=true))",
    )
    val created = TravelCertificateDateInputScreenState.Created(
      NavigationRequest.of(TravelCertificateCreated("https://secret")),
    )
    assertThat(created.toString()).doesNotContain("secret")
  }

  @Test
  fun `handle calls the closure for each case`() {
    val created = TravelCertificateDateInputScreenState.Created(
      NavigationRequest.of(TravelCertificateCreated("https://c")),
    )
    val calls = listOf(
      TravelCertificateDateInputScreenState.Loading,
      TravelCertificateDateInputScreenState.Failed,
      content,
      created,
    ).map { state ->
      var called = ""
      state.handle(
        onLoading = { called = "loading" },
        onFailed = { called = "failed" },
        onContent = { called = "content:${it.daysValid}" },
        onCreated = { called = "created:${it.certificate.target.certificateUrl}" },
      )
      called
    }

    assertThat(calls).isEqualTo(listOf("loading", "failed", "content:45", "created:https://c"))
  }

  @Test
  fun `email error handle calls the closure for each entry`() {
    val calls = TravelCertificateEmailError.entries.map { error ->
      var called = ""
      error.handle(onEmpty = { called = "empty" }, onInvalid = { called = "invalid" })
      called
    }

    assertThat(calls).isEqualTo(listOf("empty", "invalid"))
  }
}

package com.hedvig.android.feature.travelcertificate.ios

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState
import com.hedvig.android.molecule.public.NavigationRequest
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class TravelCertificateTravellersScreenStateTest {
  private val anna = CoInsured(
    id = CoInsuredId("backend-anna"),
    name = "Anna Berg",
    ssn = "201503011234",
    dateOfBirth = LocalDate(2015, 3, 1),
    isIncluded = true,
  )
  private val bo = CoInsured(
    id = CoInsuredId("generated-1"),
    name = "Bo Berg",
    ssn = null,
    dateOfBirth = null,
    isIncluded = false,
  )

  private fun success(
    isMemberIncluded: Boolean = true,
    coInsured: List<CoInsured> = listOf(anna, bo),
    coInsuredHasMissingInfo: Boolean = false,
    isCreating: Boolean = false,
    createFailed: Boolean = false,
  ) = TravelCertificateTravellersInputUiState.Success(
    memberFullName = "Member Name",
    isMemberIncluded = isMemberIncluded,
    coInsuredList = coInsured,
    coInsuredHasMissingInfo = coInsuredHasMissingInfo,
    isCreating = isCreating,
    createFailed = createFailed,
  )

  private fun content(
    isMemberIncluded: Boolean = true,
    coInsured: List<TravelCertificateTraveller> = listOf(
      TravelCertificateTraveller("backend-anna", "Anna Berg", "2015-03-01", isIncluded = true),
      TravelCertificateTraveller("generated-1", "Bo Berg", null, isIncluded = false),
    ),
    coInsuredHasMissingInfo: Boolean = false,
    canCreate: Boolean = true,
    isCreating: Boolean = false,
    createFailed: Boolean = false,
  ) = TravelCertificateTravellersScreenState.Content(
    memberFullName = "Member Name",
    isMemberIncluded = isMemberIncluded,
    coInsured = coInsured,
    coInsuredHasMissingInfo = coInsuredHasMissingInfo,
    canCreate = canCreate,
    isCreating = isCreating,
    createFailed = createFailed,
  )

  @Test
  fun `loading and failure`() {
    assertThat(TravelCertificateTravellersInputUiState.Loading.toScreenState())
      .isEqualTo(TravelCertificateTravellersScreenState.Loading)
    assertThat(TravelCertificateTravellersInputUiState.Failure.toScreenState())
      .isEqualTo(TravelCertificateTravellersScreenState.Failed)
  }

  @Test
  fun `content with a co-insured with and one without a date of birth without the SSN`() {
    assertThat(success().toScreenState()).isEqualTo(content())
  }

  @Test
  fun `missing info and no co-insured`() {
    assertThat(success(coInsured = emptyList(), coInsuredHasMissingInfo = true).toScreenState())
      .isEqualTo(content(coInsured = emptyList(), coInsuredHasMissingInfo = true))
  }

  @Test
  fun `nobody included cannot create one included co-insured can`() {
    assertThat(success(isMemberIncluded = false, coInsured = listOf(bo)).toScreenState()).isEqualTo(
      content(
        isMemberIncluded = false,
        coInsured = listOf(TravelCertificateTraveller("generated-1", "Bo Berg", null, isIncluded = false)),
        canCreate = false,
      ),
    )
    assertThat(success(isMemberIncluded = false, coInsured = listOf(anna)).toScreenState()).isEqualTo(
      content(
        isMemberIncluded = false,
        coInsured = listOf(TravelCertificateTraveller("backend-anna", "Anna Berg", "2015-03-01", isIncluded = true)),
        canCreate = true,
      ),
    )
  }

  @Test
  fun `creating and a failed create`() {
    assertThat(success(isCreating = true).toScreenState()).isEqualTo(content(isCreating = true))
    assertThat(success(createFailed = true).toScreenState()).isEqualTo(content(createFailed = true))
  }

  @Test
  fun `created carries the signed url`() {
    val created = TravelCertificateTravellersInputUiState.Created(
      NavigationRequest.of(TravelCertificateUrl("https://example.com/c.pdf")),
    )

    assertThat(created.toScreenState()).isEqualTo(
      TravelCertificateTravellersScreenState.Created(TravelCertificateCreated("https://example.com/c.pdf")),
    )
  }

  @Test
  fun `the content and its travellers leave out names ids and birth dates from their text`() {
    assertThat(content().toString()).isEqualTo(
      "Content(isMemberIncluded=true, coInsured=[TravelCertificateTraveller(isIncluded=true), " +
        "TravelCertificateTraveller(isIncluded=false)], coInsuredHasMissingInfo=false, canCreate=true, " +
        "isCreating=false, createFailed=false)",
    )
  }

  @Test
  fun `handle calls the closure for each case`() {
    val calls = listOf(
      TravelCertificateTravellersScreenState.Loading,
      TravelCertificateTravellersScreenState.Failed,
      content(),
      TravelCertificateTravellersScreenState.Created(TravelCertificateCreated("https://c")),
    ).map { state ->
      var called = ""
      state.handle(
        onLoading = { called = "loading" },
        onFailed = { called = "failed" },
        onContent = { called = "content:${it.coInsured.size}" },
        onCreated = { called = "created:${it.certificate.certificateUrl}" },
      )
      called
    }

    assertThat(calls).isEqualTo(listOf("loading", "failed", "content:2", "created:https://c"))
  }
}

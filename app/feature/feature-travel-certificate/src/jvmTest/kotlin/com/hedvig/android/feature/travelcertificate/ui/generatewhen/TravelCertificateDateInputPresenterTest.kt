package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.travelcertificate.FakeCreateTravelCertificateTransport
import com.hedvig.android.feature.travelcertificate.FakeGetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.FixedClock
import com.hedvig.android.feature.travelcertificate.TestBackstack
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateData
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateError
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.expectNoStateChange
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDateInputKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey.TravelCertificatePrimaryInput
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import hedvig.resources.PROFILE_MY_INFO_INVALID_EMAIL
import hedvig.resources.Res
import hedvig.resources.travel_certificate_email_empty_error
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.type.TravelCertificateCreateInput
import org.jetbrains.compose.resources.StringResource
import org.junit.Rule
import org.junit.Test

class TravelCertificateDateInputPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val today = LocalDate(2026, 11, 17)
  private val specifications = FakeGetTravelCertificateSpecificationsUseCase()
  private val createTransport = FakeCreateTravelCertificateTransport()
  private val dateInputKey = TravelCertificateDateInputKey(null)
  private val backstack = TestBackstack(TravelCertificateKey, dateInputKey)

  private fun specification(
    email: String = "member@hedvig.com",
    numberOfCoInsured: Int = 0,
    dateRange: ClosedRange<LocalDate> = LocalDate(2026, 11, 10)..LocalDate(2027, 1, 16),
  ) = TravelCertificateData(
    TravelCertificateData.TravelCertificateSpecification(
      contractId = "contractId",
      email = email,
      maxDurationDays = 45,
      dateRange = dateRange,
      numberOfCoInsured = numberOfCoInsured,
    ),
  )

  private fun presenter(contractId: String? = null) = TravelCertificateDateInputPresenter(
    contractId = contractId,
    getTravelCertificateSpecificationsUseCase = specifications,
    createTravelCertificateUseCase = createTransport.createUseCase(),
    languageService = FakeLanguageService(),
    backstack = backstack,
    clock = FixedClock(),
  )

  private fun expectedContent(
    actual: TravelCertificateDateInputUiState,
    email: String? = "member@hedvig.com",
    travelDate: LocalDate = today,
    hasCoInsured: Boolean = false,
    errorMessageRes: StringResource? = null,
  ) = TravelCertificateDateInputUiState.Success(
    contractId = "contractId",
    email = email,
    travelDate = travelDate,
    hasCoInsured = hasCoInsured,
    datePickerState = (actual as TravelCertificateDateInputUiState.Success).datePickerState,
    daysValid = 45,
    errorMessageRes = errorMessageRes,
  )

  private suspend fun MoleculePresenterTestContext<
    TravelCertificateDateInputEvent,
    TravelCertificateDateInputUiState,
  >.awaitContent(): TravelCertificateDateInputUiState.Success {
    val item = awaitItem()
    assertThat(item).isInstanceOf<TravelCertificateDateInputUiState.Success>()
    return item as TravelCertificateDateInputUiState.Success
  }

  private fun createInput(email: String = "member@hedvig.com", startDate: LocalDate = today) =
    TravelCertificateCreateInput(
      contractId = "contractId",
      startDate = startDate,
      isMemberIncluded = true,
      coInsured = emptyList(),
      email = email,
    )

  private fun utcMillis(date: LocalDate): Long = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

  @Test
  fun `loading then content with the email prefilled and today as the date`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      val content = awaitContent()
      assertThat(content).isEqualTo(expectedContent(content))
      assertThat(content.datePickerState.selectedDateMillis).isNull()
      assertThat(specifications.contractIds).containsExactly(null)
    }
  }

  @Test
  fun `the contract id argument is passed to the specification and the specification's id is used`() = runTest {
    presenter(contractId = "chosenContract").test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      assertThat(awaitContent().contractId).isEqualTo("contractId")
      assertThat(specifications.contractIds).containsExactly("chosenContract")
    }
  }

  @Test
  fun `the picker allows exactly the specification's date range`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      val picker = awaitContent().datePickerState
      assertThat(picker.yearRange).isEqualTo(2026..2027)
      val selectable = picker.selectableDates
      assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2026, 11, 9)))).isFalse()
      assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2026, 11, 10)))).isTrue()
      assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2027, 1, 16)))).isTrue()
      assertThat(selectable.isSelectableDate(utcMillis(LocalDate(2027, 1, 17)))).isFalse()
      assertThat(selectable.isSelectableYear(2025)).isFalse()
      assertThat(selectable.isSelectableYear(2026)).isTrue()
      assertThat(selectable.isSelectableYear(2027)).isTrue()
      assertThat(selectable.isSelectableYear(2028)).isFalse()
    }
  }

  @Test
  fun `the default date is today even when the range starts later`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(
        specification(dateRange = LocalDate(2026, 11, 20)..LocalDate(2027, 1, 16)).right(),
      )
      val content = awaitContent()
      assertThat(content.travelDate).isEqualTo(today)
      assertThat(content.datePickerState.selectableDates.isSelectableDate(utcMillis(today))).isFalse()
    }
  }

  @Test
  fun `picking a date changes the travel date`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      val content = awaitContent()
      Snapshot.withMutableSnapshot {
        content.datePickerState.selectedDateMillis = utcMillis(LocalDate(2026, 12, 24))
      }
      val changed = awaitContent()
      assertThat(changed).isEqualTo(expectedContent(changed, travelDate = LocalDate(2026, 12, 24)))
    }
  }

  @Test
  fun `a failed load can be retried`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(TravelCertificateError.Error(ErrorMessage("offline")).left())
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Failure)

      sendEvent(TravelCertificateDateInputEvent.RetryLoadData)
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      val content = awaitContent()
      assertThat(content).isEqualTo(expectedContent(content))
    }
  }

  @Test
  fun `not being eligible is the same failure`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(TravelCertificateError.NotEligible.left())
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Failure)
    }
  }

  @Test
  fun `a reload while showing content does not fetch again`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      val content = awaitContent()
      sendEvent(TravelCertificateDateInputEvent.RetryLoadData)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content)
      assertThat(specifications.contractIds).containsExactly(null)
    }
  }

  @Test
  fun `an empty email gives the empty error and goes nowhere`() = runTest {
    assertEmailError(email = "", expected = Res.string.travel_certificate_email_empty_error)
  }

  @Test
  fun `a blank email gives the invalid error, not the empty one`() = runTest {
    assertEmailError(email = "   ", expected = Res.string.PROFILE_MY_INFO_INVALID_EMAIL)
  }

  @Test
  fun `a malformed email gives the invalid error`() = runTest {
    for (email in listOf("member", "member@", "@hedvig.com", "member@hedvig", "member@@hedvig.com")) {
      assertEmailError(email = email, expected = Res.string.PROFILE_MY_INFO_INVALID_EMAIL)
    }
  }

  @Test
  fun `an email with surrounding spaces is not trimmed and gives the invalid error`() = runTest {
    assertEmailError(email = " member@hedvig.com ", expected = Res.string.PROFILE_MY_INFO_INVALID_EMAIL)
  }

  private suspend fun assertEmailError(email: String, expected: StringResource) {
    val backstack = TestBackstack(TravelCertificateKey, dateInputKey)
    val specifications = FakeGetTravelCertificateSpecificationsUseCase()
    val createTransport = FakeCreateTravelCertificateTransport()
    TravelCertificateDateInputPresenter(
      contractId = null,
      getTravelCertificateSpecificationsUseCase = specifications,
      createTravelCertificateUseCase = createTransport.createUseCase(),
      languageService = FakeLanguageService(),
      backstack = backstack,
      clock = FixedClock(),
    ).test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 1).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput(email))
      val edited = awaitContent()
      assertThat(edited, email).isEqualTo(expectedContent(edited, email = email, hasCoInsured = true))

      sendEvent(TravelCertificateDateInputEvent.Submit)
      val rejected = awaitContent()
      assertThat(rejected, email).isEqualTo(
        expectedContent(rejected, email = email, hasCoInsured = true, errorMessageRes = expected),
      )
      assertThat(backstack.entries, email).containsExactly(TravelCertificateKey, dateInputKey)
      assertThat(createTransport.inputs, email).isEmpty()

      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("member@hedvig.com"))
      val fixed = awaitContent()
      assertThat(fixed, email).isEqualTo(expectedContent(fixed, hasCoInsured = true))
    }
  }

  @Test
  fun `continue with co-insured pushes the travellers step with the entered input`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 2).right())
      val content = awaitContent()
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("other@hedvig.com"))
      skipItems(1)
      Snapshot.withMutableSnapshot {
        content.datePickerState.selectedDateMillis = utcMillis(LocalDate(2026, 12, 24))
      }
      val ready = awaitContent()

      sendEvent(TravelCertificateDateInputEvent.Submit)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(ready)
      assertThat(backstack.entries).containsExactly(
        TravelCertificateKey,
        dateInputKey,
        TravelCertificateTravellersInputKey(
          TravelCertificatePrimaryInput(
            email = "other@hedvig.com",
            travelDate = LocalDate(2026, 12, 24),
            contractId = "contractId",
          ),
        ),
      )
      assertThat(createTransport.inputs).isEmpty()
    }
  }

  @Test
  fun `a second continue with co-insured pushes the travellers step again`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 1).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.Submit)
      sendEvent(TravelCertificateDateInputEvent.Submit)
      testScheduler.advanceUntilIdle()
      val travellersKey = TravelCertificateTravellersInputKey(
        TravelCertificatePrimaryInput("member@hedvig.com", today, "contractId"),
      )
      assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey, travellersKey, travellersKey)
    }
  }

  @Test
  fun `continue without co-insured creates directly and shows the certificate`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      assertThat(createTransport.inputs).containsExactly(createInput())
      assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey)

      createTransport.responses.trySend("https://example.com/new.pdf")
      testScheduler.advanceUntilIdle()
      assertThat(backstack.entries).containsExactly(
        TravelCertificateKey,
        ShowCertificateKey(TravelCertificateUrl("https://example.com/new.pdf")),
      )
      expectNoStateChange(TravelCertificateDateInputUiState.Loading)
    }
  }

  @Test
  fun `a date outside the range is not rejected and is sent to create`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      val content = awaitContent()
      Snapshot.withMutableSnapshot {
        content.datePickerState.selectedDateMillis = utcMillis(LocalDate(2027, 3, 1))
      }
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      assertThat(createTransport.inputs).containsExactly(createInput(startDate = LocalDate(2027, 3, 1)))
    }
  }

  @Test
  fun `a failed create shows the failure, and retrying reloads the specification and drops the typed email`() =
    runTest {
      presenter().test(TravelCertificateDateInputUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
        specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
        skipItems(1)
        sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("typed@hedvig.com"))
        skipItems(1)

        sendEvent(TravelCertificateDateInputEvent.Submit)
        assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
        createTransport.responses.trySend(null)
        assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Failure)
        assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey)
        assertThat(createTransport.inputs).containsExactly(createInput(email = "typed@hedvig.com"))

        sendEvent(TravelCertificateDateInputEvent.RetryLoadData)
        assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
        specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
        val reloaded = awaitContent()
        assertThat(reloaded).isEqualTo(expectedContent(reloaded, email = "member@hedvig.com"))
        assertThat(specifications.contractIds).containsExactly(null, null)
      }
    }

  @Test
  fun `a second continue while creating is ignored`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      sendEvent(TravelCertificateDateInputEvent.Submit)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(TravelCertificateDateInputUiState.Loading)
      assertThat(createTransport.inputs).containsExactly(createInput())

      createTransport.responses.trySend("https://example.com/new.pdf")
      testScheduler.advanceUntilIdle()
      assertThat(createTransport.inputs).containsExactly(createInput())
      assertThat(backstack.entries).containsExactly(
        TravelCertificateKey,
        ShowCertificateKey(TravelCertificateUrl("https://example.com/new.pdf")),
      )
      expectNoStateChange(TravelCertificateDateInputUiState.Loading)
    }
  }

  @Test
  fun `starting from earlier content keeps it, including the picker, without fetching`() = runTest {
    val first = presenter()
    var earlier: TravelCertificateDateInputUiState.Success? = null
    first.test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("typed@hedvig.com"))
      earlier = awaitContent()
    }
    val restored = earlier!!
    presenter().test(restored) {
      assertThat(awaitItem()).isEqualTo(restored.copy(errorMessageRes = null))
      testScheduler.advanceUntilIdle()
      assertThat(specifications.contractIds).containsExactly(null)
    }
  }
}

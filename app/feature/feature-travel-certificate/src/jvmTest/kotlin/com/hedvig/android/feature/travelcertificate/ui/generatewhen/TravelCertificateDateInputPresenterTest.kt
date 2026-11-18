package com.hedvig.android.feature.travelcertificate.ui.generatewhen

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.travelcertificate.FakeCreateTravelCertificateTransport
import com.hedvig.android.feature.travelcertificate.FakeGetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.FixedClock
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateData
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateError
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.expectNoStateChange
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import com.hedvig.android.molecule.test.testFrames
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.type.TravelCertificateCreateInput
import org.junit.Rule
import org.junit.Test

class TravelCertificateDateInputPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val today = LocalDate(2026, 11, 17)
  private val specifications = FakeGetTravelCertificateSpecificationsUseCase()
  private val createTransport = FakeCreateTravelCertificateTransport()

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

  private fun presenter(contractId: String? = null, clock: Clock = FixedClock()) = TravelCertificateDateInputPresenter(
    contractId = contractId,
    getTravelCertificateSpecificationsUseCase = specifications,
    createTravelCertificateUseCase = createTransport.createUseCase(),
    clock = clock,
  )

  private fun expectedContent(
    email: String = "member@hedvig.com",
    emailRevision: Int = 1,
    pickedDate: LocalDate? = null,
    selectedDate: LocalDate = pickedDate ?: today,
    hasCoInsured: Boolean = false,
    emailError: EmailError? = null,
    minDate: LocalDate = LocalDate(2026, 11, 10),
    maxDate: LocalDate = LocalDate(2027, 1, 16),
    isCreating: Boolean = false,
    createFailed: Boolean = false,
    next: NavigationRequest<TravelCertificateTravellersStep>? = null,
  ) = TravelCertificateDateInputUiState.Success(
    contractId = "contractId",
    email = email,
    emailRevision = emailRevision,
    emailError = emailError,
    minDate = minDate,
    maxDate = maxDate,
    pickedDate = pickedDate,
    selectedDate = selectedDate,
    daysValid = 45,
    hasCoInsured = hasCoInsured,
    isCreating = isCreating,
    createFailed = createFailed,
    next = next,
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

  @Test
  fun `loading then content with the email prefilled and today as the date`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      assertThat(awaitContent()).isEqualTo(expectedContent())
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
  fun `the allowed dates are exactly the specification's date range`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      val content = awaitContent()
      assertThat(content.minDate).isEqualTo(LocalDate(2026, 11, 10))
      assertThat(content.maxDate).isEqualTo(LocalDate(2027, 1, 16))
    }
  }

  @Test
  fun `the default date is clamped to the start of a range that starts after today`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      val range = LocalDate(2026, 11, 20)..LocalDate(2027, 1, 16)
      specifications.responses.trySend(specification(dateRange = range).right())
      assertThat(awaitContent()).isEqualTo(
        expectedContent(selectedDate = LocalDate(2026, 11, 20), minDate = LocalDate(2026, 11, 20)),
      )
    }
  }

  @Test
  fun `the default date is clamped to the end of a range that ends before today`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      val range = LocalDate(2026, 10, 1)..LocalDate(2026, 11, 16)
      specifications.responses.trySend(specification(dateRange = range).right())
      assertThat(awaitContent()).isEqualTo(
        expectedContent(
          selectedDate = LocalDate(2026, 11, 16),
          minDate = LocalDate(2026, 10, 1),
          maxDate = LocalDate(2026, 11, 16),
        ),
      )
    }
  }

  @Test
  fun `picking a date changes the travel date`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 12, 24)))
      assertThat(awaitContent()).isEqualTo(expectedContent(pickedDate = LocalDate(2026, 12, 24)))
    }
  }

  @Test
  fun `the first and last day of the range can be picked`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 11, 10)))
      assertThat(awaitContent()).isEqualTo(expectedContent(pickedDate = LocalDate(2026, 11, 10)))
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2027, 1, 16)))
      assertThat(awaitContent()).isEqualTo(expectedContent(pickedDate = LocalDate(2027, 1, 16)))
    }
  }

  @Test
  fun `a date outside the range is rejected`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2027, 3, 1)))
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 11, 9)))
      testScheduler.advanceUntilIdle()
      expectNoStateChange(expectedContent())

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitContent()).isEqualTo(expectedContent(isCreating = true))
      assertThat(createTransport.inputs).containsExactly(createInput(startDate = today))
    }
  }

  @Test
  fun `the default date is today when continue is tapped, not when the step loaded`() = runTest {
    // Noon UTC is the same calendar day in every zone a test may run in.
    val clock = AdvancingClock(Instant.parse("2026-11-17T12:00:00Z"))
    presenter(clock = clock).test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      assertThat(awaitContent()).isEqualTo(expectedContent())

      clock.now = Instant.parse("2026-11-18T12:00:00Z")
      sendEvent(TravelCertificateDateInputEvent.Submit)
      val tomorrow = LocalDate(2026, 11, 18)
      assertThat(awaitContent()).isEqualTo(expectedContent(selectedDate = tomorrow, isCreating = true))
      assertThat(createTransport.inputs).containsExactly(createInput(startDate = tomorrow))
    }
  }

  @Test
  fun `a picked date stays as picked when the day changes`() = runTest {
    val clock = AdvancingClock(Instant.parse("2026-11-17T12:00:00Z"))
    presenter(clock = clock).test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(today))
      assertThat(awaitContent()).isEqualTo(expectedContent(pickedDate = today))

      clock.now = Instant.parse("2026-11-18T12:00:00Z")
      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitContent()).isEqualTo(expectedContent(pickedDate = today, isCreating = true))
      assertThat(createTransport.inputs).containsExactly(createInput(startDate = today))
    }
  }

  @Test
  fun `continue with a range that has no days fails without creating or offering a step`() = runTest {
    for (numberOfCoInsured in listOf(0, 1)) {
      val specifications = FakeGetTravelCertificateSpecificationsUseCase()
      val createTransport = FakeCreateTravelCertificateTransport()
      TravelCertificateDateInputPresenter(
        contractId = null,
        getTravelCertificateSpecificationsUseCase = specifications,
        createTravelCertificateUseCase = createTransport.createUseCase(),
        clock = FixedClock(),
      ).test(TravelCertificateDateInputUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
        val emptyRange = LocalDate(2026, 11, 20)..LocalDate(2026, 11, 15)
        specifications.responses.trySend(
          specification(numberOfCoInsured = numberOfCoInsured, dateRange = emptyRange).right(),
        )
        assertThat(awaitContent().selectedDate).isEqualTo(LocalDate(2026, 11, 20))

        sendEvent(TravelCertificateDateInputEvent.Submit)
        assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Failure)
        assertThat(createTransport.inputs).isEmpty()
      }
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
      assertThat(awaitContent()).isEqualTo(expectedContent())
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
  fun `the prefill writes the email with a new revision, typing keeps the revision`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      assertThat(awaitContent()).isEqualTo(expectedContent(email = "member@hedvig.com", emailRevision = 1))

      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("m"))
      assertThat(awaitContent()).isEqualTo(expectedContent(email = "m", emailRevision = 1))
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput(""))
      assertThat(awaitContent()).isEqualTo(expectedContent(email = "", emailRevision = 1))
    }
  }

  @Test
  fun `an empty email gives the empty error and goes nowhere`() = runTest {
    assertEmailError(email = "", expected = EmailError.Empty)
  }

  @Test
  fun `a blank email gives the empty error`() = runTest {
    assertEmailError(email = "   ", expected = EmailError.Empty)
  }

  @Test
  fun `a malformed email gives the invalid error`() = runTest {
    for (email in listOf("member", "member@", "@hedvig.com", "member@hedvig", "member@@hedvig.com")) {
      assertEmailError(email = email, expected = EmailError.Invalid)
    }
  }

  private suspend fun assertEmailError(email: String, expected: EmailError) {
    val specifications = FakeGetTravelCertificateSpecificationsUseCase()
    val createTransport = FakeCreateTravelCertificateTransport()
    TravelCertificateDateInputPresenter(
      contractId = null,
      getTravelCertificateSpecificationsUseCase = specifications,
      createTravelCertificateUseCase = createTransport.createUseCase(),
      clock = FixedClock(),
    ).test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 1).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput(email))
      val edited = awaitContent()
      assertThat(edited, email).isEqualTo(expectedContent(email = email, hasCoInsured = true))

      sendEvent(TravelCertificateDateInputEvent.Submit)
      val rejected = awaitContent()
      assertThat(rejected, email).isEqualTo(
        expectedContent(email = email, hasCoInsured = true, emailError = expected),
      )
      assertThat(createTransport.inputs, email).isEmpty()

      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("member@hedvig.com"))
      val fixed = awaitContent()
      assertThat(fixed, email).isEqualTo(expectedContent(hasCoInsured = true))
    }
  }

  @Test
  fun `an email with surrounding spaces is trimmed before it is validated and used`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput(" other@hedvig.com "))
      skipItems(1)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitContent()).isEqualTo(expectedContent(email = " other@hedvig.com ", isCreating = true))
      assertThat(createTransport.inputs).containsExactly(createInput(email = "other@hedvig.com"))
    }
  }

  @Test
  fun `continue with co-insured offers the travellers step with the entered input`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 2).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput(" other@hedvig.com"))
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 12, 24)))
      skipItems(1)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      val step = TravelCertificateTravellersStep(
        email = "other@hedvig.com",
        travelDate = LocalDate(2026, 12, 24),
        contractId = "contractId",
      )
      val offered = awaitContent()
      assertThat(offered.next?.target).isEqualTo(step)
      assertThat(offered.next?.isPending).isEqualTo(true)
      assertThat(offered).isEqualTo(
        expectedContent(
          email = " other@hedvig.com",
          pickedDate = LocalDate(2026, 12, 24),
          hasCoInsured = true,
          next = offered.next,
        ),
      )
      assertThat(createTransport.inputs).isEmpty()
    }
  }

  @Test
  fun `a second continue before the step is taken offers it once, and after it is taken offers it again`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 1).right())
      skipItems(1)
      val step = TravelCertificateTravellersStep("member@hedvig.com", today, "contractId")

      sendEvent(TravelCertificateDateInputEvent.Submit)
      sendEvent(TravelCertificateDateInputEvent.Submit)
      val offered = awaitContent()
      assertThat(offered.next?.target).isEqualTo(step)
      assertThat(offered).isEqualTo(expectedContent(hasCoInsured = true, next = offered.next))
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 12, 24)))
      sendEvent(TravelCertificateDateInputEvent.Submit)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(offered.copy(pickedDate = LocalDate(2026, 12, 24), selectedDate = LocalDate(2026, 12, 24)))

      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(today))
      assertThat(offered.next!!.tryConsume()).isEqualTo(true)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(expectedContent(hasCoInsured = true, pickedDate = today, next = offered.next))

      sendEvent(TravelCertificateDateInputEvent.Submit)
      testScheduler.advanceUntilIdle()
      val offeredAgain = expectMostRecentItem() as TravelCertificateDateInputUiState.Success
      assertThat(offeredAgain.next).isNotEqualTo(offered.next)
      assertThat(offeredAgain.next?.target).isEqualTo(step)
      assertThat(offeredAgain.next?.isPending).isEqualTo(true)
      assertThat(offeredAgain).isEqualTo(offered.copy(pickedDate = today, next = offeredAgain.next))
    }
  }

  @Test
  fun `continue without co-insured creates directly and ends in created`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitContent()).isEqualTo(expectedContent(isCreating = true))
      assertThat(createTransport.inputs).containsExactly(createInput())

      createTransport.responses.trySend("https://example.com/new.pdf")
      val created = TravelCertificateDateInputUiState.Created(TravelCertificateUrl("https://example.com/new.pdf"))
      assertThat(awaitItem()).isEqualTo(created)
      sendEvent(TravelCertificateDateInputEvent.Submit)
      sendEvent(TravelCertificateDateInputEvent.RetryLoadData)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(created)
      assertThat(createTransport.inputs).containsExactly(createInput())
      assertThat(specifications.contractIds).containsExactly(null)
    }
  }

  @Test
  fun `a failed create keeps the input, shows the failure, and continue retries`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("typed@hedvig.com"))
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 12, 24)))
      skipItems(1)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      val creating = expectedContent(
        email = "typed@hedvig.com",
        pickedDate = LocalDate(2026, 12, 24),
        isCreating = true,
      )
      assertThat(awaitContent()).isEqualTo(creating)
      createTransport.responses.trySend(null)
      assertThat(awaitContent()).isEqualTo(creating.copy(isCreating = false, createFailed = true))
      val expectedInput = createInput(email = "typed@hedvig.com", startDate = LocalDate(2026, 12, 24))
      assertThat(createTransport.inputs).containsExactly(expectedInput)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitContent()).isEqualTo(creating)
      assertThat(createTransport.inputs).containsExactly(expectedInput, expectedInput)
      createTransport.responses.trySend("https://example.com/new.pdf")
      assertThat(awaitItem()).isEqualTo(
        TravelCertificateDateInputUiState.Created(TravelCertificateUrl("https://example.com/new.pdf")),
      )
      assertThat(specifications.contractIds).containsExactly(null)
    }
  }

  @Test
  fun `a second continue while creating is ignored`() = runTest {
    presenter().test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      skipItems(1)

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitContent()).isEqualTo(expectedContent(isCreating = true))
      sendEvent(TravelCertificateDateInputEvent.Submit)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("other@hedvig.com"))
      sendEvent(TravelCertificateDateInputEvent.Submit)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(expectedContent(email = "other@hedvig.com", isCreating = true))
      assertThat(createTransport.inputs).containsExactly(createInput())

      createTransport.responses.trySend("https://example.com/new.pdf")
      assertThat(awaitItem()).isEqualTo(
        TravelCertificateDateInputUiState.Created(TravelCertificateUrl("https://example.com/new.pdf")),
      )
      assertThat(createTransport.inputs).containsExactly(createInput())
    }
  }

  @Test
  fun `starting from earlier content keeps it without fetching`() = runTest {
    val first = presenter()
    var earlier: TravelCertificateDateInputUiState.Success? = null
    first.test(TravelCertificateDateInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateDateInputUiState.Loading)
      specifications.responses.trySend(specification().right())
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.SelectTravelDate(LocalDate(2026, 12, 24)))
      skipItems(1)
      sendEvent(TravelCertificateDateInputEvent.ChangeEmailInput("typed@hedvig.com"))
      earlier = awaitContent()
    }
    val restored = earlier!!
    presenter().test(restored) {
      assertThat(awaitItem()).isEqualTo(restored.copy(emailError = null))
      testScheduler.advanceUntilIdle()
      assertThat(specifications.contractIds).containsExactly(null)
    }
  }

  @Test
  fun `starting from content that was offering a step drops the step`() = runTest {
    val step = TravelCertificateTravellersStep("member@hedvig.com", today, "contractId")
    val earlier = expectedContent(
      hasCoInsured = true,
      next = NavigationRequest.of(step),
      emailError = EmailError.Invalid,
    )
    presenter().test(earlier) {
      assertThat(awaitItem()).isEqualTo(expectedContent(hasCoInsured = true))
      testScheduler.advanceUntilIdle()
      assertThat(specifications.contractIds).isEmpty()
      assertThat(createTransport.inputs).isEmpty()
    }
  }

  @Test
  fun `starting from content that was creating shows the create as failed and keeps the input`() = runTest {
    val creating = expectedContent(email = "typed@hedvig.com", isCreating = true)
    presenter().test(creating) {
      assertThat(awaitItem()).isEqualTo(expectedContent(email = "typed@hedvig.com", createFailed = true))
      testScheduler.advanceUntilIdle()
      assertThat(specifications.contractIds).isEmpty()
      assertThat(createTransport.inputs).isEmpty()

      sendEvent(TravelCertificateDateInputEvent.Submit)
      assertThat(awaitContent()).isEqualTo(creating)
      assertThat(createTransport.inputs).containsExactly(createInput(email = "typed@hedvig.com"))
    }
  }

  @Test
  fun `a continue in the frame the step was taken offers it again as a new request`() = runTest {
    presenter().testFrames(TravelCertificateDateInputUiState.Loading) {
      specifications.responses.trySend(specification(numberOfCoInsured = 1).right())
      frame()
      frame()
      val step = TravelCertificateTravellersStep("member@hedvig.com", today, "contractId")
      emit(TravelCertificateDateInputEvent.Submit)
      frame()
      val first = (latest as TravelCertificateDateInputUiState.Success).next!!
      assertThat(latest).isEqualTo(expectedContent(hasCoInsured = true, next = first))

      // The platform takes the step and the member taps Continue again before the next frame.
      assertThat(first.tryConsume()).isEqualTo(true)
      emit(TravelCertificateDateInputEvent.Submit)
      frame()
      val second = (latest as TravelCertificateDateInputUiState.Success).next!!
      assertThat(second.isPending).isEqualTo(true)
      assertThat(second.target).isEqualTo(step)
      assertThat(latest).isEqualTo(expectedContent(hasCoInsured = true, next = second))
      val observedSteps = observed.mapNotNull { (it as? TravelCertificateDateInputUiState.Success)?.next }
      assertThat(observedSteps.distinct()).containsExactly(first, second)
    }
  }

  @Test
  fun `a continue handled in the same frame as the end of a failed create is sent`() = runTest {
    presenter().testFrames(TravelCertificateDateInputUiState.Loading) {
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      frame()
      frame()
      emit(TravelCertificateDateInputEvent.Submit)
      frame()
      assertThat(latest).isEqualTo(expectedContent(isCreating = true))

      // The failure and the retry are both handled before the next frame composes the presenter.
      createTransport.responses.trySend(null)
      runCurrent()
      emit(TravelCertificateDateInputEvent.Submit)
      frame()
      assertThat(createTransport.inputs).containsExactly(createInput(), createInput())
      assertThat(latest).isEqualTo(expectedContent(isCreating = true))

      createTransport.responses.trySend("https://example.com/new.pdf")
      frame()
      assertThat(latest).isEqualTo(
        TravelCertificateDateInputUiState.Created(TravelCertificateUrl("https://example.com/new.pdf")),
      )
    }
  }

  @Test
  fun `a restart after the certificate was opened keeps it opened, so it is not opened again`() = runTest {
    presenter().testFrames(TravelCertificateDateInputUiState.Loading) {
      specifications.responses.trySend(specification(numberOfCoInsured = 0).right())
      frame()
      frame()
      emit(TravelCertificateDateInputEvent.Submit)
      frame()
      createTransport.responses.trySend("https://example.com/new.pdf")
      frame()
      val created = latest as TravelCertificateDateInputUiState.Created
      assertThat(created.leave.tryConsume()).isEqualTo(true)

      restart()
      frame()
      assertThat((latest as TravelCertificateDateInputUiState.Created).leave.tryConsume()).isEqualTo(false)
    }
  }

  @Test
  fun `starting from created stays created without fetching`() = runTest {
    val created = TravelCertificateDateInputUiState.Created(TravelCertificateUrl("https://example.com/new.pdf"))
    presenter().test(created) {
      assertThat(awaitItem()).isEqualTo(created)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(created)
      assertThat(specifications.contractIds).isEmpty()
    }
  }
}

private class AdvancingClock(var now: Instant) : Clock {
  override fun now(): Instant = now
}

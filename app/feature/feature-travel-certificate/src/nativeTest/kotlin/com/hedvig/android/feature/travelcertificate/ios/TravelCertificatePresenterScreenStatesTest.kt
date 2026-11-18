package com.hedvig.android.feature.travelcertificate.ios

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.feature.travelcertificate.data.CoInsuredData
import com.hedvig.android.feature.travelcertificate.data.CoInsuredDataWithMember
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateData
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputPresenter
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputUiState
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputEvent
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputPresenter
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputUiState
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlinx.datetime.LocalDate

/**
 * The date and travellers presenters, run as their ViewModels run them but with every frame sent at once, mapped to
 * the state Swift receives, and driven by the events the screen models build from Swift's arguments. The display
 * link of the iOS test process produces no frames, so the screen models themselves only reach Loading.
 */
class TravelCertificatePresenterScreenStatesTest {
  private val logs = CapturingLogger()

  @BeforeTest
  fun installLogger() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
  }

  private val form = TravelCertificateDateInputScreenState.Content(
    email = "member@hedvig.com",
    emailRevision = 1,
    emailError = null,
    minDate = "2026-11-17",
    maxDate = "2027-02-05",
    selectedDate = "2026-11-17",
    daysValid = 45,
    hasCoInsured = false,
    isCreating = false,
    createFailed = false,
    next = null,
  )

  @Test
  fun `the date step ignores dates outside the range and offers the travellers step with the entered input`() =
    runBlocking {
      val specifications = FakeSpecificationsUseCase()
      val transport = FakeCreateTransport()
      val run = dateRun(specifications, transport)
      assertThat(run.awaitState()).isEqualTo(TravelCertificateDateInputScreenState.Loading)
      specifications.responses.send(specification(numberOfCoInsured = 2).right())
      val withCoInsured = form.copy(hasCoInsured = true)
      assertThat(run.awaitState()).isEqualTo(withCoInsured)

      run.send(selectTravelDateEvent("2027-02-06")!!)
      run.send(selectTravelDateEvent("2026-11-16")!!)
      run.expectNoState()
      assertThat(selectTravelDateEvent("2027-02-30")).isNull()
      run.send(selectTravelDateEvent("2027-02-05")!!)
      assertThat(run.awaitState()).isEqualTo(withCoInsured.copy(selectedDate = "2027-02-05"))

      run.send(TravelCertificateDateInputEvent.ChangeEmailInput(""))
      assertThat(run.awaitState()).isEqualTo(withCoInsured.copy(email = "", selectedDate = "2027-02-05"))
      run.send(TravelCertificateDateInputEvent.Submit)
      assertThat(run.awaitState()).isEqualTo(
        withCoInsured.copy(email = "", selectedDate = "2027-02-05", emailError = TravelCertificateEmailError.EMPTY),
      )
      run.send(TravelCertificateDateInputEvent.ChangeEmailInput("member@"))
      run.send(TravelCertificateDateInputEvent.Submit)
      assertThat(run.awaitStateMatching { (it as? TravelCertificateDateInputScreenState.Content)?.emailError != null })
        .isEqualTo(
          withCoInsured.copy(
            email = "member@",
            selectedDate = "2027-02-05",
            emailError = TravelCertificateEmailError.INVALID,
          ),
        )

      run.send(TravelCertificateDateInputEvent.ChangeEmailInput("other@hedvig.com"))
      run.send(TravelCertificateDateInputEvent.Submit)
      val step = TravelCertificateTravellersStep("other@hedvig.com", LocalDate(2027, 2, 5), "contract-1")
      val offered = run.awaitStateMatching {
        (it as? TravelCertificateDateInputScreenState.Content)?.next != null
      } as TravelCertificateDateInputScreenState.Content
      val request = offered.next!!
      assertThat(request.target).isEqualTo(step)
      assertThat(offered)
        .isEqualTo(withCoInsured.copy(email = "other@hedvig.com", selectedDate = "2027-02-05", next = request))
      assertThat(request.tryConsume()).isTrue()
      run.send(TravelCertificateDateInputEvent.Submit)
      val offeredAgain = run.awaitState() as TravelCertificateDateInputScreenState.Content
      assertThat(offeredAgain.next).isNotEqualTo(request)
      assertThat(offeredAgain.next!!.target).isEqualTo(step)
      assertThat(offeredAgain.next!!.isPending).isTrue()
      assertThat(transport.inputs()).containsExactly()
      run.cancel()
    }

  @Test
  fun `the date step without co-insured creates shows the failure and a retry ends in created`() = runBlocking {
    val specifications = FakeSpecificationsUseCase()
    val transport = FakeCreateTransport()
    val run = dateRun(specifications, transport)
    assertThat(run.awaitState()).isEqualTo(TravelCertificateDateInputScreenState.Loading)
    specifications.responses.send(specification(numberOfCoInsured = 0).right())
    assertThat(run.awaitState()).isEqualTo(form)

    run.send(TravelCertificateDateInputEvent.Submit)
    assertThat(run.awaitState()).isEqualTo(form.copy(isCreating = true))
    transport.responses.send(null)
    assertThat(run.awaitState()).isEqualTo(form.copy(createFailed = true))
    run.send(TravelCertificateDateInputEvent.Submit)
    assertThat(run.awaitState()).isEqualTo(form.copy(isCreating = true))
    transport.responses.send("https://example.com/certificate.pdf?signature=secret")

    assertThat(run.awaitState()).isInstanceOf<TravelCertificateDateInputScreenState.Created>()
      .transform { it.certificate.target }
      .isEqualTo(TravelCertificateCreated("https://example.com/certificate.pdf?signature=secret"))
    assertThat(transport.inputs().map { it.email to it.startDate }).containsExactly(
      "member@hedvig.com" to LocalDate(2026, 11, 17),
      "member@hedvig.com" to LocalDate(2026, 11, 17),
    )
    run.cancel()
  }

  @Test
  fun `the travellers step maps ids both ways ignores an unknown id and ends in created after a failure`() =
    runBlocking {
      val coInsured = FakeCoInsuredUseCase()
      val transport = FakeCreateTransport()
      val run = travellersRun(coInsured, transport)
      assertThat(run.awaitState()).isEqualTo(TravelCertificateTravellersScreenState.Loading)
      coInsured.responses.emit(
        CoInsuredDataWithMember(
          coInsuredList = listOf(
            CoInsuredData("backend-anna", "Anna", "Berg", "201503011234", LocalDate(2015, 3, 1), false),
            CoInsuredData(null, "Bo", "Berg", null, null, false),
            CoInsuredData("backend-cara", "Cara", null, null, null, true),
          ),
          memberFullName = "Member Name",
        ).right(),
      )
      val loaded = TravelCertificateTravellersScreenState.Content(
        memberFullName = "Member Name",
        isMemberIncluded = true,
        coInsured = listOf(
          TravelCertificateTraveller("backend-anna", "Anna Berg", "2015-03-01", isIncluded = false),
          TravelCertificateTraveller("generated-1", "Bo Berg", null, isIncluded = false),
        ),
        coInsuredHasMissingInfo = true,
        canCreate = true,
        isCreating = false,
        createFailed = false,
      )
      assertThat(run.awaitState()).isEqualTo(loaded)

      val boId = loaded.coInsured[1].id
      run.send(setCoInsuredIncludedEvent(boId, included = true))
      val withBo = loaded.copy(coInsured = listOf(loaded.coInsured[0], loaded.coInsured[1].copy(isIncluded = true)))
      assertThat(run.awaitState()).isEqualTo(withBo)
      run.send(setCoInsuredIncludedEvent("backend-cara", included = true))
      run.send(setCoInsuredIncludedEvent("unknown", included = true))
      run.expectNoState()
      run.send(TravelCertificateTravellersInputEvent.SetMemberIncluded(false))
      val onlyBo = withBo.copy(isMemberIncluded = false)
      assertThat(run.awaitState()).isEqualTo(onlyBo)

      run.send(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(run.awaitState()).isEqualTo(onlyBo.copy(isCreating = true))
      transport.responses.send(null)
      assertThat(run.awaitState()).isEqualTo(onlyBo.copy(createFailed = true))
      run.send(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(run.awaitState()).isEqualTo(onlyBo.copy(isCreating = true))
      transport.responses.send("https://example.com/certificate.pdf?signature=secret")

      assertThat(run.awaitState()).isInstanceOf<TravelCertificateTravellersScreenState.Created>()
        .transform { it.certificate.target }
        .isEqualTo(TravelCertificateCreated("https://example.com/certificate.pdf?signature=secret"))
      val sent = transport.inputs()
      assertThat(sent.map { input -> input.isMemberIncluded to input.coInsured.map { it.fullName } }).containsExactly(
        false to listOf("Bo Berg"),
        false to listOf("Bo Berg"),
      )
      assertThat(sent.any { it.isMemberIncluded }).isFalse()
      run.cancel()
    }

  private fun CoroutineScope.dateRun(specifications: FakeSpecificationsUseCase, transport: FakeCreateTransport) =
    PresenterRun(
      scope = this,
      presenter = TravelCertificateDateInputPresenter(
        "contract-1",
        specifications,
        transport.createUseCase(),
        FixedClock(),
      ),
      initialState = TravelCertificateDateInputUiState.Loading,
      toState = { it.toScreenState() },
    )

  private fun CoroutineScope.travellersRun(coInsured: FakeCoInsuredUseCase, transport: FakeCreateTransport) =
    PresenterRun(
      scope = this,
      presenter = TravelCertificateTravellersInputPresenter(
        TravelCertificateTravellersStep("member@hedvig.com", LocalDate(2026, 12, 1), "contract-1"),
        transport.createUseCase(),
        coInsured,
      ),
      initialState = TravelCertificateTravellersInputUiState.Loading,
      toState = { it.toScreenState() },
    )

  private fun specification(numberOfCoInsured: Int) = TravelCertificateData(
    TravelCertificateData.TravelCertificateSpecification(
      contractId = "contract-1",
      email = "member@hedvig.com",
      maxDurationDays = 45,
      dateRange = LocalDate(2026, 11, 17)..LocalDate(2027, 2, 5),
      numberOfCoInsured = numberOfCoInsured,
    ),
  )
}

private class PresenterRun<Event, UiState, State>(
  scope: CoroutineScope,
  presenter: MoleculePresenter<Event, UiState>,
  initialState: UiState,
  toState: (UiState) -> State,
) {
  private val events = Channel<Event>(capacity = 20)
  private val states = Channel<State>(Channel.UNLIMITED)
  private val job = scope.launch {
    val presenterScope = MoleculePresenterScope(events.receiveAsFlow())
    moleculeFlow(RecompositionMode.Immediate) { with(presenter) { presenterScope.present(initialState) } }
      .map(toState)
      .distinctUntilChanged()
      .collect { states.send(it) }
  }

  fun send(event: Event) {
    check(events.trySend(event).isSuccess)
  }

  suspend fun awaitState(): State = withTimeout(5.seconds) { states.receive() }

  /** Skips states until one matches [predicate], for event pairs whose intermediate state is not the point. */
  suspend fun awaitStateMatching(predicate: (State) -> Boolean): State = withTimeout(5.seconds) {
    var state = states.receive()
    while (!predicate(state)) state = states.receive()
    state
  }

  suspend fun expectNoState() {
    repeat(50) { yield() }
    assertThat(states.tryReceive().getOrNull()).isNull()
  }

  fun cancel() {
    job.cancel()
  }
}

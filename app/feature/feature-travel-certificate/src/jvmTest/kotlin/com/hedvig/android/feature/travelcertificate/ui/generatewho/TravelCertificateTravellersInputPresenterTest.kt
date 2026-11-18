package com.hedvig.android.feature.travelcertificate.ui.generatewho

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.apollographql.apollo.api.Optional
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.travelcertificate.FakeCreateTravelCertificateTransport
import com.hedvig.android.feature.travelcertificate.FakeGetCoInsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.data.CoInsuredData
import com.hedvig.android.feature.travelcertificate.data.CoInsuredDataWithMember
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.expectNoStateChange
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import com.hedvig.android.molecule.test.testFrames
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.type.TravelCertificateCreateCoInsured
import octopus.type.TravelCertificateCreateInput
import org.junit.Rule
import org.junit.Test

class TravelCertificateTravellersInputPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val primaryInput = TravelCertificateTravellersStep(
    email = "member@hedvig.com",
    travelDate = LocalDate(2026, 12, 24),
    contractId = "contractId",
  )
  private val coInsuredUseCase = FakeGetCoInsuredForContractUseCase()
  private val createTransport = FakeCreateTravelCertificateTransport()
  private val presenter = TravelCertificateTravellersInputPresenter(
    primaryInput = primaryInput,
    createTravelCertificateUseCase = createTransport.createUseCase(),
    getCoInsuredForContractUseCase = coInsuredUseCase,
  )

  private val zed = CoInsuredData(
    id = "zed",
    firstName = "Zed",
    lastName = "Last",
    ssn = "199001011234",
    dateOfBirth = null,
    hasMissingInfo = false,
  )
  private val anna = CoInsuredData(
    id = null,
    firstName = "Anna",
    lastName = "Berg",
    ssn = null,
    dateOfBirth = LocalDate(2015, 3, 1),
    hasMissingInfo = false,
  )
  private val missing = CoInsuredData(
    id = "missing",
    firstName = null,
    lastName = null,
    ssn = null,
    dateOfBirth = null,
    hasMissingInfo = true,
  )

  private val annaItem = CoInsured(
    id = CoInsuredId("generated-1"),
    name = "Anna Berg",
    ssn = null,
    dateOfBirth = LocalDate(2015, 3, 1),
    isIncluded = false,
  )
  private val zedItem = CoInsured(
    id = CoInsuredId("zed"),
    name = "Zed Last",
    ssn = "199001011234",
    dateOfBirth = null,
    isIncluded = false,
  )

  private fun data(vararg coInsured: CoInsuredData) = CoInsuredDataWithMember(
    coInsuredList = coInsured.toList(),
    memberFullName = "Member Name",
  )

  private fun content(
    coInsured: List<CoInsured> = listOf(annaItem, zedItem),
    isMemberIncluded: Boolean = true,
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

  private val newUrl = TravelCertificateUrl("https://example.com/new.pdf")
  private val memberOnlyInput get() = createInput(coInsured = emptyList())
  private val annaInput = TravelCertificateCreateCoInsured(
    fullName = "Anna Berg",
    ssn = Optional.present(null),
    dateOfBirth = Optional.present(LocalDate(2015, 3, 1)),
  )
  private val zedInput = TravelCertificateCreateCoInsured(
    fullName = "Zed Last",
    ssn = Optional.present("199001011234"),
    dateOfBirth = Optional.present(null),
  )

  private suspend fun MoleculePresenterTestContext<
    TravelCertificateTravellersInputEvent,
    TravelCertificateTravellersInputUiState,
  >.loadContent() {
    assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
    coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
    assertThat(awaitItem()).isEqualTo(content())
  }

  private fun createInput(isMemberIncluded: Boolean = true, coInsured: List<TravelCertificateCreateCoInsured>) =
    TravelCertificateCreateInput(
      contractId = "contractId",
      startDate = LocalDate(2026, 12, 24),
      isMemberIncluded = isMemberIncluded,
      coInsured = coInsured,
      email = "member@hedvig.com",
    )

  @Test
  fun `content lists co-insured sorted by name, none included, the member included`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      val state = awaitItem()
      assertThat(state).isEqualTo(content())
      assertThat((state as TravelCertificateTravellersInputUiState.Success).canCreate).isTrue()
      assertThat(coInsuredUseCase.contractIds).containsExactly("contractId")
    }
  }

  @Test
  fun `co-insured with missing info are left out and flagged`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, missing).right())
      assertThat(awaitItem()).isEqualTo(content(coInsured = listOf(zedItem), coInsuredHasMissingInfo = true))
    }
  }

  @Test
  fun `setting the member and each co-insured, and when there is at least one traveller`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()

      sendEvent(TravelCertificateTravellersInputEvent.SetMemberIncluded(false))
      val nobody = awaitItem() as TravelCertificateTravellersInputUiState.Success
      assertThat(nobody).isEqualTo(content(isMemberIncluded = false))
      assertThat(nobody.canCreate).isFalse()

      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      val annaOnly = awaitItem() as TravelCertificateTravellersInputUiState.Success
      assertThat(annaOnly).isEqualTo(
        content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem), isMemberIncluded = false),
      )
      assertThat(annaOnly.canCreate).isTrue()

      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(zedItem.id, true))
      assertThat(awaitItem()).isEqualTo(
        content(
          coInsured = listOf(annaItem.copy(isIncluded = true), zedItem.copy(isIncluded = true)),
          isMemberIncluded = false,
        ),
      )

      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, false))
      assertThat(awaitItem()).isEqualTo(
        content(coInsured = listOf(annaItem, zedItem.copy(isIncluded = true)), isMemberIncluded = false),
      )
      sendEvent(TravelCertificateTravellersInputEvent.SetMemberIncluded(true))
      assertThat(awaitItem()).isEqualTo(
        content(coInsured = listOf(annaItem, zedItem.copy(isIncluded = true)), isMemberIncluded = true),
      )
    }
  }

  @Test
  fun `the setters are idempotent, so a repeated tap on a stale frame changes nothing`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()

      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      assertThat(awaitItem()).isEqualTo(content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem)))
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      sendEvent(TravelCertificateTravellersInputEvent.SetMemberIncluded(true))
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem)))
    }
  }

  @Test
  fun `setting a co-insured that is not loaded is ignored`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()

      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(CoInsuredId("someone else"), true))
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content())
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isCreating = true))
      assertThat(createTransport.inputs).containsExactly(memberOnlyInput)
    }
  }

  @Test
  fun `a co-insured without an id gets a generated id without their data, the same on every poll`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()
      val bo = anna.copy(firstName = "Bo", lastName = null, ssn = "201001019876", dateOfBirth = null)
      coInsuredUseCase.responses.tryEmit(data(zed, anna, bo).right())
      val first = awaitItem() as TravelCertificateTravellersInputUiState.Success
      assertThat(first.coInsuredList.map { it.id.value }).containsExactly("generated-1", "generated-2", "zed")

      coInsuredUseCase.responses.tryEmit(data(bo, anna, zed).right())
      testScheduler.advanceUntilIdle()
      expectNoStateChange(first)
    }
  }

  @Test
  fun `co-insured with an empty backend id get their own generated ids and are sent as before`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      val emptyIdAnna = anna.copy(id = "")
      val emptyIdBo = anna.copy(id = "", firstName = "Bo", ssn = "201001019876", dateOfBirth = null)
      coInsuredUseCase.responses.tryEmit(data(emptyIdAnna, emptyIdBo).right())
      val boItem = annaItem.copy(
        id = CoInsuredId("generated-2"),
        name = "Bo Berg",
        ssn = "201001019876",
        dateOfBirth = null,
      )
      assertThat(awaitItem()).isEqualTo(content(coInsured = listOf(annaItem, boItem)))

      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      assertThat(awaitItem()).isEqualTo(content(coInsured = listOf(annaItem.copy(isIncluded = true), boItem)))
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      skipItems(1)
      assertThat(createTransport.inputs).containsExactly(createInput(coInsured = listOf(annaInput)))
    }
  }

  @Test
  fun `an id-less co-insured whose data changes comes back unticked under a new id`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      assertThat(awaitItem()).isEqualTo(content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem)))

      coInsuredUseCase.responses.tryEmit(data(zed, anna.copy(ssn = "201503011234")).right())
      val annaWithSsn = annaItem.copy(id = CoInsuredId("generated-2"), ssn = "201503011234")
      assertThat(awaitItem()).isEqualTo(content(coInsured = listOf(annaWithSsn, zedItem)))
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      skipItems(1)
      assertThat(createTransport.inputs).containsExactly(memberOnlyInput)
    }
  }

  @Test
  fun `the selection survives a poll that returns the same people, matched by id`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()
      sendEvent(TravelCertificateTravellersInputEvent.SetMemberIncluded(false))
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(zedItem.id, true))
      testScheduler.advanceUntilIdle()
      val selected = content(
        coInsured = listOf(annaItem.copy(isIncluded = true), zedItem.copy(isIncluded = true)),
        isMemberIncluded = false,
      )
      expectNoStateChange(selected)

      // Same people in another order, and Zed (matched by the backend id) with a new name.
      coInsuredUseCase.responses.tryEmit(data(anna, zed.copy(firstName = "Zack")).right())
      val renamed = zedItem.copy(name = "Zack Last", isIncluded = true)
      assertThat(awaitItem()).isEqualTo(
        content(coInsured = listOf(annaItem.copy(isIncluded = true), renamed), isMemberIncluded = false),
      )

      // Zed gone: he is no longer listed or sent.
      coInsuredUseCase.responses.tryEmit(data(anna).right())
      assertThat(awaitItem()).isEqualTo(
        content(coInsured = listOf(annaItem.copy(isIncluded = true)), isMemberIncluded = false),
      )
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      skipItems(1)
      assertThat(createTransport.inputs).containsExactly(
        createInput(isMemberIncluded = false, coInsured = listOf(annaInput)),
      )
    }
  }

  @Test
  fun `a failed load can be retried`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(ErrorMessage("offline").left())
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Failure)

      sendEvent(TravelCertificateTravellersInputEvent.RetryLoadData)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(TravelCertificateTravellersInputUiState.Failure)
      assertThat(coInsuredUseCase.contractIds).containsExactly("contractId", "contractId")

      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())
    }
  }

  @Test
  fun `a failed emission after content keeps the content`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())
      coInsuredUseCase.responses.tryEmit(ErrorMessage("offline").left())
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content())
    }
  }

  @Test
  fun `create sends the selection and ends in created`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(zedItem.id, true))
      skipItems(2)

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(
        content(
          coInsured = listOf(annaItem.copy(isIncluded = true), zedItem.copy(isIncluded = true)),
          isCreating = true,
        ),
      )
      // A missing SSN or birth date is sent as an explicit null.
      assertThat(createTransport.inputs).containsExactly(createInput(coInsured = listOf(annaInput, zedInput)))

      createTransport.responses.trySend("https://example.com/new.pdf")
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Created(newUrl))
    }
  }

  @Test
  fun `create is ignored when nobody is included`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()
      sendEvent(TravelCertificateTravellersInputEvent.SetMemberIncluded(false))
      assertThat(awaitItem()).isEqualTo(content(isMemberIncluded = false))

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content(isMemberIncluded = false))
      assertThat(createTransport.inputs).containsExactly()
    }
  }

  @Test
  fun `create before the travellers are loaded does nothing`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(TravelCertificateTravellersInputUiState.Loading)
      assertThat(createTransport.inputs).containsExactly()
    }
  }

  @Test
  fun `a second create while creating is ignored and does not cancel the first`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isCreating = true))
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content(isCreating = true))
      assertThat(createTransport.inputs).containsExactly(memberOnlyInput)

      createTransport.responses.trySend("https://example.com/new.pdf")
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Created(newUrl))
      testScheduler.advanceUntilIdle()
      assertThat(createTransport.inputs).containsExactly(memberOnlyInput)
    }
  }

  @Test
  fun `the selection cannot change while creating, so the screen shows what was sent`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isCreating = true))
      sendEvent(TravelCertificateTravellersInputEvent.SetMemberIncluded(false))
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content(isCreating = true))

      createTransport.responses.trySend(null)
      assertThat(awaitItem()).isEqualTo(content(createFailed = true))
      assertThat(createTransport.inputs).containsExactly(memberOnlyInput)
    }
  }

  @Test
  fun `a retry handled in the same frame as the end of a failed create is sent`() = runTest {
    presenter.testFrames(TravelCertificateTravellersInputUiState.Loading) {
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      frame()
      frame()
      assertThat(latest).isEqualTo(content())
      emit(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      frame()
      assertThat(latest).isEqualTo(content(isCreating = true))

      // The failure and the retry are both handled before the next frame composes the presenter.
      createTransport.responses.trySend(null)
      runCurrent()
      emit(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      frame()
      assertThat(createTransport.inputs).containsExactly(memberOnlyInput, memberOnlyInput)
      assertThat(latest).isEqualTo(content(isCreating = true))

      createTransport.responses.trySend("https://example.com/new.pdf")
      frame()
      assertThat(latest).isEqualTo(TravelCertificateTravellersInputUiState.Created(newUrl))
    }
  }

  @Test
  fun `a co-insured poll while creating keeps it creating`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isCreating = true))
      coInsuredUseCase.responses.tryEmit(data(anna, zed).right())
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content(isCreating = true))
      coInsuredUseCase.responses.tryEmit(data(anna, zed, missing).right())
      assertThat(awaitItem()).isEqualTo(content(isCreating = true, coInsuredHasMissingInfo = true))
    }
  }

  @Test
  fun `a failed create ends creating, shows the failure, keeps the selection, and create retries`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      skipItems(1)
      val annaIncluded = listOf(annaItem.copy(isIncluded = true), zedItem)

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(coInsured = annaIncluded, isCreating = true))
      createTransport.responses.trySend(null)
      assertThat(awaitItem()).isEqualTo(content(coInsured = annaIncluded, createFailed = true))

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(coInsured = annaIncluded, isCreating = true))
      val withAnna = createInput(coInsured = listOf(annaInput))
      assertThat(createTransport.inputs).containsExactly(withAnna, withAnna)
      assertThat(coInsuredUseCase.contractIds).containsExactly("contractId")

      createTransport.responses.trySend("https://example.com/new.pdf")
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Created(newUrl))
    }
  }

  @Test
  fun `nothing happens after created, and the co-insured polling stops`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      loadContent()
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      skipItems(1)
      createTransport.responses.trySend("https://example.com/new.pdf")
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Created(newUrl))
      testScheduler.advanceUntilIdle()
      assertThat(coInsuredUseCase.responses.subscriptionCount.value).isEqualTo(0)

      sendEvent(TravelCertificateTravellersInputEvent.SetMemberIncluded(false))
      sendEvent(TravelCertificateTravellersInputEvent.SetCoInsuredIncluded(annaItem.id, true))
      sendEvent(TravelCertificateTravellersInputEvent.RetryLoadData)
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      coInsuredUseCase.responses.tryEmit(data(anna).right())
      testScheduler.advanceUntilIdle()
      expectNoStateChange(TravelCertificateTravellersInputUiState.Created(newUrl))
      assertThat(createTransport.inputs).containsExactly(memberOnlyInput)
      assertThat(coInsuredUseCase.contractIds).containsExactly("contractId")
    }
  }

  @Test
  fun `a restart after the certificate was opened keeps it opened, so it is not opened again`() = runTest {
    presenter.testFrames(TravelCertificateTravellersInputUiState.Loading) {
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      frame()
      frame()
      emit(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      frame()
      createTransport.responses.trySend("https://example.com/new.pdf")
      frame()
      val created = latest as TravelCertificateTravellersInputUiState.Created
      assertThat(created.leave.tryConsume()).isTrue()

      restart()
      frame()
      assertThat((latest as TravelCertificateTravellersInputUiState.Created).leave.tryConsume()).isFalse()
    }
  }

  @Test
  fun `starting from created stays created without fetching`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Created(newUrl)) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Created(newUrl))
      testScheduler.advanceUntilIdle()
      expectNoStateChange(TravelCertificateTravellersInputUiState.Created(newUrl))
      assertThat(coInsuredUseCase.contractIds).containsExactly()
    }
  }

  @Test
  fun `starting from content keeps the selection and the failure`() = runTest {
    // An id the counter of a new run would not hand out again, so only the restored mapping keeps Anna ticked.
    val restoredAnna = annaItem.copy(id = CoInsuredId("generated-7"), isIncluded = true)
    val earlier = content(coInsured = listOf(restoredAnna, zedItem), isMemberIncluded = false, createFailed = true)
    presenter.test(earlier) {
      assertThat(awaitItem()).isEqualTo(earlier)
      assertThat(coInsuredUseCase.contractIds).containsExactly("contractId")
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      testScheduler.advanceUntilIdle()
      expectNoStateChange(earlier)
    }
  }

  @Test
  fun `starting from content that was creating shows the create as failed and keeps the selection`() = runTest {
    val annaIncluded = listOf(annaItem.copy(isIncluded = true), zedItem)
    val earlier = content(coInsured = annaIncluded, isMemberIncluded = false, isCreating = true)
    presenter.test(earlier) {
      val restored = content(coInsured = annaIncluded, isMemberIncluded = false, createFailed = true)
      assertThat(awaitItem()).isEqualTo(restored)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      testScheduler.advanceUntilIdle()
      expectNoStateChange(restored)
      assertThat(createTransport.inputs).containsExactly()

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(earlier)
      assertThat(createTransport.inputs).containsExactly(
        createInput(isMemberIncluded = false, coInsured = listOf(annaInput)),
      )
    }
  }
}

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
import com.hedvig.android.feature.travelcertificate.TestBackstack
import com.hedvig.android.feature.travelcertificate.data.CoInsuredData
import com.hedvig.android.feature.travelcertificate.data.CoInsuredDataWithMember
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.expectNoStateChange
import com.hedvig.android.feature.travelcertificate.navigation.ShowCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDateInputKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateTravellersInputKey
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.type.TravelCertificateCreateCoInsured
import octopus.type.TravelCertificateCreateInput
import org.junit.Ignore
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
  private val dateInputKey = TravelCertificateDateInputKey(null)
  private val travellersKey = TravelCertificateTravellersInputKey(primaryInput)
  private val backstack = TestBackstack(TravelCertificateKey, dateInputKey, travellersKey)
  private val coInsuredUseCase = FakeGetCoInsuredForContractUseCase()
  private val createTransport = FakeCreateTravelCertificateTransport()
  private val presenter = TravelCertificateTravellersInputPresenter(
    primaryInput = primaryInput,
    createTravelCertificateUseCase = createTransport.createUseCase(),
    getCoInsuredForContractUseCase = coInsuredUseCase,
    backstack = backstack,
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
    id = CoInsuredId("AnnaBergnull2015-03-01"),
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
    isButtonLoading: Boolean = false,
  ) = TravelCertificateTravellersInputUiState.Success(
    coInsuredHasMissingInfo = coInsuredHasMissingInfo,
    coInsuredList = coInsured,
    memberFullName = "Member Name",
    isMemberIncluded = isMemberIncluded,
    isButtonLoading = isButtonLoading,
  )

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
      assertThat((state as TravelCertificateTravellersInputUiState.Success).hasAtLeastOneTraveler).isTrue()
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
  fun `toggling the member and each co-insured, and when there is at least one traveller`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())

      sendEvent(TravelCertificateTravellersInputEvent.ChangeMemberChecked)
      val nobody = awaitItem() as TravelCertificateTravellersInputUiState.Success
      assertThat(nobody).isEqualTo(content(isMemberIncluded = false))
      assertThat(nobody.hasAtLeastOneTraveler).isFalse()

      sendEvent(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(annaItem))
      val annaOnly = awaitItem() as TravelCertificateTravellersInputUiState.Success
      assertThat(annaOnly).isEqualTo(
        content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem), isMemberIncluded = false),
      )
      assertThat(annaOnly.hasAtLeastOneTraveler).isTrue()

      sendEvent(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(zedItem))
      assertThat(awaitItem()).isEqualTo(
        content(
          coInsured = listOf(annaItem.copy(isIncluded = true), zedItem.copy(isIncluded = true)),
          isMemberIncluded = false,
        ),
      )

      sendEvent(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(annaItem.copy(isIncluded = true)))
      assertThat(awaitItem()).isEqualTo(
        content(coInsured = listOf(annaItem, zedItem.copy(isIncluded = true)), isMemberIncluded = false),
      )
      sendEvent(TravelCertificateTravellersInputEvent.ChangeMemberChecked)
      assertThat(awaitItem()).isEqualTo(
        content(coInsured = listOf(annaItem, zedItem.copy(isIncluded = true)), isMemberIncluded = true),
      )
    }
  }

  @Test
  fun `a co-insured toggle sets the opposite of the item it carries, so a stale item does not flip it back`() =
    runTest {
      presenter.test(TravelCertificateTravellersInputUiState.Loading) {
        assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
        coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
        assertThat(awaitItem()).isEqualTo(content())

        sendEvent(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(annaItem))
        assertThat(awaitItem()).isEqualTo(content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem)))
        sendEvent(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(annaItem))
        testScheduler.advanceUntilIdle()
        expectNoStateChange(content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem)))
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
  fun `create sends the selection and shows the certificate in place of the flow`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())
      sendEvent(TravelCertificateTravellersInputEvent.ChangeCoInsuredChecked(annaItem))
      skipItems(1)

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(
        content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem), isButtonLoading = true),
      )
      assertThat(createTransport.inputs).containsExactly(
        createInput(
          coInsured = listOf(
            TravelCertificateCreateCoInsured(
              fullName = "Anna Berg",
              ssn = Optional.present(null),
              dateOfBirth = Optional.present(LocalDate(2015, 3, 1)),
            ),
          ),
        ),
      )

      createTransport.responses.trySend("https://example.com/new.pdf")
      testScheduler.advanceUntilIdle()
      assertThat(backstack.entries).containsExactly(
        TravelCertificateKey,
        ShowCertificateKey(TravelCertificateUrl("https://example.com/new.pdf")),
      )
      expectNoStateChange(
        content(coInsured = listOf(annaItem.copy(isIncluded = true), zedItem), isButtonLoading = true),
      )
    }
  }

  @Test
  fun `create is not gated on having a traveller`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())
      sendEvent(TravelCertificateTravellersInputEvent.ChangeMemberChecked)
      skipItems(1)

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isMemberIncluded = false, isButtonLoading = true))
      assertThat(createTransport.inputs).containsExactly(
        createInput(isMemberIncluded = false, coInsured = emptyList()),
      )
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
  fun `a second create while creating cancels the first request and sends another`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isButtonLoading = true))
      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      testScheduler.advanceUntilIdle()
      val memberOnly = createInput(coInsured = emptyList())
      assertThat(createTransport.inputs).containsExactly(memberOnly, memberOnly)

      createTransport.responses.trySend("https://example.com/new.pdf")
      testScheduler.advanceUntilIdle()
      assertThat(backstack.entries).containsExactly(
        TravelCertificateKey,
        ShowCertificateKey(TravelCertificateUrl("https://example.com/new.pdf")),
      )
      expectNoStateChange(content(isButtonLoading = true))
    }
  }

  @Test
  fun `a co-insured emission while creating ends the button loading`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isButtonLoading = true))
      coInsuredUseCase.responses.tryEmit(data(anna, zed).right())
      assertThat(awaitItem()).isEqualTo(content(isButtonLoading = false))
    }
  }

  @Test
  fun `today a failed create leaves the button loading and shows nothing`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isButtonLoading = true))
      createTransport.responses.trySend(null)
      testScheduler.advanceUntilIdle()
      expectNoStateChange(content(isButtonLoading = true))
      assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey, travellersKey)
    }
  }

  @Ignore("Fails today: the failed create result is discarded and the button keeps loading. T4 fixes it.")
  @Test
  fun `a failed create ends the loading and shows the failure`() = runTest {
    presenter.test(TravelCertificateTravellersInputUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Loading)
      coInsuredUseCase.responses.tryEmit(data(zed, anna).right())
      assertThat(awaitItem()).isEqualTo(content())

      sendEvent(TravelCertificateTravellersInputEvent.GenerateTravelCertificate)
      assertThat(awaitItem()).isEqualTo(content(isButtonLoading = true))
      createTransport.responses.trySend(null)
      assertThat(awaitItem()).isEqualTo(TravelCertificateTravellersInputUiState.Failure)
      assertThat(backstack.entries).containsExactly(TravelCertificateKey, dateInputKey, travellersKey)
    }
  }
}

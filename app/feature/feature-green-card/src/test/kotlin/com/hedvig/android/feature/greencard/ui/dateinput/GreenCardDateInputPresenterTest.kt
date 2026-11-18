package com.hedvig.android.feature.greencard.ui.dateinput

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.mutableStateListOf
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.greencard.data.CreateGreenCardUseCase
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
import com.hedvig.android.feature.greencard.data.GreenCardAddress
import com.hedvig.android.feature.greencard.data.GreenCardCoDriver
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import com.hedvig.android.feature.greencard.data.GreenCardSpecifications
import com.hedvig.android.feature.greencard.data.GreenCardUrl
import com.hedvig.android.feature.greencard.navigation.GreenCardCreatedKey
import com.hedvig.android.feature.greencard.navigation.GreenCardDateInputKey
import com.hedvig.android.feature.greencard.navigation.GreenCardKey
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Rule
import org.junit.Test

internal class GreenCardDateInputPresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  private val specification = GreenCardContractSpecification(
    contractId = CONTRACT_ID,
    registrationNumber = "ABC 123",
    minStartDate = LocalDate(2026, 12, 1),
    maxEndDate = LocalDate(2027, 2, 28),
  )

  @Test
  fun `departure date defaults to the earliest allowed date and return date starts empty`() = runTest {
    val presenter = presenter()
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      assertThat(form.startDatePickerState.selectedDate()).isEqualTo(specification.minStartDate)
      assertThat(form.endDatePickerState.selectedDate()).isNull()
      assertThat(form.mailingAddress.toAddress()).isEqualTo(GreenCardAddress("", "", "", ""))
      assertThat(form.coDrivers).isEmpty()
    }
  }

  @Test
  fun `submitting without a return date shows an error and does not create`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    val presenter = presenter(createUseCase = createUseCase)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      (awaitItem() as GreenCardDateInputUiState.Success).form.mailingAddress.fillWith(ADDRESS)
      sendEvent(GreenCardDateInputEvent.Submit)
      assertThat((awaitItem() as GreenCardDateInputUiState.Success).dateError).isEqualTo(DateError.MissingEndDate)
      assertThat(createUseCase.requests).isEmpty()
    }
  }

  @Test
  fun `a return date before the departure date shows an error`() = runTest {
    val presenter = presenter()
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      form.startDatePickerState.selectedDateMillis = LocalDate(2027, 1, 10).toUtcMillis()
      form.endDatePickerState.selectedDateMillis = LocalDate(2027, 1, 5).toUtcMillis()
      sendEvent(GreenCardDateInputEvent.Submit)
      assertThat((awaitItem() as GreenCardDateInputUiState.Success).dateError).isEqualTo(DateError.EndBeforeStart)
    }
  }

  @Test
  fun `a missing mailing address shows field errors and does not create`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    val presenter = presenter(createUseCase = createUseCase)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      form.endDatePickerState.selectedDateMillis = LocalDate(2027, 1, 5).toUtcMillis()
      sendEvent(GreenCardDateInputEvent.Submit)
      val state = awaitItem() as GreenCardDateInputUiState.Success
      assertThat(state.showMissingFieldErrors).isTrue()
      assertThat(state.dateError).isNull()
      assertThat(createUseCase.requests).isEmpty()
    }
  }

  @Test
  fun `an incomplete co-driver shows field errors and does not create`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    val presenter = presenter(createUseCase = createUseCase)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      form.endDatePickerState.selectedDateMillis = LocalDate(2027, 1, 5).toUtcMillis()
      form.mailingAddress.fillWith(ADDRESS)
      sendEvent(GreenCardDateInputEvent.AddCoDriver)
      val coDriver = (awaitItem() as GreenCardDateInputUiState.Success).form.coDrivers.single()
      coDriver.name.setTextAndPlaceCursorAtEnd("Anna Andersson")
      sendEvent(GreenCardDateInputEvent.Submit)
      assertThat((awaitItem() as GreenCardDateInputUiState.Success).showMissingFieldErrors).isTrue()
      assertThat(createUseCase.requests).isEmpty()
    }
  }

  @Test
  fun `removing a co-driver keeps the others`() = runTest {
    val presenter = presenter()
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(2)
      sendEvent(GreenCardDateInputEvent.AddCoDriver)
      skipItems(1)
      sendEvent(GreenCardDateInputEvent.AddCoDriver)
      val (first, second) = (awaitItem() as GreenCardDateInputUiState.Success).form.coDrivers
      sendEvent(GreenCardDateInputEvent.RemoveCoDriver(first))
      assertThat((awaitItem() as GreenCardDateInputUiState.Success).form.coDrivers).containsExactly(second)
    }
  }

  @Test
  fun `valid input creates the green card with trimmed address and co-drivers and shows it`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    val backstack = TestBackstack().apply { entries.addAll(listOf(GreenCardKey, GreenCardDateInputKey(CONTRACT_ID))) }
    val presenter = presenter(createUseCase = createUseCase, backstack = backstack)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      form.endDatePickerState.selectedDateMillis = LocalDate(2027, 1, 5).toUtcMillis()
      form.mailingAddress.fillWith(ADDRESS.copy(street = " ${ADDRESS.street} "))
      sendEvent(GreenCardDateInputEvent.AddCoDriver)
      val coDriver = (awaitItem() as GreenCardDateInputUiState.Success).form.coDrivers.single()
      coDriver.name.setTextAndPlaceCursorAtEnd("${CO_DRIVER.name} ")
      coDriver.address.fillWith(CO_DRIVER.address)
      sendEvent(GreenCardDateInputEvent.Submit)
      cancelAndIgnoreRemainingEvents()
    }
    assertThat(createUseCase.requests).containsExactly(
      CreateRequest(CONTRACT_ID, specification.minStartDate, LocalDate(2027, 1, 5), ADDRESS, listOf(CO_DRIVER)),
    )
    assertThat(backstack.entries.toList()).containsExactly(GreenCardKey, GreenCardCreatedKey(GreenCardUrl(PDF_URL)))
  }

  @Test
  fun `a contract without a specification shows the failure state`() = runTest {
    val presenter = presenter(contractId = "unknown")
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isEqualTo(GreenCardDateInputUiState.Failure)
    }
  }

  private fun presenter(
    contractId: String = CONTRACT_ID,
    specifications: Either<ErrorMessage, GreenCardSpecifications> =
      GreenCardSpecifications(listOf(specification)).right(),
    createUseCase: FakeCreateGreenCardUseCase = FakeCreateGreenCardUseCase(),
    backstack: Backstack = TestBackstack(),
  ) = GreenCardDateInputPresenter(
    contractId = contractId,
    getGreenCardSpecificationsUseCase = object : GetGreenCardSpecificationsUseCase {
      override suspend fun invoke() = specifications
    },
    createGreenCardUseCase = createUseCase,
    languageService = FakeLanguageService(),
    backstack = backstack,
  )

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private data class CreateRequest(
    val contractId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val mailingAddress: GreenCardAddress,
    val coDrivers: List<GreenCardCoDriver>,
  )

  private class FakeCreateGreenCardUseCase(
    private val result: Either<ErrorMessage, GreenCardUrl> = GreenCardUrl(PDF_URL).right(),
  ) : CreateGreenCardUseCase {
    val requests = mutableListOf<CreateRequest>()

    override suspend fun invoke(
      contractId: String,
      travelStartDate: LocalDate,
      travelEndDate: LocalDate,
      mailingAddress: GreenCardAddress,
      coDrivers: List<GreenCardCoDriver>,
    ): Either<ErrorMessage, GreenCardUrl> {
      requests += CreateRequest(contractId, travelStartDate, travelEndDate, mailingAddress, coDrivers)
      return result
    }
  }
}

private fun AddressFieldsState.fillWith(address: GreenCardAddress) {
  street.setTextAndPlaceCursorAtEnd(address.street)
  postalCode.setTextAndPlaceCursorAtEnd(address.postalCode)
  city.setTextAndPlaceCursorAtEnd(address.city)
  country.setTextAndPlaceCursorAtEnd(address.country)
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private const val CONTRACT_ID = "contract-id"
private val ADDRESS = GreenCardAddress("Storgatan 1", "111 22", "Stockholm", "Sverige")
private val CO_DRIVER = GreenCardCoDriver(
  "Anna Andersson",
  GreenCardAddress("Lillgatan 2", "222 33", "Uppsala", "Sverige"),
)
private const val PDF_URL = "https://example.com/green-card.pdf"

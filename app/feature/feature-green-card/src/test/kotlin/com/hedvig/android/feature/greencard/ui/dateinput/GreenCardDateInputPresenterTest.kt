package com.hedvig.android.feature.greencard.ui.dateinput

import androidx.compose.runtime.mutableStateListOf
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.greencard.data.CreateGreenCardUseCase
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
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
      assertThat(form.email).isEqualTo(EMAIL)
    }
  }

  @Test
  fun `submitting without a return date shows an error and does not create`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    val presenter = presenter(createUseCase = createUseCase)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      assertThat(awaitItem()).isInstanceOf(GreenCardDateInputUiState.Success::class)
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
  fun `an invalid email shows an error`() = runTest {
    val presenter = presenter()
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      form.endDatePickerState.selectedDateMillis = LocalDate(2027, 1, 5).toUtcMillis()
      sendEvent(GreenCardDateInputEvent.ChangeEmail("not an email"))
      skipItems(1)
      sendEvent(GreenCardDateInputEvent.Submit)
      assertThat((awaitItem() as GreenCardDateInputUiState.Success).emailError).isEqualTo(EmailError.Invalid)
    }
  }

  @Test
  fun `valid input with a blank email creates the green card without an email and shows it`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    val backstack = TestBackstack().apply { entries.addAll(listOf(GreenCardKey, GreenCardDateInputKey(CONTRACT_ID))) }
    val presenter = presenter(createUseCase = createUseCase, backstack = backstack)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      form.endDatePickerState.selectedDateMillis = LocalDate(2027, 1, 5).toUtcMillis()
      sendEvent(GreenCardDateInputEvent.ChangeEmail("  "))
      sendEvent(GreenCardDateInputEvent.Submit)
      cancelAndIgnoreRemainingEvents()
    }
    assertThat(createUseCase.requests).containsExactly(
      CreateRequest(CONTRACT_ID, specification.minStartDate, LocalDate(2027, 1, 5), null),
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
      GreenCardSpecifications(EMAIL, listOf(specification)).right(),
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
    val email: String?,
  )

  private class FakeCreateGreenCardUseCase(
    private val result: Either<ErrorMessage, GreenCardUrl> = GreenCardUrl(PDF_URL).right(),
  ) : CreateGreenCardUseCase {
    val requests = mutableListOf<CreateRequest>()

    override suspend fun invoke(
      contractId: String,
      travelStartDate: LocalDate,
      travelEndDate: LocalDate,
      email: String?,
    ): Either<ErrorMessage, GreenCardUrl> {
      requests += CreateRequest(contractId, travelStartDate, travelEndDate, email)
      return result
    }
  }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private const val CONTRACT_ID = "contract-id"
private const val EMAIL = "member@example.com"
private const val PDF_URL = "https://example.com/green-card.pdf"

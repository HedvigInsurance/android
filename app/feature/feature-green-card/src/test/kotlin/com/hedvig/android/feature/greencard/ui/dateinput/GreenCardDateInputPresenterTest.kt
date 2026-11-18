package com.hedvig.android.feature.greencard.ui.dateinput

import androidx.compose.runtime.mutableStateListOf
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import com.hedvig.android.feature.greencard.data.GreenCardSpecifications
import com.hedvig.android.feature.greencard.navigation.GreenCardAddressInputKey
import com.hedvig.android.feature.greencard.navigation.GreenCardDateInputKey
import com.hedvig.android.feature.greencard.navigation.GreenCardTripInput
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
    }
  }

  @Test
  fun `submitting without a return date shows an error and stays on the screen`() = runTest {
    val backstack = TestBackstack(GreenCardDateInputKey(CONTRACT_ID))
    val presenter = presenter(backstack = backstack)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(2)
      sendEvent(GreenCardDateInputEvent.Submit)
      assertThat((awaitItem() as GreenCardDateInputUiState.Success).dateError).isEqualTo(DateError.MissingEndDate)
    }
    assertThat(backstack.entries.toList()).containsExactly(GreenCardDateInputKey(CONTRACT_ID))
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
  fun `valid dates continue to the address step with the trip`() = runTest {
    val backstack = TestBackstack(GreenCardDateInputKey(CONTRACT_ID))
    val presenter = presenter(backstack = backstack)
    presenter.test(GreenCardDateInputUiState.Loading) {
      skipItems(1)
      val form = (awaitItem() as GreenCardDateInputUiState.Success).form
      form.endDatePickerState.selectedDateMillis = LocalDate(2027, 1, 5).toUtcMillis()
      sendEvent(GreenCardDateInputEvent.Submit)
      cancelAndIgnoreRemainingEvents()
    }
    assertThat(backstack.entries.toList()).containsExactly(
      GreenCardDateInputKey(CONTRACT_ID),
      GreenCardAddressInputKey(
        GreenCardTripInput(CONTRACT_ID, "ABC 123", specification.minStartDate, LocalDate(2027, 1, 5)),
      ),
    )
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
    backstack: Backstack = TestBackstack(),
  ) = GreenCardDateInputPresenter(
    contractId = contractId,
    getGreenCardSpecificationsUseCase = object : GetGreenCardSpecificationsUseCase {
      override suspend fun invoke() = specifications
    },
    languageService = FakeLanguageService(),
    backstack = backstack,
  )

  private class TestBackstack(vararg initial: HedvigNavKey) : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf(*initial)
  }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private const val CONTRACT_ID = "contract-id"

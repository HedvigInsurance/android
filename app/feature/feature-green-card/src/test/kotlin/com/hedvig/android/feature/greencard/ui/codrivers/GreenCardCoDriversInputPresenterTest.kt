package com.hedvig.android.feature.greencard.ui.codrivers

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.mutableStateListOf
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.hedvig.android.feature.greencard.data.CreateGreenCardError
import com.hedvig.android.feature.greencard.data.CreateGreenCardUseCase
import com.hedvig.android.feature.greencard.data.GreenCardAddress
import com.hedvig.android.feature.greencard.data.GreenCardCoDriver
import com.hedvig.android.feature.greencard.data.GreenCardUrl
import com.hedvig.android.feature.greencard.navigation.GreenCardCoDriversInputKey
import com.hedvig.android.feature.greencard.navigation.GreenCardCreatedKey
import com.hedvig.android.feature.greencard.navigation.GreenCardKey
import com.hedvig.android.feature.greencard.navigation.GreenCardTripInput
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

internal class GreenCardCoDriversInputPresenterTest {
  @get:Rule
  val testLogcatRule = TestLogcatLoggingRule()

  @Test
  fun `without co-drivers it creates the green card and shows it`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    val backstack = TestBackstack(GreenCardKey, INPUT)
    presenter(createUseCase, backstack).test(INITIAL_STATE) {
      skipItems(1)
      sendEvent(GreenCardCoDriversInputEvent.Submit)
      cancelAndIgnoreRemainingEvents()
    }
    assertThat(createUseCase.requests).containsExactly(emptyList<GreenCardCoDriver>())
    assertThat(backstack.entries.toList()).containsExactly(GreenCardKey, GreenCardCreatedKey(GreenCardUrl(PDF_URL)))
  }

  @Test
  fun `an incomplete co-driver shows field errors and does not create`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    presenter(createUseCase).test(INITIAL_STATE) {
      skipItems(1)
      sendEvent(GreenCardCoDriversInputEvent.AddCoDriver)
      awaitItem().coDrivers.single().name.setTextAndPlaceCursorAtEnd("Anna Andersson")
      sendEvent(GreenCardCoDriversInputEvent.Submit)
      assertThat(awaitItem().showMissingFieldErrors).isTrue()
      assertThat(createUseCase.requests).isEmpty()
    }
  }

  @Test
  fun `removing a co-driver keeps the others`() = runTest {
    presenter().test(INITIAL_STATE) {
      skipItems(1)
      sendEvent(GreenCardCoDriversInputEvent.AddCoDriver)
      skipItems(1)
      sendEvent(GreenCardCoDriversInputEvent.AddCoDriver)
      val (first, second) = awaitItem().coDrivers
      sendEvent(GreenCardCoDriversInputEvent.RemoveCoDriver(first))
      assertThat(awaitItem().coDrivers).containsExactly(second)
    }
  }

  @Test
  fun `co-drivers are sent trimmed`() = runTest {
    val createUseCase = FakeCreateGreenCardUseCase()
    presenter(createUseCase).test(INITIAL_STATE) {
      skipItems(1)
      sendEvent(GreenCardCoDriversInputEvent.AddCoDriver)
      awaitItem().coDrivers.single().fillWith(CO_DRIVER.copy(name = " ${CO_DRIVER.name} "))
      sendEvent(GreenCardCoDriversInputEvent.Submit)
      cancelAndIgnoreRemainingEvents()
    }
    assertThat(createUseCase.requests).containsExactly(listOf(CO_DRIVER))
  }

  @Test
  fun `a rejected create shows the backend message and stays on the screen`() = runTest {
    val backstack = TestBackstack(GreenCardKey, INPUT)
    val userError = CreateGreenCardError.UserError("The trip is too long")
    val createUseCase = FakeCreateGreenCardUseCase(userError.left())
    presenter(createUseCase, backstack).test(INITIAL_STATE) {
      skipItems(1)
      sendEvent(GreenCardCoDriversInputEvent.Submit)
      skipItems(1)
      assertThat(awaitItem().createError).isEqualTo(userError)
    }
    assertThat(backstack.entries.toList()).containsExactly(GreenCardKey, INPUT)
  }

  private fun presenter(
    createUseCase: FakeCreateGreenCardUseCase = FakeCreateGreenCardUseCase(),
    backstack: Backstack = TestBackstack(),
  ) = GreenCardCoDriversInputPresenter(INPUT, createUseCase, backstack)

  private class TestBackstack(vararg initial: HedvigNavKey) : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf(*initial)
  }

  private class FakeCreateGreenCardUseCase(
    private val result: Either<CreateGreenCardError, GreenCardUrl> = GreenCardUrl(PDF_URL).right(),
  ) : CreateGreenCardUseCase {
    val requests = mutableListOf<List<GreenCardCoDriver>>()

    override suspend fun invoke(
      contractId: String,
      travelStartDate: LocalDate,
      travelEndDate: LocalDate,
      mailingAddress: GreenCardAddress,
      coDrivers: List<GreenCardCoDriver>,
    ): Either<CreateGreenCardError, GreenCardUrl> {
      requests += coDrivers
      return result
    }
  }
}

private fun CoDriverFieldsState.fillWith(coDriver: GreenCardCoDriver) {
  name.setTextAndPlaceCursorAtEnd(coDriver.name)
  address.street.setTextAndPlaceCursorAtEnd(coDriver.address.street)
  address.postalCode.setTextAndPlaceCursorAtEnd(coDriver.address.postalCode)
  address.city.setTextAndPlaceCursorAtEnd(coDriver.address.city)
  address.country.setTextAndPlaceCursorAtEnd(coDriver.address.country)
}

private val INITIAL_STATE = GreenCardCoDriversInputUiState(
  coDrivers = emptyList(),
  showMissingFieldErrors = false,
  isCreating = false,
  createError = null,
)
private val INPUT = GreenCardCoDriversInputKey(
  tripInput = GreenCardTripInput("contract-id", "ABC 123", LocalDate(2027, 1, 1), LocalDate(2027, 1, 5)),
  mailingAddress = GreenCardAddress("Storgatan 1", "111 22", "Stockholm", "Sverige"),
)
private val CO_DRIVER = GreenCardCoDriver(
  "Anna Andersson",
  GreenCardAddress("Lillgatan 2", "222 33", "Uppsala", "Sverige"),
)
private const val PDF_URL = "https://example.com/green-card.pdf"

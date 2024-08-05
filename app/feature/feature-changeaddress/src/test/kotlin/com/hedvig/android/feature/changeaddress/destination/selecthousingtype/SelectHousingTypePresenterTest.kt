package com.hedvig.android.feature.changeaddress.destination.selecthousingtype

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.HousingType.APARTMENT_OWN
import com.hedvig.android.feature.changeaddress.data.HousingType.APARTMENT_RENT
import com.hedvig.android.feature.changeaddress.data.HousingType.VILLA
import com.hedvig.android.feature.changeaddress.destination.FakeChangeAddressRepository
import com.hedvig.android.feature.changeaddress.destination.fakeMoveIntent
import com.hedvig.android.feature.changeaddress.destination.fakeSelectHousingTypeParametersForApartment
import com.hedvig.android.feature.changeaddress.destination.fakeSelectHousingTypeParametersForVilla
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import hedvig.resources.R
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SelectHousingTypePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when error is received from BE the error dialog is displayed and when the dialog is dismissed the uiState error is nullified`() =
    runTest {
      val repository = FakeChangeAddressRepository()
      val presenter = SelectHousingTypePresenter(repository)

      presenter.test(SelectHousingTypeUiState()) {
        sendEvent(SelectHousingTypeEvent.SelectHousingType(VILLA))
        sendEvent(SelectHousingTypeEvent.SubmitHousingType)
        skipItems(3)
        repository.createMoveResponseTurbine.add(ErrorMessage("bad message").left())
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>().prop(SelectHousingTypeUiState::errorMessage)
          .isEqualTo("bad message")
        sendEvent(SelectHousingTypeEvent.DismissErrorDialog)
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>().prop(SelectHousingTypeUiState::errorMessage)
          .isNull()
      }
    }

  @Test
  fun `when try to continue without selecting housingType show error dialog and when the dialog is dismissed the uiState error is nullified`() =
    runTest {
      val repository = FakeChangeAddressRepository()
      val presenter = SelectHousingTypePresenter(repository)

      presenter.test(SelectHousingTypeUiState()) {
        skipItems(1)
        sendEvent(SelectHousingTypeEvent.SubmitHousingType)
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>().prop(SelectHousingTypeUiState::errorMessageRes)
          .isEqualTo(R.string.CHANGE_ADDRESS_HOUSING_TYPE_ERROR)
        sendEvent(SelectHousingTypeEvent.DismissHousingTypeErrorDialog)
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>().prop(SelectHousingTypeUiState::errorMessageRes)
          .isNull()
      }
    }

  @Test
  fun `when moveIntent is successfully received from BE the correct navigation parameters go to uiState`() = runTest {
    val repository = FakeChangeAddressRepository()
    val presenter = SelectHousingTypePresenter(repository)

    presenter.test(SelectHousingTypeUiState()) {
      sendEvent(SelectHousingTypeEvent.SelectHousingType(VILLA))
      sendEvent(SelectHousingTypeEvent.SubmitHousingType)
      skipItems(3)
      repository.createMoveResponseTurbine.add(fakeMoveIntent.right())
      assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>()
        .prop(SelectHousingTypeUiState::navigationParameters)
        .isEqualTo(fakeSelectHousingTypeParametersForVilla)
      sendEvent(SelectHousingTypeEvent.SelectHousingType(APARTMENT_OWN))
      sendEvent(SelectHousingTypeEvent.SubmitHousingType)
      skipItems(2)
      repository.createMoveResponseTurbine.add(fakeMoveIntent.right())
      assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>()
        .prop(SelectHousingTypeUiState::navigationParameters)
        .isEqualTo(fakeSelectHousingTypeParametersForApartment)
    }
  }

  @Test
  fun `when navigation happens the navigation parameters are cleared to avoid the loop`() = runTest {
    val repository = FakeChangeAddressRepository()
    val presenter = SelectHousingTypePresenter(repository)

    presenter.test(SelectHousingTypeUiState()) {
      sendEvent(SelectHousingTypeEvent.SelectHousingType(VILLA))
      sendEvent(SelectHousingTypeEvent.SubmitHousingType)
      skipItems(3)
      repository.createMoveResponseTurbine.add(fakeMoveIntent.right())
      assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>()
        .prop(SelectHousingTypeUiState::navigationParameters)
        .isNotNull()
      sendEvent(SelectHousingTypeEvent.ClearNavigationParameters)
      assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>()
        .prop(SelectHousingTypeUiState::navigationParameters)
        .isNull()
    }
  }

  @Test
  fun `when different housing types are selected it shows in uiState and the last selected one is sent in the request for BE`() =
    runTest {
      val repository = FakeChangeAddressRepository()
      val presenter = SelectHousingTypePresenter(repository)

      presenter.test(SelectHousingTypeUiState()) {
        skipItems(1)
        sendEvent(SelectHousingTypeEvent.SelectHousingType(VILLA))
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>().prop(SelectHousingTypeUiState::housingType)
          .isEqualTo(ValidatedInput(VILLA))
        sendEvent(SelectHousingTypeEvent.SelectHousingType(APARTMENT_RENT))
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>().prop(SelectHousingTypeUiState::housingType)
          .isEqualTo(ValidatedInput(APARTMENT_RENT))
        sendEvent(SelectHousingTypeEvent.SelectHousingType(APARTMENT_OWN))
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>().prop(SelectHousingTypeUiState::housingType)
          .isEqualTo(ValidatedInput(APARTMENT_OWN))
        sendEvent(SelectHousingTypeEvent.SubmitHousingType)
        skipItems(1)
        repository.createMoveResponseTurbine.add(fakeMoveIntent.right())
        assertThat(awaitItem()).isInstanceOf<SelectHousingTypeUiState>()
          .prop(SelectHousingTypeUiState::navigationParameters)
          .isEqualTo(fakeSelectHousingTypeParametersForApartment)
      }
    }
}

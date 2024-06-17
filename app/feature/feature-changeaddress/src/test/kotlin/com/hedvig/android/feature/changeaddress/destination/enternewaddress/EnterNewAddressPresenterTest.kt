package com.hedvig.android.feature.changeaddress.destination.enternewaddress

import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.destination.fakeSelectHousingTypeParametersForApartment
import com.hedvig.android.feature.changeaddress.destination.fakeSelectHousingTypeParametersForVilla
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.feature.changeaddress.navigation.NewAddressParameters
import com.hedvig.android.language.test.FakeLanguageService
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import java.util.Locale
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class EnterNewAddressPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `uiState first shows suggested number of coInsured then changes it if user select another number of coInsured`() =
    runTest {
      val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
      val presenter = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)

      presenter.test(
        EnterNewAddressUiState(
          datePickerUiState = DatePickerUiState(
            locale = languageService.getLocale(),
            initiallySelectedDate = null,
            minDate = fakeSelectHousingTypeParametersForApartment.minDate,
            maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
          ),
          maxNumberCoInsured = fakeSelectHousingTypeParametersForApartment.maxNumberCoInsured,
          maxSquareMeters = fakeSelectHousingTypeParametersForApartment.maxSquareMeters,
          numberInsured = ValidatedInput(fakeSelectHousingTypeParametersForApartment.suggestedNumberInsured),
        ),
      ) {
        assertk.assertThat(awaitItem()).isInstanceOf<EnterNewAddressUiState>()
          .prop(EnterNewAddressUiState::numberInsured)
          .isEqualTo(ValidatedInput(fakeSelectHousingTypeParametersForApartment.suggestedNumberInsured))
        sendEvent(EnterNewAddressEvent.OnCoInsuredIncreased)
        assertk.assertThat(awaitItem()).isInstanceOf<EnterNewAddressUiState>()
          .prop(EnterNewAddressUiState::numberInsured)
          .isEqualTo(ValidatedInput("4"))
      }
    }

  @Test
  fun `when the values are changed by the user it shows in the uiState`() = runTest {
    val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
    val presenter = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)
    presenter.test(
      EnterNewAddressUiState(
        datePickerUiState = DatePickerUiState(
          locale = languageService.getLocale(),
          initiallySelectedDate = null,
          minDate = fakeSelectHousingTypeParametersForApartment.minDate,
          maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
        ),
        maxNumberCoInsured = fakeSelectHousingTypeParametersForApartment.maxNumberCoInsured,
        maxSquareMeters = fakeSelectHousingTypeParametersForApartment.maxSquareMeters,
        numberInsured = ValidatedInput(fakeSelectHousingTypeParametersForApartment.suggestedNumberInsured),
      ),
    ) {
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterNewAddressUiState>()
        .apply {
          prop(EnterNewAddressUiState::numberInsured).isEqualTo(
            ValidatedInput(
              fakeSelectHousingTypeParametersForApartment.suggestedNumberInsured,
            ),
          )
          prop(EnterNewAddressUiState::street).isEqualTo(ValidatedInput(null))
          prop(EnterNewAddressUiState::movingDate).isEqualTo(ValidatedInput(null))
          prop(EnterNewAddressUiState::postalCode).isEqualTo(ValidatedInput(null))
          prop(EnterNewAddressUiState::squareMeters).isEqualTo(ValidatedInput(null))
        }
      sendEvent(EnterNewAddressEvent.OnCoInsuredIncreased)
      sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
      sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
      sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
      sendEvent(EnterNewAddressEvent.ChangeSquareMeters("93"))
      skipItems(4)
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterNewAddressUiState>()
        .apply {
          prop(EnterNewAddressUiState::numberInsured).isEqualTo(ValidatedInput("4"))
          prop(EnterNewAddressUiState::street).isEqualTo(ValidatedInput("newStreet"))
          prop(EnterNewAddressUiState::movingDate).isEqualTo(ValidatedInput(LocalDate(2024, 7, 1)))
          prop(EnterNewAddressUiState::postalCode).isEqualTo(ValidatedInput("newPostalCode"))
          prop(EnterNewAddressUiState::squareMeters).isEqualTo(ValidatedInput("93"))
        }
    }
  }

  @Test
  fun `if try to continue when one of the fields is not filled will highlight missing info field and not allow to continue`() =
    runTest {
      val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
      val presenter = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)
      presenter.test(
        EnterNewAddressUiState(
          datePickerUiState = DatePickerUiState(
            locale = languageService.getLocale(),
            initiallySelectedDate = null,
            minDate = fakeSelectHousingTypeParametersForApartment.minDate,
            maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
          ),
          maxNumberCoInsured = fakeSelectHousingTypeParametersForApartment.maxNumberCoInsured,
          maxSquareMeters = fakeSelectHousingTypeParametersForApartment.maxSquareMeters,
          numberInsured = ValidatedInput(fakeSelectHousingTypeParametersForApartment.suggestedNumberInsured),
        ),
      ) {
        sendEvent(EnterNewAddressEvent.OnCoInsuredDecreased)
        sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
        sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
        sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
        skipItems(5)
        sendEvent(EnterNewAddressEvent.ValidateInput)
        assertk.assertThat(awaitItem())
          .isInstanceOf<EnterNewAddressUiState>()
          .apply {
            prop(EnterNewAddressUiState::squareMeters).prop(ValidatedInput<String?>::errorMessageRes).isNotNull()
            prop(EnterNewAddressUiState::navParamsForOfferDestination).isNull()
            prop(EnterNewAddressUiState::navParamsForVillaDestination).isNull()
          }
        sendEvent(EnterNewAddressEvent.ChangeStreet(""))
        sendEvent(EnterNewAddressEvent.ChangeSquareMeters("91"))
        skipItems(2)
        sendEvent(EnterNewAddressEvent.ValidateInput)
        assertk.assertThat(awaitItem())
          .isInstanceOf<EnterNewAddressUiState>()
          .apply {
            prop(EnterNewAddressUiState::squareMeters).prop(ValidatedInput<String?>::errorMessageRes).isNull()
            prop(EnterNewAddressUiState::street).prop(ValidatedInput<String?>::errorMessageRes).isNotNull()
            prop(EnterNewAddressUiState::navParamsForOfferDestination).isNull()
            prop(EnterNewAddressUiState::navParamsForVillaDestination).isNull()
          }
      }
    }

  @Test
  fun `when continue the navDestination depends on housingType from parameters received from previous destination`() =
    runTest {
      val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
      val presenterApartment = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)
      presenterApartment.test(
        EnterNewAddressUiState(
          datePickerUiState = DatePickerUiState(
            locale = languageService.getLocale(),
            initiallySelectedDate = null,
            minDate = fakeSelectHousingTypeParametersForApartment.minDate,
            maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
          ),
          maxNumberCoInsured = fakeSelectHousingTypeParametersForApartment.maxNumberCoInsured,
          maxSquareMeters = fakeSelectHousingTypeParametersForApartment.maxSquareMeters,
          numberInsured = ValidatedInput(fakeSelectHousingTypeParametersForApartment.suggestedNumberInsured),
        ),
      ) {
        sendEvent(EnterNewAddressEvent.OnCoInsuredDecreased)
        sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
        sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
        sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
        sendEvent(EnterNewAddressEvent.ChangeSquareMeters("93"))
        skipItems(6)
        sendEvent(EnterNewAddressEvent.ValidateInput)
        assertk.assertThat(awaitItem())
          .isInstanceOf<EnterNewAddressUiState>()
          .apply {
            prop(EnterNewAddressUiState::navParamsForVillaDestination).isNull()
            prop(EnterNewAddressUiState::navParamsForOfferDestination).isNotNull()
          }
      }
      val presenterVilla = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForVilla)
      presenterVilla.test(
        EnterNewAddressUiState(
          datePickerUiState = DatePickerUiState(
            locale = languageService.getLocale(),
            initiallySelectedDate = null,
            minDate = fakeSelectHousingTypeParametersForVilla.minDate,
            maxDate = fakeSelectHousingTypeParametersForVilla.maxDate,
          ),
          maxNumberCoInsured = fakeSelectHousingTypeParametersForVilla.maxNumberCoInsured,
          maxSquareMeters = fakeSelectHousingTypeParametersForVilla.maxSquareMeters,
          numberInsured = ValidatedInput(fakeSelectHousingTypeParametersForVilla.suggestedNumberInsured),
        ),
      ) {
        sendEvent(EnterNewAddressEvent.OnCoInsuredDecreased)
        sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
        sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
        sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
        sendEvent(EnterNewAddressEvent.ChangeSquareMeters("93"))
        skipItems(6)
        sendEvent(EnterNewAddressEvent.ValidateInput)
        assertk.assertThat(awaitItem())
          .isInstanceOf<EnterNewAddressUiState>()
          .apply {
            prop(EnterNewAddressUiState::navParamsForVillaDestination).isNotNull()
            prop(EnterNewAddressUiState::navParamsForOfferDestination).isNull()
          }
      }
    }

  @Test
  fun `when continue the parameters received from previous destination are passed further to next destination along with new values`() =
    runTest {
      val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
      val presenterApartment = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)
      presenterApartment.test(
        EnterNewAddressUiState(
          datePickerUiState = DatePickerUiState(
            locale = languageService.getLocale(),
            initiallySelectedDate = null,
            minDate = fakeSelectHousingTypeParametersForApartment.minDate,
            maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
          ),
          maxNumberCoInsured = fakeSelectHousingTypeParametersForApartment.maxNumberCoInsured,
          maxSquareMeters = fakeSelectHousingTypeParametersForApartment.maxSquareMeters,
          numberInsured = ValidatedInput("3"),
        ),
      ) {
        sendEvent(EnterNewAddressEvent.OnCoInsuredDecreased)
        sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
        sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
        sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
        sendEvent(EnterNewAddressEvent.ChangeSquareMeters("93"))
        skipItems(6)
        sendEvent(EnterNewAddressEvent.ValidateInput)
        assertk.assertThat(awaitItem())
          .isInstanceOf<EnterNewAddressUiState>()
          .apply {
            prop(EnterNewAddressUiState::navParamsForOfferDestination).isEqualTo(
              MovingParameters(
                selectHousingTypeParameters = fakeSelectHousingTypeParametersForApartment,
                villaOnlyParameters = null,
                newAddressParameters = NewAddressParameters(
                  street = "newStreet",
                  numberInsured = "2",
                  isStudent = false,
                  movingDate = LocalDate(2024, 7, 1),
                  postalCode = "newPostalCode",
                  squareMeters = "93",
                ),
              ),
            )
          }
      }
    }

  @Test
  fun `when trying to lower the number of insured below 1 do not let it`() = runTest {
    val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
    val presenterApartment = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)
    presenterApartment.test(
      EnterNewAddressUiState(
        datePickerUiState = DatePickerUiState(
          locale = languageService.getLocale(),
          initiallySelectedDate = null,
          minDate = fakeSelectHousingTypeParametersForApartment.minDate,
          maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
        ),
        maxNumberCoInsured = fakeSelectHousingTypeParametersForApartment.maxNumberCoInsured,
        maxSquareMeters = fakeSelectHousingTypeParametersForApartment.maxSquareMeters,
        numberInsured = ValidatedInput("2"),
      ),
    ) {
      sendEvent(EnterNewAddressEvent.OnCoInsuredDecreased)
      sendEvent(EnterNewAddressEvent.OnCoInsuredDecreased)
      sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
      sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
      sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
      sendEvent(EnterNewAddressEvent.ChangeSquareMeters("93"))
      skipItems(6)
      sendEvent(EnterNewAddressEvent.ValidateInput)
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterNewAddressUiState>()
        .apply {
          prop(EnterNewAddressUiState::numberInsured).prop(ValidatedInput<String>::input).isEqualTo("1")
        }
    }
  }

  @Test
  fun `when trying to enter number of insured that is more than max do not increase it`() = runTest {
    val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
    val presenterApartment = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)
    presenterApartment.test(
      EnterNewAddressUiState(
        datePickerUiState = DatePickerUiState(
          locale = languageService.getLocale(),
          initiallySelectedDate = null,
          minDate = fakeSelectHousingTypeParametersForApartment.minDate,
          maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
        ),
        maxNumberCoInsured = 5,
        maxSquareMeters = fakeSelectHousingTypeParametersForApartment.maxSquareMeters,
        numberInsured = ValidatedInput("3"),
      ),
    ) {
      sendEvent(EnterNewAddressEvent.OnCoInsuredIncreased)
      sendEvent(EnterNewAddressEvent.OnCoInsuredIncreased)
      sendEvent(EnterNewAddressEvent.OnCoInsuredIncreased)
      sendEvent(EnterNewAddressEvent.OnCoInsuredIncreased)
      sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
      sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
      sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
      sendEvent(EnterNewAddressEvent.ChangeSquareMeters("93"))
      skipItems(8)
      sendEvent(EnterNewAddressEvent.ValidateInput)
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterNewAddressUiState>()
        .apply {
          prop(EnterNewAddressUiState::numberInsured).prop(ValidatedInput<String>::input).isEqualTo("6")
        }
    }
  }

  @Test
  fun `when trying to continue but size is out of bounds show error in the input field`() = runTest {
    val languageService = FakeLanguageService(fixedLocale = Locale.ENGLISH)
    val presenterApartment = EnterNewAddressPresenter(fakeSelectHousingTypeParametersForApartment)
    presenterApartment.test(
      EnterNewAddressUiState(
        datePickerUiState = DatePickerUiState(
          locale = languageService.getLocale(),
          initiallySelectedDate = null,
          minDate = fakeSelectHousingTypeParametersForApartment.minDate,
          maxDate = fakeSelectHousingTypeParametersForApartment.maxDate,
        ),
        maxNumberCoInsured = fakeSelectHousingTypeParametersForApartment.maxNumberCoInsured,
        maxSquareMeters = 90,
        numberInsured = ValidatedInput(fakeSelectHousingTypeParametersForApartment.suggestedNumberInsured),
      ),
    ) {
      sendEvent(EnterNewAddressEvent.OnCoInsuredDecreased)
      sendEvent(EnterNewAddressEvent.ChangeStreet("newStreet"))
      sendEvent(EnterNewAddressEvent.ChangePostalCode("newPostalCode"))
      sendEvent(EnterNewAddressEvent.ChangeMoveDate(LocalDate(2024, 7, 1)))
      sendEvent(EnterNewAddressEvent.ChangeSquareMeters("93"))
      skipItems(6)
      sendEvent(EnterNewAddressEvent.ValidateInput)
      assertk.assertThat(awaitItem())
        .isInstanceOf<EnterNewAddressUiState>()
        .apply {
          prop(EnterNewAddressUiState::squareMeters).prop(ValidatedInput<String?>::errorMessageRes).isNotNull()
        }
    }
  }
}

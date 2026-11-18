package com.hedvig.android.feature.greencard.ui.dateinput

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.api.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.api.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.feature.greencard.data.CreateGreenCardUseCase
import com.hedvig.android.feature.greencard.data.GetGreenCardSpecificationsUseCase
import com.hedvig.android.feature.greencard.data.GreenCardAddress
import com.hedvig.android.feature.greencard.data.GreenCardCoDriver
import com.hedvig.android.feature.greencard.data.GreenCardContractSpecification
import com.hedvig.android.feature.greencard.navigation.GreenCardCreatedKey
import com.hedvig.android.feature.greencard.navigation.GreenCardKey
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class GreenCardDateInputViewModel(
  @Assisted contractId: String,
  getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
  createGreenCardUseCase: CreateGreenCardUseCase,
  languageService: LanguageService,
  backstack: Backstack,
) : MoleculeViewModel<GreenCardDateInputEvent, GreenCardDateInputUiState>(
    initialState = GreenCardDateInputUiState.Loading,
    presenter = GreenCardDateInputPresenter(
      contractId,
      getGreenCardSpecificationsUseCase,
      createGreenCardUseCase,
      languageService,
      backstack,
    ),
  )

internal class GreenCardDateInputPresenter(
  private val contractId: String,
  private val getGreenCardSpecificationsUseCase: GetGreenCardSpecificationsUseCase,
  private val createGreenCardUseCase: CreateGreenCardUseCase,
  private val languageService: LanguageService,
  private val backstack: Backstack,
) : MoleculePresenter<GreenCardDateInputEvent, GreenCardDateInputUiState> {
  @Composable
  override fun MoleculePresenterScope<GreenCardDateInputEvent>.present(
    lastState: GreenCardDateInputUiState,
  ): GreenCardDateInputUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var form by remember {
      mutableStateOf((lastState as? GreenCardDateInputUiState.Success)?.form)
    }
    var loadFailed by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf<DateError?>(null) }
    var showMissingFieldErrors by remember { mutableStateOf(false) }
    var createRequest by remember { mutableStateOf<CreateRequest?>(null) }
    var showCreateError by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        GreenCardDateInputEvent.RetryLoadData -> {
          loadIteration++
        }

        GreenCardDateInputEvent.AddCoDriver -> {
          form = form?.let { it.copy(coDrivers = it.coDrivers + CoDriverFieldsState()) }
        }

        is GreenCardDateInputEvent.RemoveCoDriver -> {
          form = form?.let { it.copy(coDrivers = it.coDrivers - event.coDriver) }
        }

        GreenCardDateInputEvent.ClearDateError -> {
          dateError = null
        }

        GreenCardDateInputEvent.DismissCreateError -> {
          showCreateError = false
        }

        GreenCardDateInputEvent.Submit -> {
          val currentForm = form ?: return@CollectEvents
          val startDate = currentForm.startDatePickerState.selectedDate()
          val endDate = currentForm.endDatePickerState.selectedDate()
          dateError = when {
            startDate == null -> DateError.MissingStartDate
            endDate == null -> DateError.MissingEndDate
            endDate < startDate -> DateError.EndBeforeStart
            else -> null
          }
          val hasMissingFields = !currentForm.mailingAddress.isComplete() ||
            currentForm.coDrivers.any { !it.isComplete() }
          showMissingFieldErrors = hasMissingFields
          if (dateError == null && !hasMissingFields) {
            createRequest = CreateRequest(
              contractId = currentForm.specification.contractId,
              startDate = startDate!!,
              endDate = endDate!!,
              mailingAddress = currentForm.mailingAddress.toAddress(),
              coDrivers = currentForm.coDrivers.map(CoDriverFieldsState::toCoDriver),
            )
          }
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (form != null) return@LaunchedEffect
      loadFailed = false
      getGreenCardSpecificationsUseCase.invoke().fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Could not load green card specifications: $it" }
          loadFailed = true
        },
        ifRight = { specifications ->
          val specification = specifications.contracts.firstOrNull { it.contractId == contractId }
          if (specification == null) {
            logcat(LogPriority.ERROR) { "No green card specification for contract $contractId" }
            loadFailed = true
          } else {
            form = createForm(specification)
          }
        },
      )
    }

    LaunchedEffect(createRequest) {
      val request = createRequest ?: return@LaunchedEffect
      createGreenCardUseCase.invoke(
        contractId = request.contractId,
        travelStartDate = request.startDate,
        travelEndDate = request.endDate,
        mailingAddress = request.mailingAddress,
        coDrivers = request.coDrivers,
      ).fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Creating green card failed: $it" }
          showCreateError = true
        },
        ifRight = { url ->
          backstack.navigateAndPopUpTo<GreenCardKey>(GreenCardCreatedKey(url), inclusive = false)
        },
      )
      createRequest = null
    }

    val currentForm = form
    return when {
      currentForm != null -> GreenCardDateInputUiState.Success(
        form = currentForm,
        dateError = dateError,
        showMissingFieldErrors = showMissingFieldErrors,
        isCreating = createRequest != null,
        showCreateError = showCreateError,
      )

      loadFailed -> GreenCardDateInputUiState.Failure

      else -> GreenCardDateInputUiState.Loading
    }
  }

  private fun createForm(specification: GreenCardContractSpecification): GreenCardForm {
    val dateRange = specification.minStartDate..specification.maxEndDate
    val yearRange = dateRange.start.year..dateRange.endInclusive.year
    val startDatePickerState = HedvigDatePickerState(
      locale = languageService.getLocale(),
      initialSelectedDateMillis = specification.minStartDate.toUtcMillis(),
      initialDisplayedMonthMillis = null,
      yearRange = yearRange,
      selectableDates = selectableDatesIn(yearRange) { it in dateRange },
    )
    val endDatePickerState = HedvigDatePickerState(
      locale = languageService.getLocale(),
      initialSelectedDateMillis = null,
      initialDisplayedMonthMillis = null,
      yearRange = yearRange,
      selectableDates = selectableDatesIn(yearRange) { date ->
        val earliestEndDate = startDatePickerState.selectedDate() ?: specification.minStartDate
        date in earliestEndDate..specification.maxEndDate
      },
    )
    return GreenCardForm(
      specification = specification,
      startDatePickerState = startDatePickerState,
      endDatePickerState = endDatePickerState,
      mailingAddress = AddressFieldsState(),
      coDrivers = emptyList(),
    )
  }
}

private fun selectableDatesIn(yearRange: IntRange, isSelectable: (LocalDate) -> Boolean) =
  object : HedvigSelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = isSelectable(utcTimeMillis.toUtcLocalDate())

    override fun isSelectableYear(year: Int): Boolean = year in yearRange
  }

internal fun HedvigDatePickerState.selectedDate(): LocalDate? = selectedDateMillis?.toUtcLocalDate()

private fun Long.toUtcLocalDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

private fun LocalDate.toUtcMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private data class CreateRequest(
  val contractId: String,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val mailingAddress: GreenCardAddress,
  val coDrivers: List<GreenCardCoDriver>,
)

internal data class GreenCardForm(
  val specification: GreenCardContractSpecification,
  val startDatePickerState: HedvigDatePickerState,
  val endDatePickerState: HedvigDatePickerState,
  val mailingAddress: AddressFieldsState,
  val coDrivers: List<CoDriverFieldsState>,
)

/**
 * Text field states owned by the presenter, so typing edits them in place and keeps the cursor where it is.
 */
internal class AddressFieldsState(
  street: String = "",
  postalCode: String = "",
  city: String = "",
  country: String = "",
) {
  val street = TextFieldState(street)
  val postalCode = TextFieldState(postalCode)
  val city = TextFieldState(city)
  val country = TextFieldState(country)

  fun isComplete(): Boolean = listOf(street, postalCode, city, country).none { it.text.isBlank() }

  fun toAddress(): GreenCardAddress = GreenCardAddress(
    street = street.trimmedText(),
    postalCode = postalCode.trimmedText(),
    city = city.trimmedText(),
    country = country.trimmedText(),
  )
}

internal class CoDriverFieldsState(
  name: String = "",
  val address: AddressFieldsState = AddressFieldsState(),
) {
  val name = TextFieldState(name)

  fun isComplete(): Boolean = name.text.isNotBlank() && address.isComplete()

  fun toCoDriver(): GreenCardCoDriver = GreenCardCoDriver(name.trimmedText(), address.toAddress())
}

private fun TextFieldState.trimmedText(): String = text.trim().toString()

internal enum class DateError { MissingStartDate, MissingEndDate, EndBeforeStart }

internal sealed interface GreenCardDateInputEvent {
  data object RetryLoadData : GreenCardDateInputEvent

  data object AddCoDriver : GreenCardDateInputEvent

  data class RemoveCoDriver(val coDriver: CoDriverFieldsState) : GreenCardDateInputEvent

  data object ClearDateError : GreenCardDateInputEvent

  data object DismissCreateError : GreenCardDateInputEvent

  data object Submit : GreenCardDateInputEvent
}

internal sealed interface GreenCardDateInputUiState {
  data object Loading : GreenCardDateInputUiState

  data object Failure : GreenCardDateInputUiState

  data class Success(
    val form: GreenCardForm,
    val dateError: DateError?,
    val showMissingFieldErrors: Boolean,
    val isCreating: Boolean,
    val showCreateError: Boolean,
  ) : GreenCardDateInputUiState
}

# In-app House (Villa) Purchase + Home Picker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `SE_HOUSE` (villa) form as a sibling composable to `VacationHomeFormDestination` inside `feature-purchase-house`, and a modal "Home" picker dialog that disambiguates the generic `hemforsakring` / `home-insurance` cross-sell URL into Hyresrätt, Bostadsrätt, or Villa.

**Architecture:** Reuses the `feature-purchase-house` module skeleton from the vacation home PR. House form uses the same Apollo operations (`HouseShopSessionCreate`, `HousePriceIntentCreate`, `HousePriceIntentDataUpdate`, `HousePriceIntentConfirm`, `HouseMemberContactInfo`, `HouseProductOfferFragment`) — only the form-data keys differ. Nav graph `Form` destination branches on `productName` to pick which form composable to render. The picker dialog lives in `feature-insurances` `InsuranceGraph.kt`, with state held local to the navdestination block.

**Tech Stack:** Kotlin, Jetpack Compose, Apollo GraphQL, Molecule (MVI), Koin DI, Arrow (Either).

**Base branch:** `feat/in-app-vacation-home-purchase` (this PR stacks on the vacation home PR).

---

### Task 0: Verify base branch

**Files:** none

- [ ] **Step 1: Confirm branch is based off vacation home**

Run: `git merge-base --is-ancestor feat/in-app-vacation-home-purchase HEAD && echo "OK: branched from vacation home"`
Expected: `OK: branched from vacation home`.

- [ ] **Step 2: Confirm feature-purchase-house exists with vacation home form**

Run: `ls app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormDestination.kt`
Expected: file exists.

---

### Task 1: Create `SubmitHouseFormAndGetOffersUseCase`

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitHouseFormAndGetOffersUseCase.kt`

- [ ] **Step 1: Create the use case**

```kotlin
package com.hedvig.android.feature.purchase.house.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.HousePriceIntentConfirmMutation
import octopus.HousePriceIntentDataUpdateMutation
import octopus.fragment.HouseProductOfferFragment

internal interface SubmitHouseFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    ancillaryArea: Int,
    numberCoInsured: Int,
    yearOfConstruction: Int,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
  ): Either<ErrorMessage, HouseOffers>
}

internal class SubmitHouseFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitHouseFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    ancillaryArea: Int,
    numberCoInsured: Int,
    yearOfConstruction: Int,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
  ): Either<ErrorMessage, HouseOffers> {
    return either {
      val formData = buildMap<String, Any> {
        put("ssn", ssn)
        put("email", email)
        put("street", street)
        put("zipCode", zipCode)
        put("livingSpace", livingSpace)
        put("ancillaryArea", ancillaryArea)
        put("numberCoInsured", numberCoInsured)
        put("yearOfConstruction", yearOfConstruction)
        put("numberOfBathrooms", numberOfBathrooms)
        put("isSubleted", isSubleted)
        put("extraBuildings", emptyList<Map<String, Any>>())
      }

      val updateResult = apolloClient
        .mutation(HousePriceIntentDataUpdateMutation(priceIntentId = priceIntentId, data = formData))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to update price intent data: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentDataUpdate },
        )

      if (updateResult.userError != null) {
        raise(ErrorMessage(updateResult.userError?.message))
      }

      val confirmResult = apolloClient
        .mutation(HousePriceIntentConfirmMutation(priceIntentId = priceIntentId))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to confirm price intent: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentConfirm },
        )

      if (confirmResult.userError != null) {
        raise(ErrorMessage(confirmResult.userError?.message))
      }

      val offers = confirmResult.priceIntent?.offers.orEmpty()
      if (offers.isEmpty()) {
        logcat(LogPriority.ERROR) { "No offers returned after confirming price intent" }
        raise(ErrorMessage())
      }

      HouseOffers(
        productDisplayName = offers.first().variant.displayName,
        offers = offers.map { it.toHouseTierOfferForHouse() },
      )
    }
  }
}

private fun HouseProductOfferFragment.toHouseTierOfferForHouse(): HouseTierOffer {
  return HouseTierOffer(
    offerId = id,
    tierDisplayName = variant.displayNameTier ?: variant.displayName,
    tierDescription = variant.tierDescription ?: "",
    grossPrice = UiMoney.fromMoneyFragment(cost.gross),
    netPrice = UiMoney.fromMoneyFragment(cost.net),
    usps = usps,
    exposureDisplayName = exposure.displayNameShort,
    deductibleDisplayName = deductible?.displayName,
    hasDiscount = cost.net.amount < cost.gross.amount,
  )
}
```

> The `toHouseTierOfferForHouse` extension is a private duplicate of the one in `SubmitVacationHomeFormAndGetOffersUseCase.kt` (which is `internal` but lives in the same package). Naming it differently avoids JVM duplicate-symbol conflict if the linker resolves them in unexpected order. (Alternative: factor `toHouseTierOffer` into `HousePurchaseModels.kt` as a `internal fun` — see Task 1b below if you want to clean up the duplication.)

- [ ] **Step 2: Build**

Run: `./gradlew :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitHouseFormAndGetOffersUseCase.kt
git commit -m "feat: add SubmitHouseFormAndGetOffersUseCase"
```

---

### Task 1b (optional cleanup): Move `toHouseTierOffer` mapping into `HousePurchaseModels.kt`

Skip this task if you don't mind the inline duplication.

**Files:**
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/HousePurchaseModels.kt`
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt`
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitHouseFormAndGetOffersUseCase.kt`

- [ ] **Step 1: Add `toHouseTierOffer` to `HousePurchaseModels.kt`**

Add at the bottom of that file:
```kotlin
internal fun octopus.fragment.HouseProductOfferFragment.toHouseTierOffer(): HouseTierOffer {
  return HouseTierOffer(
    offerId = id,
    tierDisplayName = variant.displayNameTier ?: variant.displayName,
    tierDescription = variant.tierDescription ?: "",
    grossPrice = com.hedvig.android.core.uidata.UiMoney.fromMoneyFragment(cost.gross),
    netPrice = com.hedvig.android.core.uidata.UiMoney.fromMoneyFragment(cost.net),
    usps = usps,
    exposureDisplayName = exposure.displayNameShort,
    deductibleDisplayName = deductible?.displayName,
    hasDiscount = cost.net.amount < cost.gross.amount,
  )
}
```

(Plus the corresponding imports at the top.)

- [ ] **Step 2: Remove the private `toHouseTierOffer` from both submit-use-case files**

In `SubmitVacationHomeFormAndGetOffersUseCase.kt`, delete the bottom `internal fun HouseProductOfferFragment.toHouseTierOffer()` block.

In `SubmitHouseFormAndGetOffersUseCase.kt` (just created in Task 1), delete the bottom `private fun HouseProductOfferFragment.toHouseTierOfferForHouse()` block AND update the call site from `it.toHouseTierOfferForHouse()` to `it.toHouseTierOffer()`.

- [ ] **Step 3: Build, ktlint, commit**

```bash
./gradlew :feature-purchase-house:assemble :feature-purchase-house:ktlintFormat :feature-purchase-house:ktlintCheck
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/
git commit -m "refactor: lift toHouseTierOffer into HousePurchaseModels for reuse"
```

---

### Task 2: Add `HouseFormViewModel`

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormViewModel.kt`

- [ ] **Step 1: Create the ViewModel + Presenter**

```kotlin
package com.hedvig.android.feature.purchase.house.ui.house

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.house.data.CreateHouseSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.house.data.HouseOffers
import com.hedvig.android.feature.purchase.house.data.SessionAndIntent
import com.hedvig.android.feature.purchase.house.data.SubmitHouseFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import java.time.LocalDate

internal class HouseFormViewModel(
  productName: String,
  createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  submitHouseFormAndGetOffersUseCase: SubmitHouseFormAndGetOffersUseCase,
) : MoleculeViewModel<HouseFormEvent, HouseFormState>(
    initialState = HouseFormState(),
    presenter = HouseFormPresenter(
      productName,
      createHouseSessionAndPriceIntentUseCase,
      submitHouseFormAndGetOffersUseCase,
    ),
  )

internal sealed interface HouseFormEvent {
  data class UpdateIsSubleted(val value: Boolean) : HouseFormEvent

  data class SubmitForm(
    val street: String,
    val zipCode: String,
    val livingSpace: String,
    val ancillaryArea: String,
    val numberCoInsured: Int,
    val yearOfConstruction: String,
    val numberOfBathrooms: Int,
  ) : HouseFormEvent

  data object ClearNavigation : HouseFormEvent

  data object Retry : HouseFormEvent

  data object DismissError : HouseFormEvent
}

internal data class HouseFormState(
  val isSubleted: Boolean? = null,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val livingSpaceError: String? = null,
  val ancillaryAreaError: String? = null,
  val yearOfConstructionError: String? = null,
  val isSubletedError: String? = null,
  val isSubmitting: Boolean = false,
  val isLoadingSession: Boolean = true,
  val loadSessionError: Boolean = false,
  val submitError: String? = null,
  val offersToNavigate: OffersNavigationData? = null,
)

internal data class OffersNavigationData(
  val shopSessionId: String,
  val offers: HouseOffers,
)

private class HouseFormPresenter(
  private val productName: String,
  private val createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  private val submitHouseFormAndGetOffersUseCase: SubmitHouseFormAndGetOffersUseCase,
) : MoleculePresenter<HouseFormEvent, HouseFormState> {
  @Composable
  override fun MoleculePresenterScope<HouseFormEvent>.present(lastState: HouseFormState): HouseFormState {
    var currentState by remember { mutableStateOf(lastState) }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var sessionLoadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: HouseFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is HouseFormEvent.UpdateIsSubleted -> {
          currentState = currentState.copy(isSubleted = event.value, isSubletedError = null)
        }

        is HouseFormEvent.SubmitForm -> {
          val errors = validate(event, currentState)
          currentState = currentState.copy(
            streetError = errors.streetError,
            zipCodeError = errors.zipCodeError,
            livingSpaceError = errors.livingSpaceError,
            ancillaryAreaError = errors.ancillaryAreaError,
            yearOfConstructionError = errors.yearOfConstructionError,
            isSubletedError = errors.isSubletedError,
          )
          if (!errors.hasErrors()) {
            pendingSubmit = event
            submitIteration++
          }
        }

        HouseFormEvent.ClearNavigation -> currentState = currentState.copy(offersToNavigate = null)

        HouseFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            sessionLoadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }

        HouseFormEvent.DismissError -> currentState = currentState.copy(submitError = null)
      }
    }

    LaunchedEffect(sessionLoadIteration) {
      currentState = currentState.copy(isLoadingSession = true, loadSessionError = false)
      createHouseSessionAndPriceIntentUseCase.invoke(productName).fold(
        ifLeft = {
          currentState = currentState.copy(isLoadingSession = false, loadSessionError = true)
        },
        ifRight = { result ->
          sessionAndIntent = result
          currentState = currentState.copy(isLoadingSession = false, loadSessionError = false)
        },
      )
    }

    LaunchedEffect(submitIteration) {
      val submit = pendingSubmit ?: return@LaunchedEffect
      val session = sessionAndIntent ?: return@LaunchedEffect
      val livingSpace = submit.livingSpace.toIntOrNull() ?: return@LaunchedEffect
      val ancillaryArea = submit.ancillaryArea.toIntOrNull() ?: return@LaunchedEffect
      val yearOfConstruction = submit.yearOfConstruction.toIntOrNull() ?: return@LaunchedEffect
      val isSubleted = currentState.isSubleted ?: return@LaunchedEffect
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)
      submitHouseFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        ssn = session.ssn,
        email = session.email,
        street = submit.street,
        zipCode = submit.zipCode,
        livingSpace = livingSpace,
        ancillaryArea = ancillaryArea,
        numberCoInsured = submit.numberCoInsured,
        yearOfConstruction = yearOfConstruction,
        numberOfBathrooms = submit.numberOfBathrooms,
        isSubleted = isSubleted,
      ).fold(
        ifLeft = { error ->
          currentState = currentState.copy(
            isSubmitting = false,
            submitError = error.message ?: "Something went wrong",
          )
        },
        ifRight = { offers ->
          currentState = currentState.copy(
            isSubmitting = false,
            offersToNavigate = OffersNavigationData(
              shopSessionId = session.shopSessionId,
              offers = offers,
            ),
          )
        },
      )
    }

    return currentState
  }
}

private data class ValidationErrors(
  val streetError: String?,
  val zipCodeError: String?,
  val livingSpaceError: String?,
  val ancillaryAreaError: String?,
  val yearOfConstructionError: String?,
  val isSubletedError: String?,
) {
  fun hasErrors(): Boolean = streetError != null ||
    zipCodeError != null ||
    livingSpaceError != null ||
    ancillaryAreaError != null ||
    yearOfConstructionError != null ||
    isSubletedError != null
}

private fun validate(event: HouseFormEvent.SubmitForm, state: HouseFormState): ValidationErrors {
  val currentYear = LocalDate.now().year
  return ValidationErrors(
    // TODO: Add "Enter an address" / "Ange en adress" to Lokalise
    streetError = if (event.street.isBlank()) "Enter an address" else null,
    zipCodeError = when {
      // TODO: Add "Enter a valid zip code (5 digits)" / "Ange ett giltigt postnummer (5 siffror)" to Lokalise
      event.zipCode.length != 5 -> "Enter a valid zip code (5 digits)"
      // TODO: Add "Zip code must contain only digits" / "Postnumret får bara innehålla siffror" to Lokalise
      !event.zipCode.all { it.isDigit() } -> "Zip code must contain only digits"
      else -> null
    },
    livingSpaceError = when (val space = event.livingSpace.toIntOrNull()) {
      // TODO: Add "Enter living space" / "Ange boyta" to Lokalise
      null -> "Enter living space"
      // TODO: Add "Enter a valid living space" / "Ange en giltig boyta" to Lokalise
      !in 1..Int.MAX_VALUE -> "Enter a valid living space"
      else -> null
    },
    ancillaryAreaError = when (val area = event.ancillaryArea.toIntOrNull()) {
      // TODO: Add "Enter ancillary area" / "Ange biyta" to Lokalise
      null -> "Enter ancillary area"
      // TODO: Add "Enter a valid ancillary area" / "Ange en giltig biyta" to Lokalise
      !in 0..Int.MAX_VALUE -> "Enter a valid ancillary area"
      else -> null
    },
    yearOfConstructionError = when (val year = event.yearOfConstruction.toIntOrNull()) {
      // TODO: Add "Enter year of construction" / "Ange byggår" to Lokalise
      null -> "Enter year of construction"
      // TODO: Add "Enter a valid year of construction" / "Ange ett giltigt byggår" to Lokalise
      !in 1700..currentYear -> "Enter a valid year of construction"
      else -> null
    },
    // TODO: Add "Choose an option" / "Välj ett alternativ" to Lokalise
    isSubletedError = if (state.isSubleted == null) "Choose an option" else null,
  )
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormViewModel.kt
git commit -m "feat: add HouseFormViewModel"
```

---

### Task 3: Add `HouseFormDestination`

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormDestination.kt`

- [ ] **Step 1: Create the composable**

```kotlin
package com.hedvig.android.feature.purchase.house.ui.house

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigStepper
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperSize.Medium
import com.hedvig.android.design.system.hedvig.StepperDefaults.StepperStyle.Labeled
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.house.data.HouseOffers

@Composable
internal fun HouseFormDestination(
  viewModel: HouseFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: HouseOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(HouseFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(navigateUp = navigateUp) {
    when {
      uiState.isLoadingSession -> HedvigFullScreenCenterAlignedProgress()

      uiState.loadSessionError -> HedvigErrorSection(
        onButtonClick = { viewModel.emit(HouseFormEvent.Retry) },
      )

      else -> HouseFormBody(
        uiState = uiState,
        onEvent = { event -> viewModel.emit(event) },
      )
    }
  }
}

@Composable
private fun HouseFormBody(uiState: HouseFormState, onEvent: (HouseFormEvent) -> Unit) {
  var street by rememberSaveable { mutableStateOf("") }
  var zipCode by rememberSaveable { mutableStateOf("") }
  var livingSpace by rememberSaveable { mutableStateOf("") }
  var ancillaryArea by rememberSaveable { mutableStateOf("") }
  var numberCoInsured by rememberSaveable { mutableIntStateOf(1) }
  var yearOfConstruction by rememberSaveable { mutableStateOf("") }
  var numberOfBathrooms by rememberSaveable { mutableIntStateOf(1) }

  if (uiState.submitError != null) {
    ErrorDialog(
      // TODO: Add "Something went wrong" / "Något gick fel" to Lokalise
      title = "Something went wrong",
      message = uiState.submitError,
      onDismiss = { onEvent(HouseFormEvent.DismissError) },
    )
  }

  HouseFormContent(
    street = street,
    zipCode = zipCode,
    livingSpace = livingSpace,
    ancillaryArea = ancillaryArea,
    numberCoInsured = numberCoInsured,
    yearOfConstruction = yearOfConstruction,
    numberOfBathrooms = numberOfBathrooms,
    isSubleted = uiState.isSubleted,
    errors = uiState,
    isSubmitting = uiState.isSubmitting,
    onStreetChanged = { street = it },
    onZipCodeChanged = { value -> if (value.all { it.isDigit() } && value.length <= 5) zipCode = value },
    onLivingSpaceChanged = { value ->
      if (value.isEmpty() || value.toIntOrNull() != null) livingSpace = value
    },
    onAncillaryAreaChanged = { value ->
      if (value.isEmpty() || value.toIntOrNull() != null) ancillaryArea = value
    },
    onNumberCoInsuredChanged = { numberCoInsured = it },
    onYearOfConstructionChanged = { value ->
      if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 4)) yearOfConstruction = value
    },
    onNumberOfBathroomsChanged = { numberOfBathrooms = it },
    onIsSubletedSelected = { onEvent(HouseFormEvent.UpdateIsSubleted(it)) },
    onSubmit = {
      onEvent(
        HouseFormEvent.SubmitForm(
          street = street,
          zipCode = zipCode,
          livingSpace = livingSpace,
          ancillaryArea = ancillaryArea,
          numberCoInsured = numberCoInsured,
          yearOfConstruction = yearOfConstruction,
          numberOfBathrooms = numberOfBathrooms,
        ),
      )
    },
  )
}

@Composable
private fun HouseFormContent(
  street: String,
  zipCode: String,
  livingSpace: String,
  ancillaryArea: String,
  numberCoInsured: Int,
  yearOfConstruction: String,
  numberOfBathrooms: Int,
  isSubleted: Boolean?,
  errors: HouseFormState,
  isSubmitting: Boolean,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onLivingSpaceChanged: (String) -> Unit,
  onAncillaryAreaChanged: (String) -> Unit,
  onNumberCoInsuredChanged: (Int) -> Unit,
  onYearOfConstructionChanged: (String) -> Unit,
  onNumberOfBathroomsChanged: (Int) -> Unit,
  onIsSubletedSelected: (Boolean) -> Unit,
  onSubmit: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      // TODO: Add "Fill in your details so we can calculate your price" / "Fyll i dina uppgifter så beräknar vi ditt pris" to Lokalise
      text = "Fill in your details so we can calculate your price",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(16.dp))
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      HedvigTextField(
        text = street,
        onValueChange = onStreetChanged,
        // TODO: Add "Address" / "Adress" to Lokalise
        labelText = "Address",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.streetError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = zipCode,
        onValueChange = onZipCodeChanged,
        // TODO: Add "Zip code" / "Postnummer" to Lokalise
        labelText = "Zip code",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.zipCodeError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = livingSpace,
        onValueChange = onLivingSpaceChanged,
        // TODO: Add "Living space (m²)" / "Boyta (kvm)" to Lokalise
        labelText = "Living space (m²)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.livingSpaceError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = ancillaryArea,
        onValueChange = onAncillaryAreaChanged,
        // TODO: Add "Ancillary area (m²)" / "Biyta (kvm)" to Lokalise
        labelText = "Ancillary area (m²)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.ancillaryAreaError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )

      // TODO: Add "Household size" / "Hushållsstorlek" to Lokalise
      // TODO: Add "Only you" / "Bara du" to Lokalise
      // TODO: Add "You + {count}" / "Du + {count}" to Lokalise
      HedvigStepper(
        text = when (numberCoInsured) {
          1 -> "Only you"
          else -> "You + ${numberCoInsured - 1}"
        },
        stepperSize = Medium,
        stepperStyle = Labeled("Household size"),
        onMinusClick = { onNumberCoInsuredChanged(numberCoInsured - 1) },
        onPlusClick = { onNumberCoInsuredChanged(numberCoInsured + 1) },
        isPlusEnabled = !isSubmitting && numberCoInsured < 5,
        isMinusEnabled = !isSubmitting && numberCoInsured > 1,
      )

      HedvigTextField(
        text = yearOfConstruction,
        onValueChange = onYearOfConstructionChanged,
        // TODO: Add "Year of construction" / "Byggår" to Lokalise
        labelText = "Year of construction",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.yearOfConstructionError.toErrorState(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )

      // TODO: Add "Number of bathrooms" / "Antal badrum" to Lokalise
      // TODO: Add "1 bathroom" / "1 badrum" to Lokalise (singular)
      // TODO: Add "{count} bathrooms" / "{count} badrum" to Lokalise (plural)
      HedvigStepper(
        text = if (numberOfBathrooms == 1) "1 bathroom" else "$numberOfBathrooms bathrooms",
        stepperSize = Medium,
        stepperStyle = Labeled("Number of bathrooms"),
        onMinusClick = { onNumberOfBathroomsChanged(numberOfBathrooms - 1) },
        onPlusClick = { onNumberOfBathroomsChanged(numberOfBathrooms + 1) },
        isPlusEnabled = !isSubmitting && numberOfBathrooms < 10,
        isMinusEnabled = !isSubmitting && numberOfBathrooms > 1,
      )

      // TODO: Add "Do you sublet all or parts of the house?" / "Hyr du ut hela eller delar av huset?" to Lokalise
      RadioChoiceRow(
        label = "Do you sublet all or parts of the house?",
        selectedId = isSubleted?.toString(),
        options = yesNoOptions(),
        onSelected = { id -> onIsSubletedSelected(id.toBoolean()) },
        errorText = errors.isSubletedError,
        isEnabled = !isSubmitting,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      // TODO: Add "Calculate price" / "Beräkna pris" to Lokalise
      text = "Calculate price",
      onClick = onSubmit,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun RadioChoiceRow(
  label: String,
  selectedId: String?,
  options: List<Pair<String, String>>,
  onSelected: (String) -> Unit,
  errorText: String?,
  isEnabled: Boolean,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    RadioGroup(
      options = options.map { (id, text) -> RadioOption(RadioOptionId(id), text) },
      selectedOption = selectedId?.let(::RadioOptionId),
      onRadioOptionSelected = { id -> onSelected(id.id) },
      style = RadioGroupStyle.Labeled.HorizontalFlow(label = label),
      enabled = isEnabled,
      modifier = Modifier.fillMaxWidth(),
    )
    if (errorText != null) {
      HedvigText(
        text = errorText,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

// TODO: Add "Yes" / "Ja" to Lokalise
// TODO: Add "No" / "Nej" to Lokalise
private fun yesNoOptions(): List<Pair<String, String>> = listOf("true" to "Yes", "false" to "No")

private fun String?.toErrorState(): HedvigTextFieldDefaults.ErrorState {
  return if (this != null) {
    HedvigTextFieldDefaults.ErrorState.Error.WithMessage(this)
  } else {
    HedvigTextFieldDefaults.ErrorState.NoError
  }
}

@HedvigPreview
@Composable
private fun PreviewHouseFormEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HouseFormContent(
        street = "",
        zipCode = "",
        livingSpace = "",
        ancillaryArea = "",
        numberCoInsured = 1,
        yearOfConstruction = "",
        numberOfBathrooms = 1,
        isSubleted = null,
        errors = HouseFormState(),
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onAncillaryAreaChanged = {},
        onNumberCoInsuredChanged = {},
        onYearOfConstructionChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedSelected = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHouseFormFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HouseFormContent(
        street = "Storgatan 1",
        zipCode = "12345",
        livingSpace = "120",
        ancillaryArea = "20",
        numberCoInsured = 3,
        yearOfConstruction = "1985",
        numberOfBathrooms = 2,
        isSubleted = false,
        errors = HouseFormState(),
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onAncillaryAreaChanged = {},
        onNumberCoInsuredChanged = {},
        onYearOfConstructionChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedSelected = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHouseFormErrors() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HouseFormContent(
        street = "",
        zipCode = "12",
        livingSpace = "",
        ancillaryArea = "",
        numberCoInsured = 1,
        yearOfConstruction = "1500",
        numberOfBathrooms = 1,
        isSubleted = null,
        errors = HouseFormState(
          streetError = "Enter an address",
          zipCodeError = "Enter a valid zip code (5 digits)",
          livingSpaceError = "Enter living space",
          ancillaryAreaError = "Enter ancillary area",
          yearOfConstructionError = "Enter a valid year of construction",
          isSubletedError = "Choose an option",
        ),
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onAncillaryAreaChanged = {},
        onNumberCoInsuredChanged = {},
        onYearOfConstructionChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedSelected = {},
        onSubmit = {},
      )
    }
  }
}
```

- [ ] **Step 2: Build + ktlint**

Run: `./gradlew :feature-purchase-house:assemble :feature-purchase-house:ktlintFormat :feature-purchase-house:ktlintCheck`
Expected: BUILD SUCCESSFUL on all three.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/house/HouseFormDestination.kt
git commit -m "feat: add HouseFormDestination with Compose previews"
```

---

### Task 4: Branch `HousePurchaseNavGraph.Form` on `productName`

**Files:**
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/navigation/HousePurchaseNavGraph.kt`

- [ ] **Step 1: Update the `Form` navdestination to branch on productName**

Find this block:
```kotlin
    navdestination<Form> { backStackEntry ->
      val graphRoute = navController
        .getRouteFromBackStack<HousePurchaseGraphDestination>(backStackEntry)
      val viewModel: VacationHomeFormViewModel = koinViewModel {
        parametersOf(graphRoute.productName)
      }
      VacationHomeFormDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { popBackStack() },
        onOffersReceived = { shopSessionId, offers ->
          navController.navigate(
            SelectTier(
              SelectTierParameters(
                shopSessionId = shopSessionId,
                offers = offers.offers.map { offer ->
                  TierOfferData(
                    offerId = offer.offerId,
                    tierDisplayName = offer.tierDisplayName,
                    tierDescription = offer.tierDescription,
                    grossAmount = offer.grossPrice.amount,
                    grossCurrencyCode = offer.grossPrice.currencyCode.name,
                    netAmount = offer.netPrice.amount,
                    netCurrencyCode = offer.netPrice.currencyCode.name,
                    usps = offer.usps,
                    exposureDisplayName = offer.exposureDisplayName,
                    deductibleDisplayName = offer.deductibleDisplayName,
                    hasDiscount = offer.hasDiscount,
                  )
                },
                productDisplayName = offers.productDisplayName,
              ),
            ),
          )
        },
      )
    }
```

Replace with:

```kotlin
    navdestination<Form> { backStackEntry ->
      val graphRoute = navController
        .getRouteFromBackStack<HousePurchaseGraphDestination>(backStackEntry)
      val onOffersReceivedToSelectTier: (shopSessionId: String, offers: HouseOffers) -> Unit =
        { shopSessionId, offers ->
          navController.navigate(
            SelectTier(
              SelectTierParameters(
                shopSessionId = shopSessionId,
                offers = offers.offers.map { offer ->
                  TierOfferData(
                    offerId = offer.offerId,
                    tierDisplayName = offer.tierDisplayName,
                    tierDescription = offer.tierDescription,
                    grossAmount = offer.grossPrice.amount,
                    grossCurrencyCode = offer.grossPrice.currencyCode.name,
                    netAmount = offer.netPrice.amount,
                    netCurrencyCode = offer.netPrice.currencyCode.name,
                    usps = offer.usps,
                    exposureDisplayName = offer.exposureDisplayName,
                    deductibleDisplayName = offer.deductibleDisplayName,
                    hasDiscount = offer.hasDiscount,
                  )
                },
                productDisplayName = offers.productDisplayName,
              ),
            ),
          )
        }
      when (graphRoute.productName) {
        "SE_VACATION_HOME" -> {
          val viewModel: VacationHomeFormViewModel = koinViewModel { parametersOf(graphRoute.productName) }
          VacationHomeFormDestination(
            viewModel = viewModel,
            navigateUp = dropUnlessResumed { popBackStack() },
            onOffersReceived = onOffersReceivedToSelectTier,
          )
        }
        "SE_HOUSE" -> {
          val viewModel: HouseFormViewModel = koinViewModel { parametersOf(graphRoute.productName) }
          HouseFormDestination(
            viewModel = viewModel,
            navigateUp = dropUnlessResumed { popBackStack() },
            onOffersReceived = onOffersReceivedToSelectTier,
          )
        }
        else -> error("Unknown productName for HousePurchaseGraph: ${graphRoute.productName}")
      }
    }
```

Add the new imports at the top of the file:
```kotlin
import com.hedvig.android.feature.purchase.house.data.HouseOffers
import com.hedvig.android.feature.purchase.house.ui.house.HouseFormDestination
import com.hedvig.android.feature.purchase.house.ui.house.HouseFormViewModel
```

- [ ] **Step 2: Build + ktlint**

Run: `./gradlew :feature-purchase-house:assemble :feature-purchase-house:ktlintFormat :feature-purchase-house:ktlintCheck`
Expected: BUILD SUCCESSFUL on all three.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/navigation/HousePurchaseNavGraph.kt
git commit -m "feat: branch HousePurchaseNavGraph Form destination on productName"
```

---

### Task 5: Register `HouseFormViewModel` + use case in DI

**Files:**
- Modify: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/di/HousePurchaseModule.kt`

- [ ] **Step 1: Add the new bindings**

Find the existing `housePurchaseModule = module { ... }` block. Add inside it (after the existing `viewModel<VacationHomeFormViewModel> { ... }`):

```kotlin
  single<SubmitHouseFormAndGetOffersUseCase> { SubmitHouseFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<HouseFormViewModel> { params ->
    HouseFormViewModel(
      productName = params.get(),
      createHouseSessionAndPriceIntentUseCase = get(),
      submitHouseFormAndGetOffersUseCase = get(),
    )
  }
```

Add imports at the top of the file:
```kotlin
import com.hedvig.android.feature.purchase.house.data.SubmitHouseFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.house.data.SubmitHouseFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.house.ui.house.HouseFormViewModel
```

- [ ] **Step 2: Build**

Run: `./gradlew :feature-purchase-house:assemble :feature-purchase-house:ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/di/HousePurchaseModule.kt
git commit -m "feat: register HouseFormViewModel + SubmitHouseFormAndGetOffersUseCase in Koin"
```

---

### Task 6: Add `HomePickerDialog` Composable

**Files:**
- Create: `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/insurance/HomePickerDialog.kt`

- [ ] **Step 1: Create the dialog**

```kotlin
package com.hedvig.android.feature.insurances.insurance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun HomePickerDialog(
  onDismiss: () -> Unit,
  onSelectApartmentRent: () -> Unit,
  onSelectApartmentBrf: () -> Unit,
  onSelectVilla: () -> Unit,
) {
  HedvigDialog(onDismissRequest = onDismiss) {
    Column(modifier = Modifier.fillMaxWidth()) {
      HedvigText(
        // TODO: Add "Which type of home insurance?" / "Vilken typ av hemförsäkring?" to Lokalise
        text = "Which type of home insurance?",
        style = HedvigTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      )
      // TODO: Add "Rental apartment" / "Hyresrätt" to Lokalise
      HomePickerRow(text = "Rental apartment (Hyresrätt)", onClick = onSelectApartmentRent)
      // TODO: Add "Owned apartment" / "Bostadsrätt" to Lokalise
      HomePickerRow(text = "Owned apartment (Bostadsrätt)", onClick = onSelectApartmentBrf)
      // TODO: Add "House" / "Villa" to Lokalise
      HomePickerRow(text = "House (Villa)", onClick = onSelectVilla)
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        // TODO: Add "Cancel" / "Avbryt" to Lokalise
        text = "Cancel",
        onClick = onDismiss,
        enabled = true,
        buttonStyle = Ghost,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun HomePickerRow(text: String, onClick: () -> Unit) {
  HedvigText(
    text = text,
    style = HedvigTheme.typography.bodyMedium,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(role = Role.Button) { onClick() }
      .padding(horizontal = 16.dp, vertical = 14.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewHomePickerDialog() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HomePickerDialog(
        onDismiss = {},
        onSelectApartmentRent = {},
        onSelectApartmentBrf = {},
        onSelectVilla = {},
      )
    }
  }
}
```

> **If `ButtonStyle.Ghost` doesn't exist**, swap the cancel button to the default style or check the design system's button style enum and pick the closest "subtle/secondary" option.

- [ ] **Step 2: Build + ktlint**

Run: `./gradlew :feature-insurances:assemble :feature-insurances:ktlintFormat :feature-insurances:ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/insurance/HomePickerDialog.kt
git commit -m "feat: add HomePickerDialog for hem cross-sell disambiguation"
```

---

### Task 7: Wire picker dialog + new URL routes in `InsuranceGraph.kt`

**Files:**
- Modify: `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt`

- [ ] **Step 1: Add imports + dialog state + new URL routes**

Add imports at the top of the file:
```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.insurances.insurance.HomePickerDialog
```

Then inside the `navdestination<InsurancesDestination.Insurances>(...) { ... }` block, find the existing `val viewModel: InsuranceViewModel = koinViewModel()` line. Right before it, add the dialog state:
```kotlin
      var showHomePicker by rememberSaveable { mutableStateOf(false) }
```

Then update the `onCrossSellClick` `when {}` block from:
```kotlin
          when {
            "fritidshusforsakring" in lower || "vacation-home" in lower -> {
              onNavigateToHousePurchase("SE_VACATION_HOME")
            }

            "car-insurance" in lower || "bilforsakring" in lower -> {
              onNavigateToCarPurchase("SE_CAR")
            }

            "pet-insurance" in lower || "djurforsakring" in lower -> {
              onNavigateToPetPurchase()
            }

            "bostadsratt" in lower || "home-insurance/homeowner" in lower -> {
              onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
            }

            "hyresratt" in lower || "home-insurance" in lower || "hemforsakring" in lower -> {
              onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
            }

            else -> openUrl(url)
          }
```

to:
```kotlin
          when {
            "fritidshusforsakring" in lower || "vacation-home" in lower -> {
              onNavigateToHousePurchase("SE_VACATION_HOME")
            }

            "villaforsakring" in lower || "home-insurance/house" in lower -> {
              onNavigateToHousePurchase("SE_HOUSE")
            }

            "bostadsratt" in lower || "home-insurance/homeowner" in lower -> {
              onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
            }

            "hyresratt" in lower || "home-insurance/rental" in lower -> {
              onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
            }

            "hemforsakring" in lower || "home-insurance" in lower -> {
              showHomePicker = true
            }

            "car-insurance" in lower || "bilforsakring" in lower -> {
              onNavigateToCarPurchase("SE_CAR")
            }

            "pet-insurance" in lower || "djurforsakring" in lower -> {
              onNavigateToPetPurchase()
            }

            else -> openUrl(url)
          }
```

(Specific home variants checked first; the generic `hemforsakring` / `home-insurance` rule now opens the picker instead of defaulting to RENT.)

Finally, after the `InsuranceDestination(...)` call inside the same `navdestination` block, add the dialog rendering. Look for the closing `)` of `InsuranceDestination(...)`. After it, add:

```kotlin
      if (showHomePicker) {
        HomePickerDialog(
          onDismiss = { showHomePicker = false },
          onSelectApartmentRent = {
            showHomePicker = false
            onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
          },
          onSelectApartmentBrf = {
            showHomePicker = false
            onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
          },
          onSelectVilla = {
            showHomePicker = false
            onNavigateToHousePurchase("SE_HOUSE")
          },
        )
      }
```

- [ ] **Step 2: Build + ktlint**

Run: `./gradlew :feature-insurances:assemble :feature-insurances:ktlintFormat :feature-insurances:ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt
git commit -m "feat: route villa cross-sells to SE_HOUSE; show HomePickerDialog for generic hem URLs"
```

---

### Task 8: Full app build

**Files:** none

`app` module needs no changes (callbacks already accept arbitrary productNames). Just verify everything links together.

- [ ] **Step 1: Build the full app**

Run: `./gradlew :app:assembleDevelopDebug` (or `./gradlew :app:assemble`)
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Top-level ktlint**

Run: `./gradlew :app:ktlintCheck :feature-purchase-house:ktlintCheck :feature-insurances:ktlintCheck`
Expected: BUILD SUCCESSFUL.

---

### Task 9: Manual emulator verification

**Files:** none

Per the `verifying-android-changes-in-emulator` skill. Required before claiming done.

- [ ] **Step 1: Install on emulator**

Run: `./gradlew :app:installDebug`
Expected: app installed.

- [ ] **Step 2: Verify the picker dialog flow**

Get a logged-in session via impersonation (ask the user for the deep link). Then:

1. Trigger a generic "Home" cross-sell. Inject test URL `hemforsakring` if needed via the cross-sell card.
2. Verify `HomePickerDialog` opens with three options.
3. Tap "Villa" → `HouseFormDestination` opens.
4. Press back from the dialog → dialog dismisses, no navigation.

- [ ] **Step 3: Verify the SE_HOUSE form**

1. Fill all 7 user-input fields with valid values (street, zip `12345`, livingSpace `120`, ancillaryArea `20`, household stepper `1`, year `1985`, bathrooms `1`, sublet `No`).
2. Tap "Calculate price" → navigate to `SelectTier` showing the SE_HOUSE offer.
3. Continue: Summary → BankID → Success → back to insurances tab.

- [ ] **Step 4: Verify validation edge cases**

1. Submit empty form → 6 errors surface.
2. Zip `123` → 5-digit error.
3. Year `1500` → year-range error.
4. LivingSpace `0` → "valid living space" error.

- [ ] **Step 5: Verify direct URL shortcuts still work**

1. Inject URL `hemforsakring/villaforsakring` → house form opens directly (no dialog).
2. Inject URL `hemforsakring/bostadsratt` → apartment form opens with BRF productName.
3. Inject URL `hemforsakring/hyresratt` → apartment form opens with RENT productName.

- [ ] **Step 6: Verify picker dialog dismissal**

1. Open dialog, tap outside (scrim) → dialog dismisses.
2. Open dialog, system back → dialog dismisses.
3. Open dialog, tap Cancel → dialog dismisses.

---

### Task 10: PR prep

- [ ] **Step 1: Full sanity build**

Run: `./gradlew :app:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Lint**

Run: `./gradlew :feature-purchase-house:lint :feature-insurances:lint`
Expected: no new errors.

- [ ] **Step 3: Push + open PR**

Push to origin, open a PR with base `feat/in-app-vacation-home-purchase` (this PR stacks on the vacation home PR). Title: `Add in-app house (villa) purchase + Home picker dialog`. Body should highlight:

- New `HouseFormDestination` + `HouseFormViewModel` + `SubmitHouseFormAndGetOffersUseCase` in `feature-purchase-house`.
- `HousePurchaseNavGraph.Form` branches on productName (SE_VACATION_HOME → vacation home form, SE_HOUSE → house form).
- `HomePickerDialog` in `feature-insurances` replaces the silent generic-home → APARTMENT_RENT default.
- `useRegistrationAddress`, `currentInsurance`, `extraBuildings` UI deferred to follow-up PRs.
- Base branch will retarget to `develop` once the vacation home PR merges.

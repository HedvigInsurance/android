# In-app Vacation Home (Fritidshus) Purchase Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create `feature-purchase-house` module hosting the `SE_VACATION_HOME` (fritidshus) in-app purchase flow, wire it into app navigation, and route fritidshus cross-sell URLs to it.

**Architecture:** New feature module `feature-purchase-house` depends on the existing `purchase-common` module (which lives on `feat/in-app-car-purchase`). The module owns a `VacationHomeFormDestination` composable, its ViewModel/Presenter, two use cases (session + form-submit), one set of `House`-prefixed Apollo operations, a nav graph, and a DI module. After the form, navigation hands off to `purchase-common` for tier selection, summary, BankID signing, and success/failure — identical to how car and apartment work.

**Tech Stack:** Kotlin, Jetpack Compose, Apollo GraphQL, Molecule (MVI), Koin DI, Arrow (Either), kotlinx.serialization.

**Base branch:** This work depends on the `purchase-common` module, which currently only lives on `feat/in-app-car-purchase`. The implementation branch **must** be cut from `feat/in-app-car-purchase` (not `develop`).

---

### Task 0: Verify base branch

**Files:** none

- [ ] **Step 1: Confirm the current branch is based off `feat/in-app-car-purchase`**

Run: `git log --oneline feat/in-app-car-purchase..HEAD | head -5; git merge-base --is-ancestor feat/in-app-car-purchase HEAD && echo "OK: branched from car" || echo "FAIL: not branched from car"`
Expected: `OK: branched from car` (the merge-base check confirms car is an ancestor of HEAD).

- [ ] **Step 2: Confirm `purchase-common` exists**

Run: `ls app/purchase-common/build.gradle.kts && ls app/purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/offer/SelectTierDestination.kt`
Expected: Both files exist (no `No such file or directory` errors).

If either check fails, stop — the branch is not based off `feat/in-app-car-purchase`. Rebase or branch correctly before continuing.

---

### Task 1: Create `feature-purchase-house` module scaffold

**Files:**
- Create: `app/feature/feature-purchase-house/build.gradle.kts`

- [ ] **Step 1: Create `build.gradle.kts`**

File: `app/feature/feature-purchase-house/build.gradle.kts`

```kotlin
plugins {
  id("hedvig.android.library")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
  compose()
}

android {
  testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
  api(libs.androidx.navigation.common)

  implementation(libs.androidx.navigation.compose)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.koin.composeViewModel)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.dataCrossSellAfterFlow)
  implementation(projects.designSystemHedvig)
  implementation(projects.purchaseCommon)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
}
```

- [ ] **Step 2: Verify Gradle picks up the new module**

Run: `./gradlew :feature-purchase-house:tasks --quiet | head -5`
Expected: Gradle prints tasks (no `Project ... not found` error). The settings.gradle.kts auto-discovers everything under `app/`.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/build.gradle.kts
git commit -m "feat: scaffold feature-purchase-house module"
```

---

### Task 2: Add GraphQL operations

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/graphql/HouseShopSessionCreateMutation.graphql`
- Create: `app/feature/feature-purchase-house/src/main/graphql/HousePriceIntentCreateMutation.graphql`
- Create: `app/feature/feature-purchase-house/src/main/graphql/HousePriceIntentDataUpdateMutation.graphql`
- Create: `app/feature/feature-purchase-house/src/main/graphql/HousePriceIntentConfirmMutation.graphql`
- Create: `app/feature/feature-purchase-house/src/main/graphql/HouseMemberContactInfoQuery.graphql`
- Create: `app/feature/feature-purchase-house/src/main/graphql/HouseProductOfferFragment.graphql`

- [ ] **Step 1: Create `HouseShopSessionCreateMutation.graphql`**

```graphql
mutation HouseShopSessionCreate($countryCode: CountryCode!) {
  shopSessionCreate(input: { countryCode: $countryCode }) {
    id
  }
}
```

- [ ] **Step 2: Create `HousePriceIntentCreateMutation.graphql`**

```graphql
mutation HousePriceIntentCreate($shopSessionId: UUID!, $productName: String!) {
  priceIntentCreate(input: { shopSessionId: $shopSessionId, productName: $productName }) {
    id
  }
}
```

- [ ] **Step 3: Create `HousePriceIntentDataUpdateMutation.graphql`**

```graphql
mutation HousePriceIntentDataUpdate($priceIntentId: UUID!, $data: PricingFormData!) {
  priceIntentDataUpdate(priceIntentId: $priceIntentId, data: $data) {
    priceIntent {
      id
    }
    userError {
      message
    }
  }
}
```

- [ ] **Step 4: Create `HousePriceIntentConfirmMutation.graphql`**

```graphql
mutation HousePriceIntentConfirm($priceIntentId: UUID!) {
  priceIntentConfirm(priceIntentId: $priceIntentId) {
    priceIntent {
      id
      offers {
        ...HouseProductOfferFragment
      }
    }
    userError {
      message
    }
  }
}
```

- [ ] **Step 5: Create `HouseMemberContactInfoQuery.graphql`**

```graphql
query HouseMemberContactInfo {
  currentMember {
    id
    ssn
    email
  }
}
```

- [ ] **Step 6: Create `HouseProductOfferFragment.graphql`**

```graphql
fragment HouseProductOfferFragment on ProductOffer {
  id
  variant {
    displayName
    displayNameSubtype
    displayNameTier
    tierDescription
    typeOfContract
    perils {
      title
      description
      colorCode
      covered
      info
    }
    documents {
      type
      displayName
      url
    }
  }
  cost {
    gross {
      ...MoneyFragment
    }
    net {
      ...MoneyFragment
    }
    discountsV2 {
      amount {
        ...MoneyFragment
      }
    }
  }
  startDate
  deductible {
    displayName
    amount
  }
  usps
  exposure {
    displayNameShort
  }
  bundleDiscount {
    isEligible
    potentialYearlySavings {
      ...MoneyFragment
    }
  }
}
```

- [ ] **Step 7: Run Apollo codegen and assemble**

Run: `./gradlew :feature-purchase-house:generateApolloSources :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL. Generated Kotlin classes appear under `app/feature/feature-purchase-house/build/generated/source/apollo/octopus/octopus/`.

- [ ] **Step 8: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/graphql/
git commit -m "feat: add GraphQL operations for house purchase module"
```

---

### Task 3: Add domain models

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/HousePurchaseModels.kt`

- [ ] **Step 1: Create `HousePurchaseModels.kt`**

```kotlin
package com.hedvig.android.feature.purchase.house.data

import com.hedvig.android.core.uidata.UiMoney

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
  val ssn: String,
  val email: String,
)

internal data class HouseOffers(
  val productDisplayName: String,
  val offers: List<HouseTierOffer>,
)

internal data class HouseTierOffer(
  val offerId: String,
  val tierDisplayName: String,
  val tierDescription: String,
  val grossPrice: UiMoney,
  val netPrice: UiMoney,
  val usps: List<String>,
  val exposureDisplayName: String,
  val deductibleDisplayName: String?,
  val hasDiscount: Boolean,
)
```

- [ ] **Step 2: Build to verify compilation**

Run: `./gradlew :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/HousePurchaseModels.kt
git commit -m "feat: add domain models for house purchase module"
```

---

### Task 4: Add `CreateHouseSessionAndPriceIntentUseCase`

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/CreateHouseSessionAndPriceIntentUseCase.kt`

- [ ] **Step 1: Create the use case**

```kotlin
package com.hedvig.android.feature.purchase.house.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.HouseMemberContactInfoQuery
import octopus.HousePriceIntentCreateMutation
import octopus.HouseShopSessionCreateMutation
import octopus.type.CountryCode

internal interface CreateHouseSessionAndPriceIntentUseCase {
  suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent>
}

internal class CreateHouseSessionAndPriceIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateHouseSessionAndPriceIntentUseCase {
  override suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent> {
    return either {
      val shopSessionId = apolloClient
        .mutation(HouseShopSessionCreateMutation(CountryCode.SE))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create shop session: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCreate.id },
        )

      val priceIntentId = apolloClient
        .mutation(HousePriceIntentCreateMutation(shopSessionId = shopSessionId, productName = productName))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create price intent: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentCreate.id },
        )

      val member = apolloClient
        .query(HouseMemberContactInfoQuery())
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to fetch member contact info: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.currentMember },
        )
      val ssn = member.ssn
      if (ssn == null) {
        logcat(LogPriority.ERROR) { "Member is missing SSN — cannot continue house purchase" }
        raise(ErrorMessage())
      }

      SessionAndIntent(
        shopSessionId = shopSessionId,
        priceIntentId = priceIntentId,
        ssn = ssn,
        email = member.email,
      )
    }
  }
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `./gradlew :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/CreateHouseSessionAndPriceIntentUseCase.kt
git commit -m "feat: add CreateHouseSessionAndPriceIntentUseCase"
```

---

### Task 5: Add `SubmitVacationHomeFormAndGetOffersUseCase`

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt`

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

internal interface SubmitVacationHomeFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    multipleOwners: Boolean,
    yearOfConstruction: Int,
    livingSpace: Int,
    hasWaterConnected: Boolean,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
  ): Either<ErrorMessage, HouseOffers>
}

internal class SubmitVacationHomeFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitVacationHomeFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    email: String,
    street: String,
    zipCode: String,
    multipleOwners: Boolean,
    yearOfConstruction: Int,
    livingSpace: Int,
    hasWaterConnected: Boolean,
    numberOfBathrooms: Int,
    isSubleted: Boolean,
  ): Either<ErrorMessage, HouseOffers> {
    return either {
      val formData = buildMap<String, Any> {
        put("ssn", ssn)
        put("email", email)
        put("street", street)
        put("zipCode", zipCode)
        put("multipleOwners", multipleOwners)
        put("yearOfConstruction", yearOfConstruction)
        put("livingSpace", livingSpace)
        put("hasWaterConnected", hasWaterConnected)
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
        offers = offers.map { it.toHouseTierOffer() },
      )
    }
  }
}

internal fun HouseProductOfferFragment.toHouseTierOffer(): HouseTierOffer {
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

- [ ] **Step 2: Build to verify compilation**

Run: `./gradlew :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/data/SubmitVacationHomeFormAndGetOffersUseCase.kt
git commit -m "feat: add SubmitVacationHomeFormAndGetOffersUseCase"
```

---

### Task 6: Add `VacationHomeFormViewModel`

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormViewModel.kt`

- [ ] **Step 1: Create the ViewModel + Presenter**

```kotlin
package com.hedvig.android.feature.purchase.house.ui.vacationhome

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
import com.hedvig.android.feature.purchase.house.data.SubmitVacationHomeFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import java.time.LocalDate

internal class VacationHomeFormViewModel(
  productName: String,
  createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  submitVacationHomeFormAndGetOffersUseCase: SubmitVacationHomeFormAndGetOffersUseCase,
) : MoleculeViewModel<VacationHomeFormEvent, VacationHomeFormState>(
    initialState = VacationHomeFormState(),
    presenter = VacationHomeFormPresenter(
      productName,
      createHouseSessionAndPriceIntentUseCase,
      submitVacationHomeFormAndGetOffersUseCase,
    ),
  )

internal sealed interface VacationHomeFormEvent {
  data class SubmitForm(
    val street: String,
    val zipCode: String,
    val multipleOwners: Boolean?,
    val yearOfConstruction: String,
    val livingSpace: String,
    val hasWaterConnected: Boolean?,
    val numberOfBathrooms: Int,
    val isSubleted: Boolean?,
  ) : VacationHomeFormEvent

  data object ClearNavigation : VacationHomeFormEvent

  data object Retry : VacationHomeFormEvent

  data object DismissError : VacationHomeFormEvent
}

internal data class VacationHomeFormState(
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val multipleOwnersError: String? = null,
  val yearOfConstructionError: String? = null,
  val livingSpaceError: String? = null,
  val hasWaterConnectedError: String? = null,
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

private class VacationHomeFormPresenter(
  private val productName: String,
  private val createHouseSessionAndPriceIntentUseCase: CreateHouseSessionAndPriceIntentUseCase,
  private val submitVacationHomeFormAndGetOffersUseCase: SubmitVacationHomeFormAndGetOffersUseCase,
) : MoleculePresenter<VacationHomeFormEvent, VacationHomeFormState> {
  @Composable
  override fun MoleculePresenterScope<VacationHomeFormEvent>.present(
    lastState: VacationHomeFormState,
  ): VacationHomeFormState {
    var currentState by remember { mutableStateOf(lastState) }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var sessionLoadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: VacationHomeFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is VacationHomeFormEvent.SubmitForm -> {
          val errors = validate(
            street = event.street,
            zipCode = event.zipCode,
            multipleOwners = event.multipleOwners,
            yearOfConstruction = event.yearOfConstruction,
            livingSpace = event.livingSpace,
            hasWaterConnected = event.hasWaterConnected,
            isSubleted = event.isSubleted,
          )
          if (errors.hasErrors()) {
            currentState = currentState.copy(
              streetError = errors.streetError,
              zipCodeError = errors.zipCodeError,
              multipleOwnersError = errors.multipleOwnersError,
              yearOfConstructionError = errors.yearOfConstructionError,
              livingSpaceError = errors.livingSpaceError,
              hasWaterConnectedError = errors.hasWaterConnectedError,
              isSubletedError = errors.isSubletedError,
            )
          } else {
            currentState = currentState.copy(
              streetError = null,
              zipCodeError = null,
              multipleOwnersError = null,
              yearOfConstructionError = null,
              livingSpaceError = null,
              hasWaterConnectedError = null,
              isSubletedError = null,
            )
            pendingSubmit = event
            submitIteration++
          }
        }

        VacationHomeFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }

        VacationHomeFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            sessionLoadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }

        VacationHomeFormEvent.DismissError -> {
          currentState = currentState.copy(submitError = null)
        }
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
      val multipleOwners = submit.multipleOwners ?: return@LaunchedEffect
      val yearOfConstruction = submit.yearOfConstruction.toIntOrNull() ?: return@LaunchedEffect
      val livingSpace = submit.livingSpace.toIntOrNull() ?: return@LaunchedEffect
      val hasWaterConnected = submit.hasWaterConnected ?: return@LaunchedEffect
      val isSubleted = submit.isSubleted ?: return@LaunchedEffect
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)
      submitVacationHomeFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        ssn = session.ssn,
        email = session.email,
        street = submit.street,
        zipCode = submit.zipCode,
        multipleOwners = multipleOwners,
        yearOfConstruction = yearOfConstruction,
        livingSpace = livingSpace,
        hasWaterConnected = hasWaterConnected,
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
  val multipleOwnersError: String?,
  val yearOfConstructionError: String?,
  val livingSpaceError: String?,
  val hasWaterConnectedError: String?,
  val isSubletedError: String?,
) {
  fun hasErrors(): Boolean = streetError != null ||
    zipCodeError != null ||
    multipleOwnersError != null ||
    yearOfConstructionError != null ||
    livingSpaceError != null ||
    hasWaterConnectedError != null ||
    isSubletedError != null
}

private fun validate(
  street: String,
  zipCode: String,
  multipleOwners: Boolean?,
  yearOfConstruction: String,
  livingSpace: String,
  hasWaterConnected: Boolean?,
  isSubleted: Boolean?,
): ValidationErrors {
  val currentYear = LocalDate.now().year
  return ValidationErrors(
    streetError = if (street.isBlank()) "Ange en adress" else null,
    zipCodeError = when {
      zipCode.length != 5 -> "Ange ett giltigt postnummer (5 siffror)"
      !zipCode.all { it.isDigit() } -> "Postnumret får bara innehålla siffror"
      else -> null
    },
    multipleOwnersError = if (multipleOwners == null) "Välj ett alternativ" else null,
    yearOfConstructionError = when (val year = yearOfConstruction.toIntOrNull()) {
      null -> "Ange byggår"
      !in 1700..currentYear -> "Ange ett giltigt byggår"
      else -> null
    },
    livingSpaceError = when (val space = livingSpace.toIntOrNull()) {
      null -> "Ange boyta"
      !in 1..Int.MAX_VALUE -> "Ange en giltig boyta"
      else -> null
    },
    hasWaterConnectedError = if (hasWaterConnected == null) "Välj ett alternativ" else null,
    isSubletedError = if (isSubleted == null) "Välj ett alternativ" else null,
  )
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `./gradlew :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormViewModel.kt
git commit -m "feat: add VacationHomeFormViewModel"
```

---

### Task 7: Add `VacationHomeFormDestination`

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormDestination.kt`

- [ ] **Step 1: Create the composable**

```kotlin
package com.hedvig.android.feature.purchase.house.ui.vacationhome

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
import androidx.compose.runtime.remember
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
internal fun VacationHomeFormDestination(
  viewModel: VacationHomeFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: HouseOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(VacationHomeFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    when {
      uiState.isLoadingSession -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      uiState.loadSessionError -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(VacationHomeFormEvent.Retry) },
        )
      }

      else -> {
        var street by remember { mutableStateOf("") }
        var zipCode by remember { mutableStateOf("") }
        var multipleOwners by remember { mutableStateOf<Boolean?>(null) }
        var yearOfConstruction by remember { mutableStateOf("") }
        var livingSpace by remember { mutableStateOf("") }
        var hasWaterConnected by remember { mutableStateOf<Boolean?>(null) }
        var numberOfBathrooms by remember { mutableIntStateOf(1) }
        var isSubleted by remember { mutableStateOf<Boolean?>(null) }

        if (uiState.submitError != null) {
          ErrorDialog(
            title = "Något gick fel",
            message = uiState.submitError,
            onDismiss = { viewModel.emit(VacationHomeFormEvent.DismissError) },
          )
        }
        VacationHomeFormContent(
          street = street,
          zipCode = zipCode,
          multipleOwners = multipleOwners,
          yearOfConstruction = yearOfConstruction,
          livingSpace = livingSpace,
          hasWaterConnected = hasWaterConnected,
          numberOfBathrooms = numberOfBathrooms,
          isSubleted = isSubleted,
          streetError = uiState.streetError,
          zipCodeError = uiState.zipCodeError,
          multipleOwnersError = uiState.multipleOwnersError,
          yearOfConstructionError = uiState.yearOfConstructionError,
          livingSpaceError = uiState.livingSpaceError,
          hasWaterConnectedError = uiState.hasWaterConnectedError,
          isSubletedError = uiState.isSubletedError,
          isSubmitting = uiState.isSubmitting,
          onStreetChanged = { street = it },
          onZipCodeChanged = { value -> if (value.all { it.isDigit() } && value.length <= 5) zipCode = value },
          onMultipleOwnersChanged = { multipleOwners = it },
          onYearOfConstructionChanged = { value ->
            if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 4)) yearOfConstruction = value
          },
          onLivingSpaceChanged = { value ->
            if (value.isEmpty() || value.toIntOrNull() != null) livingSpace = value
          },
          onHasWaterConnectedChanged = { hasWaterConnected = it },
          onNumberOfBathroomsChanged = { numberOfBathrooms = it },
          onIsSubletedChanged = { isSubleted = it },
          onSubmit = {
            viewModel.emit(
              VacationHomeFormEvent.SubmitForm(
                street = street,
                zipCode = zipCode,
                multipleOwners = multipleOwners,
                yearOfConstruction = yearOfConstruction,
                livingSpace = livingSpace,
                hasWaterConnected = hasWaterConnected,
                numberOfBathrooms = numberOfBathrooms,
                isSubleted = isSubleted,
              ),
            )
          },
        )
      }
    }
  }
}

@Composable
private fun VacationHomeFormContent(
  street: String,
  zipCode: String,
  multipleOwners: Boolean?,
  yearOfConstruction: String,
  livingSpace: String,
  hasWaterConnected: Boolean?,
  numberOfBathrooms: Int,
  isSubleted: Boolean?,
  streetError: String?,
  zipCodeError: String?,
  multipleOwnersError: String?,
  yearOfConstructionError: String?,
  livingSpaceError: String?,
  hasWaterConnectedError: String?,
  isSubletedError: String?,
  isSubmitting: Boolean,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onMultipleOwnersChanged: (Boolean) -> Unit,
  onYearOfConstructionChanged: (String) -> Unit,
  onLivingSpaceChanged: (String) -> Unit,
  onHasWaterConnectedChanged: (Boolean) -> Unit,
  onNumberOfBathroomsChanged: (Int) -> Unit,
  onIsSubletedChanged: (Boolean) -> Unit,
  onSubmit: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    // TODO: Add "Fill in your details and we'll calculate your price" / "Fyll i dina uppgifter så beräknar vi ditt pris" to Lokalise
    HedvigText(
      text = "Fyll i dina uppgifter så beräknar vi ditt pris",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(16.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      // TODO: Add "Address" / "Adress" to Lokalise
      HedvigTextField(
        text = street,
        onValueChange = onStreetChanged,
        labelText = "Adress",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = streetError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      // TODO: Add "Postal code" / "Postnummer" to Lokalise
      HedvigTextField(
        text = zipCode,
        onValueChange = onZipCodeChanged,
        labelText = "Postnummer",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = zipCodeError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Do you own the house with someone else?" / "Äger du huset tillsammans med någon annan?" to Lokalise
      HedvigText(
        text = "Äger du huset tillsammans med någon annan?",
        style = HedvigTheme.typography.bodyMedium,
      )
      YesNoRadio(
        selected = multipleOwners,
        onSelectionChanged = onMultipleOwnersChanged,
        enabled = !isSubmitting,
        errorText = multipleOwnersError,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Year built" / "Byggår" to Lokalise
      HedvigTextField(
        text = yearOfConstruction,
        onValueChange = onYearOfConstructionChanged,
        labelText = "Byggår",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = yearOfConstructionError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )
      // TODO: Add "Living space (m²)" / "Boyta (kvm)" to Lokalise
      HedvigTextField(
        text = livingSpace,
        onValueChange = onLivingSpaceChanged,
        labelText = "Boyta (kvm)",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = livingSpaceError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Is water connected?" / "Är vatten anslutet?" to Lokalise
      HedvigText(
        text = "Är vatten anslutet?",
        style = HedvigTheme.typography.bodyMedium,
      )
      YesNoRadio(
        selected = hasWaterConnected,
        onSelectionChanged = onHasWaterConnectedChanged,
        enabled = !isSubmitting,
        errorText = hasWaterConnectedError,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Number of bathrooms" / "Antal badrum" to Lokalise
      HedvigStepper(
        text = when (numberOfBathrooms) {
          1 -> "1 badrum"
          else -> "$numberOfBathrooms badrum"
        },
        stepperSize = Medium,
        stepperStyle = Labeled("Antal badrum"),
        onMinusClick = { onNumberOfBathroomsChanged(numberOfBathrooms - 1) },
        onPlusClick = { onNumberOfBathroomsChanged(numberOfBathrooms + 1) },
        isPlusEnabled = !isSubmitting && numberOfBathrooms < 10,
        isMinusEnabled = !isSubmitting && numberOfBathrooms > 1,
      )

      Spacer(Modifier.height(8.dp))
      // TODO: Add "Do you sublet all or parts of the house?" / "Hyr du ut hela eller delar av huset?" to Lokalise
      HedvigText(
        text = "Hyr du ut hela eller delar av huset?",
        style = HedvigTheme.typography.bodyMedium,
      )
      YesNoRadio(
        selected = isSubleted,
        onSelectionChanged = onIsSubletedChanged,
        enabled = !isSubmitting,
        errorText = isSubletedError,
      )
    }
    Spacer(Modifier.height(16.dp))
    // TODO: Add "Calculate price" / "Beräkna pris" to Lokalise
    HedvigButton(
      text = "Beräkna pris",
      onClick = onSubmit,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

private const val OPTION_YES = "YES"
private const val OPTION_NO = "NO"

@Composable
private fun YesNoRadio(
  selected: Boolean?,
  onSelectionChanged: (Boolean) -> Unit,
  enabled: Boolean,
  errorText: String?,
) {
  val options = listOf(
    // TODO: Add "Yes" / "Ja" to Lokalise
    RadioOption(id = RadioOptionId(OPTION_YES), text = "Ja"),
    // TODO: Add "No" / "Nej" to Lokalise
    RadioOption(id = RadioOptionId(OPTION_NO), text = "Nej"),
  )
  val selectedId = when (selected) {
    true -> RadioOptionId(OPTION_YES)
    false -> RadioOptionId(OPTION_NO)
    null -> null
  }
  RadioGroup(
    options = options,
    selectedOption = selectedId,
    onRadioOptionSelected = { id ->
      onSelectionChanged(id == RadioOptionId(OPTION_YES))
    },
    style = RadioGroupStyle.Horizontal,
    enabled = enabled,
    modifier = Modifier.fillMaxWidth(),
  )
  if (errorText != null) {
    HedvigText(
      text = errorText,
      style = HedvigTheme.typography.label,
      color = HedvigTheme.colorScheme.signalRedElement,
    )
  }
}

private fun String?.toErrorState(): HedvigTextFieldDefaults.ErrorState {
  return if (this != null) {
    HedvigTextFieldDefaults.ErrorState.Error.WithMessage(this)
  } else {
    HedvigTextFieldDefaults.ErrorState.NoError
  }
}

@HedvigPreview
@Composable
private fun PreviewVacationHomeFormEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VacationHomeFormContent(
        street = "",
        zipCode = "",
        multipleOwners = null,
        yearOfConstruction = "",
        livingSpace = "",
        hasWaterConnected = null,
        numberOfBathrooms = 1,
        isSubleted = null,
        streetError = null,
        zipCodeError = null,
        multipleOwnersError = null,
        yearOfConstructionError = null,
        livingSpaceError = null,
        hasWaterConnectedError = null,
        isSubletedError = null,
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onMultipleOwnersChanged = {},
        onYearOfConstructionChanged = {},
        onLivingSpaceChanged = {},
        onHasWaterConnectedChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedChanged = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewVacationHomeFormFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VacationHomeFormContent(
        street = "Storgatan 1",
        zipCode = "12345",
        multipleOwners = false,
        yearOfConstruction = "1985",
        livingSpace = "60",
        hasWaterConnected = true,
        numberOfBathrooms = 1,
        isSubleted = false,
        streetError = null,
        zipCodeError = null,
        multipleOwnersError = null,
        yearOfConstructionError = null,
        livingSpaceError = null,
        hasWaterConnectedError = null,
        isSubletedError = null,
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onMultipleOwnersChanged = {},
        onYearOfConstructionChanged = {},
        onLivingSpaceChanged = {},
        onHasWaterConnectedChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedChanged = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewVacationHomeFormErrors() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      VacationHomeFormContent(
        street = "",
        zipCode = "12",
        multipleOwners = null,
        yearOfConstruction = "1500",
        livingSpace = "",
        hasWaterConnected = null,
        numberOfBathrooms = 1,
        isSubleted = null,
        streetError = "Ange en adress",
        zipCodeError = "Ange ett giltigt postnummer (5 siffror)",
        multipleOwnersError = "Välj ett alternativ",
        yearOfConstructionError = "Ange ett giltigt byggår",
        livingSpaceError = "Ange boyta",
        hasWaterConnectedError = "Välj ett alternativ",
        isSubletedError = "Välj ett alternativ",
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onMultipleOwnersChanged = {},
        onYearOfConstructionChanged = {},
        onLivingSpaceChanged = {},
        onHasWaterConnectedChanged = {},
        onNumberOfBathroomsChanged = {},
        onIsSubletedChanged = {},
        onSubmit = {},
      )
    }
  }
}
```

- [ ] **Step 2: Build and check ktlint**

Run: `./gradlew :feature-purchase-house:assemble :feature-purchase-house:ktlintFormat :feature-purchase-house:ktlintCheck`
Expected: BUILD SUCCESSFUL on all three; any auto-formatting from `ktlintFormat` is already applied before `ktlintCheck` runs.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/ui/vacationhome/VacationHomeFormDestination.kt
git commit -m "feat: add VacationHomeFormDestination with Compose previews"
```

---

### Task 8: Add navigation destinations + nav graph

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/navigation/HousePurchaseDestination.kt`
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/navigation/HousePurchaseNavGraph.kt`

- [ ] **Step 1: Create `HousePurchaseDestination.kt`**

```kotlin
package com.hedvig.android.feature.purchase.house.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class HousePurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface HousePurchaseDestination {
  @Serializable
  data object Form : HousePurchaseDestination, Destination
}
```

- [ ] **Step 2: Create `HousePurchaseNavGraph.kt`**

```kotlin
package com.hedvig.android.feature.purchase.house.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Signing
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.SelectTier
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Success
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Summary
import com.hedvig.android.feature.purchase.common.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierDestination
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.common.ui.sign.SigningDestination
import com.hedvig.android.feature.purchase.common.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryDestination
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryViewModel
import com.hedvig.android.feature.purchase.house.navigation.HousePurchaseDestination.Form
import com.hedvig.android.feature.purchase.house.ui.vacationhome.VacationHomeFormDestination
import com.hedvig.android.feature.purchase.house.ui.vacationhome.VacationHomeFormViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.housePurchaseNavGraph(
  navController: NavController,
  popBackStack: () -> Unit,
  finishApp: () -> Unit,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) {
  navgraph<HousePurchaseGraphDestination>(
    startDestination = Form::class,
  ) {
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

    navdestination<SelectTier>(SelectTier) { backStackEntry ->
      val route = backStackEntry.toRoute<SelectTier>()
      val viewModel: SelectTierViewModel = koinViewModel {
        parametersOf(route.params)
      }
      SelectTierDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        onContinueToSummary = { params -> navController.navigate(Summary(params)) },
      )
    }

    navdestination<Summary>(Summary) { backStackEntry ->
      val route = backStackEntry.toRoute<Summary>()
      val viewModel: PurchaseSummaryViewModel = koinViewModel {
        parametersOf(route.params)
      }
      PurchaseSummaryDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        navigateToSigning = { params -> navController.navigate(Signing(params)) },
      )
    }

    navdestination<Signing>(Signing) { backStackEntry ->
      val route = backStackEntry.toRoute<Signing>()
      val viewModel: SigningViewModel = koinViewModel {
        parametersOf(route.params)
      }
      SigningDestination(
        viewModel = viewModel,
        navigateToSuccess = { startDate ->
          crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
            CrossSellInfoType.Purchase,
          )
          navController.navigate(Success(startDate)) {
            typedPopUpTo<HousePurchaseGraphDestination>({ inclusive = true })
          }
        },
      )
    }
  }
}
```

- [ ] **Step 3: Build and check ktlint**

Run: `./gradlew :feature-purchase-house:assemble :feature-purchase-house:ktlintFormat :feature-purchase-house:ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/navigation/
git commit -m "feat: add navigation graph for house purchase module"
```

---

### Task 9: Add DI module

**Files:**
- Create: `app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/di/HousePurchaseModule.kt`

- [ ] **Step 1: Create the Koin module**

```kotlin
package com.hedvig.android.feature.purchase.house.di

import com.hedvig.android.feature.purchase.house.data.CreateHouseSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.house.data.CreateHouseSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.house.data.SubmitVacationHomeFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.house.data.SubmitVacationHomeFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.house.ui.vacationhome.VacationHomeFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val housePurchaseModule = module {
  single<CreateHouseSessionAndPriceIntentUseCase> { CreateHouseSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<SubmitVacationHomeFormAndGetOffersUseCase> { SubmitVacationHomeFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<VacationHomeFormViewModel> { params ->
    VacationHomeFormViewModel(
      productName = params.get(),
      createHouseSessionAndPriceIntentUseCase = get(),
      submitVacationHomeFormAndGetOffersUseCase = get(),
    )
  }
}
```

- [ ] **Step 2: Build**

Run: `./gradlew :feature-purchase-house:assemble`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-house/src/main/kotlin/com/hedvig/android/feature/purchase/house/di/HousePurchaseModule.kt
git commit -m "feat: add Koin module for house purchase"
```

---

### Task 10: Wire `feature-insurances` cross-sell routing

**Files:**
- Modify: `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt`

- [ ] **Step 1: Add `onNavigateToHousePurchase` parameter to the graph function**

In `InsuranceGraph.kt`, find the existing parameter list that includes `onNavigateToCarPurchase: (productName: String) -> Unit` and add:

```kotlin
onNavigateToHousePurchase: (productName: String) -> Unit,
```

Place it directly after `onNavigateToCarPurchase` in the function signature for consistency with how callbacks are ordered.

- [ ] **Step 2: Route fritidshus URLs in `onCrossSellClick`**

In the same file, find the `when { ... }` block inside `onCrossSellClick = dropUnlessResumed { url: String -> ... }`. Replace it with:

```kotlin
when {
  "fritidshusforsakring" in lower || "vacation-home" in lower ->
    onNavigateToHousePurchase("SE_VACATION_HOME")
  "car-insurance" in lower || "bilforsakring" in lower ->
    onNavigateToCarPurchase("SE_CAR")
  "bostadsratt" in lower || "home-insurance/homeowner" in lower ->
    onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
  "hyresratt" in lower || "home-insurance" in lower || "hemforsakring" in lower ->
    onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
  else -> openUrl(url)
}
```

The fritidshus branch is first; even though there's no current substring conflict, this ordering defends against future SE_HOUSE additions where `hemforsakring/villaforsakring` would otherwise be stolen by the apartment branch.

- [ ] **Step 3: Build and verify**

Run: `./gradlew :feature-insurances:assemble :feature-insurances:ktlintFormat :feature-insurances:ktlintCheck`
Expected: BUILD SUCCESSFUL. (At this point the `app` module won't compile yet because it doesn't pass `onNavigateToHousePurchase`; that's wired in Task 11.)

- [ ] **Step 4: Commit**

```bash
git add app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt
git commit -m "feat: route fritidshus cross-sells to in-app purchase flow"
```

---

### Task 11: Wire `housePurchaseModule` and `housePurchaseNavGraph` into the app

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`

- [ ] **Step 1: Register `housePurchaseModule` in `ApplicationModule.kt`**

Add this import (place near the other purchase-module imports, alphabetically with `carPurchaseModule`):

```kotlin
import com.hedvig.android.feature.purchase.house.di.housePurchaseModule
```

Then add `housePurchaseModule,` to the `includes(listOf(...))` block — insert it directly after `carPurchaseModule,`:

```kotlin
val applicationModule = module {
  includes(
    listOf(
      addonPurchaseModule,
      addonRemovalModule,
      apartmentPurchaseModule,
      carPurchaseModule,
      housePurchaseModule,
      // ... existing modules continue ...
```

- [ ] **Step 2: Add nav graph import in `HedvigNavHost.kt`**

Add this import (place near other `feature.purchase.car.navigation` imports):

```kotlin
import com.hedvig.android.feature.purchase.house.navigation.HousePurchaseGraphDestination
import com.hedvig.android.feature.purchase.house.navigation.housePurchaseNavGraph
```

- [ ] **Step 3: Wire the insurances graph callback**

In `HedvigNavHost.kt`, find the existing insurances graph callsite (look for `onNavigateToCarPurchase = { productName -> navController.navigate(CarPurchaseGraphDestination(productName)) },`). Add directly after it:

```kotlin
      onNavigateToHousePurchase = { productName ->
        navController.navigate(HousePurchaseGraphDestination(productName))
      },
```

- [ ] **Step 4: Register the nav graph in `HedvigNavHost`**

Find the existing `carPurchaseNavGraph(...)` call and add `housePurchaseNavGraph(...)` directly after it, with the same arguments:

```kotlin
    carPurchaseNavGraph(
      navController = navController,
      popBackStack = popBackStackOrFinish,
      finishApp = finishApp,
      crossSellAfterFlowRepository = crossSellAfterFlowRepository,
    )
    housePurchaseNavGraph(
      navController = navController,
      popBackStack = popBackStackOrFinish,
      finishApp = finishApp,
      crossSellAfterFlowRepository = crossSellAfterFlowRepository,
    )
```

- [ ] **Step 5: Build the full app**

Run: `./gradlew :app:assembleDevelopDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Run ktlint across modified modules**

Run: `./gradlew :app:ktlintFormat :app:ktlintCheck :feature-purchase-house:ktlintCheck :feature-insurances:ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt \
        app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt
git commit -m "feat: integrate house purchase flow into main app navigation"
```

---

### Task 12: Manual emulator verification

**Files:** none (verification only)

This task follows the `verifying-android-changes-in-emulator` skill. Manual verification is required; type-checking and unit tests are not sufficient evidence for UI/navigation work.

- [ ] **Step 1: Install on emulator**

Run: `./gradlew :app:installDevelopDebug`
Expected: app installed on the running emulator. If no emulator is running, start one first.

- [ ] **Step 2: Verify golden path**

1. Open the app, navigate to **Insurances** tab.
2. Trigger a fritidshus cross-sell. Easiest path: use a deep-link test or inject a cross-sell URL containing `fritidshusforsakring` via debug menu, or follow the natural cross-sell surface for this user.
3. Verify the form opens.
4. Fill in all fields with valid values (any plausible street, zip `12345`, year `1985`, livingSpace `60`, bathrooms `1`, all three radios `Ja`/`Nej`).
5. Tap "Beräkna pris".
6. Verify `SelectTier` screen appears with BAS and STANDARD offers.
7. Select STANDARD, continue to Summary.
8. Continue to Signing — BankID flow should start.
9. Complete signing on the test BankID app (or use the QR fallback if testing on a different device).
10. Verify `PurchaseSuccess` screen appears with a start date.
11. Tap close — verify navigation returns to the Insurances tab (not closing the app).

- [ ] **Step 3: Verify form validation edge cases**

For each of these, observe that the right error appears under the field and submission is blocked:
1. Submit with all fields empty → 7 validation errors (radio errors below each radio group, text errors inside each text field).
2. Enter zip code `123` (3 digits) → "Ange ett giltigt postnummer (5 siffror)".
3. Enter yearOfConstruction `1500` → "Ange ett giltigt byggår".
4. Enter livingSpace `0` → "Ange en giltig boyta".

- [ ] **Step 4: Verify error states**

1. Turn off device wifi/data, open the flow → `HedvigErrorSection` appears after the session-create call fails. Tap retry; turn data back on; verify the form loads.
2. Fill the form with valid values but a backend-rejected payload (e.g. a yearOfConstruction that the backend rejects, if known). Verify the `ErrorDialog` appears with the userError message. Dismiss and verify the form is interactive again.

- [ ] **Step 5: Verify navigate-up at each step**

From each of: Form, SelectTier, Summary, Signing — press the back arrow / system back. Confirm the user lands one step back, and that pressing back from Form returns to the Insurances tab (not closing the app).

- [ ] **Step 6: Record verification notes**

Record what worked and any deviations in the eventual PR description. If anything failed, fix it and re-run from Step 1.

---

### Task 13: PR-prep, ktlint, full build

**Files:** none

- [ ] **Step 1: Full app build (release flavor for sanity)**

Run: `./gradlew :app:assembleStagingDebug`
Expected: BUILD SUCCESSFUL. (Staging flavor catches any release-build-only issues.)

- [ ] **Step 2: Top-level ktlint check**

Run: `./gradlew ktlintCheck`
Expected: BUILD SUCCESSFUL across all modules.

- [ ] **Step 3: Lint the new module**

Run: `./gradlew :feature-purchase-house:lint`
Expected: no new lint errors. Address any new findings before opening PR.

- [ ] **Step 4: Verify nothing unintended is staged**

Run: `git status` and `git log feat/in-app-car-purchase..HEAD --oneline`
Expected: clean working tree; commit list reads as a coherent feature progression.

- [ ] **Step 5: Open PR**

Push the branch and open a PR against `feat/in-app-car-purchase` (or `develop` if car has merged by now). Title format (no Notion ID, since this is `feat`-scoped but the user can attach one if applicable): `Add in-app vacation home (fritidshus) purchase flow`. Body should summarize:
- New `feature-purchase-house` module hosting `VacationHomeFormDestination`
- Cross-sell URL routing for `fritidshusforsakring` / `vacation-home`
- Extra-buildings UI deferred to follow-up PR (empty list sent in V1)
- SE_HOUSE form deferred to follow-up PR (module is forward-named to accommodate it)
- Pre-existing apartment-substring routing risk for future SE_HOUSE URLs (flagged for that PR, not fixed here)

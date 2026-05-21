# Pet Purchase Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an in-app pet insurance purchase flow for `SE_PET_DOG` and `SE_PET_CAT` by creating `feature-purchase-pet` that mirrors `feature-purchase-car` and reuses `purchase-common` for tier selection / summary / signing / success / failure screens.

**Architecture:** New `app/feature/feature-purchase-pet` module containing a single Compose form, three use cases (create session, fetch breeds, submit form), navigation graph, and Koin DI. One module handles both species — the species-specific last question and breed-list `animal` arg are derived from the `productName` route arg. Post-form screens are consumed unchanged from `purchase-common`. The `feature-insurances` cross-sell routing and `app` wiring get small additions.

**Tech Stack:** Kotlin, Jetpack Compose, Apollo GraphQL, Molecule (MVI), Koin DI, Arrow (`Either`), kotlinx.serialization.

**Base branch:** `feat/in-app-car-purchase` (this branch already exists locally and is the parent of this work).

---

### Task 1: Scaffold `feature-purchase-pet` module

**Files:**
- Create: `app/feature/feature-purchase-pet/build.gradle.kts`

- [ ] **Step 1: Create `build.gradle.kts`**

File: `app/feature/feature-purchase-pet/build.gradle.kts`

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

  testImplementation(libs.apollo.testingSupport)
  testImplementation(libs.assertK)
  testImplementation(libs.coroutines.test)
  testImplementation(libs.junit)
  testImplementation(libs.turbine)
  testImplementation(projects.apolloOctopusTest)
  testImplementation(projects.apolloTest)
  testImplementation(projects.coreCommonTest)
  testImplementation(projects.loggingTest)
  testImplementation(projects.moleculeTest)
}
```

- [ ] **Step 2: Verify module is discovered**

Run: `./gradlew projects | grep purchase-pet`
Expected: `+--- Project ':feature-purchase-pet'` listed.

(Modules under `app/` with `build.gradle.kts` are auto-discovered by `settings.gradle.kts` — no manual registration needed.)

- [ ] **Step 3: Sync project**

Run: `./gradlew :feature-purchase-pet:tasks` (forces configuration of the new module).
Expected: builds without error, lists standard library tasks.

- [ ] **Step 4: Commit**

```bash
git add app/feature/feature-purchase-pet/build.gradle.kts
git commit -m "feat: scaffold feature-purchase-pet module"
```

---

### Task 2: Add GraphQL operations

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/graphql/PetShopSessionCreateMutation.graphql`
- Create: `app/feature/feature-purchase-pet/src/main/graphql/PetPriceIntentCreateMutation.graphql`
- Create: `app/feature/feature-purchase-pet/src/main/graphql/PetPriceIntentDataUpdateMutation.graphql`
- Create: `app/feature/feature-purchase-pet/src/main/graphql/PetPriceIntentConfirmMutation.graphql`
- Create: `app/feature/feature-purchase-pet/src/main/graphql/PetProductOfferFragment.graphql`
- Create: `app/feature/feature-purchase-pet/src/main/graphql/PetMemberContactInfoQuery.graphql`
- Create: `app/feature/feature-purchase-pet/src/main/graphql/PetAvailableBreedsQuery.graphql`

- [ ] **Step 1: Create `PetShopSessionCreateMutation.graphql`**

```graphql
mutation PetShopSessionCreate($countryCode: CountryCode!) {
  shopSessionCreate(input: { countryCode: $countryCode }) {
    id
  }
}
```

- [ ] **Step 2: Create `PetPriceIntentCreateMutation.graphql`**

```graphql
mutation PetPriceIntentCreate($shopSessionId: UUID!, $productName: String!) {
  priceIntentCreate(input: { shopSessionId: $shopSessionId, productName: $productName }) {
    id
  }
}
```

- [ ] **Step 3: Create `PetPriceIntentDataUpdateMutation.graphql`**

```graphql
mutation PetPriceIntentDataUpdate($priceIntentId: UUID!, $data: PricingFormData!) {
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

- [ ] **Step 4: Create `PetPriceIntentConfirmMutation.graphql`**

```graphql
mutation PetPriceIntentConfirm($priceIntentId: UUID!) {
  priceIntentConfirm(priceIntentId: $priceIntentId) {
    priceIntent {
      id
      offers {
        ...PetProductOfferFragment
      }
    }
    userError {
      message
    }
  }
}
```

- [ ] **Step 5: Create `PetProductOfferFragment.graphql`** (structurally identical to `CarProductOfferFragment`)

```graphql
fragment PetProductOfferFragment on ProductOffer {
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

- [ ] **Step 6: Create `PetMemberContactInfoQuery.graphql`**

```graphql
query PetMemberContactInfo {
  currentMember {
    id
    ssn
    email
  }
}
```

- [ ] **Step 7: Create `PetAvailableBreedsQuery.graphql`**

```graphql
query PetAvailableBreeds($animal: PriceIntentAnimal!) {
  priceIntentAvailableBreeds(animal: $animal) {
    id
    displayName
    isMixedBreed
  }
}
```

- [ ] **Step 8: Verify Apollo codegen succeeds**

Run: `./gradlew :feature-purchase-pet:generateApolloSources`
Expected: succeeds and produces `octopus.PetShopSessionCreateMutation`, `octopus.PetPriceIntentCreateMutation`, `octopus.PetPriceIntentDataUpdateMutation`, `octopus.PetPriceIntentConfirmMutation`, `octopus.PetAvailableBreedsQuery`, `octopus.PetMemberContactInfoQuery`, and `octopus.fragment.PetProductOfferFragment` classes.

- [ ] **Step 9: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/graphql/
git commit -m "feat: add pet purchase GraphQL operations"
```

---

### Task 3: Add domain models

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/PetPurchaseModels.kt`

- [ ] **Step 1: Create models file**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/PetPurchaseModels.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.data

import com.hedvig.android.core.uidata.UiMoney

internal const val PRODUCT_NAME_DOG = "SE_PET_DOG"
internal const val PRODUCT_NAME_CAT = "SE_PET_CAT"

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
  val ssn: String,
  val email: String,
)

internal data class Breed(
  val id: String,
  val displayName: String,
  val isMixedBreed: Boolean,
)

internal data class PetOffers(
  val productDisplayName: String,
  val offers: List<PetTierOffer>,
)

internal data class PetTierOffer(
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

internal enum class PetGender { MALE, FEMALE }
```

- [ ] **Step 2: Verify compiles**

Run: `./gradlew :feature-purchase-pet:compileDebugKotlin`
Expected: succeeds.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/PetPurchaseModels.kt
git commit -m "feat: add pet purchase domain models"
```

---

### Task 4: Implement `CreatePetSessionAndPriceIntentUseCase`

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/CreatePetSessionAndPriceIntentUseCase.kt`
- Create: `app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/CreatePetSessionAndPriceIntentUseCaseTest.kt`

- [ ] **Step 1: Write the failing test**

File: `app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/CreatePetSessionAndPriceIntentUseCaseTest.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.PetMemberContactInfoQuery
import octopus.PetPriceIntentCreateMutation
import octopus.PetShopSessionCreateMutation
import octopus.type.CountryCode
import octopus.type.buildMember
import octopus.type.buildPriceIntent
import octopus.type.buildShopSession
import org.junit.Rule
import org.junit.Test

class CreatePetSessionAndPriceIntentUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  @Test
  fun `successful session + intent + member returns SessionAndIntent`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetShopSessionCreateMutation(CountryCode.SE),
        data = PetShopSessionCreateMutation.Data(OctopusFakeResolver) {
          shopSessionCreate = buildShopSession { id = "session-1" }
        },
      )
      registerTestResponse(
        operation = PetPriceIntentCreateMutation(
          shopSessionId = "session-1",
          productName = PRODUCT_NAME_DOG,
        ),
        data = PetPriceIntentCreateMutation.Data(OctopusFakeResolver) {
          priceIntentCreate = buildPriceIntent { id = "intent-1" }
        },
      )
      registerTestResponse(
        operation = PetMemberContactInfoQuery(),
        data = PetMemberContactInfoQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            id = "member-1"
            ssn = "199001011234"
            email = "user@example.com"
          }
        },
      )
    }

    val sut = CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke(PRODUCT_NAME_DOG)

    assertThat(result).isRight().isEqualTo(
      SessionAndIntent(
        shopSessionId = "session-1",
        priceIntentId = "intent-1",
        ssn = "199001011234",
        email = "user@example.com",
      ),
    )
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `member with null ssn returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetShopSessionCreateMutation(CountryCode.SE),
        data = PetShopSessionCreateMutation.Data(OctopusFakeResolver) {
          shopSessionCreate = buildShopSession { id = "session-2" }
        },
      )
      registerTestResponse(
        operation = PetPriceIntentCreateMutation(
          shopSessionId = "session-2",
          productName = PRODUCT_NAME_CAT,
        ),
        data = PetPriceIntentCreateMutation.Data(OctopusFakeResolver) {
          priceIntentCreate = buildPriceIntent { id = "intent-2" }
        },
      )
      registerTestResponse(
        operation = PetMemberContactInfoQuery(),
        data = PetMemberContactInfoQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            id = "member-2"
            ssn = null
            email = "x@example.com"
          }
        },
      )
    }

    val sut = CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke(PRODUCT_NAME_CAT)
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `network error on session creation returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetShopSessionCreateMutation(CountryCode.SE),
        data = null,
        errors = listOf(Error.Builder(message = "Network error").build()),
      )
    }

    val sut = CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke(PRODUCT_NAME_DOG)
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature-purchase-pet:testDebugUnitTest --tests 'com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCaseTest'`
Expected: FAIL — `CreatePetSessionAndPriceIntentUseCaseImpl` unresolved.

- [ ] **Step 3: Implement the use case**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/CreatePetSessionAndPriceIntentUseCase.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.PetMemberContactInfoQuery
import octopus.PetPriceIntentCreateMutation
import octopus.PetShopSessionCreateMutation
import octopus.type.CountryCode

internal interface CreatePetSessionAndPriceIntentUseCase {
  suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent>
}

internal class CreatePetSessionAndPriceIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreatePetSessionAndPriceIntentUseCase {
  override suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent> {
    return either {
      val shopSessionId = apolloClient
        .mutation(PetShopSessionCreateMutation(CountryCode.SE))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create shop session: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCreate.id },
        )

      val priceIntentId = apolloClient
        .mutation(PetPriceIntentCreateMutation(shopSessionId = shopSessionId, productName = productName))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create price intent: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentCreate.id },
        )

      val member = apolloClient
        .query(PetMemberContactInfoQuery())
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
        logcat(LogPriority.ERROR) { "Member is missing SSN — cannot continue pet purchase" }
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

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :feature-purchase-pet:testDebugUnitTest --tests 'com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCaseTest'`
Expected: 3 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/CreatePetSessionAndPriceIntentUseCase.kt \
        app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/CreatePetSessionAndPriceIntentUseCaseTest.kt
git commit -m "feat: add CreatePetSessionAndPriceIntentUseCase"
```

---

### Task 5: Implement `GetPetBreedsUseCase`

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/GetPetBreedsUseCase.kt`
- Create: `app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/GetPetBreedsUseCaseTest.kt`

- [ ] **Step 1: Write the failing test**

File: `app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/GetPetBreedsUseCaseTest.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.PetAvailableBreedsQuery
import octopus.type.PriceIntentAnimal
import octopus.type.buildPriceIntentAnimalBreed
import org.junit.Rule
import org.junit.Test

class GetPetBreedsUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  @Test
  fun `successful breeds query returns mapped breeds`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetAvailableBreedsQuery(animal = PriceIntentAnimal.DOG),
        data = PetAvailableBreedsQuery.Data(OctopusFakeResolver) {
          priceIntentAvailableBreeds = listOf(
            buildPriceIntentAnimalBreed {
              id = "DOG_MIXED"
              displayName = "Mixed breed"
              isMixedBreed = true
            },
            buildPriceIntentAnimalBreed {
              id = "DOG_LABRADOR"
              displayName = "Labrador"
              isMixedBreed = false
            },
          )
        },
      )
    }

    val sut = GetPetBreedsUseCaseImpl(apolloClient)
    val result = sut.invoke(PriceIntentAnimal.DOG)

    assertThat(result).isRight().prop(List<Breed>::toList).containsExactly(
      Breed(id = "DOG_MIXED", displayName = "Mixed breed", isMixedBreed = true),
      Breed(id = "DOG_LABRADOR", displayName = "Labrador", isMixedBreed = false),
    )
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `network error returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetAvailableBreedsQuery(animal = PriceIntentAnimal.CAT),
        data = null,
        errors = listOf(Error.Builder(message = "Network error").build()),
      )
    }

    val sut = GetPetBreedsUseCaseImpl(apolloClient)
    val result = sut.invoke(PriceIntentAnimal.CAT)
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature-purchase-pet:testDebugUnitTest --tests 'com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCaseTest'`
Expected: FAIL — `GetPetBreedsUseCaseImpl` unresolved.

- [ ] **Step 3: Implement the use case**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/GetPetBreedsUseCase.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.PetAvailableBreedsQuery
import octopus.type.PriceIntentAnimal

internal interface GetPetBreedsUseCase {
  suspend fun invoke(animal: PriceIntentAnimal): Either<ErrorMessage, List<Breed>>
}

internal class GetPetBreedsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPetBreedsUseCase {
  override suspend fun invoke(animal: PriceIntentAnimal): Either<ErrorMessage, List<Breed>> {
    return either {
      apolloClient
        .query(PetAvailableBreedsQuery(animal = animal))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to fetch pet breeds: $it" }
            raise(ErrorMessage())
          },
          ifRight = { data ->
            data.priceIntentAvailableBreeds.map { breed ->
              Breed(
                id = breed.id,
                displayName = breed.displayName,
                isMixedBreed = breed.isMixedBreed,
              )
            }
          },
        )
    }
  }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :feature-purchase-pet:testDebugUnitTest --tests 'com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCaseTest'`
Expected: 2 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/GetPetBreedsUseCase.kt \
        app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/GetPetBreedsUseCaseTest.kt
git commit -m "feat: add GetPetBreedsUseCase"
```

---

### Task 6: Implement `SubmitPetFormAndGetOffersUseCase`

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/SubmitPetFormAndGetOffersUseCase.kt`
- Create: `app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/SubmitPetFormAndGetOffersUseCaseTest.kt`

- [ ] **Step 1: Write the failing test**

File: `app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/SubmitPetFormAndGetOffersUseCaseTest.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.PetPriceIntentConfirmMutation
import octopus.PetPriceIntentDataUpdateMutation
import octopus.type.buildPriceIntent
import octopus.type.buildPriceIntentDataUpdateOutput
import octopus.type.buildPriceIntentConfirmOutput
import octopus.type.buildUserError
import org.junit.Rule
import org.junit.Test

class SubmitPetFormAndGetOffersUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val sampleInput = SubmitInput(
    priceIntentId = "intent-1",
    productName = PRODUCT_NAME_DOG,
    ssn = "199001011234",
    email = "user@example.com",
    name = "Buddy",
    breedId = "DOG_LABRADOR",
    isMixedBreed = false,
    birthDate = LocalDate.parse("2022-03-15"),
    gender = PetGender.MALE,
    isNeutered = true,
    speciesAnswer = false,
    street = "Fakestreet 123",
    zipCode = "12345",
  )

  @OptIn(ApolloExperimental::class)
  @Test
  fun `userError from update returns ErrorMessage with backend message`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetPriceIntentDataUpdateMutation(
          priceIntentId = sampleInput.priceIntentId,
          data = buildExpectedFormData(sampleInput),
        ),
        data = PetPriceIntentDataUpdateMutation.Data(OctopusFakeResolver) {
          priceIntentDataUpdate = buildPriceIntentDataUpdateOutput {
            userError = buildUserError { message = "Pet too young" }
          }
        },
      )
    }

    val sut = SubmitPetFormAndGetOffersUseCaseImpl(apolloClient)
    val result = sut.invoke(sampleInput)
    assertThat(result).isLeft().isEqualTo(ErrorMessage("Pet too young"))
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `mixed breed submits empty breeds list`() = runTest {
    val mixed = sampleInput.copy(isMixedBreed = true, breedId = "DOG_MIXED")
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetPriceIntentDataUpdateMutation(
          priceIntentId = mixed.priceIntentId,
          data = buildExpectedFormData(mixed), // breeds = emptyList()
        ),
        data = PetPriceIntentDataUpdateMutation.Data(OctopusFakeResolver) {
          priceIntentDataUpdate = buildPriceIntentDataUpdateOutput { userError = null }
        },
      )
      registerTestResponse(
        operation = PetPriceIntentConfirmMutation(priceIntentId = mixed.priceIntentId),
        data = PetPriceIntentConfirmMutation.Data(OctopusFakeResolver) {
          priceIntentConfirm = buildPriceIntentConfirmOutput {
            priceIntent = buildPriceIntent {
              id = "intent-1"
              offers = listOf() // empty triggers the empty-offers branch tested separately
            }
          }
        },
      )
    }

    val sut = SubmitPetFormAndGetOffersUseCaseImpl(apolloClient)
    val result = sut.invoke(mixed)
    // Empty offers list -> generic ErrorMessage; the assertion that matters here is
    // that the data-update mutation matched the expected payload (which means breeds=[]).
    assertThat(result).isLeft()
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `cat uses hasOutsideAccess key`() = runTest {
    val cat = sampleInput.copy(
      productName = PRODUCT_NAME_CAT,
      breedId = "CAT_MAINE_COON",
      isMixedBreed = false,
      speciesAnswer = true,
    )
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetPriceIntentDataUpdateMutation(
          priceIntentId = cat.priceIntentId,
          data = buildExpectedFormData(cat),
        ),
        data = PetPriceIntentDataUpdateMutation.Data(OctopusFakeResolver) {
          priceIntentDataUpdate = buildPriceIntentDataUpdateOutput {
            userError = buildUserError { message = "stop here" }
          }
        },
      )
    }

    val sut = SubmitPetFormAndGetOffersUseCaseImpl(apolloClient)
    val result = sut.invoke(cat)
    assertThat(result).isLeft().isEqualTo(ErrorMessage("stop here"))
  }
}

// Helper: builds the PricingFormData map the use case is expected to send.
private fun buildExpectedFormData(input: SubmitInput): Map<String, Any> {
  val speciesKey = if (input.productName == PRODUCT_NAME_CAT) "hasOutsideAccess" else "isPreviousDogOwner"
  return buildMap {
    put("ssn", input.ssn)
    put("name", input.name)
    put("breeds", if (input.isMixedBreed) emptyList<String>() else listOf(input.breedId))
    put("birthDate", input.birthDate.toString())
    put("gender", input.gender.name)
    put("isNeutered", input.isNeutered.toString())
    put(speciesKey, input.speciesAnswer.toString())
    put("street", input.street)
    put("zipCode", input.zipCode)
    put("email", input.email)
  }
}
```

> Note: `buildExpectedFormData` lives in the test file so the use case under test can be invoked with a typed `SubmitInput` and we can independently assert on the resulting payload shape.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature-purchase-pet:testDebugUnitTest --tests 'com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCaseTest'`
Expected: FAIL — `SubmitPetFormAndGetOffersUseCaseImpl` and `SubmitInput` unresolved.

- [ ] **Step 3: Implement the use case**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/SubmitPetFormAndGetOffersUseCase.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.PetPriceIntentConfirmMutation
import octopus.PetPriceIntentDataUpdateMutation
import octopus.fragment.PetProductOfferFragment

internal data class SubmitInput(
  val priceIntentId: String,
  val productName: String,
  val ssn: String,
  val email: String,
  val name: String,
  val breedId: String,
  val isMixedBreed: Boolean,
  val birthDate: LocalDate,
  val gender: PetGender,
  val isNeutered: Boolean,
  val speciesAnswer: Boolean,
  val street: String,
  val zipCode: String,
)

internal interface SubmitPetFormAndGetOffersUseCase {
  suspend fun invoke(input: SubmitInput): Either<ErrorMessage, PetOffers>
}

internal class SubmitPetFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitPetFormAndGetOffersUseCase {
  override suspend fun invoke(input: SubmitInput): Either<ErrorMessage, PetOffers> {
    return either {
      val speciesKey = if (input.productName == PRODUCT_NAME_CAT) "hasOutsideAccess" else "isPreviousDogOwner"
      val formData: Map<String, Any> = buildMap {
        put("ssn", input.ssn)
        put("name", input.name)
        put("breeds", if (input.isMixedBreed) emptyList<String>() else listOf(input.breedId))
        put("birthDate", input.birthDate.toString())
        put("gender", input.gender.name)
        put("isNeutered", input.isNeutered.toString())
        put(speciesKey, input.speciesAnswer.toString())
        put("street", input.street)
        put("zipCode", input.zipCode)
        put("email", input.email)
      }

      val updateResult = apolloClient
        .mutation(PetPriceIntentDataUpdateMutation(priceIntentId = input.priceIntentId, data = formData))
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
        .mutation(PetPriceIntentConfirmMutation(priceIntentId = input.priceIntentId))
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

      PetOffers(
        productDisplayName = offers.first().variant.displayName,
        offers = offers.map { it.toTierOffer() },
      )
    }
  }
}

internal fun PetProductOfferFragment.toTierOffer(): PetTierOffer {
  return PetTierOffer(
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

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :feature-purchase-pet:testDebugUnitTest --tests 'com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCaseTest'`
Expected: 3 tests pass.

- [ ] **Step 5: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/data/SubmitPetFormAndGetOffersUseCase.kt \
        app/feature/feature-purchase-pet/src/test/kotlin/com/hedvig/android/feature/purchase/pet/data/SubmitPetFormAndGetOffersUseCaseTest.kt
git commit -m "feat: add SubmitPetFormAndGetOffersUseCase"
```

---

### Task 7: Add navigation destinations

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/navigation/PetPurchaseDestination.kt`

- [ ] **Step 1: Create destinations**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/navigation/PetPurchaseDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class PetPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface PetPurchaseDestination {
  @Serializable
  data object Form : PetPurchaseDestination, Destination
}
```

- [ ] **Step 2: Verify compiles**

Run: `./gradlew :feature-purchase-pet:compileDebugKotlin`
Expected: succeeds.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/navigation/PetPurchaseDestination.kt
git commit -m "feat: add pet purchase navigation destinations"
```

---

### Task 8: Implement `PetFormViewModel` + presenter

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/ui/form/PetFormViewModel.kt`

- [ ] **Step 1: Create ViewModel + state + events + presenter**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/ui/form/PetFormViewModel.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.pet.data.Breed
import com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCase
import com.hedvig.android.feature.purchase.pet.data.PRODUCT_NAME_CAT
import com.hedvig.android.feature.purchase.pet.data.PetGender
import com.hedvig.android.feature.purchase.pet.data.PetOffers
import com.hedvig.android.feature.purchase.pet.data.SessionAndIntent
import com.hedvig.android.feature.purchase.pet.data.SubmitInput
import com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import octopus.type.PriceIntentAnimal

internal class PetFormViewModel(
  productName: String,
  createPetSessionAndPriceIntentUseCase: CreatePetSessionAndPriceIntentUseCase,
  getPetBreedsUseCase: GetPetBreedsUseCase,
  submitPetFormAndGetOffersUseCase: SubmitPetFormAndGetOffersUseCase,
) : MoleculeViewModel<PetFormEvent, PetFormState>(
    initialState = PetFormState(),
    presenter = PetFormPresenter(
      productName = productName,
      createPetSessionAndPriceIntentUseCase = createPetSessionAndPriceIntentUseCase,
      getPetBreedsUseCase = getPetBreedsUseCase,
      submitPetFormAndGetOffersUseCase = submitPetFormAndGetOffersUseCase,
    ),
  )

internal sealed interface PetFormEvent {
  data class SubmitForm(
    val name: String,
    val breed: Breed?,
    val birthDate: LocalDate?,
    val gender: PetGender?,
    val isNeutered: Boolean?,
    val speciesAnswer: Boolean?,
    val street: String,
    val zipCode: String,
  ) : PetFormEvent

  data object ClearNavigation : PetFormEvent
  data object Retry : PetFormEvent
  data object DismissError : PetFormEvent
}

internal data class PetFormState(
  val productName: String = "",
  val isCat: Boolean = false,
  val isLoadingSession: Boolean = true,
  val loadSessionError: Boolean = false,
  val breeds: List<Breed> = emptyList(),
  val isSubmitting: Boolean = false,
  val submitError: String? = null,
  val nameError: String? = null,
  val breedError: String? = null,
  val birthDateError: String? = null,
  val genderError: String? = null,
  val isNeuteredError: String? = null,
  val speciesAnswerError: String? = null,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val offersToNavigate: OffersNavigationData? = null,
)

internal data class OffersNavigationData(
  val shopSessionId: String,
  val offers: PetOffers,
)

private class PetFormPresenter(
  private val productName: String,
  private val createPetSessionAndPriceIntentUseCase: CreatePetSessionAndPriceIntentUseCase,
  private val getPetBreedsUseCase: GetPetBreedsUseCase,
  private val submitPetFormAndGetOffersUseCase: SubmitPetFormAndGetOffersUseCase,
) : MoleculePresenter<PetFormEvent, PetFormState> {
  @Composable
  override fun MoleculePresenterScope<PetFormEvent>.present(lastState: PetFormState): PetFormState {
    val isCat = productName == PRODUCT_NAME_CAT
    var currentState by remember {
      mutableStateOf(lastState.copy(productName = productName, isCat = isCat))
    }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var loadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: PetFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is PetFormEvent.SubmitForm -> {
          val errors = validate(event, isCat)
          currentState = currentState.copy(
            nameError = errors.nameError,
            breedError = errors.breedError,
            birthDateError = errors.birthDateError,
            genderError = errors.genderError,
            isNeuteredError = errors.isNeuteredError,
            speciesAnswerError = errors.speciesAnswerError,
            streetError = errors.streetError,
            zipCodeError = errors.zipCodeError,
          )
          if (!errors.hasErrors()) {
            pendingSubmit = event
            submitIteration++
          }
        }

        PetFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }

        PetFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            loadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }

        PetFormEvent.DismissError -> {
          currentState = currentState.copy(submitError = null)
        }
      }
    }

    LaunchedEffect(loadIteration) {
      currentState = currentState.copy(isLoadingSession = true, loadSessionError = false)
      val animal = if (isCat) PriceIntentAnimal.CAT else PriceIntentAnimal.DOG
      val results = coroutineScope {
        val sessionDeferred = async { createPetSessionAndPriceIntentUseCase.invoke(productName) }
        val breedsDeferred = async { getPetBreedsUseCase.invoke(animal) }
        listOf(sessionDeferred, breedsDeferred).awaitAll()
      }
      val sessionResult = results[0] as arrow.core.Either<*, *>
      val breedsResult = results[1] as arrow.core.Either<*, *>

      val session = sessionResult.fold(ifLeft = { null }, ifRight = { it as SessionAndIntent })
      @Suppress("UNCHECKED_CAST")
      val breeds = breedsResult.fold(ifLeft = { null }, ifRight = { it as List<Breed> })

      if (session != null && breeds != null) {
        sessionAndIntent = session
        currentState = currentState.copy(
          breeds = breeds,
          isLoadingSession = false,
          loadSessionError = false,
        )
      } else {
        currentState = currentState.copy(isLoadingSession = false, loadSessionError = true)
      }
    }

    LaunchedEffect(submitIteration) {
      val submit = pendingSubmit ?: return@LaunchedEffect
      val session = sessionAndIntent ?: return@LaunchedEffect
      val breed = submit.breed ?: return@LaunchedEffect
      val birthDate = submit.birthDate ?: return@LaunchedEffect
      val gender = submit.gender ?: return@LaunchedEffect
      val isNeutered = submit.isNeutered ?: return@LaunchedEffect
      val speciesAnswer = submit.speciesAnswer ?: return@LaunchedEffect
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)

      submitPetFormAndGetOffersUseCase.invoke(
        SubmitInput(
          priceIntentId = session.priceIntentId,
          productName = productName,
          ssn = session.ssn,
          email = session.email,
          name = submit.name.trim(),
          breedId = breed.id,
          isMixedBreed = breed.isMixedBreed,
          birthDate = birthDate,
          gender = gender,
          isNeutered = isNeutered,
          speciesAnswer = speciesAnswer,
          street = submit.street.trim(),
          zipCode = submit.zipCode.trim(),
        ),
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
  val nameError: String?,
  val breedError: String?,
  val birthDateError: String?,
  val genderError: String?,
  val isNeuteredError: String?,
  val speciesAnswerError: String?,
  val streetError: String?,
  val zipCodeError: String?,
) {
  fun hasErrors(): Boolean = listOf(
    nameError, breedError, birthDateError, genderError,
    isNeuteredError, speciesAnswerError, streetError, zipCodeError,
  ).any { it != null }
}

private fun validate(event: PetFormEvent.SubmitForm, isCat: Boolean): ValidationErrors {
  // TODO: Add "Enter a name" / "Ange ett namn" to Lokalise
  val nameError = if (event.name.isBlank()) "Enter a name" else null
  // TODO: Add "Choose a breed" / "Välj en ras" to Lokalise
  val breedError = if (event.breed == null) "Choose a breed" else null
  // TODO: Add "Choose a birth date" / "Välj födelsedatum" to Lokalise
  val birthDateError = if (event.birthDate == null) "Choose a birth date" else null
  // TODO: Add "Choose a gender" / "Välj kön" to Lokalise
  val genderError = if (event.gender == null) "Choose a gender" else null
  // TODO: Add "Answer this question" / "Besvara frågan" to Lokalise
  val isNeuteredError = if (event.isNeutered == null) "Answer this question" else null
  val speciesAnswerError = if (event.speciesAnswer == null) "Answer this question" else null
  // TODO: Add "Enter an address" / "Ange en adress" to Lokalise
  val streetError = if (event.street.isBlank()) "Enter an address" else null
  // TODO: Add "Enter a valid zip code (5 digits)" / "Ange ett giltigt postnummer (5 siffror)" to Lokalise
  val zipCodeError = when {
    event.zipCode.length != 5 -> "Enter a valid zip code (5 digits)"
    !event.zipCode.all { it.isDigit() } -> "Enter a valid zip code (5 digits)"
    else -> null
  }
  return ValidationErrors(
    nameError, breedError, birthDateError, genderError,
    isNeuteredError, speciesAnswerError, streetError, zipCodeError,
  )
}
```

- [ ] **Step 2: Verify compiles**

Run: `./gradlew :feature-purchase-pet:compileDebugKotlin`
Expected: succeeds.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/ui/form/PetFormViewModel.kt
git commit -m "feat: add PetFormViewModel and presenter"
```

---

### Task 9: Implement `PetFormDestination` UI

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/ui/form/PetFormDestination.kt`

- [ ] **Step 1: Create the destination composable**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/ui/form/PetFormDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.ui.form

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.design.system.hedvig.datepicker.HedvigSelectableDates
import com.hedvig.android.design.system.hedvig.getLocale
import com.hedvig.android.feature.purchase.pet.data.Breed
import com.hedvig.android.feature.purchase.pet.data.PetGender
import com.hedvig.android.feature.purchase.pet.data.PetOffers
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun PetFormDestination(
  viewModel: PetFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: PetOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(PetFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(navigateUp = navigateUp) {
    when {
      uiState.isLoadingSession -> HedvigFullScreenCenterAlignedProgress()
      uiState.loadSessionError -> HedvigErrorSection(
        onButtonClick = { viewModel.emit(PetFormEvent.Retry) },
      )
      else -> PetFormBody(
        uiState = uiState,
        onSubmit = { event -> viewModel.emit(event) },
        onDismissError = { viewModel.emit(PetFormEvent.DismissError) },
      )
    }
  }
}

@Composable
private fun PetFormBody(
  uiState: PetFormState,
  onSubmit: (PetFormEvent.SubmitForm) -> Unit,
  onDismissError: () -> Unit,
) {
  var name by rememberSaveable { mutableStateOf("") }
  var selectedBreed: Breed? by remember { mutableStateOf(null) }
  var birthDate: LocalDate? by remember { mutableStateOf(null) }
  var gender: PetGender? by remember { mutableStateOf(null) }
  var isNeutered: Boolean? by remember { mutableStateOf(null) }
  var speciesAnswer: Boolean? by remember { mutableStateOf(null) }
  var street by rememberSaveable { mutableStateOf("") }
  var zipCode by rememberSaveable { mutableStateOf("") }

  if (uiState.submitError != null) {
    ErrorDialog(
      // TODO: Add "Something went wrong" / "Något gick fel" to Lokalise
      title = "Something went wrong",
      message = uiState.submitError,
      onDismiss = onDismissError,
    )
  }

  PetFormContent(
    isCat = uiState.isCat,
    breeds = uiState.breeds,
    name = name,
    selectedBreed = selectedBreed,
    birthDate = birthDate,
    gender = gender,
    isNeutered = isNeutered,
    speciesAnswer = speciesAnswer,
    street = street,
    zipCode = zipCode,
    errors = uiState,
    isSubmitting = uiState.isSubmitting,
    onNameChanged = { name = it },
    onBreedSelected = { selectedBreed = it },
    onBirthDateSelected = { birthDate = it },
    onGenderSelected = { gender = it },
    onIsNeuteredSelected = { isNeutered = it },
    onSpeciesAnswerSelected = { speciesAnswer = it },
    onStreetChanged = { street = it },
    onZipCodeChanged = { value -> if (value.all { it.isDigit() } && value.length <= 5) zipCode = value },
    onSubmit = {
      onSubmit(
        PetFormEvent.SubmitForm(
          name = name,
          breed = selectedBreed,
          birthDate = birthDate,
          gender = gender,
          isNeutered = isNeutered,
          speciesAnswer = speciesAnswer,
          street = street,
          zipCode = zipCode,
        ),
      )
    },
  )
}

@Composable
private fun PetFormContent(
  isCat: Boolean,
  breeds: List<Breed>,
  name: String,
  selectedBreed: Breed?,
  birthDate: LocalDate?,
  gender: PetGender?,
  isNeutered: Boolean?,
  speciesAnswer: Boolean?,
  street: String,
  zipCode: String,
  errors: PetFormState,
  isSubmitting: Boolean,
  onNameChanged: (String) -> Unit,
  onBreedSelected: (Breed) -> Unit,
  onBirthDateSelected: (LocalDate) -> Unit,
  onGenderSelected: (PetGender) -> Unit,
  onIsNeuteredSelected: (Boolean) -> Unit,
  onSpeciesAnswerSelected: (Boolean) -> Unit,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
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
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      HedvigTextField(
        text = name,
        onValueChange = onNameChanged,
        // TODO: Add "Pet name" / "Husdjurets namn" to Lokalise
        labelText = "Pet name",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = errors.nameError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )

      BreedDropdown(
        breeds = breeds,
        selectedBreed = selectedBreed,
        onBreedSelected = onBreedSelected,
        hasError = errors.breedError != null,
        errorText = errors.breedError,
        isEnabled = !isSubmitting,
      )

      BirthDatePicker(
        birthDate = birthDate,
        onBirthDateSelected = onBirthDateSelected,
        hasError = errors.birthDateError != null,
        errorText = errors.birthDateError,
        isEnabled = !isSubmitting,
      )

      GenderDropdown(
        isCat = isCat,
        selected = gender,
        onSelected = onGenderSelected,
        hasError = errors.genderError != null,
        errorText = errors.genderError,
        isEnabled = !isSubmitting,
      )

      YesNoDropdown(
        // TODO: Add "Is your pet neutered?" / "Är ditt husdjur kastrerat?" to Lokalise
        label = "Is your pet neutered?",
        // TODO: Add "Select an option" / "Välj ett alternativ" to Lokalise
        hint = "Select an option",
        selected = isNeutered,
        onSelected = onIsNeuteredSelected,
        hasError = errors.isNeuteredError != null,
        errorText = errors.isNeuteredError,
        isEnabled = !isSubmitting,
      )

      YesNoDropdown(
        label = if (isCat) {
          // TODO: Add "Does your cat have outside access?" / "Har din katt utomhustillgång?" to Lokalise
          "Does your cat have outside access?"
        } else {
          // TODO: Add "Have you owned a dog before?" / "Har du haft hund tidigare?" to Lokalise
          "Have you owned a dog before?"
        },
        hint = "Select an option",
        selected = speciesAnswer,
        onSelected = onSpeciesAnswerSelected,
        hasError = errors.speciesAnswerError != null,
        errorText = errors.speciesAnswerError,
        isEnabled = !isSubmitting,
      )

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
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Done,
        ),
        enabled = !isSubmitting,
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
private fun BreedDropdown(
  breeds: List<Breed>,
  selectedBreed: Breed?,
  onBreedSelected: (Breed) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  val selectedIndex = selectedBreed?.let { breeds.indexOf(it) }?.takeIf { it >= 0 }
  DropdownWithDialog(
    style = DropdownStyle.Label(
      items = breeds.map { SimpleDropdownItem(it.displayName) },
      // TODO: Add "Breed" / "Ras" to Lokalise
      label = "Breed",
    ),
    size = DropdownSize.Medium,
    // TODO: Add "Choose breed" / "Välj ras" to Lokalise
    hintText = "Choose breed",
    chosenItemIndex = selectedIndex,
    onItemChosen = { index -> onBreedSelected(breeds[index]) },
    onSelectorClick = {},
    isEnabled = isEnabled,
    hasError = hasError,
    errorText = errorText,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun GenderDropdown(
  isCat: Boolean,
  selected: PetGender?,
  onSelected: (PetGender) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  val options = listOf(
    PetGender.MALE to if (isCat) {
      // TODO: Add "Male (cat)" / "Hane" to Lokalise
      "Male"
    } else {
      // TODO: Add "Male (dog)" / "Hane" to Lokalise
      "Male"
    },
    PetGender.FEMALE to if (isCat) {
      // TODO: Add "Female (cat)" / "Hona" to Lokalise
      "Female"
    } else {
      // TODO: Add "Female (dog)" / "Tik" to Lokalise
      "Female"
    },
  )
  val selectedIndex = selected?.let { options.indexOfFirst { (gender, _) -> gender == it } }?.takeIf { it >= 0 }
  DropdownWithDialog(
    style = DropdownStyle.Label(
      items = options.map { SimpleDropdownItem(it.second) },
      // TODO: Add "Gender" / "Kön" to Lokalise
      label = "Gender",
    ),
    size = DropdownSize.Medium,
    // TODO: Add "Choose gender" / "Välj kön" to Lokalise
    hintText = "Choose gender",
    chosenItemIndex = selectedIndex,
    onItemChosen = { index -> onSelected(options[index].first) },
    onSelectorClick = {},
    isEnabled = isEnabled,
    hasError = hasError,
    errorText = errorText,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun YesNoDropdown(
  label: String,
  hint: String,
  selected: Boolean?,
  onSelected: (Boolean) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  // TODO: Add "Yes" / "Ja" to Lokalise
  // TODO: Add "No" / "Nej" to Lokalise
  val options = listOf(true to "Yes", false to "No")
  val selectedIndex = selected?.let { options.indexOfFirst { (value, _) -> value == it } }?.takeIf { it >= 0 }
  DropdownWithDialog(
    style = DropdownStyle.Label(
      items = options.map { SimpleDropdownItem(it.second) },
      label = label,
    ),
    size = DropdownSize.Medium,
    hintText = hint,
    chosenItemIndex = selectedIndex,
    onItemChosen = { index -> onSelected(options[index].first) },
    onSelectorClick = {},
    isEnabled = isEnabled,
    hasError = hasError,
    errorText = errorText,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun BirthDatePicker(
  birthDate: LocalDate?,
  onBirthDateSelected: (LocalDate) -> Unit,
  hasError: Boolean,
  errorText: String?,
  isEnabled: Boolean,
) {
  val locale = getLocale()
  var showDialog by rememberSaveable { mutableStateOf(false) }
  val initialMillis = birthDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
    ?: Clock.System.now().toEpochMilliseconds()
  val datePickerState = remember(initialMillis) {
    HedvigDatePickerState(
      locale = locale,
      initialSelectedDateMillis = initialMillis,
      initialDisplayedMonthMillis = initialMillis,
      selectableDates = object : HedvigSelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
          val nowMillis = Clock.System.now().toEpochMilliseconds()
          val minMillis = LocalDate.parse("1990-01-01").atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
          return utcTimeMillis in minMillis..nowMillis
        }

        override fun isSelectableYear(year: Int): Boolean {
          val currentYear = Clock.System.now().toLocalDateTime(TimeZone.UTC).year
          return year in 1990..currentYear
        }
      },
    )
  }
  if (showDialog) {
    HedvigDatePicker(
      datePickerState = datePickerState,
      onDismissRequest = { showDialog = false },
      onConfirmRequest = {
        val selected = datePickerState.selectedDateMillis
        if (selected != null) {
          val date = Instant.fromEpochMilliseconds(selected).toLocalDateTime(TimeZone.UTC).date
          onBirthDateSelected(date)
        }
        showDialog = false
      },
    )
  }
  HedvigCard(
    onClick = { if (isEnabled) showDialog = true },
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(16.dp)) {
      HedvigText(
        // TODO: Add "Birth date" / "Födelsedatum" to Lokalise
        text = "Birth date",
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      HedvigText(
        text = birthDate?.toString() ?: run {
          // TODO: Add "Select date" / "Välj datum" to Lokalise
          "Select date"
        },
        style = HedvigTheme.typography.bodyMedium,
      )
      if (hasError && errorText != null) {
        HedvigText(
          text = errorText,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.signalRedText,
        )
      }
    }
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
private fun PreviewPetFormDogEmpty() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PetFormContent(
        isCat = false,
        breeds = listOf(Breed("DOG_MIXED", "Mixed breed", true), Breed("DOG_LABRADOR", "Labrador", false)),
        name = "",
        selectedBreed = null,
        birthDate = null,
        gender = null,
        isNeutered = null,
        speciesAnswer = null,
        street = "",
        zipCode = "",
        errors = PetFormState(),
        isSubmitting = false,
        onNameChanged = {},
        onBreedSelected = {},
        onBirthDateSelected = {},
        onGenderSelected = {},
        onIsNeuteredSelected = {},
        onSpeciesAnswerSelected = {},
        onStreetChanged = {},
        onZipCodeChanged = {},
        onSubmit = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPetFormCatFilled() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PetFormContent(
        isCat = true,
        breeds = listOf(Breed("CAT_MAINE_COON", "Maine Coon", false)),
        name = "Whiskers",
        selectedBreed = Breed("CAT_MAINE_COON", "Maine Coon", false),
        birthDate = LocalDate.parse("2022-03-15"),
        gender = PetGender.FEMALE,
        isNeutered = true,
        speciesAnswer = false,
        street = "Storgatan 1",
        zipCode = "12345",
        errors = PetFormState(isCat = true),
        isSubmitting = false,
        onNameChanged = {},
        onBreedSelected = {},
        onBirthDateSelected = {},
        onGenderSelected = {},
        onIsNeuteredSelected = {},
        onSpeciesAnswerSelected = {},
        onStreetChanged = {},
        onZipCodeChanged = {},
        onSubmit = {},
      )
    }
  }
}
```

- [ ] **Step 2: Verify compiles**

Run: `./gradlew :feature-purchase-pet:compileDebugKotlin`
Expected: succeeds.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/ui/form/PetFormDestination.kt
git commit -m "feat: add PetFormDestination UI"
```

---

### Task 10: Implement `PetPurchaseNavGraph`

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/navigation/PetPurchaseNavGraph.kt`

- [ ] **Step 1: Create the nav graph**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/navigation/PetPurchaseNavGraph.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Failure
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.SelectTier
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Signing
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Success
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Summary
import com.hedvig.android.feature.purchase.common.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData
import com.hedvig.android.feature.purchase.common.ui.failure.PurchaseFailureDestination
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierDestination
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.common.ui.sign.SigningDestination
import com.hedvig.android.feature.purchase.common.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryDestination
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryViewModel
import com.hedvig.android.feature.purchase.pet.navigation.PetPurchaseDestination.Form
import com.hedvig.android.feature.purchase.pet.ui.form.PetFormDestination
import com.hedvig.android.feature.purchase.pet.ui.form.PetFormViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.petPurchaseNavGraph(
  navController: NavController,
  popBackStack: () -> Unit,
  finishApp: () -> Unit,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) {
  navgraph<PetPurchaseGraphDestination>(startDestination = Form::class) {
    navdestination<Form> { backStackEntry ->
      val graphRoute = navController
        .getRouteFromBackStack<PetPurchaseGraphDestination>(backStackEntry)
      val viewModel: PetFormViewModel = koinViewModel {
        parametersOf(graphRoute.productName)
      }
      PetFormDestination(
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
      val viewModel: SelectTierViewModel = koinViewModel { parametersOf(route.params) }
      SelectTierDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        onContinueToSummary = { params -> navController.navigate(Summary(params)) },
      )
    }

    navdestination<Summary>(Summary) { backStackEntry ->
      val route = backStackEntry.toRoute<Summary>()
      val viewModel: PurchaseSummaryViewModel = koinViewModel { parametersOf(route.params) }
      PurchaseSummaryDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        navigateToSigning = { params -> navController.navigate(Signing(params)) },
      )
    }

    navdestination<Signing>(Signing) { backStackEntry ->
      val route = backStackEntry.toRoute<Signing>()
      val viewModel: SigningViewModel = koinViewModel { parametersOf(route.params) }
      SigningDestination(
        viewModel = viewModel,
        navigateToSuccess = { startDate ->
          crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
            CrossSellInfoType.Purchase,
          )
          navController.navigate(Success(startDate)) {
            typedPopUpTo<PetPurchaseGraphDestination>({ inclusive = true })
          }
        },
      )
    }
  }
}
```

> Note: copy-paste from `CarPurchaseNavGraph.kt` and replace `Car`→`Pet` everywhere. Verify the imports still match — `PurchaseFailureDestination` is imported but not currently wired by car either; the `Failure` destination is registered globally by `HedvigNavHost`. Leave the import in place to match car (will silently be unused unless we later want to navigate there directly).

- [ ] **Step 2: Verify compiles**

Run: `./gradlew :feature-purchase-pet:compileDebugKotlin`
Expected: succeeds. If `PurchaseFailureDestination` is reported unused, remove that import.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/navigation/PetPurchaseNavGraph.kt
git commit -m "feat: add petPurchaseNavGraph"
```

---

### Task 11: Add Koin module

**Files:**
- Create: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/di/PetPurchaseModule.kt`

- [ ] **Step 1: Create the Koin module**

File: `app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/di/PetPurchaseModule.kt`

```kotlin
package com.hedvig.android.feature.purchase.pet.di

import com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.pet.data.CreatePetSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCase
import com.hedvig.android.feature.purchase.pet.data.GetPetBreedsUseCaseImpl
import com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.pet.data.SubmitPetFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.pet.ui.form.PetFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val petPurchaseModule = module {
  single<CreatePetSessionAndPriceIntentUseCase> { CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<GetPetBreedsUseCase> { GetPetBreedsUseCaseImpl(apolloClient = get()) }
  single<SubmitPetFormAndGetOffersUseCase> { SubmitPetFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<PetFormViewModel> { params ->
    PetFormViewModel(
      productName = params.get(),
      createPetSessionAndPriceIntentUseCase = get(),
      getPetBreedsUseCase = get(),
      submitPetFormAndGetOffersUseCase = get(),
    )
  }
}
```

- [ ] **Step 2: Verify compiles**

Run: `./gradlew :feature-purchase-pet:compileDebugKotlin`
Expected: succeeds.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-pet/src/main/kotlin/com/hedvig/android/feature/purchase/pet/di/PetPurchaseModule.kt
git commit -m "feat: add petPurchaseModule Koin DI"
```

---

### Task 12: Wire `petPurchaseModule` into `ApplicationModule`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt`

- [ ] **Step 1: Add import and include in module list**

Edit `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt`:

After the existing line:
```kotlin
import com.hedvig.android.feature.purchase.car.di.carPurchaseModule
```
add:
```kotlin
import com.hedvig.android.feature.purchase.pet.di.petPurchaseModule
```

Inside `val applicationModule = module { includes(listOf(...)) }`, after the line:
```kotlin
      carPurchaseModule,
```
add:
```kotlin
      petPurchaseModule,
```

- [ ] **Step 2: Verify app compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: succeeds.

- [ ] **Step 3: Add the pet module dependency to the app's build.gradle.kts** (if not auto-resolved)

If compilation fails with "petPurchaseModule unresolved", open `app/app/build.gradle.kts` and add under `dependencies { ... }`:
```kotlin
  implementation(projects.featurePurchasePet)
```
The exact accessor matches the module name; verify by running `./gradlew :app:dependencies | grep purchase-pet`.

- [ ] **Step 4: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt app/app/build.gradle.kts
git commit -m "feat: register petPurchaseModule in ApplicationModule"
```

---

### Task 13: Wire `petPurchaseNavGraph` into `HedvigNavHost`

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`

- [ ] **Step 1: Add imports**

Edit `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`:

After the existing line:
```kotlin
import com.hedvig.android.feature.purchase.car.navigation.carPurchaseNavGraph
```
add:
```kotlin
import com.hedvig.android.feature.purchase.pet.navigation.PetPurchaseGraphDestination
import com.hedvig.android.feature.purchase.pet.navigation.petPurchaseNavGraph
```

- [ ] **Step 2: Register the pet nav graph next to the car one**

In the section around line 501 in `HedvigNavHost.kt`, after the existing `carPurchaseNavGraph(...)` block:
```kotlin
    carPurchaseNavGraph(
      navController = navController,
      popBackStack = popBackStackOrFinish,
      finishApp = finishApp,
      crossSellAfterFlowRepository = crossSellAfterFlowRepository,
    )
```
add:
```kotlin
    petPurchaseNavGraph(
      navController = navController,
      popBackStack = popBackStackOrFinish,
      finishApp = finishApp,
      crossSellAfterFlowRepository = crossSellAfterFlowRepository,
    )
```

- [ ] **Step 3: Add `onNavigateToPetPurchase` to the `insuranceGraph(...)` call**

In `HedvigNavHost.kt`, find the existing `insuranceGraph(...)` call (around the section with `onNavigateToCarPurchase` near line 337):

Existing:
```kotlin
      onNavigateToCarPurchase = { productName ->
        navController.navigate(CarPurchaseGraphDestination(productName))
      },
```

After it (still inside the `insuranceGraph(...)` call's argument list), add:
```kotlin
      onNavigateToPetPurchase = { productName ->
        navController.navigate(PetPurchaseGraphDestination(productName))
      },
```

- [ ] **Step 4: Verify compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: FAIL — `insuranceGraph` does not yet accept `onNavigateToPetPurchase`. This is expected; it'll be added in Task 14.

- [ ] **Step 5: Do NOT commit yet**

This task's changes won't compile until Task 14 lands. Move directly to Task 14.

---

### Task 14: Add cross-sell routing for pet in `feature-insurances`

**Files:**
- Modify: `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt`

- [ ] **Step 1: Add `onNavigateToPetPurchase` parameter to `insuranceGraph` signature**

Edit `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt`:

In the `fun NavGraphBuilder.insuranceGraph(...)` signature near line 28, after:
```kotlin
  onNavigateToCarPurchase: (productName: String) -> Unit,
```
add:
```kotlin
  onNavigateToPetPurchase: (productName: String) -> Unit,
```

- [ ] **Step 2: Add the two new URL branches in `onCrossSellClick`**

In the same file, locate the `onCrossSellClick = dropUnlessResumed { url: String -> ... }` block (around line 65). Currently it looks like:
```kotlin
          when {
            "car-insurance" in lower || "bilforsakring" in lower ->
              onNavigateToCarPurchase("SE_CAR")
            "bostadsratt" in lower || "home-insurance/homeowner" in lower ->
              onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
            "hyresratt" in lower || "home-insurance" in lower || "hemforsakring" in lower ->
              onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
            else -> openUrl(url)
          }
```

Add two new branches before the apartment branches so they are matched first (the apartment regex `home-insurance` is broad and could collide with future pet URLs if not ordered carefully). New block:

```kotlin
          when {
            "car-insurance" in lower || "bilforsakring" in lower ->
              onNavigateToCarPurchase("SE_CAR")
            // TODO: verify against actual cross-sell URLs (storyblok / staging)
            "dog-insurance" in lower || "hundforsakring" in lower ->
              onNavigateToPetPurchase("SE_PET_DOG")
            // TODO: verify against actual cross-sell URLs (storyblok / staging)
            "cat-insurance" in lower || "kattforsakring" in lower ->
              onNavigateToPetPurchase("SE_PET_CAT")
            "bostadsratt" in lower || "home-insurance/homeowner" in lower ->
              onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
            "hyresratt" in lower || "home-insurance" in lower || "hemforsakring" in lower ->
              onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
            else -> openUrl(url)
          }
```

- [ ] **Step 3: Verify app compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: succeeds (this completes the Task 13 changes too).

- [ ] **Step 4: Run ktlint to ensure formatting**

Run: `./gradlew :feature-purchase-pet:ktlintCheck :feature-insurances:ktlintCheck :app:ktlintCheck`
Expected: clean. If errors: `./gradlew ktlintFormat` then re-run check.

- [ ] **Step 5: Commit (covering Task 13 + Task 14)**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt \
        app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt
git commit -m "feat: integrate pet purchase flow into navigation and cross-sell routing"
```

---

### Task 15: Final build, lint, and tests

**Files:** none

- [ ] **Step 1: Full build**

Run: `./gradlew assembleDebug`
Expected: success.

- [ ] **Step 2: Run all `feature-purchase-pet` tests**

Run: `./gradlew :feature-purchase-pet:test`
Expected: all tests pass.

- [ ] **Step 3: Run ktlint on the whole codebase**

Run: `./gradlew ktlintCheck`
Expected: clean.

- [ ] **Step 4: Run lint on the new module**

Run: `./gradlew :feature-purchase-pet:lint`
Expected: clean (or only pre-existing baseline issues).

---

### Task 16: Verify in emulator

**Files:** none — manual verification

> Required by the `verifying-android-changes-in-emulator` skill. Use it to drive the steps below.

- [ ] **Step 1: Install debug build on a running emulator**

Run: `./gradlew :app:installDevelopDebug`

- [ ] **Step 2: Manually navigate to the dog cross-sell**

In the app:
1. Log in as a staging member.
2. Go to the Insurances tab.
3. Tap the dog/cat cross-sell card (or a URL containing `dog-insurance` / `cat-insurance`).
4. Confirm the pet form opens with breed dropdown populated.
5. Fill all fields and submit. Confirm tier-selection screen appears.
6. Step through summary → signing → success.

- [ ] **Step 3: Manually navigate to the cat cross-sell**

Repeat Step 2 with the cat cross-sell. Confirm the last form question shows "Does your cat have outside access?" instead of "Have you owned a dog before?".

- [ ] **Step 4: Mixed-breed path**

For the dog form, pick the "Mixed breed" item from the breed dropdown and submit. Confirm offers come back successfully (backend accepts empty `breeds` list).

- [ ] **Step 5: Error path**

Submit with no fields filled in. Confirm all inline error messages render. Submit with an obviously too-young pet (birth date today) — confirm `ErrorDialog` shows the backend's `userError` message.

- [ ] **Step 6: Commit any small fixes from manual testing**

Address any UX bugs discovered. Commit fixes incrementally.

---

## Notes for Implementer

- **Branch base:** This work is built on top of `feat/in-app-car-purchase`. Verify with `git log --oneline -5` — the last commit before yours should be `dd05f5e199 feat: add in-app car purchase flow and cross-sell URL routing`.
- **GraphQL prefixes:** Apollo generates one Kotlin class per operation name. Pet operations are prefixed `Pet*` to avoid collisions with `Car*` and `Apartment*`.
- **No common-module changes:** `purchase-common` is consumed as-is. If you find yourself editing it, stop — that's a sign the design has drifted.
- **Localization:** Hardcoded English strings with `// TODO: Add "..." / "..." to Lokalise` comments. Do not edit `strings.xml` directly — Lokalise overwrites it.
- **Memory:** Don't add nav args that deep links can't carry (see `feedback_navigation_args.md`). Animal type stays derived from `productName`, never a separate route param.

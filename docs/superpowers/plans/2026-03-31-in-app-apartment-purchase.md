# In-App Apartment Purchase Flow — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a native Compose purchase flow for SE_APARTMENT_RENT and SE_APARTMENT_BRF, replacing the current link-out to hedvig.com for apartment cross-sells.

**Architecture:** New feature module `feature-purchase-apartment` using the ShopSession + PriceIntent GraphQL API (same as racoon web). Follows MoleculeViewModel + MoleculePresenter pattern. State threaded via serializable navigation arguments. Entry point via cross-sell click on insurance tab.

**Tech Stack:** Jetpack Compose, Apollo GraphQL, Molecule, Koin, Arrow Either, kotlinx.serialization, Navigation Compose (type-safe)

**Spec:** `docs/superpowers/specs/2026-03-31-in-app-apartment-purchase-design.md`

---

## File Map

### New files (feature module)

| File | Responsibility |
|------|---------------|
| `feature-purchase-apartment/build.gradle.kts` | Module build config |
| `.../navigation/ApartmentPurchaseDestination.kt` | All destination types + serializable parameter classes |
| `.../navigation/ApartmentPurchaseNavGraph.kt` | Navigation graph wiring |
| `.../data/PurchaseApartmentModels.kt` | Domain models: `TierOffer`, `ApartmentFormData` |
| `.../data/CreateSessionAndPriceIntentUseCase.kt` | Creates ShopSession + PriceIntent |
| `.../data/SubmitFormAndGetOffersUseCase.kt` | Calls priceIntentDataUpdate + priceIntentConfirm |
| `.../data/AddToCartAndStartSignUseCase.kt` | Adds to cart + starts signing |
| `.../data/PollSigningStatusUseCase.kt` | Polls shopSessionSigning |
| `.../ui/form/ApartmentFormViewModel.kt` | Form presenter with validation |
| `.../ui/form/ApartmentFormDestination.kt` | Form screen composable |
| `.../ui/offer/SelectTierViewModel.kt` | Tier selection presenter |
| `.../ui/offer/SelectTierDestination.kt` | Tier selection screen composable |
| `.../ui/summary/PurchaseSummaryViewModel.kt` | Summary + submit presenter |
| `.../ui/summary/PurchaseSummaryDestination.kt` | Summary screen composable |
| `.../ui/sign/SigningViewModel.kt` | BankID signing presenter |
| `.../ui/sign/SigningDestination.kt` | Signing screen composable |
| `.../ui/success/PurchaseSuccessDestination.kt` | Success screen (stateless) |
| `.../ui/failure/PurchaseFailureDestination.kt` | Failure screen (stateless) |
| `.../di/ApartmentPurchaseModule.kt` | Koin DI module |
| `src/main/graphql/ShopSessionCreateMutation.graphql` | GraphQL |
| `src/main/graphql/PriceIntentCreateMutation.graphql` | GraphQL |
| `src/main/graphql/PriceIntentDataUpdateMutation.graphql` | GraphQL |
| `src/main/graphql/PriceIntentConfirmMutation.graphql` | GraphQL |
| `src/main/graphql/ShopSessionCartEntriesAddMutation.graphql` | GraphQL |
| `src/main/graphql/ShopSessionStartSignMutation.graphql` | GraphQL |
| `src/main/graphql/ShopSessionSigningQuery.graphql` | GraphQL |
| `src/main/graphql/ProductOfferFragment.graphql` | Shared fragment |
| `src/main/graphql/MoneyFragment.graphql` | Shared fragment |

### Modified files (integration)

| File | Change |
|------|--------|
| `app/app/src/main/kotlin/.../navigation/HedvigNavHost.kt` | Register `apartmentPurchaseNavGraph`, add `onNavigateToApartmentPurchase` to `insuranceGraph` call |
| `app/app/src/main/kotlin/.../di/ApplicationModule.kt` | Add `apartmentPurchaseModule` to includes |
| `app/feature/feature-insurances/src/main/kotlin/.../navigation/InsuranceGraph.kt` | Add `onNavigateToApartmentPurchase` parameter, wire cross-sell click routing |
| `app/feature/feature-insurances/src/main/kotlin/.../ui/InsurancePresenter.kt` | Pass product type info through cross-sell click |
| `app/data/data-cross-sell-after-flow/.../CrossSellAfterFlowRepository.kt` | Add `Purchase` to `CrossSellInfoType` |

---

## Task 1: Module scaffolding and GraphQL operations

**Files:**
- Create: `app/feature/feature-purchase-apartment/build.gradle.kts`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/MoneyFragment.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/ProductOfferFragment.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/ShopSessionCreateMutation.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/PriceIntentCreateMutation.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/PriceIntentDataUpdateMutation.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/PriceIntentConfirmMutation.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/ShopSessionCartEntriesAddMutation.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/ShopSessionStartSignMutation.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/graphql/ShopSessionSigningQuery.graphql`
- Create: `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/navigation/ApartmentPurchaseDestination.kt`

- [ ] **Step 1: Create the module directory and build.gradle.kts**

```bash
mkdir -p app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment
mkdir -p app/feature/feature-purchase-apartment/src/main/graphql
```

`app/feature/feature-purchase-apartment/build.gradle.kts`:
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
  implementation(projects.languageCore)
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

- [ ] **Step 2: Create the GraphQL fragment files**

`src/main/graphql/MoneyFragment.graphql`:
```graphql
fragment MoneyFragment on Money {
  amount
  currencyCode
}
```

`src/main/graphql/ProductOfferFragment.graphql`:
```graphql
fragment ApartmentProductOfferFragment on ProductOffer {
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

- [ ] **Step 3: Create the mutation and query GraphQL files**

`src/main/graphql/ShopSessionCreateMutation.graphql`:
```graphql
mutation ApartmentShopSessionCreate($countryCode: CountryCode!) {
  shopSessionCreate(input: { countryCode: $countryCode }) {
    id
  }
}
```

`src/main/graphql/PriceIntentCreateMutation.graphql`:
```graphql
mutation ApartmentPriceIntentCreate($shopSessionId: UUID!, $productName: String!) {
  priceIntentCreate(input: { shopSessionId: $shopSessionId, productName: $productName }) {
    id
  }
}
```

`src/main/graphql/PriceIntentDataUpdateMutation.graphql`:
```graphql
mutation ApartmentPriceIntentDataUpdate($priceIntentId: UUID!, $data: PricingFormData!) {
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

`src/main/graphql/PriceIntentConfirmMutation.graphql`:
```graphql
mutation ApartmentPriceIntentConfirm($priceIntentId: UUID!) {
  priceIntentConfirm(priceIntentId: $priceIntentId) {
    priceIntent {
      id
      offers {
        ...ApartmentProductOfferFragment
      }
    }
    userError {
      message
    }
  }
}
```

`src/main/graphql/ShopSessionCartEntriesAddMutation.graphql`:
```graphql
mutation ApartmentCartEntriesAdd($shopSessionId: UUID!, $offerIds: [UUID!]!) {
  shopSessionCartEntriesAdd(input: { shopSessionId: $shopSessionId, offerIds: $offerIds }) {
    shopSession {
      id
    }
    userError {
      message
    }
  }
}
```

`src/main/graphql/ShopSessionStartSignMutation.graphql`:
```graphql
mutation ApartmentStartSign($shopSessionId: UUID!) {
  shopSessionStartSign(shopSessionId: $shopSessionId) {
    signing {
      id
      status
      seBankidProperties {
        autoStartToken
        liveQrCodeData
        bankidAppOpened
      }
      userError {
        message
      }
    }
    userError {
      message
    }
  }
}
```

`src/main/graphql/ShopSessionSigningQuery.graphql`:
```graphql
query ApartmentShopSessionSigning($signingId: UUID!) {
  shopSessionSigning(id: $signingId) {
    id
    status
    seBankidProperties {
      autoStartToken
      liveQrCodeData
      bankidAppOpened
    }
    completion {
      authorizationCode
    }
    userError {
      message
    }
  }
}
```

- [ ] **Step 4: Create the destination definitions**

`app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/navigation/ApartmentPurchaseDestination.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.navigation

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data class ApartmentPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface ApartmentPurchaseDestination {
  @Serializable
  data object Form : ApartmentPurchaseDestination, Destination

  @Serializable
  data class SelectTier(
    val params: SelectTierParameters,
  ) : ApartmentPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SelectTierParameters>())
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : ApartmentPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class Signing(
    val params: SigningParameters,
  ) : ApartmentPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SigningParameters>())
    }
  }

  @Serializable
  data class Success(
    val startDate: String?,
  ) : ApartmentPurchaseDestination, Destination

  @Serializable
  data object Failure : ApartmentPurchaseDestination, Destination
}

@Serializable
internal data class TierOfferData(
  val offerId: String,
  val tierDisplayName: String,
  val tierDescription: String,
  val grossAmount: Double,
  val grossCurrencyCode: String,
  val netAmount: Double,
  val netCurrencyCode: String,
  val usps: List<String>,
  val exposureDisplayName: String,
  val deductibleDisplayName: String?,
  val hasDiscount: Boolean,
)

@Serializable
internal data class SelectTierParameters(
  val shopSessionId: String,
  val offers: List<TierOfferData>,
  val productDisplayName: String,
)

@Serializable
internal data class SummaryParameters(
  val shopSessionId: String,
  val selectedOffer: TierOfferData,
  val productDisplayName: String,
)

@Serializable
internal data class SigningParameters(
  val signingId: String,
  val autoStartToken: String,
  val startDate: String?,
)
```

- [ ] **Step 5: Verify the module builds with Apollo codegen**

```bash
./gradlew :feature-purchase-apartment:generateApolloSources
```

Expected: BUILD SUCCESSFUL. Apollo generates Kotlin types for all 7 GraphQL operations and 2 fragments.

- [ ] **Step 6: Commit**

```bash
git add app/feature/feature-purchase-apartment/
git commit -m "feat: scaffold feature-purchase-apartment module with GraphQL operations"
```

---

## Task 2: Domain models and use cases

**Files:**
- Create: `.../data/PurchaseApartmentModels.kt`
- Create: `.../data/CreateSessionAndPriceIntentUseCase.kt`
- Create: `.../data/SubmitFormAndGetOffersUseCase.kt`
- Create: `.../data/AddToCartAndStartSignUseCase.kt`
- Create: `.../data/PollSigningStatusUseCase.kt`
- Test: `src/test/kotlin/data/CreateSessionAndPriceIntentUseCaseTest.kt`
- Test: `src/test/kotlin/data/SubmitFormAndGetOffersUseCaseTest.kt`
- Test: `src/test/kotlin/data/AddToCartAndStartSignUseCaseTest.kt`

- [ ] **Step 1: Create domain models**

`.../data/PurchaseApartmentModels.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.data

import com.hedvig.android.core.uidata.UiMoney

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
)

internal data class ApartmentOffers(
  val productDisplayName: String,
  val offers: List<ApartmentTierOffer>,
)

internal data class ApartmentTierOffer(
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

internal data class SigningStart(
  val signingId: String,
  val autoStartToken: String,
)

internal enum class SigningStatus {
  PENDING,
  SIGNED,
  FAILED,
}
```

- [ ] **Step 2: Create CreateSessionAndPriceIntentUseCase**

`.../data/CreateSessionAndPriceIntentUseCase.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ApartmentPriceIntentCreateMutation
import octopus.ApartmentShopSessionCreateMutation
import octopus.type.CountryCode

internal interface CreateSessionAndPriceIntentUseCase {
  suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent>
}

internal class CreateSessionAndPriceIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateSessionAndPriceIntentUseCase {
  override suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent> {
    return either {
      val shopSessionId = apolloClient
        .mutation(ApartmentShopSessionCreateMutation(CountryCode.SE))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create shop session: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCreate.id },
        )

      val priceIntentId = apolloClient
        .mutation(ApartmentPriceIntentCreateMutation(shopSessionId = shopSessionId, productName = productName))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create price intent: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.priceIntentCreate.id },
        )

      SessionAndIntent(shopSessionId = shopSessionId, priceIntentId = priceIntentId)
    }
  }
}
```

- [ ] **Step 3: Create SubmitFormAndGetOffersUseCase**

`.../data/SubmitFormAndGetOffersUseCase.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ApartmentPriceIntentConfirmMutation
import octopus.ApartmentPriceIntentDataUpdateMutation
import octopus.fragment.ApartmentProductOfferFragment

internal interface SubmitFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    numberCoInsured: Int,
  ): Either<ErrorMessage, ApartmentOffers>
}

internal class SubmitFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    street: String,
    zipCode: String,
    livingSpace: Int,
    numberCoInsured: Int,
  ): Either<ErrorMessage, ApartmentOffers> {
    return either {
      val formData = buildMap {
        put("street", street)
        put("zipCode", zipCode)
        put("livingSpace", livingSpace)
        put("numberCoInsured", numberCoInsured)
      }

      val updateResult = apolloClient
        .mutation(ApartmentPriceIntentDataUpdateMutation(priceIntentId = priceIntentId, data = formData))
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
        .mutation(ApartmentPriceIntentConfirmMutation(priceIntentId = priceIntentId))
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

      ApartmentOffers(
        productDisplayName = offers.first().apartmentProductOfferFragment.variant.displayName,
        offers = offers.map { it.apartmentProductOfferFragment.toTierOffer() },
      )
    }
  }
}

internal fun ApartmentProductOfferFragment.toTierOffer(): ApartmentTierOffer {
  val cost = this.cost
  return ApartmentTierOffer(
    offerId = this.id,
    tierDisplayName = this.variant.displayNameTier ?: this.variant.displayName,
    tierDescription = this.variant.tierDescription ?: "",
    grossPrice = UiMoney(cost.gross.moneyFragment.amount, cost.gross.moneyFragment.currencyCode),
    netPrice = UiMoney(cost.net.moneyFragment.amount, cost.net.moneyFragment.currencyCode),
    usps = this.usps,
    exposureDisplayName = this.exposure.displayNameShort,
    deductibleDisplayName = this.deductible?.displayName,
    hasDiscount = cost.net.moneyFragment.amount < cost.gross.moneyFragment.amount,
  )
}
```

- [ ] **Step 4: Create AddToCartAndStartSignUseCase**

`.../data/AddToCartAndStartSignUseCase.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ApartmentCartEntriesAddMutation
import octopus.ApartmentStartSignMutation

internal interface AddToCartAndStartSignUseCase {
  suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, SigningStart>
}

internal class AddToCartAndStartSignUseCaseImpl(
  private val apolloClient: ApolloClient,
) : AddToCartAndStartSignUseCase {
  override suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, SigningStart> {
    return either {
      val cartResult = apolloClient
        .mutation(ApartmentCartEntriesAddMutation(shopSessionId = shopSessionId, offerIds = listOf(offerId)))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to add to cart: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCartEntriesAdd },
        )

      if (cartResult?.userError != null) {
        raise(ErrorMessage(cartResult.userError?.message))
      }

      val signResult = apolloClient
        .mutation(ApartmentStartSignMutation(shopSessionId = shopSessionId))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to start signing: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionStartSign },
        )

      if (signResult.userError != null) {
        raise(ErrorMessage(signResult.userError?.message))
      }

      val signing = signResult.signing ?: run {
        logcat(LogPriority.ERROR) { "No signing session returned" }
        raise(ErrorMessage())
      }

      val autoStartToken = signing.seBankidProperties?.autoStartToken ?: run {
        logcat(LogPriority.ERROR) { "No BankID autoStartToken in signing response" }
        raise(ErrorMessage())
      }

      SigningStart(
        signingId = signing.id,
        autoStartToken = autoStartToken,
      )
    }
  }
}
```

- [ ] **Step 5: Create PollSigningStatusUseCase**

`.../data/PollSigningStatusUseCase.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.ApartmentShopSessionSigningQuery
import octopus.type.ShopSessionSigningStatus

internal interface PollSigningStatusUseCase {
  suspend fun invoke(signingId: String): Either<ErrorMessage, SigningStatus>
}

internal class PollSigningStatusUseCaseImpl(
  private val apolloClient: ApolloClient,
) : PollSigningStatusUseCase {
  override suspend fun invoke(signingId: String): Either<ErrorMessage, SigningStatus> {
    return either {
      apolloClient
        .query(ApartmentShopSessionSigningQuery(signingId = signingId))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to poll signing status: $it" }
            raise(ErrorMessage())
          },
          ifRight = { result ->
            when (result.shopSessionSigning.status) {
              ShopSessionSigningStatus.SIGNED -> SigningStatus.SIGNED
              ShopSessionSigningStatus.FAILED -> SigningStatus.FAILED
              ShopSessionSigningStatus.PENDING,
              ShopSessionSigningStatus.CREATING,
              ShopSessionSigningStatus.UNKNOWN__ -> SigningStatus.PENDING
            }
          },
        )
    }
  }
}
```

- [ ] **Step 6: Write tests for CreateSessionAndPriceIntentUseCase**

`src/test/kotlin/data/CreateSessionAndPriceIntentUseCaseTest.kt`:
```kotlin
package data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.SessionAndIntent
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.ApartmentPriceIntentCreateMutation
import octopus.ApartmentShopSessionCreateMutation
import octopus.type.CountryCode
import org.junit.Rule
import org.junit.Test

class CreateSessionAndPriceIntentUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  @Test
  fun `successful session and price intent creation returns both ids`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ApartmentShopSessionCreateMutation(CountryCode.SE),
        data = ApartmentShopSessionCreateMutation.Data(OctopusFakeResolver) {
          shopSessionCreate = buildShopSession {
            id = "session-123"
          }
        },
      )
      registerTestResponse(
        operation = ApartmentPriceIntentCreateMutation(shopSessionId = "session-123", productName = "SE_APARTMENT_RENT"),
        data = ApartmentPriceIntentCreateMutation.Data(OctopusFakeResolver) {
          priceIntentCreate = buildPriceIntent {
            id = "intent-456"
          }
        },
      )
    }

    val sut = CreateSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke("SE_APARTMENT_RENT")
    assertThat(result).isRight().isEqualTo(SessionAndIntent("session-123", "intent-456"))
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `network error on session creation returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ApartmentShopSessionCreateMutation(CountryCode.SE),
        data = null,
        errors = listOf(Error.Builder(message = "Network error").build()),
      )
    }

    val sut = CreateSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke("SE_APARTMENT_RENT")
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }
}
```

- [ ] **Step 7: Run tests to verify they pass**

```bash
./gradlew :feature-purchase-apartment:test
```

Expected: All tests pass.

- [ ] **Step 8: Commit**

```bash
git add app/feature/feature-purchase-apartment/
git commit -m "feat: add domain models and use cases for apartment purchase"
```

---

## Task 3: Form screen (presenter + UI)

**Files:**
- Create: `.../ui/form/ApartmentFormViewModel.kt`
- Create: `.../ui/form/ApartmentFormDestination.kt`

- [ ] **Step 1: Create ApartmentFormViewModel with presenter**

`.../ui/form/ApartmentFormViewModel.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.purchase.apartment.data.ApartmentOffers
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.apartment.data.SessionAndIntent
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class ApartmentFormViewModel(
  productName: String,
  createSessionAndPriceIntentUseCase: CreateSessionAndPriceIntentUseCase,
  submitFormAndGetOffersUseCase: SubmitFormAndGetOffersUseCase,
) : MoleculeViewModel<ApartmentFormEvent, ApartmentFormState>(
    initialState = ApartmentFormState.Idle(),
    presenter = ApartmentFormPresenter(productName, createSessionAndPriceIntentUseCase, submitFormAndGetOffersUseCase),
  )

internal class ApartmentFormPresenter(
  private val productName: String,
  private val createSessionAndPriceIntentUseCase: CreateSessionAndPriceIntentUseCase,
  private val submitFormAndGetOffersUseCase: SubmitFormAndGetOffersUseCase,
) : MoleculePresenter<ApartmentFormEvent, ApartmentFormState> {
  @Composable
  override fun MoleculePresenterScope<ApartmentFormEvent>.present(lastState: ApartmentFormState): ApartmentFormState {
    var street by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var livingSpace by remember { mutableStateOf("") }
    var numberCoInsured by remember { mutableIntStateOf(0) }

    var streetError by remember { mutableStateOf<String?>(null) }
    var zipCodeError by remember { mutableStateOf<String?>(null) }
    var livingSpaceError by remember { mutableStateOf<String?>(null) }

    var submitIteration by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<ErrorMessage?>(null) }
    var offersToNavigate by remember { mutableStateOf<OffersNavigationData?>(null) }

    var sessionAndIntent by remember { mutableStateOf<SessionAndIntent?>(null) }
    var sessionCreateIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        is ApartmentFormEvent.UpdateStreet -> {
          street = event.value
          streetError = null
        }
        is ApartmentFormEvent.UpdateZipCode -> {
          zipCode = event.value
          zipCodeError = null
        }
        is ApartmentFormEvent.UpdateLivingSpace -> {
          livingSpace = event.value
          livingSpaceError = null
        }
        is ApartmentFormEvent.UpdateNumberCoInsured -> {
          numberCoInsured = event.value
        }
        ApartmentFormEvent.Submit -> {
          val validStreet = street.isNotBlank()
          val validZipCode = zipCode.length == 5 && zipCode.all { it.isDigit() }
          val validLivingSpace = livingSpace.toIntOrNull()?.let { it > 0 } ?: false

          streetError = if (!validStreet) "Ange en adress" else null
          zipCodeError = if (!validZipCode) "Ange ett giltigt postnummer (5 siffror)" else null
          livingSpaceError = if (!validLivingSpace) "Ange boyta i kvadratmeter" else null

          if (validStreet && validZipCode && validLivingSpace) {
            submitIteration++
          }
        }
        ApartmentFormEvent.ClearNavigation -> {
          offersToNavigate = null
        }
        ApartmentFormEvent.Retry -> {
          submitError = null
          sessionCreateIteration++
        }
      }
    }

    LaunchedEffect(sessionCreateIteration) {
      if (sessionAndIntent != null) return@LaunchedEffect
      createSessionAndPriceIntentUseCase.invoke(productName).fold(
        ifLeft = { submitError = it },
        ifRight = { sessionAndIntent = it },
      )
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect
      val session = sessionAndIntent ?: return@LaunchedEffect
      isSubmitting = true
      submitError = null

      submitFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        street = street,
        zipCode = zipCode,
        livingSpace = livingSpace.toInt(),
        numberCoInsured = numberCoInsured,
      ).fold(
        ifLeft = {
          isSubmitting = false
          submitError = it
        },
        ifRight = { offers ->
          isSubmitting = false
          offersToNavigate = OffersNavigationData(
            shopSessionId = session.shopSessionId,
            offers = offers,
          )
        },
      )
    }

    return ApartmentFormState(
      street = street,
      zipCode = zipCode,
      livingSpace = livingSpace,
      numberCoInsured = numberCoInsured,
      streetError = streetError,
      zipCodeError = zipCodeError,
      livingSpaceError = livingSpaceError,
      isSubmitting = isSubmitting,
      submitError = submitError,
      offersToNavigate = offersToNavigate,
    )
  }
}

internal data class OffersNavigationData(
  val shopSessionId: String,
  val offers: ApartmentOffers,
)

internal sealed interface ApartmentFormEvent {
  data class UpdateStreet(val value: String) : ApartmentFormEvent
  data class UpdateZipCode(val value: String) : ApartmentFormEvent
  data class UpdateLivingSpace(val value: String) : ApartmentFormEvent
  data class UpdateNumberCoInsured(val value: Int) : ApartmentFormEvent
  data object Submit : ApartmentFormEvent
  data object ClearNavigation : ApartmentFormEvent
  data object Retry : ApartmentFormEvent
}

internal data class ApartmentFormState(
  val street: String = "",
  val zipCode: String = "",
  val livingSpace: String = "",
  val numberCoInsured: Int = 0,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val livingSpaceError: String? = null,
  val isSubmitting: Boolean = false,
  val submitError: ErrorMessage? = null,
  val offersToNavigate: OffersNavigationData? = null,
) {
  constructor() : this("", "", "", 0)
}
```

- [ ] **Step 2: Create ApartmentFormDestination composable**

`.../ui/form/ApartmentFormDestination.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.feature.purchase.apartment.data.ApartmentOffers

@Composable
internal fun ApartmentFormDestination(
  viewModel: ApartmentFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: ApartmentOffers) -> Unit,
) {
  val state = viewModel.state
  LaunchedEffect(state.offersToNavigate) {
    val nav = state.offersToNavigate ?: return@LaunchedEffect
    viewModel.emit(ApartmentFormEvent.ClearNavigation)
    onOffersReceived(nav.shopSessionId, nav.offers)
  }

  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "Hemförsäkring",
  ) {
    if (state.isSubmitting) {
      HedvigFullScreenCenterAlignedProgress()
      return@HedvigScaffold
    }

    if (state.submitError != null) {
      HedvigErrorSection(
        onButtonClick = { viewModel.emit(ApartmentFormEvent.Retry) },
      )
      return@HedvigScaffold
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
    ) {
      Spacer(Modifier.height(16.dp))

      HedvigTextField(
        value = state.street,
        onValueChange = { viewModel.emit(ApartmentFormEvent.UpdateStreet(it)) },
        labelText = "Gatuadress",
        errorState = state.streetError?.let { ErrorState.Error.WithMessage(it) } ?: ErrorState.NoError,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))

      HedvigTextField(
        value = state.zipCode,
        onValueChange = { viewModel.emit(ApartmentFormEvent.UpdateZipCode(it)) },
        labelText = "Postnummer",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        errorState = state.zipCodeError?.let { ErrorState.Error.WithMessage(it) } ?: ErrorState.NoError,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))

      HedvigTextField(
        value = state.livingSpace,
        onValueChange = { viewModel.emit(ApartmentFormEvent.UpdateLivingSpace(it)) },
        labelText = "Boyta (kvm)",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        errorState = state.livingSpaceError?.let { ErrorState.Error.WithMessage(it) } ?: ErrorState.NoError,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))

      HedvigTextField(
        value = state.numberCoInsured.toString(),
        onValueChange = { newValue ->
          newValue.toIntOrNull()?.let { viewModel.emit(ApartmentFormEvent.UpdateNumberCoInsured(it)) }
        },
        labelText = "Antal medförsäkrade",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
      )

      Spacer(Modifier.height(32.dp))
      Spacer(Modifier.weight(1f))

      HedvigButton(
        text = "Beräkna pris",
        onClick = { viewModel.emit(ApartmentFormEvent.Submit) },
        enabled = !state.isSubmitting,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}
```

- [ ] **Step 3: Verify it compiles**

```bash
./gradlew :feature-purchase-apartment:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/feature/feature-purchase-apartment/
git commit -m "feat: add apartment purchase form screen with validation"
```

---

## Task 4: Tier selection screen

**Files:**
- Create: `.../ui/offer/SelectTierViewModel.kt`
- Create: `.../ui/offer/SelectTierDestination.kt`

- [ ] **Step 1: Create SelectTierViewModel**

`.../ui/offer/SelectTierViewModel.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.apartment.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.apartment.navigation.TierOfferData
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class SelectTierViewModel(
  selectTierParameters: SelectTierParameters,
) : MoleculeViewModel<SelectTierEvent, SelectTierState>(
    initialState = SelectTierState(
      offers = selectTierParameters.offers,
      selectedOfferId = selectTierParameters.offers.firstOrNull { it.tierDisplayName.contains("Standard", ignoreCase = true) }?.offerId
        ?: selectTierParameters.offers.firstOrNull()?.offerId,
      shopSessionId = selectTierParameters.shopSessionId,
      productDisplayName = selectTierParameters.productDisplayName,
    ),
    presenter = SelectTierPresenter(selectTierParameters),
  )

internal class SelectTierPresenter(
  private val params: SelectTierParameters,
) : MoleculePresenter<SelectTierEvent, SelectTierState> {
  @Composable
  override fun MoleculePresenterScope<SelectTierEvent>.present(lastState: SelectTierState): SelectTierState {
    var selectedOfferId by remember { mutableStateOf(lastState.selectedOfferId) }
    var summaryToNavigate by remember { mutableStateOf<SummaryParameters?>(null) }

    CollectEvents { event ->
      when (event) {
        is SelectTierEvent.SelectOffer -> {
          selectedOfferId = event.offerId
        }
        SelectTierEvent.Continue -> {
          val selectedOffer = params.offers.first { it.offerId == selectedOfferId }
          summaryToNavigate = SummaryParameters(
            shopSessionId = params.shopSessionId,
            selectedOffer = selectedOffer,
            productDisplayName = params.productDisplayName,
          )
        }
        SelectTierEvent.ClearNavigation -> {
          summaryToNavigate = null
        }
      }
    }

    return SelectTierState(
      offers = params.offers,
      selectedOfferId = selectedOfferId,
      shopSessionId = params.shopSessionId,
      productDisplayName = params.productDisplayName,
      summaryToNavigate = summaryToNavigate,
    )
  }
}

internal sealed interface SelectTierEvent {
  data class SelectOffer(val offerId: String) : SelectTierEvent
  data object Continue : SelectTierEvent
  data object ClearNavigation : SelectTierEvent
}

internal data class SelectTierState(
  val offers: List<TierOfferData>,
  val selectedOfferId: String?,
  val shopSessionId: String,
  val productDisplayName: String,
  val summaryToNavigate: SummaryParameters? = null,
)
```

- [ ] **Step 2: Create SelectTierDestination composable**

`.../ui/offer/SelectTierDestination.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.apartment.navigation.TierOfferData

@Composable
internal fun SelectTierDestination(
  viewModel: SelectTierViewModel,
  navigateUp: () -> Unit,
  onContinueToSummary: (SummaryParameters) -> Unit,
) {
  val state = viewModel.state

  LaunchedEffect(state.summaryToNavigate) {
    val params = state.summaryToNavigate ?: return@LaunchedEffect
    viewModel.emit(SelectTierEvent.ClearNavigation)
    onContinueToSummary(params)
  }

  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = state.productDisplayName,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
    ) {
      Spacer(Modifier.height(16.dp))

      HedvigText(
        text = "Anpassa din försäkring",
        style = HedvigTheme.typography.headlineMedium,
      )
      Spacer(Modifier.height(4.dp))
      HedvigText(
        text = "Välj en försäkringsnivå som passar dig",
        style = HedvigTheme.typography.bodyMedium,
        color = HedvigTheme.colorScheme.textSecondary,
      )

      Spacer(Modifier.height(24.dp))

      state.offers.forEach { offer ->
        TierCard(
          offer = offer,
          isSelected = offer.offerId == state.selectedOfferId,
          onSelect = { viewModel.emit(SelectTierEvent.SelectOffer(offer.offerId)) },
        )
        Spacer(Modifier.height(12.dp))
      }

      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))

      HedvigButton(
        text = "Fortsätt",
        onClick = { viewModel.emit(SelectTierEvent.Continue) },
        enabled = state.selectedOfferId != null,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun TierCard(
  offer: TierOfferData,
  isSelected: Boolean,
  onSelect: () -> Unit,
) {
  val backgroundColor = if (isSelected) {
    HedvigTheme.colorScheme.surfacePrimary
  } else {
    HedvigTheme.colorScheme.surfaceSecondary
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(backgroundColor)
      .clickable(onClick = onSelect)
      .padding(16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      HedvigText(
        text = offer.tierDisplayName,
        style = HedvigTheme.typography.bodyLarge,
      )
      val priceText = if (offer.hasDiscount) {
        "${offer.netAmount.toInt()} kr/mån"
      } else {
        "${offer.grossAmount.toInt()} kr/mån"
      }
      HedvigText(
        text = priceText,
        style = HedvigTheme.typography.labelLarge,
      )
    }

    Spacer(Modifier.height(4.dp))
    HedvigText(
      text = offer.tierDescription,
      style = HedvigTheme.typography.bodySmall,
      color = HedvigTheme.colorScheme.textSecondary,
    )

    AnimatedVisibility(visible = isSelected && offer.usps.isNotEmpty()) {
      Column(modifier = Modifier.padding(top = 12.dp)) {
        offer.usps.forEach { usp ->
          HedvigText(
            text = "✓ $usp",
            style = HedvigTheme.typography.bodySmall,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
    }

    Spacer(Modifier.height(12.dp))

    if (isSelected) {
      HedvigButton(
        text = "Se vad som ingår",
        onClick = { /* v1: no coverage comparison */ },
        modifier = Modifier.fillMaxWidth(),
      )
    } else {
      HedvigButton(
        text = "Välj ${offer.tierDisplayName}",
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
```

- [ ] **Step 3: Verify it compiles**

```bash
./gradlew :feature-purchase-apartment:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/feature/feature-purchase-apartment/
git commit -m "feat: add tier selection screen for apartment purchase"
```

---

## Task 5: Summary and signing screens

**Files:**
- Create: `.../ui/summary/PurchaseSummaryViewModel.kt`
- Create: `.../ui/summary/PurchaseSummaryDestination.kt`
- Create: `.../ui/sign/SigningViewModel.kt`
- Create: `.../ui/sign/SigningDestination.kt`
- Create: `.../ui/success/PurchaseSuccessDestination.kt`
- Create: `.../ui/failure/PurchaseFailureDestination.kt`

- [ ] **Step 1: Create PurchaseSummaryViewModel**

`.../ui/summary/PurchaseSummaryViewModel.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.purchase.apartment.data.AddToCartAndStartSignUseCase
import com.hedvig.android.feature.purchase.apartment.data.SigningStart
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.apartment.navigation.SigningParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class PurchaseSummaryViewModel(
  summaryParameters: SummaryParameters,
  addToCartAndStartSignUseCase: AddToCartAndStartSignUseCase,
) : MoleculeViewModel<PurchaseSummaryEvent, PurchaseSummaryState>(
    initialState = PurchaseSummaryState.Content(summaryParameters),
    presenter = PurchaseSummaryPresenter(summaryParameters, addToCartAndStartSignUseCase),
  )

internal class PurchaseSummaryPresenter(
  private val params: SummaryParameters,
  private val addToCartAndStartSignUseCase: AddToCartAndStartSignUseCase,
) : MoleculePresenter<PurchaseSummaryEvent, PurchaseSummaryState> {
  @Composable
  override fun MoleculePresenterScope<PurchaseSummaryEvent>.present(
    lastState: PurchaseSummaryState,
  ): PurchaseSummaryState {
    var submitIteration by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }
    var signingToNavigate by remember { mutableStateOf<SigningParameters?>(null) }
    var navigateToFailure by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        PurchaseSummaryEvent.Confirm -> submitIteration++
        PurchaseSummaryEvent.ClearNavigation -> {
          signingToNavigate = null
          navigateToFailure = false
        }
      }
    }

    LaunchedEffect(submitIteration) {
      if (submitIteration == 0) return@LaunchedEffect
      isSubmitting = true

      addToCartAndStartSignUseCase.invoke(
        shopSessionId = params.shopSessionId,
        offerId = params.selectedOffer.offerId,
      ).fold(
        ifLeft = {
          isSubmitting = false
          navigateToFailure = true
        },
        ifRight = { signingStart ->
          isSubmitting = false
          signingToNavigate = SigningParameters(
            signingId = signingStart.signingId,
            autoStartToken = signingStart.autoStartToken,
            startDate = params.selectedOffer.exposureDisplayName,
          )
        },
      )
    }

    return PurchaseSummaryState.Content(
      params = params,
      isSubmitting = isSubmitting,
      signingToNavigate = signingToNavigate,
      navigateToFailure = navigateToFailure,
    )
  }
}

internal sealed interface PurchaseSummaryEvent {
  data object Confirm : PurchaseSummaryEvent
  data object ClearNavigation : PurchaseSummaryEvent
}

internal sealed interface PurchaseSummaryState {
  data class Content(
    val params: SummaryParameters,
    val isSubmitting: Boolean = false,
    val signingToNavigate: SigningParameters? = null,
    val navigateToFailure: Boolean = false,
  ) : PurchaseSummaryState
}
```

- [ ] **Step 2: Create PurchaseSummaryDestination**

`.../ui/summary/PurchaseSummaryDestination.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.purchase.apartment.navigation.SigningParameters

@Composable
internal fun PurchaseSummaryDestination(
  viewModel: PurchaseSummaryViewModel,
  navigateUp: () -> Unit,
  onNavigateToSigning: (SigningParameters) -> Unit,
  onNavigateToFailure: () -> Unit,
) {
  val state = viewModel.state as? PurchaseSummaryState.Content ?: return

  LaunchedEffect(state.signingToNavigate) {
    val params = state.signingToNavigate ?: return@LaunchedEffect
    viewModel.emit(PurchaseSummaryEvent.ClearNavigation)
    onNavigateToSigning(params)
  }

  LaunchedEffect(state.navigateToFailure) {
    if (!state.navigateToFailure) return@LaunchedEffect
    viewModel.emit(PurchaseSummaryEvent.ClearNavigation)
    onNavigateToFailure()
  }

  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "Sammanfattning",
  ) {
    if (state.isSubmitting) {
      HedvigFullScreenCenterAlignedProgress()
      return@HedvigScaffold
    }

    val offer = state.params.selectedOffer

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
    ) {
      Spacer(Modifier.height(16.dp))

      HedvigCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          HedvigText(
            text = state.params.productDisplayName,
            style = HedvigTheme.typography.bodyLarge,
          )
          HedvigText(
            text = offer.tierDisplayName,
            style = HedvigTheme.typography.bodyMedium,
            color = HedvigTheme.colorScheme.textSecondary,
          )
          HedvigText(
            text = offer.exposureDisplayName,
            style = HedvigTheme.typography.bodySmall,
            color = HedvigTheme.colorScheme.textSecondary,
          )
          Spacer(Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth()) {
            HedvigText(text = "Ditt pris", modifier = Modifier.weight(1f))
            if (offer.hasDiscount) {
              HedvigText(
                text = "${offer.netAmount.toInt()} kr/mån",
                style = HedvigTheme.typography.bodyLarge,
              )
            } else {
              HedvigText(
                text = "${offer.grossAmount.toInt()} kr/mån",
                style = HedvigTheme.typography.bodyLarge,
              )
            }
          }
        }
      }

      Spacer(Modifier.height(32.dp))
      Spacer(Modifier.weight(1f))

      HedvigButton(
        text = "Signera med BankID",
        onClick = { viewModel.emit(PurchaseSummaryEvent.Confirm) },
        enabled = !state.isSubmitting,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}
```

- [ ] **Step 3: Create SigningViewModel**

`.../ui/sign/SigningViewModel.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.sign

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.apartment.data.PollSigningStatusUseCase
import com.hedvig.android.feature.purchase.apartment.data.SigningStatus
import com.hedvig.android.feature.purchase.apartment.navigation.SigningParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.delay

internal class SigningViewModel(
  signingParameters: SigningParameters,
  pollSigningStatusUseCase: PollSigningStatusUseCase,
) : MoleculeViewModel<SigningEvent, SigningState>(
    initialState = SigningState.Polling(signingParameters.autoStartToken, signingParameters.startDate),
    presenter = SigningPresenter(signingParameters, pollSigningStatusUseCase),
  )

internal class SigningPresenter(
  private val params: SigningParameters,
  private val pollSigningStatusUseCase: PollSigningStatusUseCase,
) : MoleculePresenter<SigningEvent, SigningState> {
  @Composable
  override fun MoleculePresenterScope<SigningEvent>.present(lastState: SigningState): SigningState {
    var status by remember { mutableStateOf<SigningStatus?>(null) }
    var bankIdOpened by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        SigningEvent.BankIdOpened -> bankIdOpened = true
        SigningEvent.ClearNavigation -> status = null
      }
    }

    LaunchedEffect(Unit) {
      while (true) {
        delay(2000)
        pollSigningStatusUseCase.invoke(params.signingId).fold(
          ifLeft = { status = SigningStatus.FAILED },
          ifRight = { polledStatus ->
            if (polledStatus != SigningStatus.PENDING) {
              status = polledStatus
              return@LaunchedEffect
            }
          },
        )
      }
    }

    return when (status) {
      SigningStatus.SIGNED -> SigningState.Success(params.startDate)
      SigningStatus.FAILED -> SigningState.Failed
      else -> SigningState.Polling(
        autoStartToken = params.autoStartToken,
        startDate = params.startDate,
        bankIdOpened = bankIdOpened,
      )
    }
  }
}

internal sealed interface SigningEvent {
  data object BankIdOpened : SigningEvent
  data object ClearNavigation : SigningEvent
}

internal sealed interface SigningState {
  data class Polling(
    val autoStartToken: String,
    val startDate: String?,
    val bankIdOpened: Boolean = false,
  ) : SigningState

  data class Success(val startDate: String?) : SigningState
  data object Failed : SigningState
}
```

- [ ] **Step 4: Create SigningDestination**

`.../ui/sign/SigningDestination.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.sign

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Composable
internal fun SigningDestination(
  viewModel: SigningViewModel,
  onSuccess: (startDate: String?) -> Unit,
  onFailure: () -> Unit,
) {
  val state = viewModel.state
  val context = LocalContext.current

  LaunchedEffect(state) {
    if (state is SigningState.Polling && !state.bankIdOpened) {
      val bankIdUri = Uri.parse("https://app.bankid.com/?autostarttoken=${state.autoStartToken}&redirect=null")
      context.startActivity(Intent(Intent.ACTION_VIEW, bankIdUri))
      viewModel.emit(SigningEvent.BankIdOpened)
    }
  }

  LaunchedEffect(state) {
    when (state) {
      is SigningState.Success -> {
        viewModel.emit(SigningEvent.ClearNavigation)
        onSuccess(state.startDate)
      }
      is SigningState.Failed -> {
        viewModel.emit(SigningEvent.ClearNavigation)
        onFailure()
      }
      is SigningState.Polling -> {}
    }
  }

  HedvigScaffold(topAppBarText = "") {
    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigFullScreenCenterAlignedProgress()
      HedvigText(
        text = "Väntar på BankID...",
        style = HedvigTheme.typography.bodyLarge,
      )
    }
  }
}
```

- [ ] **Step 5: Create success and failure destinations**

`.../ui/success/PurchaseSuccessDestination.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.success

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Composable
internal fun PurchaseSuccessDestination(
  startDate: String?,
  onClose: () -> Unit,
) {
  HedvigScaffold(topAppBarText = "") {
    Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigText(
        text = "Din försäkring är klar!",
        style = HedvigTheme.typography.headlineMedium,
      )
      if (startDate != null) {
        Spacer(Modifier.height(8.dp))
        HedvigText(
          text = "Startdatum: $startDate",
          style = HedvigTheme.typography.bodyMedium,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.height(32.dp))
      HedvigButton(
        text = "Stäng",
        onClick = onClose,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
```

`.../ui/failure/PurchaseFailureDestination.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.ui.failure

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigScaffold

@Composable
internal fun PurchaseFailureDestination(
  onRetry: () -> Unit,
  onClose: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = onClose,
    topAppBarText = "",
  ) {
    HedvigErrorSection(onButtonClick = onRetry)
  }
}
```

- [ ] **Step 6: Verify it compiles**

```bash
./gradlew :feature-purchase-apartment:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/feature/feature-purchase-apartment/
git commit -m "feat: add summary, signing, success, and failure screens"
```

---

## Task 6: Navigation graph and DI module

**Files:**
- Create: `.../navigation/ApartmentPurchaseNavGraph.kt`
- Create: `.../di/ApartmentPurchaseModule.kt`

- [ ] **Step 1: Create ApartmentPurchaseNavGraph**

`.../navigation/ApartmentPurchaseNavGraph.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Failure
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Form
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.SelectTier
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Signing
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Success
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Summary
import com.hedvig.android.feature.purchase.apartment.ui.failure.PurchaseFailureDestination
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormDestination
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormViewModel
import com.hedvig.android.feature.purchase.apartment.ui.offer.SelectTierDestination
import com.hedvig.android.feature.purchase.apartment.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.apartment.ui.sign.SigningDestination
import com.hedvig.android.feature.purchase.apartment.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.apartment.ui.success.PurchaseSuccessDestination
import com.hedvig.android.feature.purchase.apartment.ui.summary.PurchaseSummaryDestination
import com.hedvig.android.feature.purchase.apartment.ui.summary.PurchaseSummaryViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.apartmentPurchaseNavGraph(
  navController: NavController,
  popBackStack: () -> Unit,
  finishApp: () -> Unit,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) {
  navgraph<ApartmentPurchaseGraphDestination>(
    startDestination = Form::class,
  ) {
    navdestination<Form> { backStackEntry ->
      val graphRoute = navController
        .getBackStackEntry(ApartmentPurchaseGraphDestination::class.java.name)
        .toRoute<ApartmentPurchaseGraphDestination>()

      val viewModel: ApartmentFormViewModel = koinViewModel { parametersOf(graphRoute.productName) }

      ApartmentFormDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { popBackStack() },
        onOffersReceived = dropUnlessResumed { shopSessionId, offers ->
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

    navdestination<SelectTier>(SelectTier) {
      val viewModel: SelectTierViewModel = koinViewModel {
        parametersOf(it.toRoute<SelectTier>().params)
      }

      SelectTierDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        onContinueToSummary = dropUnlessResumed { params ->
          navController.navigate(Summary(params))
        },
      )
    }

    navdestination<Summary>(Summary) {
      val viewModel: PurchaseSummaryViewModel = koinViewModel {
        parametersOf(it.toRoute<Summary>().params)
      }

      PurchaseSummaryDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        onNavigateToSigning = dropUnlessResumed { params ->
          navController.navigate(Signing(params))
        },
        onNavigateToFailure = dropUnlessResumed {
          navController.navigate(Failure)
        },
      )
    }

    navdestination<Signing>(Signing) {
      val route = it.toRoute<Signing>()
      val viewModel: SigningViewModel = koinViewModel { parametersOf(route.params) }

      SigningDestination(
        viewModel = viewModel,
        onSuccess = dropUnlessResumed { startDate ->
          crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
            CrossSellInfoType.Purchase,
          )
          navController.navigate(Success(startDate)) {
            typedPopUpTo<ApartmentPurchaseGraphDestination>({ inclusive = true })
          }
        },
        onFailure = dropUnlessResumed {
          navController.navigate(Failure)
        },
      )
    }

    navdestination<Failure> {
      PurchaseFailureDestination(
        onRetry = dropUnlessResumed { navController.popBackStack() },
        onClose = dropUnlessResumed {
          if (!navController.typedPopBackStack<ApartmentPurchaseGraphDestination>(inclusive = true)) {
            finishApp()
          }
        },
      )
    }
  }

  navdestination<Success> {
    val route = it.toRoute<Success>()
    PurchaseSuccessDestination(
      startDate = route.startDate,
      onClose = dropUnlessResumed {
        if (!navController.typedPopBackStack<ApartmentPurchaseGraphDestination>(inclusive = true)) {
          finishApp()
        }
      },
    )
  }
}
```

- [ ] **Step 2: Create ApartmentPurchaseModule**

`.../di/ApartmentPurchaseModule.kt`:
```kotlin
package com.hedvig.android.feature.purchase.apartment.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.purchase.apartment.data.AddToCartAndStartSignUseCase
import com.hedvig.android.feature.purchase.apartment.data.AddToCartAndStartSignUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.PollSigningStatusUseCase
import com.hedvig.android.feature.purchase.apartment.data.PollSigningStatusUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.apartment.navigation.SigningParameters
import com.hedvig.android.feature.purchase.apartment.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormViewModel
import com.hedvig.android.feature.purchase.apartment.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.apartment.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.apartment.ui.summary.PurchaseSummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val apartmentPurchaseModule = module {
  single<CreateSessionAndPriceIntentUseCase> {
    CreateSessionAndPriceIntentUseCaseImpl(apolloClient = get<ApolloClient>())
  }
  single<SubmitFormAndGetOffersUseCase> {
    SubmitFormAndGetOffersUseCaseImpl(apolloClient = get<ApolloClient>())
  }
  single<AddToCartAndStartSignUseCase> {
    AddToCartAndStartSignUseCaseImpl(apolloClient = get<ApolloClient>())
  }
  single<PollSigningStatusUseCase> {
    PollSigningStatusUseCaseImpl(apolloClient = get<ApolloClient>())
  }

  viewModel<ApartmentFormViewModel> { params ->
    ApartmentFormViewModel(
      productName = params.get<String>(),
      createSessionAndPriceIntentUseCase = get<CreateSessionAndPriceIntentUseCase>(),
      submitFormAndGetOffersUseCase = get<SubmitFormAndGetOffersUseCase>(),
    )
  }
  viewModel<SelectTierViewModel> { params ->
    SelectTierViewModel(selectTierParameters = params.get<SelectTierParameters>())
  }
  viewModel<PurchaseSummaryViewModel> { params ->
    PurchaseSummaryViewModel(
      summaryParameters = params.get<SummaryParameters>(),
      addToCartAndStartSignUseCase = get<AddToCartAndStartSignUseCase>(),
    )
  }
  viewModel<SigningViewModel> { params ->
    SigningViewModel(
      signingParameters = params.get<SigningParameters>(),
      pollSigningStatusUseCase = get<PollSigningStatusUseCase>(),
    )
  }
}
```

- [ ] **Step 3: Verify it compiles**

```bash
./gradlew :feature-purchase-apartment:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/feature/feature-purchase-apartment/
git commit -m "feat: add navigation graph and DI module for apartment purchase"
```

---

## Task 7: Integration into the main app

**Files:**
- Modify: `app/data/data-cross-sell-after-flow/.../CrossSellAfterFlowRepository.kt`
- Modify: `app/feature/feature-insurances/.../navigation/InsuranceGraph.kt`
- Modify: `app/app/src/main/kotlin/.../navigation/HedvigNavHost.kt`
- Modify: `app/app/src/main/kotlin/.../di/ApplicationModule.kt`

- [ ] **Step 1: Add `Purchase` to CrossSellInfoType**

In `app/data/data-cross-sell-after-flow/src/main/kotlin/com/hedvig/android/data/cross/sell/after/flow/CrossSellAfterFlowRepository.kt`, add a new variant to the `CrossSellInfoType` sealed class:

```kotlin
data object Purchase : CrossSellInfoType()
```

Add it alongside the existing variants (`ClosedClaim`, `ChangeTier`, `Addon`, `EditCoInsured`, `MovingFlow`).

- [ ] **Step 2: Add apartment purchase navigation parameter to InsuranceGraph**

In `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt`, add a new parameter to the `insuranceGraph` function:

```kotlin
fun NavGraphBuilder.insuranceGraph(
  // ... existing parameters ...
  onNavigateToApartmentPurchase: (productName: String) -> Unit,
)
```

In the `InsurancesDestination.Insurances` navdestination block, change the cross-sell click handler from:

```kotlin
onCrossSellClick = dropUnlessResumed { url: String -> openUrl(url) },
```

to route apartment products in-app and everything else to the web:

```kotlin
onCrossSellClick = dropUnlessResumed { url: String ->
  // TODO: Once the backend provides product type in cross-sell data,
  // route SE_APARTMENT_RENT and SE_APARTMENT_BRF to in-app purchase.
  // For now, all cross-sells open the web URL.
  openUrl(url)
},
```

Note: The actual routing requires the cross-sell to carry a `productName` field. This likely needs a backend change to the `CrossSellV2` type or a way to derive the product type from the cross-sell data. For the initial integration, the in-app flow can be triggered from a deep link or a direct navigation call while the cross-sell routing is resolved with the backend team.

- [ ] **Step 3: Register the nav graph in HedvigNavHost**

In `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`:

Add import:
```kotlin
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseGraphDestination
import com.hedvig.android.feature.purchase.apartment.navigation.apartmentPurchaseNavGraph
```

Add to the `insuranceGraph(...)` call:
```kotlin
onNavigateToApartmentPurchase = { productName ->
  navController.navigate(ApartmentPurchaseGraphDestination(productName))
},
```

Add as a sibling graph in the NavHost (alongside `addonPurchaseNavGraph`, `changeTierGraph`, etc.):
```kotlin
apartmentPurchaseNavGraph(
  navController = navController,
  popBackStack = popBackStackOrFinish,
  finishApp = finishApp,
  crossSellAfterFlowRepository = get<CrossSellAfterFlowRepository>(),
)
```

- [ ] **Step 4: Register DI module in ApplicationModule**

In `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt`:

Add import:
```kotlin
import com.hedvig.android.feature.purchase.apartment.di.apartmentPurchaseModule
```

Add to the `includes(listOf(...))` call:
```kotlin
apartmentPurchaseModule,
```

- [ ] **Step 5: Verify the full app builds**

```bash
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL. The module is auto-discovered by `settings.gradle.kts`, the DI module is included, and the nav graph is registered.

- [ ] **Step 6: Commit**

```bash
git add app/data/data-cross-sell-after-flow/ app/feature/feature-insurances/ app/app/
git commit -m "feat: integrate apartment purchase flow into main app navigation"
```

---

## Task 8: Run ktlint and fix formatting

- [ ] **Step 1: Run ktlint check on the new module**

```bash
./gradlew :feature-purchase-apartment:ktlintCheck
```

- [ ] **Step 2: Fix any formatting issues**

```bash
./gradlew :feature-purchase-apartment:ktlintFormat
```

- [ ] **Step 3: Run all tests**

```bash
./gradlew :feature-purchase-apartment:test
```

Expected: All tests pass.

- [ ] **Step 4: Run the full app build**

```bash
./gradlew :app:assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit any formatting fixes**

```bash
git add app/feature/feature-purchase-apartment/
git commit -m "chore: fix ktlint formatting in apartment purchase module"
```

---

## Notes for Implementer

### GraphQL `PricingFormData` type
The `PricingFormData` scalar type in the schema is a `Map<String, Any>`. When calling `ApartmentPriceIntentDataUpdateMutation`, you pass a `Map<String, Any>` with keys: `"street"`, `"zipCode"`, `"livingSpace"`, `"numberCoInsured"`. Apollo's custom scalar adapter handles the serialization. Check how racoon sends this data in `/Users/hugolinder/repos/racoon/apps/store/src/graphql/PriceIntentDataUpdate.graphql` for reference.

### BankID URI pattern
The BankID app is launched via `https://app.bankid.com/?autostarttoken={token}&redirect=null`. See `app/feature/feature-login/src/main/kotlin/com/hedvig/android/feature/login/swedishlogin/BankIdState.kt` for the existing implementation including app detection and QR code fallback. For v1, the simple URI launch is sufficient.

### Cross-sell routing
The current cross-sell click handler receives only a `storeUrl: String`. To route apartment products in-app, one of these is needed:
1. Backend adds a `productName` field to the `CrossSellV2.OtherCrossSell` type
2. Derive the product type from the `storeUrl` (e.g., parse the URL path)
3. Add a new query field

Discuss with the backend team which approach they prefer. Until resolved, the in-app flow can be tested via direct navigation (e.g., a debug button or deep link).

### Key reference files
- Addon purchase flow: `app/feature/feature-addon-purchase/` — simplest existing purchase pattern
- Moving flow: `app/feature/feature-movingflow/` — multi-step form with validation
- Termination flow: `app/feature/feature-terminate-insurance/` — backend-driven navigation
- Login BankID: `app/feature/feature-login/src/main/kotlin/.../BankIdState.kt` — BankID launch
- Racoon GraphQL: `/Users/hugolinder/repos/racoon/apps/store/src/graphql/` — web query patterns

# Car Purchase Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract shared purchase screens into `feature-purchase-common`, create `feature-purchase-car` with car-specific form, and wire both into the app navigation.

**Architecture:** Three-module approach — shared screens/use-cases/models in `feature-purchase-common`, product-specific forms and GraphQL in `feature-purchase-apartment` and `feature-purchase-car`. Both product modules depend on common but not each other.

**Tech Stack:** Kotlin, Jetpack Compose, Apollo GraphQL, Molecule (MVI), Koin DI, Arrow (Either), kotlinx.serialization, ZXing (QR codes)

---

### Task 1: Create `feature-purchase-common` module with shared models and GraphQL

**Files:**
- Create: `app/feature/feature-purchase-common/build.gradle.kts`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/data/PurchaseCommonModels.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/navigation/PurchaseCommonDestination.kt`
- Create: `app/feature/feature-purchase-common/src/main/graphql/ShopSessionCartEntriesAddMutation.graphql`
- Create: `app/feature/feature-purchase-common/src/main/graphql/ShopSessionStartSignMutation.graphql`
- Create: `app/feature/feature-purchase-common/src/main/graphql/ShopSessionSigningQuery.graphql`
- Create: `app/feature/feature-purchase-common/src/main/graphql/ProductOfferFragment.graphql`

- [ ] **Step 1: Create `build.gradle.kts`**

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
  implementation(libs.apollo.normalizedCache)
  implementation(libs.arrow.core)
  implementation(libs.arrow.fx)
  implementation(libs.jetbrains.lifecycle.runtime.compose)
  implementation(libs.koin.composeViewModel)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.zXing)
  implementation(projects.apolloCore)
  implementation(projects.apolloOctopusPublic)
  implementation(projects.composeUi)
  implementation(projects.coreCommonPublic)
  implementation(projects.coreResources)
  implementation(projects.coreUiData)
  implementation(projects.designSystemHedvig)
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

- [ ] **Step 2: Create shared models**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/data/PurchaseCommonModels.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.data

data class SigningStart(
  val signingId: String,
  val autoStartToken: String,
)

data class SigningPollResult(
  val status: SigningStatus,
  val liveQrCodeData: String?,
)

enum class SigningStatus {
  PENDING,
  SIGNED,
  FAILED,
}
```

- [ ] **Step 3: Create shared navigation models**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/navigation/PurchaseCommonDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data class TierOfferData(
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
data class SelectTierParameters(
  val shopSessionId: String,
  val offers: List<TierOfferData>,
  val productDisplayName: String,
)

@Serializable
data class SummaryParameters(
  val shopSessionId: String,
  val selectedOffer: TierOfferData,
  val productDisplayName: String,
)

@Serializable
data class SigningParameters(
  val signingId: String,
  val autoStartToken: String,
  val startDate: String?,
)

sealed interface PurchaseCommonDestination {
  @Serializable
  data class SelectTier(
    val params: SelectTierParameters,
  ) : PurchaseCommonDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SelectTierParameters>())
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : PurchaseCommonDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class Signing(
    val params: SigningParameters,
  ) : PurchaseCommonDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SigningParameters>())
    }
  }

  @Serializable
  data class Success(
    val startDate: String?,
  ) : PurchaseCommonDestination, Destination

  @Serializable
  data object Failure : PurchaseCommonDestination, Destination
}
```

- [ ] **Step 4: Create GraphQL operations**

File: `app/feature/feature-purchase-common/src/main/graphql/ShopSessionCartEntriesAddMutation.graphql`
```graphql
mutation PurchaseCartEntriesAdd($shopSessionId: UUID!, $offerIds: [UUID!]!) {
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

File: `app/feature/feature-purchase-common/src/main/graphql/ShopSessionStartSignMutation.graphql`
```graphql
mutation PurchaseStartSign($shopSessionId: UUID!) {
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

File: `app/feature/feature-purchase-common/src/main/graphql/ShopSessionSigningQuery.graphql`
```graphql
query PurchaseShopSessionSigning($signingId: UUID!) {
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

File: `app/feature/feature-purchase-common/src/main/graphql/ProductOfferFragment.graphql`
```graphql
fragment PurchaseProductOfferFragment on ProductOffer {
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

- [ ] **Step 5: Commit**

```bash
git add app/feature/feature-purchase-common/
git commit -m "feat: scaffold feature-purchase-common module with shared models and GraphQL"
```

---

### Task 2: Move shared use cases to `feature-purchase-common`

**Files:**
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/data/AddToCartAndStartSignUseCase.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/data/PollSigningStatusUseCase.kt`

- [ ] **Step 1: Create `AddToCartAndStartSignUseCase`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/data/AddToCartAndStartSignUseCase.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.PurchaseCartEntriesAddMutation
import octopus.PurchaseStartSignMutation

interface AddToCartAndStartSignUseCase {
  suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, SigningStart>
}

internal class AddToCartAndStartSignUseCaseImpl(
  private val apolloClient: ApolloClient,
) : AddToCartAndStartSignUseCase {
  override suspend fun invoke(shopSessionId: String, offerId: String): Either<ErrorMessage, SigningStart> {
    return either {
      val cartResult = apolloClient
        .mutation(PurchaseCartEntriesAddMutation(shopSessionId = shopSessionId, offerIds = listOf(offerId)))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to add to cart: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCartEntriesAdd },
        )

      if (cartResult.userError != null) {
        raise(ErrorMessage(cartResult.userError?.message))
      }

      val signResult = apolloClient
        .mutation(PurchaseStartSignMutation(shopSessionId = shopSessionId))
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

- [ ] **Step 2: Create `PollSigningStatusUseCase`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/data/PollSigningStatusUseCase.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.PurchaseShopSessionSigningQuery
import octopus.type.ShopSessionSigningStatus

interface PollSigningStatusUseCase {
  suspend fun invoke(signingId: String): Either<ErrorMessage, SigningPollResult>
}

internal class PollSigningStatusUseCaseImpl(
  private val apolloClient: ApolloClient,
) : PollSigningStatusUseCase {
  override suspend fun invoke(signingId: String): Either<ErrorMessage, SigningPollResult> {
    return either {
      apolloClient
        .query(PurchaseShopSessionSigningQuery(signingId = signingId))
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to poll signing status: $it" }
            raise(ErrorMessage())
          },
          ifRight = { result ->
            val signing = result.shopSessionSigning
            val status = when (signing.status) {
              ShopSessionSigningStatus.SIGNED -> SigningStatus.SIGNED
              ShopSessionSigningStatus.FAILED -> SigningStatus.FAILED
              ShopSessionSigningStatus.PENDING,
              ShopSessionSigningStatus.CREATING,
              ShopSessionSigningStatus.UNKNOWN__,
              -> SigningStatus.PENDING
            }
            SigningPollResult(
              status = status,
              liveQrCodeData = signing.seBankidProperties?.liveQrCodeData,
            )
          },
        )
    }
  }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/data/
git commit -m "feat: add shared AddToCartAndStartSign and PollSigningStatus use cases"
```

---

### Task 3: Move shared screens to `feature-purchase-common`

**Files:**
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/offer/SelectTierDestination.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/offer/SelectTierViewModel.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/summary/PurchaseSummaryDestination.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/summary/PurchaseSummaryViewModel.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/sign/SigningDestination.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/sign/SigningViewModel.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/success/PurchaseSuccessDestination.kt`
- Create: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/failure/PurchaseFailureDestination.kt`

- [ ] **Step 1: Move `SelectTierViewModel.kt`**

Copy from `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/ui/offer/SelectTierViewModel.kt` — update package to `com.hedvig.android.feature.purchase.common.ui.offer` and update imports from `com.hedvig.android.feature.purchase.apartment.navigation.*` to `com.hedvig.android.feature.purchase.common.navigation.*`. Remove `internal` visibility from `SelectTierViewModel`, `SelectTierPresenter`, `TierGroup`, `DeductibleOption`, `SelectTierUiState`, and `SelectTierEvent` since they'll be consumed cross-module.

Full file: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/offer/SelectTierViewModel.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.ui.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.common.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

class SelectTierViewModel(
  params: SelectTierParameters,
) : MoleculeViewModel<SelectTierEvent, SelectTierUiState>(
  buildInitialState(params),
  SelectTierPresenter(params),
)

private fun buildInitialState(params: SelectTierParameters): SelectTierUiState {
  val tierGroups = groupOffersByTier(params.offers)
  val defaultTierName = tierGroups.firstOrNull { "Standard" in it.tierDisplayName }?.tierDisplayName
    ?: tierGroups.firstOrNull()?.tierDisplayName
    ?: ""
  val defaultDeductibleByTier = tierGroups.associate { group ->
    group.tierDisplayName to (group.deductibleOptions.minByOrNull { it.netAmount }?.offerId ?: "")
  }
  return SelectTierUiState(
    tierGroups = tierGroups,
    selectedTierName = defaultTierName,
    selectedDeductibleByTier = defaultDeductibleByTier,
    shopSessionId = params.shopSessionId,
    productDisplayName = params.productDisplayName,
    summaryToNavigate = null,
  )
}

private fun groupOffersByTier(offers: List<TierOfferData>): List<TierGroup> {
  return offers.groupBy { it.tierDisplayName }.map { (tierName, tierOffers) ->
    val first = tierOffers.first()
    TierGroup(
      tierDisplayName = tierName,
      tierDescription = first.tierDescription,
      usps = first.usps,
      deductibleOptions = tierOffers.map { offer ->
        DeductibleOption(
          offerId = offer.offerId,
          deductibleDisplayName = offer.deductibleDisplayName ?: "",
          netAmount = offer.netAmount,
          netCurrencyCode = offer.netCurrencyCode,
          grossAmount = offer.grossAmount,
          grossCurrencyCode = offer.grossCurrencyCode,
          hasDiscount = offer.hasDiscount,
        )
      }.sortedBy { it.netAmount },
    )
  }
}

class SelectTierPresenter(
  private val params: SelectTierParameters,
) : MoleculePresenter<SelectTierEvent, SelectTierUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectTierEvent>.present(lastState: SelectTierUiState): SelectTierUiState {
    var selectedTierName by remember { mutableStateOf(lastState.selectedTierName) }
    var selectedDeductibleByTier by remember { mutableStateOf(lastState.selectedDeductibleByTier) }
    var summaryToNavigate: SummaryParameters? by remember { mutableStateOf(lastState.summaryToNavigate) }

    CollectEvents { event ->
      when (event) {
        is SelectTierEvent.SelectTier -> {
          selectedTierName = event.tierName
        }

        is SelectTierEvent.SelectDeductible -> {
          selectedDeductibleByTier = selectedDeductibleByTier + (event.tierName to event.offerId)
        }

        SelectTierEvent.Continue -> {
          val selectedOfferId = selectedDeductibleByTier[selectedTierName] ?: return@CollectEvents
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

    return SelectTierUiState(
      tierGroups = lastState.tierGroups,
      selectedTierName = selectedTierName,
      selectedDeductibleByTier = selectedDeductibleByTier,
      shopSessionId = params.shopSessionId,
      productDisplayName = params.productDisplayName,
      summaryToNavigate = summaryToNavigate,
    )
  }
}

data class TierGroup(
  val tierDisplayName: String,
  val tierDescription: String,
  val usps: List<String>,
  val deductibleOptions: List<DeductibleOption>,
)

data class DeductibleOption(
  val offerId: String,
  val deductibleDisplayName: String,
  val netAmount: Double,
  val netCurrencyCode: String,
  val grossAmount: Double,
  val grossCurrencyCode: String,
  val hasDiscount: Boolean,
)

data class SelectTierUiState(
  val tierGroups: List<TierGroup>,
  val selectedTierName: String,
  val selectedDeductibleByTier: Map<String, String>,
  val shopSessionId: String,
  val productDisplayName: String,
  val summaryToNavigate: SummaryParameters?,
)

sealed interface SelectTierEvent {
  data class SelectTier(val tierName: String) : SelectTierEvent
  data class SelectDeductible(val tierName: String, val offerId: String) : SelectTierEvent
  data object Continue : SelectTierEvent
  data object ClearNavigation : SelectTierEvent
}
```

- [ ] **Step 2: Move `SelectTierDestination.kt`**

Copy from apartment module, update package to `com.hedvig.android.feature.purchase.common.ui.offer`, update imports to use `com.hedvig.android.feature.purchase.common.navigation.SummaryParameters`. Remove `internal` visibility.

Full file: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/offer/SelectTierDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.ui.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun SelectTierDestination(
  viewModel: SelectTierViewModel,
  navigateUp: () -> Unit,
  onContinueToSummary: (SummaryParameters) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  if (uiState.summaryToNavigate != null) {
    LaunchedEffect(uiState.summaryToNavigate) {
      viewModel.emit(SelectTierEvent.ClearNavigation)
      onContinueToSummary(uiState.summaryToNavigate)
    }
  }
  SelectTierContent(
    uiState = uiState,
    navigateUp = navigateUp,
    onSelectTier = { viewModel.emit(SelectTierEvent.SelectTier(it)) },
    onSelectDeductible = { tierName, offerId ->
      viewModel.emit(SelectTierEvent.SelectDeductible(tierName, offerId))
    },
    onContinue = { viewModel.emit(SelectTierEvent.Continue) },
  )
}

@Composable
private fun SelectTierContent(
  uiState: SelectTierUiState,
  navigateUp: () -> Unit = {},
  onSelectTier: (String) -> Unit = {},
  onSelectDeductible: (tierName: String, offerId: String) -> Unit = { _, _ -> },
  onContinue: () -> Unit = {},
) {
  HedvigScaffold(
    navigateUp = navigateUp,
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = "Anpassa din f\u00f6rs\u00e4kring",
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(4.dp))
    HedvigText(
      text = "V\u00e4lj den skyddsniv\u00e5 som passar dig b\u00e4st",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    for ((index, tierGroup) in uiState.tierGroups.withIndex()) {
      val isSelected = tierGroup.tierDisplayName == uiState.selectedTierName
      val selectedDeductibleId = uiState.selectedDeductibleByTier[tierGroup.tierDisplayName]
      val selectedDeductible = tierGroup.deductibleOptions.firstOrNull { it.offerId == selectedDeductibleId }
        ?: tierGroup.deductibleOptions.firstOrNull()
      TierGroupCard(
        tierGroup = tierGroup,
        isSelected = isSelected,
        selectedDeductibleId = selectedDeductible?.offerId ?: "",
        onSelectTier = { onSelectTier(tierGroup.tierDisplayName) },
        onSelectDeductible = { offerId -> onSelectDeductible(tierGroup.tierDisplayName, offerId) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      if (index < uiState.tierGroups.lastIndex) {
        Spacer(Modifier.height(12.dp))
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigButton(
      text = "Forts\u00e4tt",
      onClick = dropUnlessResumed { onContinue() },
      enabled = uiState.selectedTierName.isNotEmpty(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun TierGroupCard(
  tierGroup: TierGroup,
  isSelected: Boolean,
  selectedDeductibleId: String,
  onSelectTier: () -> Unit,
  onSelectDeductible: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedOption = tierGroup.deductibleOptions.firstOrNull { it.offerId == selectedDeductibleId }
  HedvigCard(
    onClick = onSelectTier,
    borderColor = if (isSelected) {
      HedvigTheme.colorScheme.signalGreenElement
    } else {
      HedvigTheme.colorScheme.borderSecondary
    },
    modifier = modifier,
  ) {
    Column(Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          text = tierGroup.tierDisplayName,
          style = HedvigTheme.typography.bodyLarge,
        )
        if (selectedOption != null) {
          HedvigText(
            text = formatPrice(selectedOption.netAmount, selectedOption.netCurrencyCode),
            style = HedvigTheme.typography.bodyLarge,
          )
        }
      }
      AnimatedVisibility(
        visible = isSelected,
        enter = expandVertically(),
        exit = shrinkVertically(),
      ) {
        Column {
          if (tierGroup.usps.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            for (usp in tierGroup.usps) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp),
              ) {
                Icon(
                  HedvigIcons.Checkmark,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                  tint = HedvigTheme.colorScheme.signalGreenElement,
                )
                Spacer(Modifier.width(8.dp))
                HedvigText(
                  text = usp,
                  style = HedvigTheme.typography.bodyMedium,
                  color = HedvigTheme.colorScheme.textSecondary,
                )
              }
            }
          }
          if (tierGroup.deductibleOptions.size > 1) {
            Spacer(Modifier.height(12.dp))
            HedvigText(
              text = "Sj\u00e4lvrisk",
              style = HedvigTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            RadioGroup(
              options = tierGroup.deductibleOptions.map { option ->
                RadioOption(
                  id = RadioOptionId(option.offerId),
                  text = option.deductibleDisplayName,
                  label = formatPrice(option.netAmount, option.netCurrencyCode),
                )
              },
              selectedOption = RadioOptionId(selectedDeductibleId),
              onRadioOptionSelected = { onSelectDeductible(it.id) },
              size = RadioGroupSize.Small,
            )
          }
        }
      }
      AnimatedVisibility(
        visible = !isSelected,
        enter = expandVertically(),
        exit = shrinkVertically(),
      ) {
        Column {
          Spacer(Modifier.height(8.dp))
          HedvigText(
            text = "V\u00e4lj ${tierGroup.tierDisplayName}",
            style = HedvigTheme.typography.bodyMedium,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
    }
  }
}

private fun formatPrice(amount: Double, currencyCode: String): String {
  @Suppress("DEPRECATION")
  val format = NumberFormat.getCurrencyInstance(Locale("sv", "SE"))
  format.currency = Currency.getInstance(currencyCode)
  format.maximumFractionDigits = 0
  return "${format.format(amount)}/m\u00e5n"
}
```

- [ ] **Step 3: Move `PurchaseSummaryViewModel.kt`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/summary/PurchaseSummaryViewModel.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.ui.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.common.data.AddToCartAndStartSignUseCase
import com.hedvig.android.feature.purchase.common.navigation.SigningParameters
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

class PurchaseSummaryViewModel(
  summaryParameters: SummaryParameters,
  addToCartAndStartSignUseCase: AddToCartAndStartSignUseCase,
) : MoleculeViewModel<PurchaseSummaryEvent, PurchaseSummaryUiState>(
  initialState = PurchaseSummaryUiState(
    params = summaryParameters,
    isSubmitting = false,
    signingToNavigate = null,
    navigateToFailure = false,
  ),
  presenter = PurchaseSummaryPresenter(
    summaryParameters,
    addToCartAndStartSignUseCase,
  ),
)

class PurchaseSummaryPresenter(
  private val summaryParameters: SummaryParameters,
  private val addToCartAndStartSignUseCase: AddToCartAndStartSignUseCase,
) : MoleculePresenter<PurchaseSummaryEvent, PurchaseSummaryUiState> {
  @Composable
  override fun MoleculePresenterScope<PurchaseSummaryEvent>.present(
    lastState: PurchaseSummaryUiState,
  ): PurchaseSummaryUiState {
    var confirmIteration by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(lastState.isSubmitting) }
    var signingToNavigate by remember { mutableStateOf(lastState.signingToNavigate) }
    var navigateToFailure by remember { mutableStateOf(lastState.navigateToFailure) }

    CollectEvents { event ->
      when (event) {
        PurchaseSummaryEvent.Confirm -> {
          confirmIteration++
        }

        PurchaseSummaryEvent.ClearNavigation -> {
          signingToNavigate = null
          navigateToFailure = false
        }
      }
    }

    LaunchedEffect(confirmIteration) {
      if (confirmIteration > 0) {
        isSubmitting = true
        addToCartAndStartSignUseCase.invoke(
          summaryParameters.shopSessionId,
          summaryParameters.selectedOffer.offerId,
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
              startDate = null,
            )
          },
        )
      }
    }

    return PurchaseSummaryUiState(
      params = summaryParameters,
      isSubmitting = isSubmitting,
      signingToNavigate = signingToNavigate,
      navigateToFailure = navigateToFailure,
    )
  }
}

data class PurchaseSummaryUiState(
  val params: SummaryParameters,
  val isSubmitting: Boolean,
  val signingToNavigate: SigningParameters?,
  val navigateToFailure: Boolean,
)

sealed interface PurchaseSummaryEvent {
  data object Confirm : PurchaseSummaryEvent
  data object ClearNavigation : PurchaseSummaryEvent
}
```

- [ ] **Step 4: Move `PurchaseSummaryDestination.kt`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/summary/PurchaseSummaryDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.common.navigation.SigningParameters
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData

@Composable
fun PurchaseSummaryDestination(
  viewModel: PurchaseSummaryViewModel,
  navigateUp: () -> Unit,
  navigateToSigning: (SigningParameters) -> Unit,
  navigateToFailure: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(uiState.signingToNavigate) {
    val signing = uiState.signingToNavigate ?: return@LaunchedEffect
    viewModel.emit(PurchaseSummaryEvent.ClearNavigation)
    navigateToSigning(signing)
  }

  LaunchedEffect(uiState.navigateToFailure) {
    if (!uiState.navigateToFailure) return@LaunchedEffect
    viewModel.emit(PurchaseSummaryEvent.ClearNavigation)
    navigateToFailure()
  }

  PurchaseSummaryScreen(
    params = uiState.params,
    isSubmitting = uiState.isSubmitting,
    navigateUp = navigateUp,
    onConfirm = { viewModel.emit(PurchaseSummaryEvent.Confirm) },
  )
}

@Composable
private fun PurchaseSummaryScreen(
  params: SummaryParameters,
  isSubmitting: Boolean,
  navigateUp: () -> Unit,
  onConfirm: () -> Unit,
) {
  HedvigScaffold(navigateUp) {
    val offer = params.selectedOffer
    HedvigCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Column(modifier = Modifier.padding(16.dp)) {
        HedvigText(
          text = params.productDisplayName,
          style = HedvigTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(4.dp))
        HedvigText(
          text = offer.tierDisplayName,
          style = HedvigTheme.typography.bodySmall,
          color = HedvigTheme.colorScheme.textSecondary,
        )
        Spacer(Modifier.height(8.dp))
        HedvigText(
          text = offer.exposureDisplayName,
          style = HedvigTheme.typography.bodySmall,
        )
        if (offer.deductibleDisplayName != null) {
          Spacer(Modifier.height(4.dp))
          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              HedvigText(
                text = "Sj\u00e4lvrisk",
                style = HedvigTheme.typography.bodySmall,
                color = HedvigTheme.colorScheme.textSecondary,
              )
            },
            spaceBetween = 8.dp,
            endSlot = {
              HedvigText(
                text = offer.deductibleDisplayName,
                style = HedvigTheme.typography.bodySmall,
                color = HedvigTheme.colorScheme.textSecondary,
              )
            },
          )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            HedvigText(
              text = "Pris",
              style = HedvigTheme.typography.bodySmall,
            )
          },
          spaceBetween = 8.dp,
          endSlot = {
            if (offer.hasDiscount && offer.grossAmount != offer.netAmount) {
              Row {
                HedvigText(
                  text = "${offer.grossAmount.toInt()} ${offer.grossCurrencyCode}/m\u00e5n",
                  style = HedvigTheme.typography.bodySmall,
                  color = HedvigTheme.colorScheme.textSecondary,
                  textDecoration = TextDecoration.LineThrough,
                )
                Spacer(Modifier.width(4.dp))
                HedvigText(
                  text = "${offer.netAmount.toInt()} ${offer.netCurrencyCode}/m\u00e5n",
                  style = HedvigTheme.typography.bodySmall,
                )
              }
            } else {
              HedvigText(
                text = "${offer.netAmount.toInt()} ${offer.netCurrencyCode}/m\u00e5n",
                style = HedvigTheme.typography.bodySmall,
              )
            }
          },
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Signera med BankID",
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      buttonStyle = Primary,
      buttonSize = Large,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      onClick = onConfirm,
    )
    Spacer(Modifier.height(16.dp))
  }
}
```

- [ ] **Step 5: Move `SigningViewModel.kt`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/sign/SigningViewModel.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.ui.sign

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.common.data.PollSigningStatusUseCase
import com.hedvig.android.feature.purchase.common.data.SigningStatus
import com.hedvig.android.feature.purchase.common.navigation.SigningParameters
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.delay

class SigningViewModel(
  signingParameters: SigningParameters,
  pollSigningStatusUseCase: PollSigningStatusUseCase,
) : MoleculeViewModel<SigningEvent, SigningUiState>(
  initialState = SigningUiState.Polling(
    autoStartToken = signingParameters.autoStartToken,
    startDate = signingParameters.startDate,
    liveQrCodeData = null,
    bankIdOpened = false,
  ),
  presenter = SigningPresenter(signingParameters, pollSigningStatusUseCase),
)

class SigningPresenter(
  private val signingParameters: SigningParameters,
  private val pollSigningStatusUseCase: PollSigningStatusUseCase,
) : MoleculePresenter<SigningEvent, SigningUiState> {
  @Composable
  override fun MoleculePresenterScope<SigningEvent>.present(lastState: SigningUiState): SigningUiState {
    var bankIdOpened by remember { mutableStateOf((lastState as? SigningUiState.Polling)?.bankIdOpened ?: false) }
    var currentState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        SigningEvent.BankIdOpened -> {
          bankIdOpened = true
        }

        SigningEvent.ClearNavigation -> {}
      }
    }

    LaunchedEffect(Unit) {
      while (true) {
        pollSigningStatusUseCase.invoke(signingParameters.signingId).fold(
          ifLeft = {
            currentState = SigningUiState.Failed
            return@LaunchedEffect
          },
          ifRight = { pollResult ->
            when (pollResult.status) {
              SigningStatus.SIGNED -> {
                currentState = SigningUiState.Success(startDate = signingParameters.startDate)
                return@LaunchedEffect
              }

              SigningStatus.FAILED -> {
                currentState = SigningUiState.Failed
                return@LaunchedEffect
              }

              SigningStatus.PENDING -> {
                currentState = SigningUiState.Polling(
                  autoStartToken = signingParameters.autoStartToken,
                  startDate = signingParameters.startDate,
                  liveQrCodeData = pollResult.liveQrCodeData,
                  bankIdOpened = bankIdOpened,
                )
              }
            }
          },
        )
        delay(2_000)
      }
    }

    return when (val state = currentState) {
      is SigningUiState.Polling -> state.copy(bankIdOpened = bankIdOpened)
      else -> currentState
    }
  }
}

sealed interface SigningUiState {
  data class Polling(
    val autoStartToken: String,
    val startDate: String?,
    val liveQrCodeData: String?,
    val bankIdOpened: Boolean,
  ) : SigningUiState

  data class Success(val startDate: String?) : SigningUiState

  data object Failed : SigningUiState
}

sealed interface SigningEvent {
  data object BankIdOpened : SigningEvent
  data object ClearNavigation : SigningEvent
}
```

- [ ] **Step 6: Move `SigningDestination.kt`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/sign/SigningDestination.kt`

Same as existing file but with package `com.hedvig.android.feature.purchase.common.ui.sign`, public visibility, and imports updated to reference `com.hedvig.android.feature.purchase.common.ui.sign.SigningViewModel`, etc.

```kotlin
package com.hedvig.android.feature.purchase.common.ui.sign

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SigningDestination(
  viewModel: SigningViewModel,
  navigateToSuccess: (startDate: String?) -> Unit,
  navigateToFailure: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val canOpenBankId = remember { canBankIdAppHandleUri(context) }
  var hasNavigated by remember { mutableStateOf(false) }

  LaunchedEffect(uiState) {
    if (hasNavigated) return@LaunchedEffect
    when (val state = uiState) {
      is SigningUiState.Success -> {
        hasNavigated = true
        navigateToSuccess(state.startDate)
      }
      is SigningUiState.Failed -> {
        hasNavigated = true
        navigateToFailure()
      }
      is SigningUiState.Polling -> {}
    }
  }

  when (val state = uiState) {
    is SigningUiState.Polling -> {
      if (canOpenBankId && !state.bankIdOpened) {
        LaunchedEffect(Unit) {
          val bankIdUri = Uri.parse("https://app.bankid.com/?autostarttoken=${state.autoStartToken}&redirect=null")
          context.startActivity(Intent(Intent.ACTION_VIEW, bankIdUri))
          viewModel.emit(SigningEvent.BankIdOpened)
        }
        HedvigFullScreenCenterAlignedProgress()
      } else if (!canOpenBankId) {
        QrCodeSigningScreen(
          liveQrCodeData = state.liveQrCodeData,
          onOpenBankId = {
            val bankIdUri = Uri.parse("https://app.bankid.com/?autostarttoken=${state.autoStartToken}&redirect=null")
            context.startActivity(Intent(Intent.ACTION_VIEW, bankIdUri))
            viewModel.emit(SigningEvent.BankIdOpened)
          },
        )
      } else {
        HedvigFullScreenCenterAlignedProgress()
      }
    }

    is SigningUiState.Success,
    is SigningUiState.Failed,
    -> HedvigFullScreenCenterAlignedProgress()
  }
}

@Composable
private fun QrCodeSigningScreen(liveQrCodeData: String?, onOpenBankId: () -> Unit) {
  HedvigScaffold(navigateUp = {}) {
    Spacer(Modifier.weight(1f))
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      HedvigText(
        text = "Logga in med BankID",
        style = HedvigTheme.typography.headlineMedium,
      )
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = "Skanna QR-koden med BankID-appen p\u00e5 en annan enhet",
        style = HedvigTheme.typography.bodyMedium,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      Spacer(Modifier.height(24.dp))
      if (liveQrCodeData != null) {
        QRCode(
          data = liveQrCodeData,
          modifier = Modifier.size(200.dp),
        )
      } else {
        HedvigFullScreenCenterAlignedProgress()
      }
      Spacer(Modifier.height(24.dp))
      HedvigButton(
        text = "\u00d6ppna BankID",
        onClick = onOpenBankId,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.weight(1f))
  }
}

@Composable
private fun QRCode(data: String, modifier: Modifier = Modifier) {
  var intSize: IntSize? by remember { mutableStateOf(null) }
  val painter by produceState<Painter>(ColorPainter(Color.Transparent), intSize, data) {
    val size = intSize ?: return@produceState
    val bitmapPainter: BitmapPainter = withContext(Dispatchers.Default) {
      val bitMatrix: BitMatrix = QRCodeWriter().encode(
        data,
        BarcodeFormat.QR_CODE,
        size.width,
        size.height,
      )
      val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.RGB_565)
      for (x in 0 until size.width) {
        for (y in 0 until size.height) {
          val color = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
          bitmap.setPixel(x, y, color)
        }
      }
      BitmapPainter(bitmap.asImageBitmap())
    }
    value = bitmapPainter
  }
  Image(
    painter,
    contentDescription = "BankID QR code",
    modifier.onSizeChanged { intSize = it },
  )
}

@SuppressLint("QueryPermissionsNeeded")
private fun canBankIdAppHandleUri(context: Context): Boolean {
  return try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.packageManager.getPackageInfo(
        BANK_ID_APP_PACKAGE_NAME,
        PackageManager.PackageInfoFlags.of(0),
      )
    } else {
      @Suppress("DEPRECATION")
      context.packageManager.getPackageInfo(BANK_ID_APP_PACKAGE_NAME, 0)
    }
    true
  } catch (e: PackageManager.NameNotFoundException) {
    logcat(LogPriority.INFO) { "BankID app not installed, will show QR code" }
    false
  }
}

private const val BANK_ID_APP_PACKAGE_NAME = "com.bankid.bus"
```

- [ ] **Step 7: Move `PurchaseSuccessDestination.kt`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/success/PurchaseSuccessDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.ui.success

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Primary
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.TopAppBarActionType

@Composable
fun PurchaseSuccessDestination(startDate: String?, close: () -> Unit) {
  HedvigScaffold(
    navigateUp = close,
    topAppBarActionType = TopAppBarActionType.CLOSE,
    itemsColumnHorizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Spacer(Modifier.weight(1f))
    HedvigText(
      text = "Din f\u00f6rs\u00e4kring \u00e4r klar!",
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (startDate != null) {
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = "Startdatum: $startDate",
        style = HedvigTheme.typography.bodySmall,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.weight(1f))
    HedvigButton(
      text = "St\u00e4ng",
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      buttonStyle = Primary,
      buttonSize = Large,
      enabled = true,
      onClick = close,
    )
    Spacer(Modifier.height(16.dp))
  }
}
```

- [ ] **Step 8: Move `PurchaseFailureDestination.kt`**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/ui/failure/PurchaseFailureDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.ui.failure

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.TopAppBarActionType

@Composable
fun PurchaseFailureDestination(onRetry: () -> Unit, close: () -> Unit) {
  HedvigScaffold(
    navigateUp = close,
    topAppBarActionType = TopAppBarActionType.CLOSE,
  ) {
    HedvigErrorSection(
      onButtonClick = onRetry,
      modifier = Modifier.weight(1f),
    )
  }
}
```

- [ ] **Step 9: Create DI module for common**

File: `app/feature/feature-purchase-common/src/main/kotlin/com/hedvig/android/feature/purchase/common/di/PurchaseCommonModule.kt`

```kotlin
package com.hedvig.android.feature.purchase.common.di

import com.hedvig.android.feature.purchase.common.data.AddToCartAndStartSignUseCase
import com.hedvig.android.feature.purchase.common.data.AddToCartAndStartSignUseCaseImpl
import com.hedvig.android.feature.purchase.common.data.PollSigningStatusUseCase
import com.hedvig.android.feature.purchase.common.data.PollSigningStatusUseCaseImpl
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.common.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val purchaseCommonModule = module {
  single<AddToCartAndStartSignUseCase> { AddToCartAndStartSignUseCaseImpl(apolloClient = get()) }
  single<PollSigningStatusUseCase> { PollSigningStatusUseCaseImpl(apolloClient = get()) }

  viewModel<SelectTierViewModel> { params ->
    SelectTierViewModel(params = params.get())
  }
  viewModel<PurchaseSummaryViewModel> { params ->
    PurchaseSummaryViewModel(
      summaryParameters = params.get(),
      addToCartAndStartSignUseCase = get(),
    )
  }
  viewModel<SigningViewModel> { params ->
    SigningViewModel(
      signingParameters = params.get(),
      pollSigningStatusUseCase = get(),
    )
  }
}
```

- [ ] **Step 10: Commit**

```bash
git add app/feature/feature-purchase-common/
git commit -m "feat: add shared screens, use cases, and DI to feature-purchase-common"
```

---

### Task 4: Update `feature-purchase-apartment` to use common module

**Files:**
- Modify: `app/feature/feature-purchase-apartment/build.gradle.kts`
- Modify: `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/navigation/ApartmentPurchaseDestination.kt`
- Modify: `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/navigation/ApartmentPurchaseNavGraph.kt`
- Modify: `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/di/ApartmentPurchaseModule.kt`
- Modify: `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/data/PurchaseApartmentModels.kt`
- Delete: All shared files that moved to common (offer/, summary/, sign/, success/, failure/ directories, AddToCartAndStartSignUseCase, PollSigningStatusUseCase, shared GraphQL files)

- [ ] **Step 1: Add common dependency to build.gradle.kts**

Add `implementation(projects.featurePurchaseCommon)` to dependencies. Remove `libs.zXing` (now in common).

- [ ] **Step 2: Simplify `ApartmentPurchaseDestination.kt`**

Remove all shared types (TierOfferData, SelectTierParameters, SummaryParameters, SigningParameters) — these now come from common. Keep only ApartmentPurchaseGraphDestination and internal apartment-specific destinations (Form only, plus re-exports from common for nav graph use).

New contents:

```kotlin
package com.hedvig.android.feature.purchase.apartment.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class ApartmentPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface ApartmentPurchaseDestination {
  @Serializable
  data object Form : ApartmentPurchaseDestination, Destination
}
```

- [ ] **Step 3: Update `ApartmentPurchaseNavGraph.kt`**

Replace all apartment-specific imports for shared screens/models with imports from `com.hedvig.android.feature.purchase.common.*`. The nav graph now uses `PurchaseCommonDestination.*` for SelectTier, Summary, Signing, Success, Failure destinations, and `TierOfferData`, `SelectTierParameters`, `SummaryParameters`, `SigningParameters` from common navigation.

```kotlin
package com.hedvig.android.feature.purchase.apartment.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormDestination
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormViewModel
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Failure
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.SelectTier
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Signing
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Success
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Summary
import com.hedvig.android.feature.purchase.common.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData
import com.hedvig.android.feature.purchase.common.ui.failure.PurchaseFailureDestination
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierDestination
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.common.ui.sign.SigningDestination
import com.hedvig.android.feature.purchase.common.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.common.ui.success.PurchaseSuccessDestination
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryDestination
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
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
    startDestination = ApartmentPurchaseDestination.Form::class,
  ) {
    navdestination<ApartmentPurchaseDestination.Form> { backStackEntry ->
      val graphRoute = navController
        .getRouteFromBackStack<ApartmentPurchaseGraphDestination>(backStackEntry)
      val viewModel: ApartmentFormViewModel = koinViewModel {
        parametersOf(graphRoute.productName)
      }
      ApartmentFormDestination(
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
        navigateToFailure = dropUnlessResumed { navController.navigate(Failure) },
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
            typedPopUpTo<ApartmentPurchaseGraphDestination>({ inclusive = true })
          }
        },
        navigateToFailure = dropUnlessResumed { navController.navigate(Failure) },
      )
    }

    navdestination<Failure> {
      PurchaseFailureDestination(
        onRetry = dropUnlessResumed { navController.popBackStack() },
        close = dropUnlessResumed {
          if (!navController.typedPopBackStack<ApartmentPurchaseGraphDestination>(inclusive = true)) finishApp()
        },
      )
    }
  }
  // NOTE: Success destination is registered once in HedvigNavHost, not here.
  // Both apartment and car nav graphs navigate to the same PurchaseCommonDestination.Success.
}
```

- [ ] **Step 4: Update `ApartmentPurchaseModule.kt`**

Remove shared use cases and shared ViewModels (they're now in `purchaseCommonModule`). Keep only apartment-specific ones:

```kotlin
package com.hedvig.android.feature.purchase.apartment.di

import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.apartment.data.CreateSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.apartment.data.SubmitFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val apartmentPurchaseModule = module {
  single<CreateSessionAndPriceIntentUseCase> { CreateSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<SubmitFormAndGetOffersUseCase> { SubmitFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<ApartmentFormViewModel> { params ->
    ApartmentFormViewModel(
      productName = params.get(),
      createSessionAndPriceIntentUseCase = get(),
      submitFormAndGetOffersUseCase = get(),
    )
  }
}
```

- [ ] **Step 5: Update `PurchaseApartmentModels.kt`**

Remove `SigningStart`, `SigningPollResult`, `SigningStatus` (now in common). Keep only apartment-specific models:

```kotlin
package com.hedvig.android.feature.purchase.apartment.data

import com.hedvig.android.core.uidata.UiMoney

data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
)

data class ApartmentOffers(
  val productDisplayName: String,
  val offers: List<ApartmentTierOffer>,
)

data class ApartmentTierOffer(
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

- [ ] **Step 6: Delete shared files from apartment module**

Delete these files/directories that moved to common:
- `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/ui/offer/` (entire directory)
- `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/ui/summary/` (entire directory)
- `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/ui/sign/` (entire directory)
- `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/ui/success/` (entire directory)
- `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/ui/failure/` (entire directory)
- `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/data/AddToCartAndStartSignUseCase.kt`
- `app/feature/feature-purchase-apartment/src/main/kotlin/com/hedvig/android/feature/purchase/apartment/data/PollSigningStatusUseCase.kt`
- `app/feature/feature-purchase-apartment/src/main/graphql/ShopSessionCartEntriesAddMutation.graphql`
- `app/feature/feature-purchase-apartment/src/main/graphql/ShopSessionStartSignMutation.graphql`
- `app/feature/feature-purchase-apartment/src/main/graphql/ShopSessionSigningQuery.graphql`
- `app/feature/feature-purchase-apartment/src/main/graphql/ProductOfferFragment.graphql`

- [ ] **Step 7: Verify apartment build compiles**

Run: `./gradlew :app:feature:feature-purchase-apartment:compileDebugKotlin :app:feature:feature-purchase-common:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A app/feature/feature-purchase-apartment/ app/feature/feature-purchase-common/
git commit -m "refactor: migrate apartment purchase to use feature-purchase-common"
```

---

### Task 5: Create `feature-purchase-car` module

**Files:**
- Create: `app/feature/feature-purchase-car/build.gradle.kts`
- Create: `app/feature/feature-purchase-car/src/main/graphql/CarShopSessionCreateMutation.graphql`
- Create: `app/feature/feature-purchase-car/src/main/graphql/CarPriceIntentCreateMutation.graphql`
- Create: `app/feature/feature-purchase-car/src/main/graphql/CarPriceIntentDataUpdateMutation.graphql`
- Create: `app/feature/feature-purchase-car/src/main/graphql/CarPriceIntentConfirmMutation.graphql`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/data/CarPurchaseModels.kt`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/data/CreateCarSessionAndPriceIntentUseCase.kt`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/data/SubmitCarFormAndGetOffersUseCase.kt`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/ui/form/CarFormViewModel.kt`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/ui/form/CarFormDestination.kt`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/navigation/CarPurchaseDestination.kt`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/navigation/CarPurchaseNavGraph.kt`
- Create: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/di/CarPurchaseModule.kt`

- [ ] **Step 1: Create `build.gradle.kts`**

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
  implementation(projects.featurePurchaseCommon)
  implementation(projects.moleculePublic)
  implementation(projects.navigationCommon)
  implementation(projects.navigationCompose)
  implementation(projects.navigationComposeTyped)
  implementation(projects.navigationCore)
}
```

- [ ] **Step 2: Create GraphQL operations**

File: `app/feature/feature-purchase-car/src/main/graphql/CarShopSessionCreateMutation.graphql`
```graphql
mutation CarShopSessionCreate($countryCode: CountryCode!) {
  shopSessionCreate(input: { countryCode: $countryCode }) {
    id
  }
}
```

File: `app/feature/feature-purchase-car/src/main/graphql/CarPriceIntentCreateMutation.graphql`
```graphql
mutation CarPriceIntentCreate($shopSessionId: UUID!, $productName: String!) {
  priceIntentCreate(input: { shopSessionId: $shopSessionId, productName: $productName }) {
    id
  }
}
```

File: `app/feature/feature-purchase-car/src/main/graphql/CarPriceIntentDataUpdateMutation.graphql`
```graphql
mutation CarPriceIntentDataUpdate($priceIntentId: UUID!, $data: PricingFormData!) {
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

File: `app/feature/feature-purchase-car/src/main/graphql/CarPriceIntentConfirmMutation.graphql`
```graphql
mutation CarPriceIntentConfirm($priceIntentId: UUID!) {
  priceIntentConfirm(priceIntentId: $priceIntentId) {
    priceIntent {
      id
      offers {
        ...PurchaseProductOfferFragment
      }
    }
    userError {
      message
    }
  }
}
```

Note: `PurchaseProductOfferFragment` is defined in `feature-purchase-common`. The car module depends on common, so Apollo will find this fragment via the dependency.

- [ ] **Step 3: Create data models**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/data/CarPurchaseModels.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.data

import com.hedvig.android.core.uidata.UiMoney

internal data class SessionAndIntent(
  val shopSessionId: String,
  val priceIntentId: String,
)

internal data class CarOffers(
  val productDisplayName: String,
  val offers: List<CarTierOffer>,
)

internal data class CarTierOffer(
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

- [ ] **Step 4: Create `CreateCarSessionAndPriceIntentUseCase`**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/data/CreateCarSessionAndPriceIntentUseCase.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.CarPriceIntentCreateMutation
import octopus.CarShopSessionCreateMutation
import octopus.type.CountryCode

internal interface CreateCarSessionAndPriceIntentUseCase {
  suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent>
}

internal class CreateCarSessionAndPriceIntentUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateCarSessionAndPriceIntentUseCase {
  override suspend fun invoke(productName: String): Either<ErrorMessage, SessionAndIntent> {
    return either {
      val shopSessionId = apolloClient
        .mutation(CarShopSessionCreateMutation(CountryCode.SE))
        .safeExecute()
        .fold(
          ifLeft = {
            logcat(LogPriority.ERROR) { "Failed to create shop session: $it" }
            raise(ErrorMessage())
          },
          ifRight = { it.shopSessionCreate.id },
        )

      val priceIntentId = apolloClient
        .mutation(CarPriceIntentCreateMutation(shopSessionId = shopSessionId, productName = productName))
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

- [ ] **Step 5: Create `SubmitCarFormAndGetOffersUseCase`**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/data/SubmitCarFormAndGetOffersUseCase.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.CarPriceIntentConfirmMutation
import octopus.CarPriceIntentDataUpdateMutation
import octopus.fragment.PurchaseProductOfferFragment

internal interface SubmitCarFormAndGetOffersUseCase {
  suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    registrationNumber: String,
    mileage: Int,
    street: String,
    zipCode: String,
    email: String,
  ): Either<ErrorMessage, CarOffers>
}

internal class SubmitCarFormAndGetOffersUseCaseImpl(
  private val apolloClient: ApolloClient,
) : SubmitCarFormAndGetOffersUseCase {
  override suspend fun invoke(
    priceIntentId: String,
    ssn: String,
    registrationNumber: String,
    mileage: Int,
    street: String,
    zipCode: String,
    email: String,
  ): Either<ErrorMessage, CarOffers> {
    return either {
      val formData = buildMap {
        put("ssn", ssn)
        put("registrationNumber", registrationNumber)
        put("mileage", mileage)
        put("street", street)
        put("zipCode", zipCode)
        put("email", email)
      }

      val updateResult = apolloClient
        .mutation(CarPriceIntentDataUpdateMutation(priceIntentId = priceIntentId, data = formData))
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
        .mutation(CarPriceIntentConfirmMutation(priceIntentId = priceIntentId))
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

      CarOffers(
        productDisplayName = offers.first().variant.displayName,
        offers = offers.map { it.toTierOffer() },
      )
    }
  }
}

private fun PurchaseProductOfferFragment.toTierOffer(): CarTierOffer {
  return CarTierOffer(
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

- [ ] **Step 6: Create `CarFormViewModel`**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/ui/form/CarFormViewModel.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.purchase.car.data.CarOffers
import com.hedvig.android.feature.purchase.car.data.CreateCarSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.car.data.SessionAndIntent
import com.hedvig.android.feature.purchase.car.data.SubmitCarFormAndGetOffersUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class CarFormViewModel(
  productName: String,
  createCarSessionAndPriceIntentUseCase: CreateCarSessionAndPriceIntentUseCase,
  submitCarFormAndGetOffersUseCase: SubmitCarFormAndGetOffersUseCase,
) : MoleculeViewModel<CarFormEvent, CarFormState>(
  initialState = CarFormState(),
  presenter = CarFormPresenter(productName, createCarSessionAndPriceIntentUseCase, submitCarFormAndGetOffersUseCase),
)

internal sealed interface CarFormEvent {
  data class SubmitForm(
    val ssn: String,
    val registrationNumber: String,
    val mileage: Int?,
    val street: String,
    val zipCode: String,
    val email: String,
  ) : CarFormEvent

  data object ClearNavigation : CarFormEvent
  data object Retry : CarFormEvent
}

internal data class CarFormState(
  val ssnError: String? = null,
  val registrationNumberError: String? = null,
  val mileageError: String? = null,
  val streetError: String? = null,
  val zipCodeError: String? = null,
  val emailError: String? = null,
  val isSubmitting: Boolean = false,
  val isLoadingSession: Boolean = true,
  val loadSessionError: Boolean = false,
  val submitError: String? = null,
  val offersToNavigate: CarOffersNavigationData? = null,
)

internal data class CarOffersNavigationData(
  val shopSessionId: String,
  val offers: CarOffers,
)

private class CarFormPresenter(
  private val productName: String,
  private val createCarSessionAndPriceIntentUseCase: CreateCarSessionAndPriceIntentUseCase,
  private val submitCarFormAndGetOffersUseCase: SubmitCarFormAndGetOffersUseCase,
) : MoleculePresenter<CarFormEvent, CarFormState> {
  @Composable
  override fun MoleculePresenterScope<CarFormEvent>.present(lastState: CarFormState): CarFormState {
    var currentState by remember { mutableStateOf(lastState) }
    var sessionAndIntent: SessionAndIntent? by remember { mutableStateOf(null) }
    var sessionLoadIteration by remember { mutableIntStateOf(0) }
    var submitIteration by remember { mutableIntStateOf(0) }
    var pendingSubmit: CarFormEvent.SubmitForm? by remember { mutableStateOf(null) }

    CollectEvents { event ->
      when (event) {
        is CarFormEvent.SubmitForm -> {
          val errors = validate(event)
          if (errors.hasErrors()) {
            currentState = currentState.copy(
              ssnError = errors.ssnError,
              registrationNumberError = errors.registrationNumberError,
              mileageError = errors.mileageError,
              streetError = errors.streetError,
              zipCodeError = errors.zipCodeError,
              emailError = errors.emailError,
            )
          } else {
            currentState = currentState.copy(
              ssnError = null,
              registrationNumberError = null,
              mileageError = null,
              streetError = null,
              zipCodeError = null,
              emailError = null,
            )
            pendingSubmit = event
            submitIteration++
          }
        }

        CarFormEvent.ClearNavigation -> {
          currentState = currentState.copy(offersToNavigate = null)
        }

        CarFormEvent.Retry -> {
          if (sessionAndIntent == null) {
            currentState = currentState.copy(loadSessionError = false, isLoadingSession = true)
            sessionLoadIteration++
          } else {
            currentState = currentState.copy(submitError = null)
          }
        }
      }
    }

    LaunchedEffect(sessionLoadIteration) {
      currentState = currentState.copy(isLoadingSession = true, loadSessionError = false)
      createCarSessionAndPriceIntentUseCase.invoke(productName).fold(
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
      pendingSubmit = null
      currentState = currentState.copy(isSubmitting = true, submitError = null)
      submitCarFormAndGetOffersUseCase.invoke(
        priceIntentId = session.priceIntentId,
        ssn = submit.ssn,
        registrationNumber = submit.registrationNumber.replace(" ", ""),
        mileage = submit.mileage!!,
        street = submit.street,
        zipCode = submit.zipCode,
        email = submit.email,
      ).fold(
        ifLeft = { error ->
          currentState = currentState.copy(
            isSubmitting = false,
            submitError = error.message ?: "N\u00e5got gick fel",
          )
        },
        ifRight = { offers ->
          currentState = currentState.copy(
            isSubmitting = false,
            offersToNavigate = CarOffersNavigationData(
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
  val ssnError: String?,
  val registrationNumberError: String?,
  val mileageError: String?,
  val streetError: String?,
  val zipCodeError: String?,
  val emailError: String?,
) {
  fun hasErrors(): Boolean = ssnError != null || registrationNumberError != null ||
    mileageError != null || streetError != null || zipCodeError != null || emailError != null
}

private val SSN_REGEX = Regex("^\\d{12}$")
private val REG_NUMBER_REGEX = Regex("^[A-Za-z]{3}\\s?\\d{2}[A-Za-z0-9]$")
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

private fun validate(form: CarFormEvent.SubmitForm): ValidationErrors {
  return ValidationErrors(
    ssnError = when {
      form.ssn.isBlank() -> "Ange personnummer"
      !SSN_REGEX.matches(form.ssn) -> "Ange ett giltigt personnummer (12 siffror)"
      else -> null
    },
    registrationNumberError = when {
      form.registrationNumber.isBlank() -> "Ange registreringsnummer"
      !REG_NUMBER_REGEX.matches(form.registrationNumber.replace(" ", "").let {
        if (it.length >= 3) it.substring(0, 3) + " " + it.substring(3) else it
      }) -> "Ange ett giltigt registreringsnummer (t.ex. ABC 123)"
      else -> null
    },
    mileageError = if (form.mileage == null) "V\u00e4lj miltal" else null,
    streetError = if (form.street.isBlank()) "Ange en adress" else null,
    zipCodeError = when {
      form.zipCode.length != 5 -> "Ange ett giltigt postnummer (5 siffror)"
      !form.zipCode.all { it.isDigit() } -> "Postnumret f\u00e5r bara inneh\u00e5lla siffror"
      else -> null
    },
    emailError = when {
      form.email.isBlank() -> "Ange e-postadress"
      !EMAIL_REGEX.matches(form.email) -> "Ange en giltig e-postadress"
      else -> null
    },
  )
}
```

- [ ] **Step 7: Create `CarFormDestination`**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/ui/form/CarFormDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.ui.form

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDropdownField
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.purchase.car.data.CarOffers

@Composable
internal fun CarFormDestination(
  viewModel: CarFormViewModel,
  navigateUp: () -> Unit,
  onOffersReceived: (shopSessionId: String, offers: CarOffers) -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
  val offersData = uiState.offersToNavigate
  if (offersData != null) {
    LaunchedEffect(offersData) {
      viewModel.emit(CarFormEvent.ClearNavigation)
      onOffersReceived(offersData.shopSessionId, offersData.offers)
    }
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "Bilf\u00f6rs\u00e4kring",
  ) {
    when {
      uiState.isLoadingSession -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      uiState.loadSessionError -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(CarFormEvent.Retry) },
        )
      }

      else -> {
        var ssn by remember { mutableStateOf("") }
        var registrationNumber by remember { mutableStateOf("") }
        var selectedMileage: MileageOption? by remember { mutableStateOf(null) }
        var street by remember { mutableStateOf("") }
        var zipCode by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        CarFormContent(
          ssn = ssn,
          registrationNumber = registrationNumber,
          selectedMileage = selectedMileage,
          street = street,
          zipCode = zipCode,
          email = email,
          ssnError = uiState.ssnError,
          registrationNumberError = uiState.registrationNumberError,
          mileageError = uiState.mileageError,
          streetError = uiState.streetError,
          zipCodeError = uiState.zipCodeError,
          emailError = uiState.emailError,
          isSubmitting = uiState.isSubmitting,
          onSsnChanged = { value -> if (value.all { it.isDigit() } && value.length <= 12) ssn = value },
          onRegistrationNumberChanged = { value ->
            val cleaned = value.uppercase().filter { it.isLetterOrDigit() }
            registrationNumber = if (cleaned.length > 3) {
              cleaned.substring(0, 3) + " " + cleaned.substring(3, minOf(cleaned.length, 6))
            } else {
              cleaned
            }
          },
          onMileageSelected = { selectedMileage = it },
          onStreetChanged = { street = it },
          onZipCodeChanged = { value -> if (value.all { it.isDigit() }) zipCode = value },
          onEmailChanged = { email = it },
          onSubmit = {
            viewModel.emit(
              CarFormEvent.SubmitForm(
                ssn = ssn,
                registrationNumber = registrationNumber,
                mileage = selectedMileage?.value,
                street = street,
                zipCode = zipCode,
                email = email,
              ),
            )
          },
          onRetry = { viewModel.emit(CarFormEvent.Retry) },
        )
      }
    }
  }
}

internal enum class MileageOption(val value: Int, val label: String) {
  M_1000(1000, "0 - 1 000 mil"),
  M_1500(1500, "1 000 - 1 500 mil"),
  M_2000(2000, "1 500 - 2 000 mil"),
  M_2500(2500, "2 000 - 2 500 mil"),
  M_2501(2501, "2 500+ mil"),
}

@Composable
private fun CarFormContent(
  ssn: String,
  registrationNumber: String,
  selectedMileage: MileageOption?,
  street: String,
  zipCode: String,
  email: String,
  ssnError: String?,
  registrationNumberError: String?,
  mileageError: String?,
  streetError: String?,
  zipCodeError: String?,
  emailError: String?,
  isSubmitting: Boolean,
  onSsnChanged: (String) -> Unit,
  onRegistrationNumberChanged: (String) -> Unit,
  onMileageSelected: (MileageOption) -> Unit,
  onStreetChanged: (String) -> Unit,
  onZipCodeChanged: (String) -> Unit,
  onEmailChanged: (String) -> Unit,
  onSubmit: () -> Unit,
  onRetry: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = "Fyll i dina uppgifter s\u00e5 ber\u00e4knar vi ditt pris",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(16.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      HedvigTextField(
        text = ssn,
        onValueChange = onSsnChanged,
        labelText = "Personnummer",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = ssnError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next,
        ),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = registrationNumber,
        onValueChange = onRegistrationNumberChanged,
        labelText = "Registreringsnummer",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = registrationNumberError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
      var mileageExpanded by remember { mutableStateOf(false) }
      HedvigDropdownField(
        text = selectedMileage?.label ?: "",
        labelText = "Miltal per \u00e5r",
        onItemChosen = { index ->
          onMileageSelected(MileageOption.entries[index])
          mileageExpanded = false
        },
        items = MileageOption.entries.map { it.label },
        expanded = mileageExpanded,
        onExpandedChange = { mileageExpanded = it },
        errorState = mileageError.toErrorState(),
        enabled = !isSubmitting,
      )
      HedvigTextField(
        text = street,
        onValueChange = onStreetChanged,
        labelText = "Adress",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = streetError.toErrorState(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        enabled = !isSubmitting,
      )
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
      HedvigTextField(
        text = email,
        onValueChange = onEmailChanged,
        labelText = "E-post",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        errorState = emailError.toErrorState(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Email,
          imeAction = ImeAction.Done,
        ),
        enabled = !isSubmitting,
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Ber\u00e4kna pris",
      onClick = onSubmit,
      enabled = !isSubmitting,
      isLoading = isSubmitting,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

private fun String?.toErrorState(): HedvigTextFieldDefaults.ErrorState {
  return if (this != null) {
    HedvigTextFieldDefaults.ErrorState.Error.WithMessage(this)
  } else {
    HedvigTextFieldDefaults.ErrorState.NoError
  }
}
```

**Note:** `HedvigDropdownField` may not exist in the design system. If it doesn't, we'll need to check what dropdown component is available. The implementation agent should verify this and use whatever dropdown/exposed-dropdown-menu component the design system provides. If none exists, use a simple clickable text field that opens a bottom sheet or menu with the mileage options.

- [ ] **Step 8: Create navigation destinations**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/navigation/CarPurchaseDestination.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class CarPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface CarPurchaseDestination {
  @Serializable
  data object Form : CarPurchaseDestination, Destination
}
```

- [ ] **Step 9: Create navigation graph**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/navigation/CarPurchaseNavGraph.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.purchase.car.ui.form.CarFormDestination
import com.hedvig.android.feature.purchase.car.ui.form.CarFormViewModel
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Failure
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.SelectTier
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Signing
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Success
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination.Summary
import com.hedvig.android.feature.purchase.common.navigation.SelectTierParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData
import com.hedvig.android.feature.purchase.common.ui.failure.PurchaseFailureDestination
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierDestination
import com.hedvig.android.feature.purchase.common.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.common.ui.sign.SigningDestination
import com.hedvig.android.feature.purchase.common.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.common.ui.success.PurchaseSuccessDestination
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryDestination
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.carPurchaseNavGraph(
  navController: NavController,
  popBackStack: () -> Unit,
  finishApp: () -> Unit,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) {
  navgraph<CarPurchaseGraphDestination>(
    startDestination = CarPurchaseDestination.Form::class,
  ) {
    navdestination<CarPurchaseDestination.Form> { backStackEntry ->
      val graphRoute = navController
        .getRouteFromBackStack<CarPurchaseGraphDestination>(backStackEntry)
      val viewModel: CarFormViewModel = koinViewModel {
        parametersOf(graphRoute.productName)
      }
      CarFormDestination(
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
        navigateToFailure = dropUnlessResumed { navController.navigate(Failure) },
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
            typedPopUpTo<CarPurchaseGraphDestination>({ inclusive = true })
          }
        },
        navigateToFailure = dropUnlessResumed { navController.navigate(Failure) },
      )
    }

    navdestination<Failure> {
      PurchaseFailureDestination(
        onRetry = dropUnlessResumed { navController.popBackStack() },
        close = dropUnlessResumed {
          if (!navController.typedPopBackStack<CarPurchaseGraphDestination>(inclusive = true)) finishApp()
        },
      )
    }
  }
  // NOTE: Success destination is registered once in HedvigNavHost, not here.
  // Both apartment and car nav graphs navigate to the same PurchaseCommonDestination.Success.
}
```

- [ ] **Step 10: Create DI module**

File: `app/feature/feature-purchase-car/src/main/kotlin/com/hedvig/android/feature/purchase/car/di/CarPurchaseModule.kt`

```kotlin
package com.hedvig.android.feature.purchase.car.di

import com.hedvig.android.feature.purchase.car.data.CreateCarSessionAndPriceIntentUseCase
import com.hedvig.android.feature.purchase.car.data.CreateCarSessionAndPriceIntentUseCaseImpl
import com.hedvig.android.feature.purchase.car.data.SubmitCarFormAndGetOffersUseCase
import com.hedvig.android.feature.purchase.car.data.SubmitCarFormAndGetOffersUseCaseImpl
import com.hedvig.android.feature.purchase.car.ui.form.CarFormViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val carPurchaseModule = module {
  single<CreateCarSessionAndPriceIntentUseCase> { CreateCarSessionAndPriceIntentUseCaseImpl(apolloClient = get()) }
  single<SubmitCarFormAndGetOffersUseCase> { SubmitCarFormAndGetOffersUseCaseImpl(apolloClient = get()) }

  viewModel<CarFormViewModel> { params ->
    CarFormViewModel(
      productName = params.get(),
      createCarSessionAndPriceIntentUseCase = get(),
      submitCarFormAndGetOffersUseCase = get(),
    )
  }
}
```

- [ ] **Step 11: Commit**

```bash
git add app/feature/feature-purchase-car/
git commit -m "feat: add feature-purchase-car module with form, use cases, and navigation"
```

---

### Task 6: Wire car purchase into app navigation and DI

**Files:**
- Modify: `app/app/build.gradle.kts`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`
- Modify: `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt`
- Modify: `app/feature/feature-insurances/build.gradle.kts` (if needed for nav dependency)

- [ ] **Step 1: Add dependencies to app build.gradle.kts**

Add to `app/app/build.gradle.kts` dependencies:
```kotlin
implementation(projects.featurePurchaseCommon)
implementation(projects.featurePurchaseCar)
```

- [ ] **Step 2: Register modules in ApplicationModule**

In `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt`, add imports and include the new modules:

Add import:
```kotlin
import com.hedvig.android.feature.purchase.common.di.purchaseCommonModule
import com.hedvig.android.feature.purchase.car.di.carPurchaseModule
```

Add to the `includes(...)` block (near `apartmentPurchaseModule`):
```kotlin
purchaseCommonModule,
carPurchaseModule,
```

- [ ] **Step 3: Add car purchase nav graph to HedvigNavHost**

In `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`:

Add imports:
```kotlin
import com.hedvig.android.feature.purchase.car.navigation.CarPurchaseGraphDestination
import com.hedvig.android.feature.purchase.car.navigation.carPurchaseNavGraph
```

Add the nav graph call after `apartmentPurchaseNavGraph(...)`:
```kotlin
carPurchaseNavGraph(
  navController = navController,
  popBackStack = popBackStackOrFinish,
  finishApp = finishApp,
  crossSellAfterFlowRepository = crossSellAfterFlowRepository,
)
```

Also add the shared `PurchaseCommonDestination.Success` destination registration (once, shared by both purchase flows):
```kotlin
import com.hedvig.android.feature.purchase.common.navigation.PurchaseCommonDestination
import com.hedvig.android.feature.purchase.common.ui.success.PurchaseSuccessDestination

// In the NavHost builder, after both purchase nav graphs:
navdestination<PurchaseCommonDestination.Success> { backStackEntry ->
  val route = backStackEntry.toRoute<PurchaseCommonDestination.Success>()
  PurchaseSuccessDestination(
    startDate = route.startDate,
    close = dropUnlessResumed {
      if (!navController.popBackStack()) finishApp()
    },
  )
}
```

Add car purchase navigation callback in the insurances graph setup. Find `onNavigateToApartmentPurchase` and add a new parameter:
```kotlin
onNavigateToCarPurchase = { productName ->
  navController.navigate(CarPurchaseGraphDestination(productName))
},
```

- [ ] **Step 4: Update InsuranceGraph to route car cross-sells**

In `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsuranceGraph.kt`:

Add `onNavigateToCarPurchase: (productName: String) -> Unit` parameter to `insuranceGraph()`.

Update the `onCrossSellClick` callback (currently hardcoded at line 64-67):

```kotlin
onCrossSellClick = dropUnlessResumed { url: String ->
  // TODO: Extract product name from cross-sell data and route accordingly
  // For now, route apartment cross-sells to apartment and car cross-sells to car
  onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
},
```

Note: The proper product routing (determining if a cross-sell is for apartment vs car based on the URL or cross-sell metadata) depends on how cross-sell data is structured. The implementing agent should check what data `onCrossSellClick` receives and route to the correct flow. For now, the car flow is wired up and can be tested directly via `CarPurchaseGraphDestination("SE_CAR")`.

- [ ] **Step 5: Verify full build compiles**

Run: `./gradlew :app:app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Run ktlint formatting**

Run: `./gradlew ktlintFormat`

- [ ] **Step 7: Commit**

```bash
git add app/app/ app/feature/feature-insurances/
git commit -m "feat: wire car purchase flow into app navigation and DI"
```

---

### Task 7: Verify everything works end-to-end

- [ ] **Step 1: Run full project build**

Run: `./gradlew :app:app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run tests for affected modules**

Run: `./gradlew :app:feature:feature-purchase-common:test :app:feature:feature-purchase-apartment:test :app:feature:feature-purchase-car:test`
Expected: All tests pass (or no tests to run for new modules)

- [ ] **Step 3: Run ktlint check**

Run: `./gradlew ktlintCheck`
Expected: No violations

- [ ] **Step 4: Commit any fixes**

If any build or lint issues found, fix and commit.

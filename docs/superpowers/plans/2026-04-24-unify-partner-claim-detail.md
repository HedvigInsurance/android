# Unify Partner Claim Detail Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the `feature-partner-claim-details` module and reuse the existing `feature-claim-details` destination for both regular and partner claims.

**Architecture:** Add `isPartnerClaim` flag to navigation destination. Branch in use case to call either `ClaimQuery` or `PartnerClaimDetailQuery`, mapping both into `ClaimDetailUiState.Content`. Remove all partner-specific navigation wiring and delete the partner module.

**Tech Stack:** Kotlin, Jetpack Compose Navigation, Apollo GraphQL, Koin DI, Molecule

---

### Task 1: Add `isPartnerClaim` to navigation destination and wire through ViewModel/DI

**Files:**
- Modify: `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinations.kt`
- Modify: `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinationGraph.kt`
- Modify: `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/di/FeatureClaimDetailsModule.kt`
- Modify: `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/ui/ClaimDetailsViewModel.kt`

- [ ] **Step 1: Add `isPartnerClaim` to `ClaimOverviewDestination`**

In `ClaimDetailDestinations.kt`, add the parameter:

```kotlin
sealed interface ClaimDetailDestination {
  @Serializable
  data class ClaimOverviewDestination(
    @SerialName("claimId")
    val claimId: String,
    val isPartnerClaim: Boolean = false,
  ) : ClaimDetailDestination, Destination
}
```

- [ ] **Step 2: Pass `isPartnerClaim` from nav destination to ViewModel**

In `ClaimDetailDestinationGraph.kt`, update the ViewModel instantiation at line 34 to pass `isPartnerClaim`:

```kotlin
navdestination<ClaimDetailDestination.ClaimOverviewDestination>(
  deepLinks = navDeepLinks(hedvigDeepLinkContainer.claimDetails),
) {
  val viewModel: ClaimDetailsViewModel = koinViewModel { parametersOf(claimId, isPartnerClaim) }
```

- [ ] **Step 3: Update ViewModel and Presenter to accept `isPartnerClaim`**

In `ClaimDetailsViewModel.kt`, update the ViewModel class to accept the new parameter and forward it to the presenter:

```kotlin
internal class ClaimDetailsViewModel(
  claimId: String,
  isPartnerClaim: Boolean,
  getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  claimsServiceUploadFileUseCase: ClaimsServiceUploadFileUseCase,
  downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculeViewModel<ClaimDetailsEvent, ClaimDetailUiState>(
    ClaimDetailUiState.Loading,
    ClaimDetailPresenter(claimId, isPartnerClaim, getClaimDetailUiStateUseCase, claimsServiceUploadFileUseCase, downloadPdfUseCase),
  )
```

Update `ClaimDetailPresenter`:

```kotlin
private class ClaimDetailPresenter(
  private val claimId: String,
  private val isPartnerClaim: Boolean,
  private val getClaimDetailUiStateUseCase: GetClaimDetailUiStateUseCase,
  private val claimsServiceUploadFileUseCase: ClaimsServiceUploadFileUseCase,
  private val downloadPdfUseCase: DownloadPdfUseCase,
) : MoleculePresenter<ClaimDetailsEvent, ClaimDetailUiState> {
```

In the `present()` function, pass `isPartnerClaim` to the use case invocation at line 63:

```kotlin
getClaimDetailUiStateUseCase.invoke(claimId, isPartnerClaim).collect { result ->
```

- [ ] **Step 4: Update Koin module to pass `isPartnerClaim`**

In `FeatureClaimDetailsModule.kt`, update the ViewModel factory:

```kotlin
viewModel<ClaimDetailsViewModel> { (claimId: String, isPartnerClaim: Boolean) ->
  ClaimDetailsViewModel(
    claimId = claimId,
    isPartnerClaim = isPartnerClaim,
    getClaimDetailUiStateUseCase = get<GetClaimDetailUiStateUseCase>(),
    claimsServiceUploadFileUseCase = get<ClaimsServiceUploadFileUseCase>(),
    downloadPdfUseCase = get<DownloadPdfUseCase>(),
  )
}
```

- [ ] **Step 5: Commit**

```bash
git add app/feature/feature-claim-details/
git commit -m "feat: add isPartnerClaim flag to claim detail destination and wire through ViewModel"
```

---

### Task 2: Add partner claim query support to `GetClaimDetailUiStateUseCase`

**Files:**
- Modify: `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/data/GetClaimDetailUiStateUseCase.kt`

- [ ] **Step 1: Add `isPartnerClaim` parameter to `invoke()` and add partner claim mapping**

Update `GetClaimDetailUiStateUseCase` to accept `isPartnerClaim` and branch on it. Add a new `partnerQueryFlow` method and a `fromPartnerClaim` mapping function.

The full updated file:

```kotlin
package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.audio.player.data.SignedAudioUrl
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import octopus.ClaimQuery
import octopus.PartnerClaimDetailQuery
import octopus.fragment.ClaimFragment
import octopus.fragment.PartnerClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus
import octopus.type.InsuranceDocumentType

internal class GetClaimDetailUiStateUseCase(
  private val apolloClient: ApolloClient,
  private val crossSellAfterClaimClosedRepository: CrossSellAfterClaimClosedRepository,
) {
  fun invoke(claimId: String, isPartnerClaim: Boolean = false): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        if (isPartnerClaim) {
          emitAll(partnerQueryFlow(claimId))
        } else {
          emitAll(queryFlow(claimId))
        }
        delay(POLL_INTERVAL)
      }
    }
  }

  private fun queryFlow(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return apolloClient
      .query(ClaimQuery(claimId))
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow { Error.NetworkError }
      .map { response ->
        either {
          val claim = response.bind().claim
          ensureNotNull(claim) { Error.NoClaimFound }
          if (claim.showClaimClosedFlow) {
            crossSellAfterClaimClosedRepository.acknowledgeClaimClosedStatus(claim)
          }
          ClaimDetailUiState.Content.fromClaim(claim, claim.conversation?.id, claim.conversation?.unreadMessageCount)
        }
      }
  }

  private fun partnerQueryFlow(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return apolloClient
      .query(PartnerClaimDetailQuery(claimId))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow { Error.NetworkError }
      .map { response ->
        either {
          val claim = response.bind().partnerClaim
          ensureNotNull(claim) { Error.NoClaimFound }
          fromPartnerClaim(claim)
        }
      }
  }

  private fun fromPartnerClaim(claim: PartnerClaimFragment): ClaimDetailUiState.Content {
    val termsConditionsUrl = claim.productVariant?.documents
      ?.firstOrNull { it.type == InsuranceDocumentType.TERMS_AND_CONDITIONS }?.url
    val submittedAt = claim.submittedAt
      ?.atStartOfDayIn(TimeZone.UTC)
      ?.toLocalDateTime(TimeZone.UTC)
      ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return ClaimDetailUiState.Content(
      claimId = claim.id,
      conversationId = null,
      hasUnreadMessages = false,
      submittedContent = null,
      files = emptyList(),
      claimStatusCardUiState = ClaimStatusCardUiState.fromPartnerClaim(claim),
      claimStatus = when (claim.status) {
        ClaimStatus.CREATED -> ClaimDetailUiState.Content.ClaimStatus.CREATED
        ClaimStatus.IN_PROGRESS -> ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS
        ClaimStatus.CLOSED -> ClaimDetailUiState.Content.ClaimStatus.CLOSED
        ClaimStatus.REOPENED -> ClaimDetailUiState.Content.ClaimStatus.REOPENED
        ClaimStatus.UNKNOWN__, null -> ClaimDetailUiState.Content.ClaimStatus.UNKNOWN
      },
      claimOutcome = ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN,
      uploadUri = "",
      isUploadingFile = false,
      uploadError = null,
      claimType = claim.claimType,
      insuranceDisplayName = claim.exposureDisplayName ?: claim.productVariant?.displayName,
      submittedAt = submittedAt,
      termsConditionsUrl = termsConditionsUrl,
      savedFileUri = null,
      downloadError = null,
      isLoadingPdf = null,
      appealInstructionsUrl = null,
      isUploadingFilesEnabled = false,
      infoText = null,
      displayItems = claim.displayItems.map {
        DisplayItem.fromStrings(it.displayTitle, it.displayValue)
      },
    )
  }

  private fun ClaimDetailUiState.Content.Companion.fromClaim(
    claim: ClaimFragment,
    conversationId: String?,
    conversationUnreadMessageCount: Int?,
  ): ClaimDetailUiState.Content {
    val audioUrl = claim.audioUrl
    val memberFreeText = claim.memberFreeText

    val claimType: String? = claim.claimType
    val submittedAt = claim.submittedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val insuranceDisplayName = claim.productVariant?.displayName
    val termsConditionsUrl =
      claim.productVariant
        ?.documents
        ?.firstOrNull { it.type == InsuranceDocumentType.TERMS_AND_CONDITIONS }
        ?.url

    return ClaimDetailUiState.Content(
      claimId = claim.id,
      conversationId = conversationId,
      hasUnreadMessages = (conversationUnreadMessageCount ?: 0) > 0,
      submittedContent = when {
        audioUrl != null -> {
          ClaimDetailUiState.Content.SubmittedContent.Audio(SignedAudioUrl.fromSignedAudioUrlString(audioUrl))
        }

        memberFreeText != null -> {
          ClaimDetailUiState.Content.SubmittedContent.FreeText(memberFreeText)
        }

        else -> {
          null
        }
      },
      files = claim.files.map {
        UiFile(
          id = it.id,
          name = it.name,
          mimeType = it.mimeType,
          url = it.url,
          localPath = null,
        )
      },
      claimStatusCardUiState = ClaimStatusCardUiState.fromClaimStatusCardsQuery(claim),
      claimStatus = when (claim.status) {
        ClaimStatus.CREATED -> ClaimDetailUiState.Content.ClaimStatus.CREATED
        ClaimStatus.IN_PROGRESS -> ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS
        ClaimStatus.CLOSED -> ClaimDetailUiState.Content.ClaimStatus.CLOSED
        ClaimStatus.REOPENED -> ClaimDetailUiState.Content.ClaimStatus.REOPENED
        ClaimStatus.UNKNOWN__, null -> ClaimDetailUiState.Content.ClaimStatus.UNKNOWN
      },
      claimOutcome = when (claim.outcome) {
        ClaimOutcome.PAID -> ClaimDetailUiState.Content.ClaimOutcome.PAID
        ClaimOutcome.NOT_COMPENSATED -> ClaimDetailUiState.Content.ClaimOutcome.NOT_COMPENSATED
        ClaimOutcome.NOT_COVERED -> ClaimDetailUiState.Content.ClaimOutcome.NOT_COVERED
        ClaimOutcome.UNKNOWN__, null -> ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN
        ClaimOutcome.UNRESPONSIVE -> ClaimDetailUiState.Content.ClaimOutcome.UNRESPONSIVE
      },
      uploadUri = claim.targetFileUploadUri,
      isUploadingFile = false,
      uploadError = null,
      claimType = claimType,
      insuranceDisplayName = insuranceDisplayName,
      submittedAt = submittedAt,
      termsConditionsUrl = termsConditionsUrl,
      savedFileUri = null,
      downloadError = null,
      isLoadingPdf = null,
      appealInstructionsUrl = claim.appealInstructionsUrl,
      isUploadingFilesEnabled = claim.isUploadingFilesEnabled,
      infoText = claim.infoText,
      displayItems = claim.displayItems.map {
        DisplayItem.fromStrings(it.displayTitle, it.displayValue)
      },
    )
  }

  companion object {
    private val POLL_INTERVAL = 10.seconds
  }
}

sealed interface Error {
  data object NetworkError : Error
  data object NoClaimFound : Error
}
```

- [ ] **Step 2: Commit**

```bash
git add app/feature/feature-claim-details/
git commit -m "feat: add partner claim query support to GetClaimDetailUiStateUseCase"
```

---

### Task 3: Guard the "uploaded files" section header in the UI

**Files:**
- Modify: `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/ui/ClaimDetailsDestination.kt`

- [ ] **Step 1: Wrap the "uploaded files" header and submitted content in a conditional**

In `ClaimDetailsDestination.kt`, in the `BeforeGridContent` composable, the "uploaded files" section header at lines 475-496 is always shown. Wrap lines 474-496 in a condition:

Find this block (starting after the `ClaimDisplayItemsSection`):

```kotlin
  Spacer(Modifier.height(24.dp))
  HedvigText(
    stringResource(Res.string.claim_status_detail_uploaded_files_info_title),
    Modifier.padding(horizontal = 2.dp),
  )
  Spacer(Modifier.height(8.dp))
  when (uiState.submittedContent) {
    is ClaimDetailUiState.Content.SubmittedContent.Audio -> {
      ClaimDetailHedvigAudioPlayerItem(uiState.submittedContent.signedAudioURL)
    }

    is ClaimDetailUiState.Content.SubmittedContent.FreeText -> {
      HedvigCard(Modifier.fillMaxWidth()) {
        HedvigText(
          uiState.submittedContent.text,
          Modifier.padding(16.dp),
        )
      }
    }

    else -> {}
  }
  Spacer(Modifier.height(8.dp))
```

Replace with:

```kotlin
  if (uiState.submittedContent != null || uiState.files.isNotEmpty()) {
    Spacer(Modifier.height(24.dp))
    HedvigText(
      stringResource(Res.string.claim_status_detail_uploaded_files_info_title),
      Modifier.padding(horizontal = 2.dp),
    )
    Spacer(Modifier.height(8.dp))
    when (uiState.submittedContent) {
      is ClaimDetailUiState.Content.SubmittedContent.Audio -> {
        ClaimDetailHedvigAudioPlayerItem(uiState.submittedContent.signedAudioURL)
      }

      is ClaimDetailUiState.Content.SubmittedContent.FreeText -> {
        HedvigCard(Modifier.fillMaxWidth()) {
          HedvigText(
            uiState.submittedContent.text,
            Modifier.padding(16.dp),
          )
        }
      }

      else -> {}
    }
    Spacer(Modifier.height(8.dp))
  }
```

- [ ] **Step 2: Commit**

```bash
git add app/feature/feature-claim-details/
git commit -m "fix: hide uploaded files header when no submitted content or files"
```

---

### Task 4: Update navigation callers to pass `isPartnerClaim` flag

**Files:**
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeGraph.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`
- Modify: `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt`
- Modify: `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/nav/ClaimHistoryDestination.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`

- [ ] **Step 1: Update `HomeGraph.kt` — remove `navigateToPartnerClaimDetails`, change `navigateToClaimDetails` signature**

Replace the function signature to change `navigateToClaimDetails` to accept both `claimId` and `isPartnerClaim`, and remove `navigateToPartnerClaimDetails`:

```kotlin
fun NavGraphBuilder.homeGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navController: NavController,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToClaimDetails: (claimId: String, isPartnerClaim: Boolean) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToHelpCenter: () -> Unit,
  navigateToClaimChat: () -> Unit,
  navigateToClaimChatInDevMode: () -> Unit,
  navigateToChipIdScreen: () -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  imageLoader: ImageLoader,
) {
```

Inside the function body, update the `HomeDestination` call. Replace lines 54-59:

```kotlin
      onClaimDetailCardClicked = dropUnlessResumed { claimId: String ->
        navigateToClaimDetails(claimId)
      },
      onPartnerClaimDetailCardClicked = dropUnlessResumed { claimId: String ->
        navigateToPartnerClaimDetails(claimId)
      },
```

With:

```kotlin
      onClaimDetailCardClicked = dropUnlessResumed { claimId: String, isPartnerClaim: Boolean ->
        navigateToClaimDetails(claimId, isPartnerClaim)
      },
```

- [ ] **Step 2: Update `HomeDestination.kt` — merge two callbacks into one**

In `HomeDestination.kt`, throughout all the composable function signatures, replace:

```kotlin
onClaimDetailCardClicked: (String) -> Unit,
onPartnerClaimDetailCardClicked: (String) -> Unit,
```

With:

```kotlin
onClaimDetailCardClicked: (claimId: String, isPartnerClaim: Boolean) -> Unit,
```

This appears in `HomeDestination` (line 159-160), `HomeScreen` (line 211-212), and `HomeScreenSuccess` (line 428-429).

In `HomeScreenSuccess`, update the `ClaimStatusCards` click handler (lines 482-488):

```kotlin
ClaimStatusCards(
  onClick = { claimId ->
    onClaimDetailCardClicked(
      claimId,
      claimId in (uiState.claimStatusCardsData.partnerClaimIds),
    )
  },
```

Update all preview composables to use the new signature — replace each `onClaimDetailCardClicked = {},` + `onPartnerClaimDetailCardClicked = {},` pair with `onClaimDetailCardClicked = { _, _ -> },`.

- [ ] **Step 3: Update claim history nav `ClaimHistoryDestination.kt` (nav file)**

In `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/nav/ClaimHistoryDestination.kt`, update:

```kotlin
fun NavGraphBuilder.claimHistoryGraph(
  navigateUp: () -> Unit,
  navigateToClaimDetails: (claimId: String, isPartnerClaim: Boolean) -> Unit,
) {
  navdestination<ClaimHistoryDestination> {
    ClaimHistoryDestination(
      claimHistoryViewModel = koinViewModel(),
      navigateUp = navigateUp,
      navigateToClaimDetails = navigateToClaimDetails,
    )
  }
}
```

- [ ] **Step 4: Update claim history UI `ClaimHistoryDestination.kt`**

In `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt`, remove `navigateToPartnerClaimDetails` from all function signatures:

`ClaimHistoryDestination`:
```kotlin
@Composable
internal fun ClaimHistoryDestination(
  claimHistoryViewModel: ClaimHistoryViewModel,
  navigateUp: () -> Unit,
  navigateToClaimDetails: (String, Boolean) -> Unit,
)
```

`ClaimHistoryScreen`:
```kotlin
@Composable
private fun ClaimHistoryScreen(
  uiState: ClaimHistoryUiState,
  navigateUp: () -> Unit,
  navigateToClaimDetails: (String, Boolean) -> Unit,
  reload: () -> Unit,
)
```

`ClaimHistoryContent` — remove `navigateToPartnerClaimDetails` parameter:
```kotlin
@Composable
private fun ColumnScope.ClaimHistoryContent(
  uiState: ClaimHistoryUiState.Content,
  navigateToClaimDetails: (String, Boolean) -> Unit,
)
```

`ClaimHistoryItem` — remove `navigateToPartnerClaimDetails` and update click handler:
```kotlin
@Composable
private fun ClaimHistoryItem(
  index: Int,
  claim: ClaimHistory,
  navigateToClaimDetails: (String, Boolean) -> Unit,
)
```

Update the click handler in `ClaimHistoryItem` (lines 193-201):

```kotlin
    modifier = Modifier
      .fillMaxWidth()
      .clickable(
        onClick = dropUnlessResumed {
          navigateToClaimDetails(claim.id, claim.isPartnerClaim)
        },
      )
```

Update the `forEachIndexed` in `ClaimHistoryContent` to pass only `navigateToClaimDetails`:
```kotlin
uiState.claims.forEachIndexed { index, claim ->
  ClaimHistoryItem(index, claim, navigateToClaimDetails)
}
```

Update the preview at the bottom. Replace:
```kotlin
ClaimHistoryScreen(
  uiState = uiState,
  {},
  {},
  {},
  {},
)
```

With:
```kotlin
ClaimHistoryScreen(
  uiState = uiState,
  {},
  { _, _ -> },
  {},
)
```

- [ ] **Step 5: Update `HedvigNavHost.kt`**

Remove the `PartnerClaimOverviewDestination` import and the `partnerClaimDetailsGraph` import.

Update the `homeGraph` call (around line 185-190). Replace:

```kotlin
navigateToClaimDetails = { claimId ->
  navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId))
},
navigateToPartnerClaimDetails = { claimId ->
  navController.navigate(PartnerClaimOverviewDestination(claimId))
},
```

With:

```kotlin
navigateToClaimDetails = { claimId, isPartnerClaim ->
  navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId, isPartnerClaim))
},
```

Update the `claimHistoryGraph` call (around lines 357-365). Replace:

```kotlin
claimHistoryGraph(
  navigateUp = navController::navigateUp,
  navigateToClaimDetails = { claimId ->
    navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId))
  },
  navigateToPartnerClaimDetails = { claimId ->
    navController.navigate(PartnerClaimOverviewDestination(claimId))
  },
)
```

With:

```kotlin
claimHistoryGraph(
  navigateUp = navController::navigateUp,
  navigateToClaimDetails = { claimId, isPartnerClaim ->
    navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId, isPartnerClaim))
  },
)
```

Remove the `partnerClaimDetailsGraph` call (around lines 566-569):

```kotlin
partnerClaimDetailsGraph(
  navigateUp = navController::navigateUp,
  openUrl = openUrl,
)
```

Delete those 4 lines entirely.

- [ ] **Step 6: Commit**

```bash
git add app/feature/feature-home/ app/feature/feature-claim-history/ app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt
git commit -m "refactor: unify claim detail navigation — remove partner-specific callbacks"
```

---

### Task 5: Delete `feature-partner-claim-details` module and clean up references

**Files:**
- Delete: `app/feature/feature-partner-claim-details/` (entire directory)
- Delete: `hedvig-lint/lint-baseline/lint-baseline-feature-partner-claim-details.xml`
- Modify: `app/app/build.gradle.kts` (line 193)
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationModule.kt` (lines 81, 348)

- [ ] **Step 1: Delete the partner claim details module directory**

```bash
rm -rf app/feature/feature-partner-claim-details
```

- [ ] **Step 2: Delete the lint baseline file**

```bash
rm hedvig-lint/lint-baseline/lint-baseline-feature-partner-claim-details.xml
```

- [ ] **Step 3: Remove dependency from `app/build.gradle.kts`**

Remove line 193:
```kotlin
  implementation(projects.featurePartnerClaimDetails)
```

- [ ] **Step 4: Remove from `ApplicationModule.kt`**

Remove the import at line 81:
```kotlin
import com.hedvig.android.feature.partner.claim.details.di.partnerClaimDetailsModule
```

Remove the module inclusion at line 348:
```kotlin
      partnerClaimDetailsModule,
```

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: delete feature-partner-claim-details module and all references"
```

---

### Task 6: Format and verify build

- [ ] **Step 1: Run ktlint formatting**

```bash
./gradlew ktlintFormat
```

- [ ] **Step 2: Build the affected modules**

```bash
./gradlew :app:assemble
```

Fix any compilation errors.

- [ ] **Step 3: Run tests for claim details module**

```bash
./gradlew :feature-claim-details:test
```

- [ ] **Step 4: Run tests for claim history module**

```bash
./gradlew :feature-claim-history:test
```

- [ ] **Step 5: Commit any formatting fixes**

```bash
git add -A
git commit -m "chore: run ktlint formatting"
```

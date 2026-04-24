# Unify Partner Claim Detail Into Regular Claim Detail Destination

## Problem

PR #2930 introduces a separate `feature-partner-claim-details` module with its own destination, ViewModel, presenter, use case, and navigation wiring for partner claims. This creates dual navigation paths (home screen and claim history both need `navigateToPartnerClaimDetails` alongside `navigateToClaimDetails`), a separate module to maintain, and duplicated UI that is a subset of the regular claim detail screen.

## Goal

Remove `feature-partner-claim-details` and reuse the existing `feature-claim-details` destination for both regular and partner claims. The regular claim detail screen already conditionally renders most sections (chat, files, upload, audio), so partner claims can be represented as `ClaimDetailUiState.Content` with null/empty/disabled values for unsupported features.

## Approach

Extend the existing claim detail flow to handle partner claims by:
1. Adding an `isPartnerClaim` flag to the navigation destination
2. Branching in the use case to call the appropriate GraphQL query
3. Mapping partner claim data into the existing UI state model
4. Removing the partner claim module entirely

## Design

### 1. Navigation Destination

Add `isPartnerClaim: Boolean = false` to `ClaimOverviewDestination`:

```kotlin
data class ClaimOverviewDestination(
  val claimId: String,
  val isPartnerClaim: Boolean = false,
) : ClaimDetailDestination, Destination
```

Default `false` preserves backward compatibility with deep links and existing callers.

### 2. Use Case Changes

`GetClaimDetailUiStateUseCase` receives `isPartnerClaim` alongside `claimId` and branches:

- **Regular claim:** Calls `ClaimQuery(claimId)` as today, maps via `fromClaim()`
- **Partner claim:** Calls `PartnerClaimDetailQuery(claimId)`, maps into `ClaimDetailUiState.Content` with:
  - `conversationId = null`
  - `hasUnreadMessages = false`
  - `submittedContent = null`
  - `files = emptyList()`
  - `claimOutcome = UNKNOWN`
  - `uploadUri = ""`, `isUploadingFile = false`, `isUploadingFilesEnabled = false`
  - `appealInstructionsUrl = null`, `infoText = null`
  - `claimStatus`, `displayItems`, `termsConditionsUrl`, `claimStatusCardUiState` mapped from `PartnerClaimFragment`

### 3. UI Changes

Minimal. The existing screen already guards most sections:

- Chat link: `if (navigateToConversation != null)` -- null conversationId means no chat
- Files grid: `if (uiState.files.isNotEmpty())` -- empty list means no grid
- File upload: `if (uiState.isUploadingFilesEnabled)` -- false means no upload
- Submitted content: `when (uiState.submittedContent)` with `else -> {}` -- null means nothing
- Appeal instructions: `if (uiState.appealInstructionsUrl != null)`
- Info notification: `if (uiState.infoText != null)`
- T&C card: `if (uiState.termsConditionsUrl != null)`

One fix needed: guard the "uploaded files" section header with `if (uiState.submittedContent != null || uiState.files.isNotEmpty())` to avoid showing an empty header for partner claims.

### 4. Navigation Cleanup

- Remove `navigateToPartnerClaimDetails` from `homeGraph()`, `HomeDestination`, `HomeScreen`, `HomeScreenSuccess`, `claimHistoryGraph()`, `ClaimHistoryDestination`, `HedvigNavHost`
- Callers pass `isPartnerClaim = true` to the existing `navigateToClaimDetails` callback instead
- Keep `partnerClaimIds` in `ClaimStatusCardsData` and `isPartnerClaim` in `ClaimHistory` -- still needed to pass the flag

### 5. Module Deletion

Delete entirely:
- `app/feature/feature-partner-claim-details/` directory
- `hedvig-lint/lint-baseline/lint-baseline-feature-partner-claim-details.xml`

Remove references:
- `partnerClaimDetailsModule` from `ApplicationModule.kt`
- `projects.featurePartnerClaimDetails` from `app/build.gradle.kts`
- `partnerClaimDetailsGraph` from `HedvigNavHost.kt`

## Files to Modify

| Area | File | Change |
|------|------|--------|
| Nav destination | `ClaimDetailDestinations.kt` | Add `isPartnerClaim` param |
| Nav graph | `ClaimDetailDestinationGraph.kt` | Pass `isPartnerClaim` to ViewModel |
| DI | `FeatureClaimDetailsModule.kt` | Pass `isPartnerClaim` through |
| ViewModel | `ClaimDetailsViewModel.kt` | Accept + forward `isPartnerClaim` |
| Use case | `GetClaimDetailUiStateUseCase.kt` | Branch on `isPartnerClaim`, add partner claim mapping |
| UI | `ClaimDetailsDestination.kt` | Guard "uploaded files" header |
| Home nav | `HomeGraph.kt`, `HomeDestination.kt` | Remove partner callback, pass flag |
| Claim history | `ClaimHistoryDestination.kt` (nav + ui) | Remove partner callback, pass flag |
| App nav host | `HedvigNavHost.kt` | Remove partner graph, update callbacks |
| App module | `ApplicationModule.kt`, `build.gradle.kts` | Remove partner module refs |
| Delete | `feature-partner-claim-details/`, lint baseline | Full removal |

## Out of Scope

- No changes to GraphQL queries/fragments
- No changes to `ui-claim-status` shared components
- No changes to `GetHomeDataUseCase` or `GetClaimsHistoryUseCase`

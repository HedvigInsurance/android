# Remove `isPartnerClaim` plumbing — fall back to partner claim query when regular claim is not found

PR: https://github.com/HedvigInsurance/android/pull/2930
Branch: `feat/show-car-claims`

## Context

PR #2930 introduces partner claims. To know which Apollo query to run for the claim details screen, the PR threads an `isPartnerClaim: Boolean` parameter through:

- Home (computed via `claimId in partnerClaimIds`) → nav callback → `ClaimOverviewDestination`
- Claim history (computed via `claim.isPartnerClaim`) → nav callback → `ClaimOverviewDestination`
- `ClaimDetailsViewModel` / `ClaimDetailPresenter` → `GetClaimDetailUiStateUseCase.invoke(claimId, isPartnerClaim)`

This forces every navigation site to know the claim type up-front, and adds a navigation argument that callers can get wrong (e.g. deep links currently default to `false`, so a partner-claim deep link silently breaks).

We want callers to navigate with just a `claimId`. The use case will probe the regular `claim(id:)` query first and fall back to `partnerClaim(id:)` if the regular query returns no claim. This makes the navigation API claim-type-agnostic and fixes the deep-link case.

## Approach

### 1. Use case: try regular, fall back to partner — `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/data/GetClaimDetailUiStateUseCase.kt`

- Drop the `isPartnerClaim: Boolean` parameter from `invoke`.
- Resolve the claim type **once** per `invoke` call, then poll the appropriate query.
- Resolution rule: run the regular query; if it succeeds and `data.claim == null`, treat as a partner claim. If it emits `Error.NetworkError`, surface the error (do *not* assume partner) and retry on the next polling cycle.
- Keep existing helpers (`queryFlow`, `partnerQueryFlow`, `fromPartnerClaim`, `fromClaim`) as they are.

Sketch:

```kotlin
fun invoke(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> = flow {
  var isPartnerClaim: Boolean? = null
  while (currentCoroutineContext().isActive) {
    when (isPartnerClaim) {
      true -> emitAll(partnerQueryFlow(claimId))
      false -> emitAll(queryFlow(claimId))
      null -> emitAll(
        queryFlow(claimId).onEach { result ->
          result.fold(
            ifLeft = { /* leave isPartnerClaim null on NetworkError; NoClaimFound also stays null */ },
            ifRight = { isPartnerClaim = false },
          )
        },
      ).also {
        // After first attempt, if still unresolved AND last emission was NoClaimFound, try partner.
      }
    }
    delay(POLL_INTERVAL)
  }
}
```

Concretely use a small helper that returns whether resolution was reached:

```kotlin
private fun resolveAndEmit(claimId: String): Flow<Pair<Boolean?, Either<Error, ClaimDetailUiState.Content>>> { ... }
```

Or — simpler and easier to read — use a sequential one-shot resolution before the polling loop:

```kotlin
fun invoke(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> = flow {
  // Probe with the regular query; the NoClaimFound branch falls back to partner.
  // Network errors short-circuit the probe and are emitted; we retry on the next poll cycle.
  val initial = queryFlow(claimId).first()
  val isPartnerClaim = when {
    initial.isRight() -> { emit(initial); false }
    initial.leftOrNull() is Error.NoClaimFound -> {
      val partnerInitial = partnerQueryFlow(claimId).first()
      emit(partnerInitial)
      partnerInitial.isRight() // stick with partner only if it actually returned a claim
    }
    else -> { emit(initial); null } // NetworkError — leave unresolved, retry below
  }
  while (currentCoroutineContext().isActive) {
    delay(POLL_INTERVAL)
    when (isPartnerClaim) {
      true -> emitAll(partnerQueryFlow(claimId))
      false -> emitAll(queryFlow(claimId))
      null -> {
        // re-run the probe sequence
      }
    }
  }
}
```

Implementer should pick the cleaner of these — both meet the spec. The key invariants:
1. On a **regular** claim, never hit the partner endpoint.
2. On a **partner** claim, the partner endpoint is hit only after the regular probe returned `NoClaimFound`.
3. On a `NetworkError` from the probe, we do **not** silently fall back to partner — we surface the error and retry.

### 2. Drop `isPartnerClaim` from the navigation argument — `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinations.kt`

Remove the `isPartnerClaim: Boolean = false` field from `ClaimOverviewDestination` so it carries only `claimId`.

### 3. Drop `isPartnerClaim` from VM/presenter wiring

- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/ui/ClaimDetailsViewModel.kt`: remove `isPartnerClaim` from `ClaimDetailsViewModel` constructor and from `ClaimDetailPresenter`. The `LaunchedEffect(loadIteration)` block now calls `getClaimDetailUiStateUseCase.invoke(claimId)`.
- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/di/FeatureClaimDetailsModule.kt`: viewModel factory becomes `viewModel<ClaimDetailsViewModel> { (claimId: String) -> ClaimDetailsViewModel(claimId, get(), get(), get()) }`.
- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinationGraph.kt`: `koinViewModel { parametersOf(claimId, isPartnerClaim) }` → `koinViewModel { parametersOf(claimId) }`.

### 4. Simplify call sites that previously needed to know the claim type

- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`: change two `navigateToClaimDetails = { claimId, isPartnerClaim -> navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId, isPartnerClaim)) }` lambdas to `{ claimId -> navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId)) }`. (The other two call sites already use the single-arg form.)
- `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`: change the `onClaimDetailCardClicked: (claimId: String, isPartnerClaim: Boolean) -> Unit` parameter (and its three preview-stub call sites) to `(claimId: String) -> Unit`. The call site currently doing `onClaimDetailCardClicked(claimId, claimId in (uiState.claimStatusCardsData.partnerClaimIds))` becomes `onClaimDetailCardClicked(claimId)`.
- `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeGraph.kt`: update the `navigateToClaimDetails` and `onClaimDetailCardClicked` lambda signatures to drop `isPartnerClaim`.
- `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt`: change `navigateToClaimDetails(claim.id, claim.isPartnerClaim)` to `navigateToClaimDetails(claim.id)`; drop `isPartnerClaim = false` from preview/test data; update the `(claimId, isPartnerClaim) -> Unit` parameter to `(claimId) -> Unit`.
- `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/nav/ClaimHistoryDestination.kt`: update the `navigateToClaimDetails` parameter signature.

### 5. Remove now-dead supporting data

These exist only to compute `isPartnerClaim` for navigation; with the use-case fallback they have no other consumers and should be deleted to avoid dead state:

- `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/data/GetHomeDataUseCase.kt`: remove the `partnerClaimIds: Set<String>` field from `claimStatusCardsData` and the `partnerClaims.map { it.id }.toSet()` line that populates it. (The `partnerClaims` mapping into `partnerCards` stays — that's how partner claims render on home.)
- `app/feature/feature-claim-history/src/commonMain/kotlin/com/hedvig/android/feature/claimhistory/GetClaimsHistoryUseCase.kt`: remove `isPartnerClaim` from the `ClaimHistory` data class and its two assignments. (Keep the partner-claims mapping itself — they still need to appear in the history list.)

If a grep shows either field referenced elsewhere (e.g. the "Closed" label on partner claims in history), keep the field and only remove the navigation usage. **Verification step before deleting**: `rg "isPartnerClaim|partnerClaimIds" app/feature/feature-home app/feature/feature-claim-history` — only the navigation/use-case sites should remain; if anything else references it, leave it.

## Critical files

- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/data/GetClaimDetailUiStateUseCase.kt`
- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/ui/ClaimDetailsViewModel.kt`
- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinations.kt`
- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinationGraph.kt`
- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/di/FeatureClaimDetailsModule.kt`
- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`
- `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/data/GetHomeDataUseCase.kt`
- `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`
- `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeGraph.kt`
- `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/ClaimHistoryDestination.kt`
- `app/feature/feature-claim-history/src/androidMain/kotlin/com/hedvig/android/feature/claimhistory/nav/ClaimHistoryDestination.kt`
- `app/feature/feature-claim-history/src/commonMain/kotlin/com/hedvig/android/feature/claimhistory/GetClaimsHistoryUseCase.kt`

## Verification

1. Branch: check out `feat/show-car-claims` (the PR branch), then apply these changes.
2. Compile: `./gradlew :feature-claim-details:assemble :feature-home:assemble :feature-claim-history:assemble :app:assembleDebug`
3. Format: `./gradlew ktlintFormat`
4. Tests: `./gradlew :feature-claim-details:test :feature-home:test :feature-claim-history:test`
5. Manual smoke (per PR test plan):
   - Open a regular claim from home — verify the regular query is used (no partner endpoint hit) and the full screen (chat, files, audio) renders.
   - Open a partner claim from home — verify the regular probe runs once, returns no claim, then the partner query renders the simplified screen.
   - Open a partner claim from claim history — same.
   - Open a regular claim via deep link — works (this previously assumed `isPartnerClaim = false`, which still resolves correctly under the new logic).
   - Pull-to-refresh / re-enter the screen — polling does not re-probe the regular query for a known partner claim.
6. Static check: `rg "isPartnerClaim" app/` should match nothing.

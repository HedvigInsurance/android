# Home Quick-Action Tiles Mirror the Help Center — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Home's hardcoded quick-action tiles with the first three of the member's actual Help Center quick-action list (same items, order, eligibility, and behavior, including the Edit-insurance sub-options sheet).

**Architecture:** Extract the member-actions / quick-links domain logic out of `feature-help-center` into a new KMP shared module `member-quick-actions` that both features depend on. The `QuickLinkDestination → HedvigNavKey` mapping moves into that shared module as `QuickLinkDestination.toNavKey()`, so both consumers just call it and `backstack.add(...)`. Home's presenter consumes the shared use case; Home's UI renders the first three actions as tiles.

**Tech Stack:** Kotlin Multiplatform, Jetpack Compose, Apollo GraphQL (octopus), Metro DI, Molecule, Arrow, assertK + Turbine (tests).

## Global Constraints

- DI is Metro. Bindings are `@ContributesBinding(AppScope::class)` / `@SingleIn(AppScope::class)` + `@Inject`; never Koin, never `get()`.
- Navigation is Navigation 3. Destinations are `@Serializable HedvigNavKey`s; push with `backstack.add(...)`. No `NavController`, no route strings.
- Feature modules cannot depend on other feature modules; the shared module and `-navigation` modules are the carve-outs.
- Never expose `octopus.*` Apollo types in public API; confine them to `internal` impl classes and map to project-owned types.
- Never add strings to `strings.xml` (Lokalise-managed). Reuse existing `HC_QUICK_ACTIONS_*` / `HOME_*` keys; if a needed string is missing, hardcode English + `// TODO: Add "<EN>" / "<SV>" to Lokalise`.
- No em-dash in prose/comments. Run `./gradlew ktlintFormat` before each commit.
- Comments describe current code only (no history/migration/process notes).

---

## File Structure

**New shared module `app/shared/member-quick-actions`** (KMP, commonMain; project accessor `projects.memberQuickActions`):
- `build.gradle.kts` — KMP + apollo("octopus").
- `src/commonMain/graphql/QueryMemberActions.graphql` — moved.
- `src/commonMain/graphql/QueryAvailableSelfServiceOnContracts.graphql` — moved.
- `.../data/GetMemberActionsUseCase.kt` — moved (`MemberAction`, `MemberActionWithDetails`, use case).
- `.../data/GetMemberQuickActionsUseCase.kt` — moved+renamed from `GetQuickLinksUseCase.kt` (`QuickLinkDestination`, the builder).
- `.../model/QuickAction.kt` — moved.
- `.../navigation/QuickLinkNavKey.kt` — new: `fun QuickLinkDestination.toNavKey(): HedvigNavKey` (mapping moved out of `HelpCenterPresenter`).
- `src/jvmTest/.../GetMemberQuickActionsUseCaseTest.kt` — moved from `GetQuickLinksUseCaseTest.kt`.

**`feature-help-center`** — consumes the shared module; deletes the moved files; `HelpCenterPresenter` replaces its `when` mapping with `destination.toNavKey()`.

**`feature-home`**:
- `QueryHome.graphql` — remove the `memberActions { isChangeTierEnabled isMovingEnabled isTravelCertificateEnabled }` additions.
- `GetHomeDataUseCase.kt` / `GetHomeDataUseCaseDemo.kt` — remove `isEditInsuranceEnabled` / `isMovingEnabled` / `isTravelCertificateEnabled` from `HomeData`.
- `HomePresenter.kt` — inject `GetMemberQuickActionsUseCase`; replace the three booleans on `HomeUiState.Success` with `quickActions: List<QuickAction>` (first three).
- `HomeDestination.kt` — `QuickActionTilesSection` renders tiles from `quickActions`; new `EditInsuranceQuickActionSheet`; icon mapping.
- `HomePresenterTest.kt` — update construction sites; add first-3/fewer/empty test.

**`:app`** — `HedvigEntryProvider.kt` passes a `navigateToQuickLink: (QuickLinkDestination) -> Unit` into `homeEntries(...)` implemented as `{ backstack.add(it.toNavKey()) }`.

---

## Task 1: Create the shared module skeleton and move the GraphQL queries

**Files:**
- Create: `app/shared/member-quick-actions/build.gradle.kts`
- Move: `app/feature/feature-help-center/src/commonMain/graphql/QueryMemberActions.graphql` → `app/shared/member-quick-actions/src/commonMain/graphql/QueryMemberActions.graphql`
- Move: `app/feature/feature-help-center/src/commonMain/graphql/QueryAvailableSelfServiceOnContracts.graphql` → `app/shared/member-quick-actions/src/commonMain/graphql/QueryAvailableSelfServiceOnContracts.graphql`

**Interfaces:**
- Produces: a buildable module `projects.memberQuickActions` generating `octopus.MemberActionsQuery` and `octopus.AvailableSelfServiceOnContractsQuery`.

- [ ] **Step 1: Create the module build file**

Create `app/shared/member-quick-actions/build.gradle.kts`:

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("hedvig.multiplatform.library")
  id("hedvig.multiplatform.library.android")
  id("hedvig.gradle.plugin")
}

hedvig {
  apollo("octopus")
  serialization()
}

kotlin {
  sourceSets {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
      common {
        group("jvmAndAndroid") {
          withAndroidLibraryTarget()
          withJvm()
        }
      }
    }
    commonMain.dependencies {
      implementation(libs.apollo.runtime)
      implementation(libs.arrow.core)
      implementation(libs.arrow.fx)
      implementation(libs.coroutines.core)
      implementation(libs.kotlinx.serialization.core)
      implementation(projects.apolloCore)
      implementation(projects.apolloOctopusPublic)
      implementation(projects.coreCommonPublic)
      implementation(projects.coreResources)
      implementation(projects.dataCoinsured)
      implementation(projects.featureFlags)
      implementation(projects.loggingPublic)
      implementation(projects.navigationCommon)
      implementation(projects.partnersDeflect)
      implementation(projects.uiEmergency)
      // -navigation modules for QuickLinkDestination.toNavKey() (Task 3)
      implementation(projects.featureChooseTierNavigation)
      implementation(projects.featureConnectPaymentTrustlyNavigation)
      implementation(projects.featureEditCoinsuredNavigation)
      implementation(projects.featureMovingflowNavigation)
      implementation(projects.featureTerminateInsuranceNavigation)
      implementation(projects.featureTravelCertificateNavigation)
    }
    jvmTest.dependencies {
      implementation(libs.apollo.testingSupport)
      implementation(libs.assertK)
      implementation(libs.coroutines.test)
      implementation(libs.junit)
      implementation(libs.turbine)
      implementation(projects.apolloOctopusTest)
      implementation(projects.apolloTest)
      implementation(projects.coreCommonTest)
      implementation(projects.featureFlagsTest)
      implementation(projects.loggingTest)
    }
  }
}
```

Note: `EmergencyKey` and `FirstVetKey` are resolved in Task 3; if they live in modules not listed above, add those `-navigation` deps when Task 3's compile fails (see Task 3 Step 4).

- [ ] **Step 2: Move the two GraphQL files**

```bash
cd /Users/stylianosgakis/hedvig/apps/android_copy
mkdir -p app/shared/member-quick-actions/src/commonMain/graphql
git mv app/feature/feature-help-center/src/commonMain/graphql/QueryMemberActions.graphql \
       app/shared/member-quick-actions/src/commonMain/graphql/QueryMemberActions.graphql
git mv app/feature/feature-help-center/src/commonMain/graphql/QueryAvailableSelfServiceOnContracts.graphql \
       app/shared/member-quick-actions/src/commonMain/graphql/QueryAvailableSelfServiceOnContracts.graphql
```

- [ ] **Step 3: Confirm module auto-discovery and generate sources**

Run: `./gradlew :member-quick-actions:generateApolloSources`
Expected: BUILD SUCCESSFUL, and `octopus.MemberActionsQuery` / `octopus.AvailableSelfServiceOnContractsQuery` are generated under the module's build dir. (settings.gradle.kts auto-includes any dir under `app/` with a `build.gradle.kts`.)

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q
git add -A
git commit -m "member-quick-actions: scaffold shared module and move member-actions graphql"
```

---

## Task 2: Move MemberAction model + GetMemberActionsUseCase into the shared module

**Files:**
- Move: `app/feature/feature-help-center/.../data/MemberAction.kt` → `app/shared/member-quick-actions/src/commonMain/kotlin/com/hedvig/android/memberquickactions/MemberAction.kt`

**Interfaces:**
- Produces: `com.hedvig.android.memberquickactions.GetMemberActionsUseCase` (public), returning `Either<ErrorMessage, MemberAction>`; public `MemberAction`, `MemberActionWithDetails`.

- [ ] **Step 1: Move the file and repackage**

```bash
mkdir -p app/shared/member-quick-actions/src/commonMain/kotlin/com/hedvig/android/memberquickactions
git mv app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/data/MemberAction.kt \
       app/shared/member-quick-actions/src/commonMain/kotlin/com/hedvig/android/memberquickactions/MemberAction.kt
```

- [ ] **Step 2: Move the use-case impl**

The impl is `GetMemberActionsUseCase.kt` in help-center `data/`. Move it too:

```bash
git mv app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/data/GetMemberActionsUseCase.kt \
       app/shared/member-quick-actions/src/commonMain/kotlin/com/hedvig/android/memberquickactions/GetMemberActionsUseCaseImpl.kt
```
(If `MemberAction.kt` and the use case are the same file already, skip the redundant move; keep one file.)

- [ ] **Step 3: Repackage the moved files**

In both moved files, change the package line to `package com.hedvig.android.memberquickactions` and change `internal` to public on `GetMemberActionsUseCase`, `MemberAction`, and `MemberActionWithDetails` (they are consumed cross-module now). Keep `GetMemberActionsUseCaseImpl` `internal` with its `@ContributesBinding(AppScope::class) @SingleIn(AppScope::class) @Inject`. Imports for `octopus.MemberActionsQuery`, `DeflectData`, `FirstVetSection` stay; they resolve from this module's deps.

- [ ] **Step 4: Verify the shared module compiles**

Run: `./gradlew :member-quick-actions:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. (feature-help-center will NOT compile yet — fixed in Task 4.)

- [ ] **Step 5: Commit**

```bash
./gradlew ktlintFormat -q
git add -A
git commit -m "member-quick-actions: move MemberAction + GetMemberActionsUseCase into shared module"
```

---

## Task 3: Move QuickAction / QuickLinkDestination / the builder, and add toNavKey()

**Files:**
- Move: `.../model/QuickAction.kt` → `app/shared/member-quick-actions/.../QuickAction.kt`
- Move+rename: `.../data/GetQuickLinksUseCase.kt` → `app/shared/member-quick-actions/.../GetMemberQuickActionsUseCase.kt`
- Create: `app/shared/member-quick-actions/.../QuickLinkNavKey.kt`

**Interfaces:**
- Consumes: `GetMemberActionsUseCase` (Task 2).
- Produces:
  - `sealed interface QuickAction { StandaloneQuickLink(titleRes, hintTextRes, quickLinkDestination); MultiSelectExpandedLink(titleRes, hintTextRes, links: List<StandaloneQuickLink>) }` (public)
  - `sealed interface QuickLinkDestination` with public `OuterDestination` and public `InnerHelpCenterDestination` cases (rename the type out of the `internal` scoping; make inner cases public).
  - `interface GetMemberQuickActionsUseCase { suspend fun invoke(): Either<ErrorMessage, List<QuickAction>> }` (public).
  - `fun QuickLinkDestination.toNavKey(): HedvigNavKey` (public).

- [ ] **Step 1: Move + repackage QuickAction and the builder**

```bash
git mv app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/model/QuickAction.kt \
       app/shared/member-quick-actions/src/commonMain/kotlin/com/hedvig/android/memberquickactions/QuickAction.kt
git mv app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/data/GetQuickLinksUseCase.kt \
       app/shared/member-quick-actions/src/commonMain/kotlin/com/hedvig/android/memberquickactions/GetMemberQuickActionsUseCase.kt
```

Repackage both to `package com.hedvig.android.memberquickactions`. In the builder file:
- Rename interface `GetQuickLinksUseCase` → `GetMemberQuickActionsUseCase` and impl `GetQuickLinksUseCaseImpl` → `GetMemberQuickActionsUseCaseImpl` (keep annotations).
- Make `QuickLinkDestination` and ALL its nested `OuterDestination` / `InnerHelpCenterDestination` cases public (remove `internal`); make `QuickAction` public.
- Keep `octopus.AvailableSelfServiceOnContractsQuery` import (resolves from this module).

- [ ] **Step 2: Add the toNavKey() mapping**

Create `app/shared/member-quick-actions/src/commonMain/kotlin/com/hedvig/android/memberquickactions/QuickLinkNavKey.kt`. Move the exact `when` block from `HelpCenterPresenter` (the `NavigateToQuickAction` handler, cases listed in the design). Content:

```kotlin
package com.hedvig.android.memberquickactions

import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowChooseInsuranceKey
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.movingflow.navigation.SelectContractForMovingKey
import com.hedvig.android.feature.movingflow.navigation.MovingSource
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.memberquickactions.QuickLinkDestination.InnerHelpCenterDestination
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoInsured
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoOwners
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkChangeAddress
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkChangeTier
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddInfo
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddOrRemove
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkConnectPayment
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkTermination
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.emergency.EmergencyKey

fun QuickLinkDestination.toNavKey(): HedvigNavKey = when (this) {
  is InnerHelpCenterDestination.FirstVet -> FirstVetNavKeyPlaceholder(sections)
  is InnerHelpCenterDestination.QuickLinkSickAbroad -> EmergencyKey(deflectData)
  QuickLinkChangeAddress -> SelectContractForMovingKey(MovingSource.OTHER)
  is QuickLinkCoInsuredAddInfo -> CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured)
  is QuickLinkCoInsuredAddOrRemove -> CoInsuredAddOrRemoveKey(contractId, CoInsuredFlowType.CoInsured)
  is QuickLinkCoOwnerAddInfo -> CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoOwners)
  is QuickLinkCoOwnerAddOrRemove -> CoInsuredAddOrRemoveKey(contractId, CoInsuredFlowType.CoOwners)
  QuickLinkConnectPayment -> TrustlyKey
  QuickLinkTermination -> TerminateInsuranceKey(null)
  QuickLinkTravelCertificate -> TravelCertificateKey
  QuickLinkChangeTier -> StartTierFlowChooseInsuranceKey
  ChooseInsuranceForEditCoInsured -> EditCoInsuredTriageKey()
  ChooseInsuranceForEditCoOwners -> EditCoInsuredTriageKey(type = CoInsuredFlowType.CoOwners)
}
```

NOTE for the implementer: `FirstVetKey` and the exact import paths for `EmergencyKey`, `SelectContractForMovingKey`, `MovingSource`, and `TrustlyKey` must be taken from the real imports currently in `HelpCenterPresenter.kt` (open it and copy them verbatim; replace `FirstVetNavKeyPlaceholder` with the real `FirstVetKey(...)` used there). If `FirstVetKey` lives in `feature-home` (not a `-navigation` module), the FirstVet case cannot resolve here — in that case leave `InnerHelpCenterDestination.FirstVet` OUT of `toNavKey()` (throw `error("handled by caller")`) and let each caller map FirstVet itself (Help Center already has `FirstVetKey`; Home has its own FirstVet destination). Prefer this caller-handles-FirstVet approach if FirstVetKey is not in a shared/-navigation module.

- [ ] **Step 3: Add any missing -navigation dependencies**

Run: `./gradlew :member-quick-actions:compileDebugKotlin`
If unresolved-reference errors name a nav key, add the matching `projects.feature…Navigation` (or `projects.navigationEmergency`) to `commonMain.dependencies` in the module build file, then re-run until BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
./gradlew ktlintFormat -q
git add -A
git commit -m "member-quick-actions: move QuickAction/QuickLinkDestination + builder + toNavKey into shared module"
```

---

## Task 4: Refactor feature-help-center to consume the shared module

**Files:**
- Modify: `app/feature/feature-help-center/build.gradle.kts` (add `implementation(projects.memberQuickActions)`)
- Modify: `HelpCenterPresenter.kt` (replace the `when` mapping with `destination.toNavKey()`; update imports)
- Modify: every help-center file importing the moved types (update imports to `com.hedvig.android.memberquickactions.*`)
- Move: `GetQuickLinksUseCaseTest.kt` → shared module `src/jvmTest/...` as `GetMemberQuickActionsUseCaseTest.kt`

**Interfaces:**
- Consumes: `GetMemberQuickActionsUseCase`, `QuickAction`, `QuickLinkDestination`, `toNavKey()` from `projects.memberQuickActions`.

- [ ] **Step 1: Add the dependency**

In `feature-help-center/build.gradle.kts` `commonMain.dependencies`, add `implementation(projects.memberQuickActions)`.

- [ ] **Step 2: Fix imports across help-center**

Run to find broken references, then update each import from the old packages (`...help.center.data.QuickLinkDestination`, `...help.center.model.QuickAction`, `...help.center.data.GetQuickLinksUseCase`, `...help.center.data.GetMemberActionsUseCase`, `...help.center.data.MemberAction`) to `com.hedvig.android.memberquickactions.*`. Rename `GetQuickLinksUseCase` references to `GetMemberQuickActionsUseCase`:

```bash
grep -rln "GetQuickLinksUseCase\|help.center.data.QuickLinkDestination\|help.center.model.QuickAction\|help.center.data.MemberAction\|help.center.data.GetMemberActionsUseCase" app/feature/feature-help-center/src
```

- [ ] **Step 3: Replace the mapping in HelpCenterPresenter**

In the `NavigateToQuickAction` handler, delete the whole `when (val destination = event.destination) { … }` block that built `key` and replace with:

```kotlin
is NavigateToQuickAction -> {
  selectedQuickAction = null
  backstack.add(event.destination.toNavKey())
}
```
(If Task 3 left FirstVet out of `toNavKey()`, keep only the FirstVet branch here: `val key = if (dest is InnerHelpCenterDestination.FirstVet) FirstVetKey(dest.sections) else dest.toNavKey()`.) Remove now-unused nav-key imports from `HelpCenterPresenter.kt`.

- [ ] **Step 4: Move the test**

```bash
git mv app/feature/feature-help-center/src/jvmTest/kotlin/GetQuickLinksUseCaseTest.kt \
       app/shared/member-quick-actions/src/jvmTest/kotlin/GetMemberQuickActionsUseCaseTest.kt
```
Repackage/rename inside: update class name to `GetMemberQuickActionsUseCaseTest`, update imports to `com.hedvig.android.memberquickactions.*`, rename the use-case references. Keep the test bodies identical.

- [ ] **Step 5: Verify help-center + shared tests pass**

Run: `./gradlew :feature-help-center:compileDebugKotlin :member-quick-actions:jvmTest :feature-help-center:compileReleaseKotlin`
Expected: BUILD SUCCESSFUL; the moved test passes unchanged (proving the move preserved behavior).

- [ ] **Step 6: Commit**

```bash
./gradlew ktlintFormat -q
git add -A
git commit -m "feature-help-center: consume member-quick-actions shared module"
```

---

## Task 5: Home presenter consumes the shared use case; revert the interim gating fields

**Files:**
- Modify: `feature-home/build.gradle.kts` (add `implementation(projects.memberQuickActions)`)
- Modify: `app/feature/feature-home/src/main/graphql/QueryHome.graphql` (remove the three `memberActions` fields)
- Modify: `GetHomeDataUseCase.kt` / `GetHomeDataUseCaseDemo.kt` (remove the three `is…Enabled` fields from `HomeData`)
- Modify: `HomePresenter.kt` (inject `GetMemberQuickActionsUseCase`; expose `quickActions: List<QuickAction>` on `Success`)
- Modify: `HomePresenterTest.kt`

**Interfaces:**
- Consumes: `GetMemberQuickActionsUseCase.invoke(): Either<ErrorMessage, List<QuickAction>>`, `QuickAction`.
- Produces: `HomeUiState.Success.quickActions: List<QuickAction>` (the first three of the member's list; empty on error).

- [ ] **Step 1: Revert the QueryHome memberActions additions**

In `QueryHome.graphql`, delete the `isChangeTierEnabled`, `isMovingEnabled`, `isTravelCertificateEnabled` lines added to the `memberActions { }` block (leave `firstVetAction { … }`). Run `./gradlew :feature-home:generateApolloSources` to confirm it still generates.

- [ ] **Step 2: Remove the interim HomeData flags**

In `GetHomeDataUseCase.kt` remove `isEditInsuranceEnabled` / `isMovingEnabled` / `isTravelCertificateEnabled` from the `HomeData` data class, the mapping in `GetHomeDataUseCaseImpl`, and the comment. In `GetHomeDataUseCaseDemo.kt` remove the three assignments.

- [ ] **Step 3: Write the failing presenter test**

Add `projects.memberQuickActions` to `feature-home/build.gradle.kts` (`commonMain` if KMP test needs it; the module is android — add to the main `dependencies { }` block matching how feature-home declares deps) and `projects.memberQuickActionsTest` is not needed (use a fake). In `HomePresenterTest.kt` add:

```kotlin
@Test
fun `home shows the first three member quick actions`() = runTest {
  val getHomeDataUseCase = TestGetHomeDataUseCase()
  val quickActions = listOf(
    editInsuranceMultiSelect, changeAddressLink, paymentsLink, travelCertificateLink,
  )
  val homePresenter = HomePresenter(
    getHomeDataUseCase,
    SeenImportantMessagesStorageImpl(),
    FakeCrossSellHomeNotificationService(),
    ApplicationScope(backgroundScope),
    false,
    TestDeleteClaimIntentDraftUseCase(),
    FakeGetMemberQuickActionsUseCase(quickActions.right()),
  )
  homePresenter.test(HomeUiState.Loading) {
    assertThat(awaitItem()).isInstanceOf<HomeUiState.Loading>()
    getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
    val success = assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()
    success.prop(HomeUiState.Success::quickActions)
      .isEqualTo(listOf(editInsuranceMultiSelect, changeAddressLink, paymentsLink))
  }
}
```

Define `FakeGetMemberQuickActionsUseCase(private val result: Either<ErrorMessage, List<QuickAction>>)` implementing the interface returning `result`, and the four `QuickAction` fixtures (use `StandaloneQuickLink(titleRes = Res.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE, hintTextRes = …, quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeAddress)` etc., and one `MultiSelectExpandedLink`).

- [ ] **Step 4: Run it to confirm it fails**

Run: `./gradlew :feature-home:testDebugUnitTest --tests "*HomePresenterTest*first three*"`
Expected: FAIL (compilation error — `quickActions` not on `Success` / constructor lacks the use case).

- [ ] **Step 5: Implement the presenter change**

In `HomePresenter.kt`:
- Add constructor param `private val getMemberQuickActionsUseCase: GetMemberQuickActionsUseCase`.
- Add `val quickActions: List<QuickAction>` to `HomeUiState.Success`, to `SuccessData`, to `fromLastState` (from `lastState.quickActions`), and to the `present()` build (`quickActions = successData.quickActions`).
- In the data-load path (where `getHomeDataUseCase.invoke(...)` result maps to `SuccessData.fromHomeData(...)`), also fetch quick actions: `val quickActions = getMemberQuickActionsUseCase.invoke().getOrElse { emptyList() }.take(3)` and pass it into `fromHomeData(...)` (add a `quickActions` param) or set on `SuccessData`. Keep the fetch failure-tolerant (empty list → section hidden).

- [ ] **Step 6: Run the test to confirm it passes**

Run: `./gradlew :feature-home:testDebugUnitTest --tests "*HomePresenterTest*"`
Expected: PASS (all existing tests still pass after adding the new constructor arg and `quickActions` to their expected `Success` — set `quickActions = emptyList()` on existing expected `Success` instances and pass `FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right())` to their presenter construction).

- [ ] **Step 7: Commit**

```bash
./gradlew ktlintFormat -q
git add -A
git commit -m "feature-home: source quick actions from the shared use case; drop interim eligibility flags"
```

---

## Task 6: Home UI renders the tiles and the Edit-insurance sheet

**Files:**
- Modify: `HomeDestination.kt` (`QuickActionTilesSection`, icon mapping, `EditInsuranceQuickActionSheet`, section visibility, previews)
- Modify: `HomeEntries.kt` (thread `navigateToQuickLink`)
- Modify: `HedvigEntryProvider.kt` (`navigateToQuickLink = { backstack.add(it.toNavKey()) }`)

**Interfaces:**
- Consumes: `Success.quickActions`, `QuickAction`, `QuickLinkDestination`, `toNavKey()`.
- Produces: tiles that navigate; Edit-insurance sheet.

- [ ] **Step 1: Thread the navigation lambda**

In `HomeEntries.kt` add param `navigateToQuickLink: (QuickLinkDestination) -> Unit` and pass it to `HomeDestination`. In `HedvigEntryProvider.kt`'s `homeEntries(...)` call add `navigateToQuickLink = { backstack.add(it.toNavKey()) }` (import `com.hedvig.android.memberquickactions.toNavKey`; add `implementation(projects.memberQuickActions)` to `app/app/build.gradle.kts` if not transitively present). If FirstVet is caller-handled (Task 3), map it here: `navigateToQuickLink = { d -> if (d is InnerHelpCenterDestination.FirstVet) backstack.add(FirstVetKey(d.sections)) else backstack.add(d.toNavKey()) }`.

- [ ] **Step 2: Replace QuickActionTilesSection**

Rewrite `QuickActionTilesSection` to take `quickActions: List<QuickAction>`, `onQuickLink: (QuickLinkDestination) -> Unit`, and an `onEditInsurance: (MultiSelectExpandedLink) -> Unit`, rendering one `HomeActionTile` per action:

```kotlin
@Composable
private fun QuickActionTilesSection(
  quickActions: List<QuickAction>,
  onQuickLink: (QuickLinkDestination) -> Unit,
  onEditInsurance: (QuickAction.MultiSelectExpandedLink) -> Unit,
  horizontalInsets: PaddingValues,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(horizontalInsets),
  ) {
    HedvigText(
      text = stringResource(Res.string.HC_QUICK_ACTIONS_TITLE),
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
    ) {
      quickActions.forEach { action ->
        HomeActionTile(
          icon = action.homeIcon(),
          text = stringResource(action.titleRes),
          onClick = {
            when (action) {
              is QuickAction.StandaloneQuickLink -> onQuickLink(action.quickLinkDestination)
              is QuickAction.MultiSelectExpandedLink -> onEditInsurance(action)
            }
          },
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )
      }
    }
  }
}
```

- [ ] **Step 3: Add the icon mapping**

Add a private helper mapping each action to a `HedvigIcons` glyph. Multi-select (Edit insurance) → `Settings`; standalone by destination type:

```kotlin
@Composable
private fun QuickAction.homeIcon(): ImageVector = when (this) {
  is QuickAction.MultiSelectExpandedLink -> HedvigIcons.Settings
  is QuickAction.StandaloneQuickLink -> when (quickLinkDestination) {
    QuickLinkDestination.OuterDestination.QuickLinkChangeAddress -> HedvigIcons.Reload
    QuickLinkDestination.OuterDestination.QuickLinkConnectPayment -> HedvigIcons.Card
    QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate -> HedvigIcons.Travel
    is QuickLinkDestination.InnerHelpCenterDestination.FirstVet -> HedvigIcons.HelipadOutline
    is QuickLinkDestination.InnerHelpCenterDestination.QuickLinkSickAbroad -> HedvigIcons.HelipadOutline
    else -> HedvigIcons.Settings
  }
}
```
(Verify each icon exists under `design-system-hedvig/.../icon/`; `Card` and `HelipadOutline` do. Swap to a closer glyph if design differs; these are sensible defaults per the spec.)

- [ ] **Step 4: Add the Edit-insurance bottom sheet**

Add a `HedvigBottomSheetState<QuickAction.MultiSelectExpandedLink>` in `HomeScreen` (like the existing `crossSellBottomSheetState`), shown via `onEditInsurance = { editInsuranceSheetState.show(it) }`, rendering the sub-links as rows that call `onQuickLink(link.quickLinkDestination)` then dismiss. Follow the existing `CrossSellBottomSheet` pattern in this file for structure (a `HedvigBottomSheet` with a `Column` of clickable rows using `link.titleRes`).

- [ ] **Step 5: Update the section visibility + call site**

In the `visibleSections` filter, change `HomeSection.QuickActionTiles ->` to `uiState.quickActions.isNotEmpty()`. Update the `QuickActionTilesSection(...)` call in `HomeScreenSuccess` to pass `quickActions = uiState.quickActions`, `onQuickLink = navigateToQuickLink`, `onEditInsurance = { editInsuranceSheetState.show(it) }`. Remove the old `isEditInsuranceEnabled` / `isMovingEnabled` / `isTravelCertificateEnabled` params and the `onEditInsurance/onChangeAddress/onTravelCertificate` lambdas. Delete now-unused imports (`HOME_QUICK_ACTIONS_EDIT_INSURANCE`, `HOME_QUICK_ACTIONS_CHANGE_ADDRESS`, `HedvigIcons.Reload`/`Travel`/`Settings` stay if used by the icon map).

- [ ] **Step 6: Update previews**

In the two `PreviewHomeScreen*` `Success(...)` constructions, replace the removed booleans with `quickActions = listOf(/* a MultiSelectExpandedLink + two StandaloneQuickLinks */)` so the tiles render in preview.

- [ ] **Step 7: Compile + lint**

Run: `./gradlew :feature-home:compileDebugKotlin :app:compileDebugKotlin :feature-home:ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
./gradlew ktlintFormat -q
git add -A
git commit -m "feature-home: render quick-action tiles from the member's Help Center actions"
```

---

## Task 7: Full verification

- [ ] **Step 1: Build + test the affected modules**

Run: `./gradlew :member-quick-actions:jvmTest :feature-help-center:compileReleaseKotlin :feature-home:testDebugUnitTest :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL, all tests green.

- [ ] **Step 2: ktlint across touched modules**

Run: `./gradlew :member-quick-actions:ktlintCheck :feature-help-center:ktlintCheck :feature-home:ktlintCheck :app:ktlintCheck`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Manual smoke (optional)**

Use the `run` skill / install a debug build; verify Home shows up to three tiles matching the member's Help Center list, "Edit insurance" opens the sub-options sheet, other tiles navigate, and the section disappears when the member has no actions.

---

## Self-Review notes

- **Spec coverage:** shared module (Task 1-3), Help Center refactor + test move (Task 4), Home presenter + revert gating (Task 5), Home UI + tiles + Edit-insurance sheet + section-hidden-when-empty (Task 6), nav wiring via `toNavKey` (Task 3 + Task 6 Step 1), tests (Task 4 move, Task 5 add). FirstVet included (no exclusion) — icon-mapped and navigable (Task 3 note / Task 6 Step 1).
- **Known unknowns flagged inline:** exact import paths for a few nav keys and whether `FirstVetKey` is shareable (Task 3 Step 2 note); resolve by reading `HelpCenterPresenter.kt`'s current imports. Icon glyph choices are sensible defaults per the spec, adjustable to design.
- **Interface consistency:** `GetMemberQuickActionsUseCase.invoke(): Either<ErrorMessage, List<QuickAction>>`, `QuickAction` variants, `QuickLinkDestination.toNavKey()`, and `Success.quickActions: List<QuickAction>` are used consistently across Tasks 3-6.

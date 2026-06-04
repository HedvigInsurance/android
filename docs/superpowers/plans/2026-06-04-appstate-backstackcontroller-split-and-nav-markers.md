# HedvigAppState / BackstackController Split & Navigation Capability Markers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Clean up the messy seam between `HedvigAppState` (app-shell state, feature-aware) and `BackstackController` (pure navigation state machine), replace three centralized destination-classification lists with per-key marker interfaces, reinstate the lost "deliberate logout from Profile discards session" behavior, and replace the global-mutable `CurrentDestinationInMemoryStorage` with a DI-injected `StateFlow`-backed holder.

**Architecture:** `BackstackController` owns all pure-navigation reads/writes (entries, parked runs, logged-in detection, top-level selection, login/logout transitions). `HedvigAppState` keeps only feature-knowledge-layered state (navigation suite type, top-level graph set, payments badge, force-update gate, theme, cross-sell eligibility) and stops re-exposing controller methods as forwarders. Destination classification (cross-sell eligibility, chat-notification suppression, deliberate-logout origin) moves from centralized `List<KClass<…>>` registries into marker interfaces declared in `navigation-common` (the one module every `HedvigNavKey` already depends on — features can't depend on features). The current destination is published through a `@SingleIn(AppScope::class)` `CurrentDestinationHolder` exposing a `StateFlow<HedvigNavKey?>`, written by a dedicated `ReportCurrentDestinationEffect` and read by `ChatNotificationSender` via constructor injection.

**Tech Stack:** Kotlin, Jetpack Navigation 3, Jetpack Compose (Snapshot state), Metro DI (`@SingleIn`, `@Inject`, `@Provides`, `@IntoSet`), Kotlin Multiplatform (`navigation-common` commonMain), kotlinx.coroutines `StateFlow`, assertk + JUnit.

---

## File Structure

**Created:**
- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/CurrentDestinationHolder.kt` — `@SingleIn(AppScope::class)` holder exposing `StateFlow<HedvigNavKey?>`; the single source of "what is on top right now" for non-Composable consumers.

**Modified:**
- `app/navigation/navigation-common/src/commonMain/kotlin/com/hedvig/android/navigation/common/HedvigNavKey.kt` — add three marker interfaces.
- `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeDestinations.kt` — `HomeKey` implements two markers; delete `homeCrossSellBottomSheetPermittingDestinations`.
- `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsurancesNavigation.kt` — `InsurancesKey` + `InsuranceContractDetailKey` implement cross-sell marker; delete `insurancesCrossSellBottomSheetPermittingDestinations`.
- `app/feature/feature-travel-certificate/src/main/kotlin/com/hedvig/android/feature/travelcertificate/navigation/TravelCertificateDestination.kt` — `TravelCertificateKey` implements cross-sell marker; delete `travelCertificateCrossSellBottomSheetPermittingDestinations`.
- `app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/navigation/HelpCenterDestination.kt` — `HelpCenterKey` + `HelpCenterTopicKey` + `HelpCenterQuestionKey` implement cross-sell marker; delete `helpCenterCrossSellBottomSheetPermittingDestinations`.
- `app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/navigation/ChatDestination.kt` — `ChatKey` + `InboxKey` implement chat-suppression marker.
- `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinations.kt` — `ClaimDetailsKey` implements chat-suppression marker.
- `app/feature/feature-profile/src/main/kotlin/com/hedvig/android/feature/profile/navigation/ProfileDestinations.kt` — `ProfileKey` implements deliberate-logout marker; delete dead `destinationToExcludeFromSavingState`.
- `app/app/src/main/kotlin/com/hedvig/android/app/notification/senders/ChatNotificationSender.kt` — use marker check; remove `CurrentDestinationInMemoryStorage`; read injected holder.
- `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationMetroProviders.kt` — inject `CurrentDestinationHolder` into `provideChatNotificationSender`.
- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt` — `setLoggedOut` honors `DeliberateLogoutOrigin`.
- `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt` — adjust stash test; add deliberate-logout test.
- `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt` — cross-sell marker check; remove forwarders; remove the destination-mirroring `LaunchedEffect`.
- `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt` — drop `hedvigAppState` param; take `backstackController` + `windowSizeClass` directly.
- `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppUi.kt` — pass `backstackController` + `windowSizeClass` to `HedvigNavHost`; read top-level via controller.
- `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt` — effects call controller directly; add `ReportCurrentDestinationEffect`; thread `currentDestinationHolder`.
- `app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt` — inject `CurrentDestinationHolder`, pass to `HedvigApp`.

---

## Notes for the implementing engineer

- **This repo is highly modular.** A change in `navigation-common` (Task 1) recompiles every dependent module. After the marker interfaces exist, each feature key change (Tasks 2–4) is local to that feature module.
- **Verification commands:** `./gradlew :app:testDebugUnitTest` compiles the `:app` module **and its entire dependency graph** (all the feature modules touched here) and runs `BackstackControllerTest`. That is the single best gate for almost every task. Run `./gradlew ktlintFormat` before each commit and `./gradlew ktlintCheck` to confirm.
- **Gradle exit-code trap (known issue):** do NOT pipe gradle through `tail`/`grep` to read results — a pipe returns the pipe's exit code, not gradle's, so a `BUILD FAILED` can look like success. Run the gradle command bare and read its own `BUILD SUCCESSFUL` / `BUILD FAILED` line and exit status.
- **ktlint flags unused imports.** Every task that removes the last use of an import MUST also remove that import, or `ktlintCheck` fails. Exact import removals are spelled out per task.
- Commit after each task. Stay on the current branch `eng/metro-nav3-pr2-nav2-to-nav3`. Do NOT push or open a PR unless explicitly asked.

---

### Task 1: Add the three capability marker interfaces to `navigation-common`

**Files:**
- Modify: `app/navigation/navigation-common/src/commonMain/kotlin/com/hedvig/android/navigation/common/HedvigNavKey.kt`

- [ ] **Step 1: Add the marker interfaces**

Replace the entire file contents with:

```kotlin
package com.hedvig.android.navigation.common

import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KType

interface HedvigNavKey : NavKey

interface NavKeyTypeAware {
  val typeList: List<KType>
}

/**
 * A destination on which the cross-sell bottom sheet is allowed to appear after a member finishes a
 * flow (moving, edit co-insured, add/upgrade addon, change tier). Implemented by the screens a member
 * lands on at the end of those flows. Replaces the old per-feature
 * `xxxCrossSellBottomSheetPermittingDestinations` lists.
 */
interface CrossSellEligibleDestination

/**
 * A destination where an incoming chat push notification must be suppressed (the in-app screen shows
 * the new message itself). Replaces `listOfDestinationsWhichShouldNotShowChatNotification`.
 */
interface SuppressesChatPushNotification

/**
 * A destination from which reaching the logged-out state is treated as a deliberate "log me out now"
 * action, so the session is discarded rather than stashed for a same-member restore. Restoring the
 * nav back to this screen after a fresh login would be wrong. Replaces the dead
 * `destinationToExcludeFromSavingState`.
 */
interface DeliberateLogoutOrigin
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :navigation-common:compileKotlinMetadata`
Expected: `BUILD SUCCESSFUL`. (If this task path is unavailable in the multiplatform setup, fall back to `./gradlew :app:compileDebugKotlin` which transitively compiles `navigation-common`.)

- [ ] **Step 3: ktlint**

Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/navigation/navigation-common/src/commonMain/kotlin/com/hedvig/android/navigation/common/HedvigNavKey.kt
git commit -m "$(cat <<'EOF'
feat(nav): add capability marker interfaces in navigation-common

Adds CrossSellEligibleDestination, SuppressesChatPushNotification and
DeliberateLogoutOrigin markers to replace centralized destination
classification lists.
EOF
)"
```

---

### Task 2: Convert cross-sell eligibility to `CrossSellEligibleDestination`

**Files:**
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeDestinations.kt`
- Modify: `app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsurancesNavigation.kt`
- Modify: `app/feature/feature-travel-certificate/src/main/kotlin/com/hedvig/android/feature/travelcertificate/navigation/TravelCertificateDestination.kt`
- Modify: `app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/navigation/HelpCenterDestination.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt`

- [ ] **Step 1: Mark `HomeKey` and delete the home list**

In `HomeDestinations.kt`, change the `HomeKey` declaration and delete the trailing val. Final file:

```kotlin
package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : HedvigNavKey, CrossSellEligibleDestination

@Serializable
internal data class FirstVetKey(val sections: List<FirstVetSection>) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
  }
}
```

Note: the `KClass` import is removed (the deleted val was its only use).

- [ ] **Step 2: Mark insurances keys and delete the insurances list**

In `InsurancesNavigation.kt`, final file:

```kotlin
package com.hedvig.android.feature.insurances.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.DeepLinkAncestry
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object InsurancesKey : HedvigNavKey, CrossSellEligibleDestination

@Serializable
internal data class InsuranceContractDetailKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.contract] */
  @SerialName("contractId")
  val contractId: String,
) : HedvigNavKey, DeepLinkAncestry, CrossSellEligibleDestination {
  override val owningTab = TopLevelGraph.Insurances
  override val syntheticParents = emptyList<HedvigNavKey>()
}

@Serializable
internal data object TerminatedInsurancesKey : HedvigNavKey
```

Note: the `kotlin.reflect.KClass` import is removed.

- [ ] **Step 3: Mark `TravelCertificateKey` and delete the travel-certificate list**

In `TravelCertificateDestination.kt`: change line 12-13 and delete lines 48-50. Apply:

```kotlin
@Serializable
data object TravelCertificateKey : HedvigNavKey, CrossSellEligibleDestination
```

Delete:

```kotlin
val travelCertificateCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  TravelCertificateKey::class,
)
```

Then remove the now-unused `import kotlin.reflect.KClass` and add `import com.hedvig.android.navigation.common.CrossSellEligibleDestination`. (`kotlin.reflect.KType` and `kotlin.reflect.typeOf` stay — still used by `TravelCertificateTravellersInputKey` / `ShowCertificateKey`.)

- [ ] **Step 4: Mark help-center keys and delete the help-center list**

In `HelpCenterDestination.kt`: add the marker to the three keys and delete the val. Apply these three edits:

```kotlin
@Serializable
data object HelpCenterKey : HedvigNavKey, CrossSellEligibleDestination
```

```kotlin
@Serializable
internal data class HelpCenterTopicKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
  @SerialName("id")
  val topicId: String = "",
) : HedvigNavKey, CrossSellEligibleDestination
```

```kotlin
@Serializable
internal data class HelpCenterQuestionKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
  @SerialName("id")
  val questionId: String = "",
) : HedvigNavKey, CrossSellEligibleDestination
```

Delete:

```kotlin
val helpCenterCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  HelpCenterKey::class,
  HelpCenterTopicKey::class,
  HelpCenterQuestionKey::class,
)
```

Then remove the now-unused `import kotlin.reflect.KClass` and add `import com.hedvig.android.navigation.common.CrossSellEligibleDestination`. (`KType`/`typeOf` stay — used by `EmergencyKey`/`FirstVetKey`.)

- [ ] **Step 5: Switch `HedvigAppState` cross-sell check to the marker**

In `HedvigAppState.kt`:

Remove these four imports (lines 20-23):

```kotlin
import com.hedvig.android.feature.help.center.navigation.helpCenterCrossSellBottomSheetPermittingDestinations
import com.hedvig.android.feature.home.home.navigation.homeCrossSellBottomSheetPermittingDestinations
import com.hedvig.android.feature.insurances.navigation.insurancesCrossSellBottomSheetPermittingDestinations
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateCrossSellBottomSheetPermittingDestinations
```

Add this import (keep alphabetical ordering in the `com.hedvig.android.navigation.common` group):

```kotlin
import com.hedvig.android.navigation.common.CrossSellEligibleDestination
```

Remove the now-unused `import kotlin.reflect.KClass` (line 32).

Replace the `isInScreenEligibleForCrossSells` getter (lines 115-119):

```kotlin
  val isInScreenEligibleForCrossSells: Boolean
    get() {
      val destination = currentDestination ?: return false
      return crossSellBottomSheetPermittingDestinations.any { it.isInstance(destination) }
    }
```

with:

```kotlin
  val isInScreenEligibleForCrossSells: Boolean
    get() = currentDestination is CrossSellEligibleDestination
```

Delete the entire private val + its KDoc at the bottom of the file (lines 183-197):

```kotlin
/**
 * Destinations that must show the cross-sell bottom sheet after finishing some flow
 */
private val crossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = buildList {
  // Screens that a member will end up in after finishing any of the following flows
  // 1. Moving flow
  // 2. Edit co-insured
  // 3. Add/Upgrade addon
  // 4. Change tier
  addAll(insurancesCrossSellBottomSheetPermittingDestinations)
  addAll(helpCenterCrossSellBottomSheetPermittingDestinations)
  addAll(travelCertificateCrossSellBottomSheetPermittingDestinations)
  // One could finish those flows after a deep link, so the app's start destination must also be included
  addAll(homeCrossSellBottomSheetPermittingDestinations)
}
```

Note: `HedvigNavKey` import stays — still used by the `currentDestination` getter (removed later in Task 7).

- [ ] **Step 6: Build + test**

Run: `./gradlew :app:testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, existing tests pass.

- [ ] **Step 7: ktlint**

Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeDestinations.kt app/feature/feature-insurances/src/main/kotlin/com/hedvig/android/feature/insurances/navigation/InsurancesNavigation.kt app/feature/feature-travel-certificate/src/main/kotlin/com/hedvig/android/feature/travelcertificate/navigation/TravelCertificateDestination.kt app/feature/feature-help-center/src/commonMain/kotlin/com/hedvig/android/feature/help/center/navigation/HelpCenterDestination.kt app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt
git commit -m "$(cat <<'EOF'
refactor(nav): mark cross-sell-eligible destinations via marker interface

Replaces the four per-feature CrossSellBottomSheetPermittingDestinations
lists and HedvigAppState's aggregating buildList with a
CrossSellEligibleDestination marker implemented per key.
EOF
)"
```

---

### Task 3: Convert chat-notification suppression to `SuppressesChatPushNotification`

**Files:**
- Modify: `app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/navigation/ChatDestination.kt`
- Modify: `app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinations.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeDestinations.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/notification/senders/ChatNotificationSender.kt`

- [ ] **Step 1: Mark chat keys**

In `ChatDestination.kt`, final file:

```kotlin
package com.hedvig.android.feature.chat.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
import kotlinx.serialization.Serializable

@Serializable
data object InboxKey : HedvigNavKey, SuppressesChatPushNotification

@Serializable
data class ChatKey(
  val conversationId: String,
) : HedvigNavKey, SuppressesChatPushNotification
```

- [ ] **Step 2: Mark `ClaimDetailsKey`**

In `ClaimDetailDestinations.kt`, add the import and the marker to `ClaimDetailsKey` only (NOT `AddFilesKey`):

```kotlin
package com.hedvig.android.feature.claim.details.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimDetailsKey(
  /**
   * The ID to the claim. Must match the name of the param inside in HedvigDeepLinkContainer
   */
  @SerialName("claimId")
  val claimId: String,
) : HedvigNavKey, SuppressesChatPushNotification

@Serializable
internal data class AddFilesKey(
  val targetUploadUrl: String,
  val initialFilesUri: List<String>,
) : HedvigNavKey
```

- [ ] **Step 3: Add the chat-suppression marker to `HomeKey`**

In `HomeDestinations.kt`, `HomeKey` already implements `CrossSellEligibleDestination` from Task 2. Add the second marker and its import. Change:

```kotlin
import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
```

to:

```kotlin
import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
```

and change:

```kotlin
@Serializable
data object HomeKey : HedvigNavKey, CrossSellEligibleDestination
```

to:

```kotlin
@Serializable
data object HomeKey : HedvigNavKey, CrossSellEligibleDestination, SuppressesChatPushNotification
```

- [ ] **Step 4: Switch `ChatNotificationSender` suppression check to the marker**

In `ChatNotificationSender.kt`:

Remove these four feature-key imports (lines 23-26):

```kotlin
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
```

Add (in the `com.hedvig.android.navigation.common` import group):

```kotlin
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
```

Delete the `listOfDestinationsWhichShouldNotShowChatNotification` val (lines 50-55):

```kotlin
private val listOfDestinationsWhichShouldNotShowChatNotification = setOf(
  ChatKey::class,
  InboxKey::class,
  HomeKey::class,
  ClaimDetailsKey::class,
)
```

Replace the suppression-check block inside `sendNotification` (lines 68-72):

```kotlin
    val currentDestination = CurrentDestinationInMemoryStorage.currentDestination
    val currentlyOnDestinationWhichForbidsShowingChatNotification =
      listOfDestinationsWhichShouldNotShowChatNotification.any { clazz ->
        clazz.isInstance(currentDestination)
      }
```

with:

```kotlin
    val currentDestination = CurrentDestinationInMemoryStorage.currentDestination
    val currentlyOnDestinationWhichForbidsShowingChatNotification =
      currentDestination is SuppressesChatPushNotification
```

(`CurrentDestinationInMemoryStorage` is still used here — it is replaced in Task 6. The `HedvigNavKey` import stays — still used by the `CurrentDestinationInMemoryStorage` object declaration.)

- [ ] **Step 5: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: ktlint**

Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/feature/feature-chat/src/main/kotlin/com/hedvig/android/feature/chat/navigation/ChatDestination.kt app/feature/feature-claim-details/src/main/kotlin/com/hedvig/android/feature/claim/details/navigation/ClaimDetailDestinations.kt app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/navigation/HomeDestinations.kt app/app/src/main/kotlin/com/hedvig/android/app/notification/senders/ChatNotificationSender.kt
git commit -m "$(cat <<'EOF'
refactor(nav): suppress chat notifications via marker interface

Replaces listOfDestinationsWhichShouldNotShowChatNotification with a
SuppressesChatPushNotification marker on ChatKey, InboxKey, HomeKey and
ClaimDetailsKey.
EOF
)"
```

---

### Task 4: Mark `ProfileKey` as `DeliberateLogoutOrigin` and delete dead code

**Files:**
- Modify: `app/feature/feature-profile/src/main/kotlin/com/hedvig/android/feature/profile/navigation/ProfileDestinations.kt`

- [ ] **Step 1: Add the marker and delete the dead val**

Final file:

```kotlin
package com.hedvig.android.feature.profile.navigation

import com.hedvig.android.navigation.common.DeliberateLogoutOrigin
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProfileKey : HedvigNavKey, DeliberateLogoutOrigin

@Serializable
data object ContactInfoKey : HedvigNavKey

@Serializable
internal data object EurobonusKey : HedvigNavKey

@Serializable
internal data object CertificatesKey : HedvigNavKey

@Serializable
internal data object InformationKey : HedvigNavKey

@Serializable
internal data object LicensesKey : HedvigNavKey

@Serializable
internal data object SettingsKey : HedvigNavKey
```

This adds the `DeliberateLogoutOrigin` import + marker, and removes both the `import kotlin.reflect.KClass` and the dead `destinationToExcludeFromSavingState` val (verified to have zero references outside its own declaration).

- [ ] **Step 2: Build**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: ktlint**

Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/feature/feature-profile/src/main/kotlin/com/hedvig/android/feature/profile/navigation/ProfileDestinations.kt
git commit -m "$(cat <<'EOF'
refactor(nav): mark ProfileKey as DeliberateLogoutOrigin

Adds the marker and removes the dead destinationToExcludeFromSavingState
val it replaces.
EOF
)"
```

---

### Task 5: Reinstate "deliberate logout from Profile discards the session" in `setLoggedOut`

This is the lost Nav2 feature: logging out while on Profile is the normal "I deliberately log out now" action, so the session must NOT be stashed for a same-member restore (restoring the nav back to Profile after a fresh login is wrong).

**Files:**
- Modify: `app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt`

- [ ] **Step 1: Adjust the existing stash test so it no longer logs out from a deliberate-logout origin**

The existing test `setLoggedOut stashes the live session tagged with the member id` (lines 157-169) currently navigates to Profile and expects a stash — that contradicts the new behavior. Change it to land on a non-deliberate-origin destination (Payments) while still exercising parked-run capture. Replace the test body:

```kotlin
  @Test
  fun `setLoggedOut stashes the live session tagged with the member id`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Profile) // park Insurances run, render Profile root
    controller.setLoggedOut("mem-1")
    assertThat(controller.entries.toList()).containsExactly(LoginKey)
    assertThat(controller.parkedRuns).isEmpty()
    val stash = controller.stashedSession!!
    assertThat(stash.memberId).isEqualTo("mem-1")
    assertThat(stash.entries).containsExactly(HomeKey, ProfileKey)
    assertThat(stash.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }
```

with:

```kotlin
  @Test
  fun `setLoggedOut stashes the live session tagged with the member id`() {
    val controller = controllerWith(HomeKey, InsurancesKey, HelpCenterKey)
    controller.selectTopLevel(TopLevelGraph.Payments) // park Insurances run, render Payments root
    controller.setLoggedOut("mem-1")
    assertThat(controller.entries.toList()).containsExactly(LoginKey)
    assertThat(controller.parkedRuns).isEmpty()
    val stash = controller.stashedSession!!
    assertThat(stash.memberId).isEqualTo("mem-1")
    assertThat(stash.entries).containsExactly(HomeKey, PaymentsKey)
    assertThat(stash.parkedRuns[TopLevelGraph.Insurances])
      .isEqualTo(listOf(InsurancesKey, HelpCenterKey))
  }
```

(`PaymentsKey` is already imported at line 17.)

- [ ] **Step 2: Add the new failing test**

Add this test immediately after the test edited in Step 1:

```kotlin
  @Test
  fun `setLoggedOut from a deliberate-logout origin stashes nothing even with a member id`() {
    val controller = controllerWith(HomeKey, ProfileKey)
    controller.setLoggedOut("mem-1")
    assertThat(controller.entries.toList()).containsExactly(LoginKey)
    assertThat(controller.parkedRuns).isEmpty()
    assertThat(controller.stashedSession).isEqualTo(null)
  }
```

- [ ] **Step 3: Run tests to verify the new test fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"`
Expected: the Step-1 test PASSES (lands on Payments, still stashes); the Step-2 test FAILS — `stashedSession` is non-null because `setLoggedOut` currently always stashes when `memberId != null`.

- [ ] **Step 4: Implement the behavior in `setLoggedOut`**

In `BackstackController.kt`, add the import (in the `com.hedvig.android.navigation.common` group, after the `HedvigNavKey` import on line 23):

```kotlin
import com.hedvig.android.navigation.common.DeliberateLogoutOrigin
```

Replace `setLoggedOut` (lines 260-270):

```kotlin
  fun setLoggedOut(memberId: String?) {
    Snapshot.withMutableSnapshot {
      stashedSession = if (memberId != null) {
        StashedSession(memberId, entries.toList(), parkedRuns.toMap())
      } else {
        null
      }
      parkedRuns.clear()
      entries.replaceWith(listOf(LoginKey))
    }
  }
```

with:

```kotlin
  fun setLoggedOut(memberId: String?) {
    Snapshot.withMutableSnapshot {
      val isDeliberateLogout = entries.lastOrNull() is DeliberateLogoutOrigin
      stashedSession = if (memberId != null && !isDeliberateLogout) {
        StashedSession(memberId, entries.toList(), parkedRuns.toMap())
      } else {
        null
      }
      parkedRuns.clear()
      entries.replaceWith(listOf(LoginKey))
    }
  }
```

Also update the KDoc above `setLoggedOut` (lines 254-259) to document the new case. Replace:

```kotlin
  /**
   * Drop to the login root. Stashes the live session (tagged with [memberId]) so a same-member
   * re-login can restore the history; the stash is excluded from [allLiveContentKeys], so the
   * decorators dispose every key's per-entry state while it waits. A null [memberId] (demo mode /
   * unknown identity) stashes nothing — that session can never be safely restored.
   */
```

with:

```kotlin
  /**
   * Drop to the login root. Stashes the live session (tagged with [memberId]) so a same-member
   * re-login can restore the history; the stash is excluded from [allLiveContentKeys], so the
   * decorators dispose every key's per-entry state while it waits. A null [memberId] (demo mode /
   * unknown identity) stashes nothing — that session can never be safely restored. Logging out while
   * the top destination is a [DeliberateLogoutOrigin] (Profile) is treated as an intentional sign-out,
   * so nothing is stashed even with a known [memberId] — restoring the member onto that screen would
   * be wrong.
   */
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.hedvig.android.app.navigation.BackstackControllerTest"`
Expected: PASS (both the edited stash test and the new deliberate-logout test).

- [ ] **Step 6: ktlint**

Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/BackstackController.kt app/app/src/test/kotlin/com/hedvig/android/app/navigation/BackstackControllerTest.kt
git commit -m "$(cat <<'EOF'
feat(nav): discard session on deliberate logout from Profile

setLoggedOut no longer stashes the session for a same-member restore when
the top destination is a DeliberateLogoutOrigin, reinstating the Nav2
behavior lost in the Nav3 migration.
EOF
)"
```

---

### Task 6: Replace `CurrentDestinationInMemoryStorage` with an injected `CurrentDestinationHolder`

Goal: kill the global mutable `object` (written from a Composable effect, read off the FCM/binder thread) and replace it with a `@SingleIn(AppScope::class)` holder exposing a thread-safe `StateFlow`, written by a dedicated effect and read via constructor injection.

**Files:**
- Create: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/CurrentDestinationHolder.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/notification/senders/ChatNotificationSender.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationMetroProviders.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt`

- [ ] **Step 1: Create the holder**

Create `app/app/src/main/kotlin/com/hedvig/android/app/navigation/CurrentDestinationHolder.kt`:

```kotlin
package com.hedvig.android.app.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-scoped source of truth for the destination currently on top of the rendered stack, published
 * as a [StateFlow] so non-Composable consumers (e.g. [com.hedvig.android.app.notification.senders.ChatNotificationSender],
 * which runs on the FCM/binder thread) can read it safely. Written by `ReportCurrentDestinationEffect`.
 *
 * This is intentionally non-persistent: a process death wipes it, which is the desired behavior —
 * the suppression it powers only matters while the app is resumed, and over-showing a notification is
 * preferable to wrongly hiding one.
 */
@SingleIn(AppScope::class)
@Inject
class CurrentDestinationHolder {
  private val currentDestinationState = MutableStateFlow<HedvigNavKey?>(null)
  val currentDestination: StateFlow<HedvigNavKey?> = currentDestinationState.asStateFlow()

  fun update(destination: HedvigNavKey?) {
    currentDestinationState.value = destination
  }
}
```

- [ ] **Step 2: Make `ChatNotificationSender` read the injected holder; remove the global object**

In `ChatNotificationSender.kt`:

Delete the `CurrentDestinationInMemoryStorage` object and its KDoc (lines 37-48):

```kotlin
/**
 * An in-memory storage of the current route, used to *not* show the chat notification if we are in a select list of
 * screens where we do not want to show the system notification, but we want to let the in-app screen indicate that
 * there is a new message.
 * This is not persistent storage, and will just be wiped in scenarios like the process being killed, but this is part
 * of what we want, since we only care to do this if the app is resumed anyway. On top of this, we'd rather experience
 * cases where we show the notification when we shouldn't rather than cases where we do not show the notification even
 * thought we should.
 */
object CurrentDestinationInMemoryStorage {
  var currentDestination: HedvigNavKey? = null
}
```

Add the holder as a constructor parameter. Change the class header:

```kotlin
class ChatNotificationSender(
  private val context: Context,
  private val permissionManager: PermissionManager,
  private val buildConstants: HedvigBuildConstants,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
) : NotificationSender {
```

to:

```kotlin
class ChatNotificationSender(
  private val context: Context,
  private val permissionManager: PermissionManager,
  private val buildConstants: HedvigBuildConstants,
  private val hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  private val notificationChannel: HedvigNotificationChannel,
  private val currentDestinationHolder: CurrentDestinationHolder,
) : NotificationSender {
```

Change the read inside `sendNotification`:

```kotlin
    val currentDestination = CurrentDestinationInMemoryStorage.currentDestination
```

to:

```kotlin
    val currentDestination = currentDestinationHolder.currentDestination.value
```

Add the import:

```kotlin
import com.hedvig.android.app.navigation.CurrentDestinationHolder
```

The `HedvigNavKey` import is now unused here (the object that referenced it is gone, and `is SuppressesChatPushNotification` does not name it) — remove `import com.hedvig.android.navigation.common.HedvigNavKey`.

- [ ] **Step 3: Inject the holder into the Metro provider**

In `ApplicationMetroProviders.kt`, replace `provideChatNotificationSender` (lines 210-224):

```kotlin
  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideChatNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
  ): NotificationSender = ChatNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Chat,
  )
```

with:

```kotlin
  @Provides
  @SingleIn(AppScope::class)
  @IntoSet
  fun provideChatNotificationSender(
    applicationContext: Context,
    permissionManager: PermissionManager,
    buildConstants: HedvigBuildConstants,
    deepLinkContainer: HedvigDeepLinkContainer,
    currentDestinationHolder: CurrentDestinationHolder,
  ): NotificationSender = ChatNotificationSender(
    applicationContext,
    permissionManager,
    buildConstants,
    deepLinkContainer,
    HedvigNotificationChannel.Chat,
    currentDestinationHolder,
  )
```

Add the import (alphabetical, in the `com.hedvig.android.app.navigation` group):

```kotlin
import com.hedvig.android.app.navigation.CurrentDestinationHolder
```

- [ ] **Step 4: Remove the destination-mirroring effect from `rememberHedvigAppState`**

In `HedvigAppState.kt`, delete the `LaunchedEffect` block (lines 69-74):

```kotlin
  LaunchedEffect(appState) {
    snapshotFlow { appState.currentDestination }.collect { destination ->
      logcat { "Navigated to destination:$destination" }
      CurrentDestinationInMemoryStorage.currentDestination = destination
    }
  }
```

so `rememberHedvigAppState` ends with `return appState` directly after the `remember { … }` block.

Remove the now-unused imports:

```kotlin
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.hedvig.android.app.notification.senders.CurrentDestinationInMemoryStorage
import com.hedvig.android.logger.logcat
```

(`collectAsState` and `getValue` stay — used by `darkTheme`.)

- [ ] **Step 5: Add `ReportCurrentDestinationEffect` and thread the holder through `HedvigApp`**

In `HedvigApp.kt`:

Add the holder parameter to the `HedvigApp` signature, after `missedPaymentNotificationServiceProvider` (line 93):

```kotlin
  missedPaymentNotificationServiceProvider: Provider<MissedPaymentNotificationService>,
  currentDestinationHolder: CurrentDestinationHolder,
  dismissSplashScreen: () -> Unit,
```

Add a call to the new effect right after `rememberHedvigAppState(...)` returns (immediately after line 103's closing `)` of the `rememberHedvigAppState` call, before `val lastKnownMemberId`):

```kotlin
  ReportCurrentDestinationEffect(backstackController, currentDestinationHolder)
```

Add the effect as a private composable (place it next to the other private effects, e.g. after `DetermineStartDestinationEffect`):

```kotlin
/**
 * Mirrors the current top destination into the app-scoped [CurrentDestinationHolder] so non-Composable
 * consumers (chat-notification suppression) can read it. Replaces the old LaunchedEffect that wrote the
 * global CurrentDestinationInMemoryStorage object.
 */
@Composable
private fun ReportCurrentDestinationEffect(
  backstackController: BackstackController,
  currentDestinationHolder: CurrentDestinationHolder,
) {
  LaunchedEffect(backstackController, currentDestinationHolder) {
    snapshotFlow { backstackController.currentDestination }.collect { destination ->
      logcat { "Navigated to destination:$destination" }
      currentDestinationHolder.update(destination)
    }
  }
}
```

Add the import:

```kotlin
import com.hedvig.android.app.navigation.CurrentDestinationHolder
```

(`LaunchedEffect`, `snapshotFlow`, `logcat`, `Composable` are all already imported in `HedvigApp.kt`.)

- [ ] **Step 6: Inject the holder in `MainActivity` and pass it to `HedvigApp`**

In `MainActivity.kt`:

Add the injected field next to the other `@Inject` fields (e.g. after `missedPaymentNotificationServiceProvider`, line 96):

```kotlin
  @Inject private lateinit var currentDestinationHolder: CurrentDestinationHolder
```

Pass it to `HedvigApp` (after `missedPaymentNotificationServiceProvider = …`, line 204):

```kotlin
          missedPaymentNotificationServiceProvider = missedPaymentNotificationServiceProvider,
          currentDestinationHolder = currentDestinationHolder,
          dismissSplashScreen = { showSplash.update { false } },
```

Add the import:

```kotlin
import com.hedvig.android.app.navigation.CurrentDestinationHolder
```

- [ ] **Step 7: Build + test**

Run: `./gradlew :app:testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, all tests pass.

- [ ] **Step 8: ktlint**

Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/navigation/CurrentDestinationHolder.kt app/app/src/main/kotlin/com/hedvig/android/app/notification/senders/ChatNotificationSender.kt app/app/src/main/kotlin/com/hedvig/android/app/di/ApplicationMetroProviders.kt app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt app/app/src/main/kotlin/com/hedvig/android/app/MainActivity.kt
git commit -m "$(cat <<'EOF'
refactor(nav): replace global current-destination storage with injected holder

Introduces a @SingleIn(AppScope) CurrentDestinationHolder exposing a
StateFlow, written by ReportCurrentDestinationEffect and injected into
ChatNotificationSender, removing the global-mutable
CurrentDestinationInMemoryStorage object.
EOF
)"
```

---

### Task 7: Split the seam — remove `HedvigAppState` forwarders, thread the controller into `HedvigNavHost`

After this task, `HedvigAppState` exposes only feature-knowledge state plus the `backstackController` reference (the bridge). All pure-navigation calls go straight to the controller.

**Files:**
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppUi.kt`
- Modify: `app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt`

- [ ] **Step 1: Change `HedvigNavHost` to take the controller + windowSizeClass directly**

In `HedvigNavHost.kt`:

Change the signature (lines 106-122). Replace:

```kotlin
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  memberIdService: MemberIdService,
```

with:

```kotlin
internal fun HedvigNavHost(
  backstackController: BackstackController,
  windowSizeClass: WindowSizeClass,
  memberIdService: MemberIdService,
```

Replace the alias line (line 123):

```kotlin
  val backstack = hedvigAppState.backstackController
```

with:

```kotlin
  val backstack = backstackController
```

Now replace every remaining `hedvigAppState.backstackController` with `backstackController`, every `hedvigAppState.windowSizeClass` with `windowSizeClass`, every `hedvigAppState.navigateToTopLevelGraph(...)` with `backstackController.selectTopLevel(...)`, and the `hedvigAppState.navigateToLoggedIn(...)` call with `backstackController.setLoggedIn(...)`. Concretely:

- Line 143: `val retainedContentKeys = { hedvigAppState.backstackController.allLiveContentKeys }` → `val retainedContentKeys = { backstackController.allLiveContentKeys }`
- Line 154: `backStack = hedvigAppState.backstackController.entries,` → `backStack = backstackController.entries,`
- Lines 156-159 (`onBack`): `if (!hedvigAppState.backstackController.handleBack()) {` → `if (!backstackController.handleBack()) {`
- Line 177: `backstack = hedvigAppState.backstackController,` (loginGraph) → `backstack = backstackController,`
- Lines 182-186 (`onNavigateToLoggedIn`):

  ```kotlin
        onNavigateToLoggedIn = {
          scope.launch {
            hedvigAppState.navigateToLoggedIn(memberIdService.getMemberId().first())
          }
        },
  ```

  →

  ```kotlin
        onNavigateToLoggedIn = {
          scope.launch {
            backstackController.setLoggedIn(memberIdService.getMemberId().first())
          }
        },
  ```
- Line 191: `backstack = hedvigAppState.backstackController,` (nestedHomeGraphs) → `backstack = backstackController,`
- Line 202: `backstack = hedvigAppState.backstackController,` (homeGraph) → `backstack = backstackController,`
- Line 228: `windowSizeClass = hedvigAppState.windowSizeClass,` → `windowSizeClass = windowSizeClass,`
- Line 229: `backstack = hedvigAppState.backstackController,` (terminateInsuranceGraph) → `backstack = backstackController,`
- Lines 233-236 (`navigateToInsurances`):

  ```kotlin
            navigateToInsurances = {
              backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
              hedvigAppState.navigateToTopLevelGraph(TopLevelGraph.Insurances)
            },
  ```

  →

  ```kotlin
            navigateToInsurances = {
              backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
              backstackController.selectTopLevel(TopLevelGraph.Insurances)
            },
  ```
- Line 265: `backstack = hedvigAppState.backstackController,` (insuranceGraph) → `backstack = backstackController,`
- Line 317: `backstack = hedvigAppState.backstackController,` (paymentsGraph) → `backstack = backstackController,`
- Line 325: `backstack = hedvigAppState.backstackController,` (payoutAccountGraph) → `backstack = backstackController,`
- Line 332: `deleteAccountGraph(hedvigAppState.backstackController)` → `deleteAccountGraph(backstackController)`
- Line 341: `backstack = hedvigAppState.backstackController,` (profileGraph) → `backstack = backstackController,`
- Line 369: `backstack = hedvigAppState.backstackController,` (cbmChatGraph) → `backstack = backstackController,`
- Line 372: `backstack = hedvigAppState.backstackController,` (addonPurchaseNavGraph) → `backstack = backstackController,`
- Line 381: `backstack = hedvigAppState.backstackController,` (changeTierGraph) → `backstack = backstackController,`
- Line 385: `backstack = hedvigAppState.backstackController,` (chipIdGraph) → `backstack = backstackController,`
- Lines 389-392 (`goHome`):

  ```kotlin
        goHome = {
          backstack.popUpTo<ChipIdKey>(inclusive = true)
          hedvigAppState.navigateToTopLevelGraph(TopLevelGraph.Home)
        },
  ```

  →

  ```kotlin
        goHome = {
          backstack.popUpTo<ChipIdKey>(inclusive = true)
          backstackController.selectTopLevel(TopLevelGraph.Home)
        },
  ```
- Line 395: `backstack = hedvigAppState.backstackController,` (movingFlowGraph) → `backstack = backstackController,`
- Line 398: `connectPaymentGraph(backstack = hedvigAppState.backstackController)` → `connectPaymentGraph(backstack = backstackController)`
- Line 399: `editCoInsuredGraph(hedvigAppState.backstackController)` → `editCoInsuredGraph(backstackController)`
- Line 401: `backstack = hedvigAppState.backstackController,` (helpCenterGraph) → `backstack = backstackController,`
- Line 458: `imageViewerGraph(hedvigAppState.backstackController, imageLoader)` → `imageViewerGraph(backstackController, imageLoader)`
- Line 459: `removeAddonsNavGraph(backstack = hedvigAppState.backstackController)` → `removeAddonsNavGraph(backstack = backstackController)`

After these replacements there must be zero remaining `hedvigAppState` references in the file. Verify with: `grep -n "hedvigAppState" app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt` → no matches.

Fix imports: remove `import com.hedvig.android.app.ui.HedvigAppState` (line 20). Add `import androidx.compose.material3.windowsizeclass.WindowSizeClass` (alphabetical within the `androidx.compose` group). `BackstackController` is in the same package — no import needed.

- [ ] **Step 2: Update `HedvigAppUi` to read top-level via the controller and pass new args to `HedvigNavHost`**

In `HedvigAppUi.kt`:

Replace line 70:

```kotlin
          currentTopLevelGraph = hedvigAppState.currentTopLevelGraph,
```

with:

```kotlin
          currentTopLevelGraph = hedvigAppState.backstackController.currentTopLevel,
```

Replace line 71:

```kotlin
          onNavigateToTopLevelGraph = hedvigAppState::navigateToTopLevelGraph,
```

with:

```kotlin
          onNavigateToTopLevelGraph = hedvigAppState.backstackController::selectTopLevel,
```

Replace the `HedvigNavHost(...)` call (lines 95-110). Replace:

```kotlin
        HedvigNavHost(
          hedvigAppState = hedvigAppState,
          memberIdService = memberIdService,
```

with:

```kotlin
        HedvigNavHost(
          backstackController = hedvigAppState.backstackController,
          windowSizeClass = hedvigAppState.windowSizeClass,
          memberIdService = memberIdService,
```

(`hedvigAppState.backstackController.navigateUp()` on line 82 and `hedvigAppState.backstackController.loneDeepLinkChrome` on line 85 stay as-is — they already go through the controller.)

- [ ] **Step 3: Update the `HedvigApp` effects to call the controller directly**

In `HedvigApp.kt`:

`DetermineStartDestinationEffect` already receives `backstackController`. Replace its callback args (lines 115-116):

```kotlin
    onLoggedIn = { memberId -> hedvigAppState.navigateToLoggedIn(memberId) },
    onLoggedOut = { hedvigAppState.navigateToLoggedOut(lastKnownMemberId.value) },
```

with:

```kotlin
    onLoggedIn = { memberId -> backstackController.setLoggedIn(memberId) },
    onLoggedOut = { backstackController.setLoggedOut(lastKnownMemberId.value) },
```

Change the `LogoutOnInvalidCredentialsEffect` call site (lines 133-138):

```kotlin
      LogoutOnInvalidCredentialsEffect(
        hedvigAppState,
        authTokenService,
        demoManager,
        lastKnownMemberId = { lastKnownMemberId.value },
      )
```

with:

```kotlin
      LogoutOnInvalidCredentialsEffect(
        backstackController,
        authTokenService,
        demoManager,
        lastKnownMemberId = { lastKnownMemberId.value },
      )
```

Change the `LogoutOnInvalidCredentialsEffect` definition (lines 303-347). Replace its signature parameter and the two body references. Replace:

```kotlin
private fun LogoutOnInvalidCredentialsEffect(
  hedvigAppState: HedvigAppState,
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
  lastKnownMemberId: () -> String?,
) {
```

with:

```kotlin
private fun LogoutOnInvalidCredentialsEffect(
  backstackController: BackstackController,
  authTokenService: AuthTokenService,
  demoManager: DemoManager,
  lastKnownMemberId: () -> String?,
) {
```

Replace (inside that effect) line 325:

```kotlin
  LaunchedEffect(lifecycle, hedvigAppState, authTokenService, demoManager) {
```

with:

```kotlin
  LaunchedEffect(lifecycle, backstackController, authTokenService, demoManager) {
```

Replace line 330:

```kotlin
        snapshotFlow { hedvigAppState.backstackController.isLoggedIn },
```

with:

```kotlin
        snapshotFlow { backstackController.isLoggedIn },
```

Replace line 342:

```kotlin
          hedvigAppState.navigateToLoggedOut(lastKnownMemberId())
```

with:

```kotlin
          backstackController.setLoggedOut(lastKnownMemberId())
```

- [ ] **Step 4: Remove the forwarders from `HedvigAppState`**

In `HedvigAppState.kt`:

Delete the `currentDestination` getter (lines 88-89):

```kotlin
  val currentDestination: HedvigNavKey?
    get() = backstackController.currentDestination
```

Delete the `currentTopLevelGraph` getter (lines 91-92):

```kotlin
  val currentTopLevelGraph: TopLevelGraph
    get() = backstackController.currentTopLevel
```

Delete the three navigation methods (lines 155-169):

```kotlin
  /**
   * Navigate to a top level destination. Selecting the current tab again pops it back to its start;
   * selecting another tab brings its run forward (or returns to Home), keeping Home pinned at the base.
   */
  fun navigateToTopLevelGraph(topLevelGraph: TopLevelGraph) {
    backstackController.selectTopLevel(topLevelGraph)
  }

  fun navigateToLoggedIn(memberId: String?) {
    backstackController.setLoggedIn(memberId)
  }

  fun navigateToLoggedOut(memberId: String?) {
    backstackController.setLoggedOut(memberId)
  }
```

Update `isInScreenEligibleForCrossSells` to read the controller directly (it can no longer use the removed `currentDestination` forwarder). Replace:

```kotlin
  val isInScreenEligibleForCrossSells: Boolean
    get() = currentDestination is CrossSellEligibleDestination
```

with:

```kotlin
  val isInScreenEligibleForCrossSells: Boolean
    get() = backstackController.currentDestination is CrossSellEligibleDestination
```

Now `HedvigNavKey` is no longer referenced in this file — remove `import com.hedvig.android.navigation.common.HedvigNavKey`. `TopLevelGraph` is still referenced by `topLevelGraphs` — keep its import.

- [ ] **Step 5: Build + test**

Run: `./gradlew :app:testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, all tests pass.

- [ ] **Step 6: Verify no leftover forwarders / stale references**

Run:
```bash
grep -rn "navigateToLoggedIn\|navigateToLoggedOut\|navigateToTopLevelGraph\|\.currentTopLevelGraph\b" app/app/src/main/kotlin/com/hedvig/android/app
grep -n "hedvigAppState" app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt
```
Expected: no matches for the removed forwarder names; no `hedvigAppState` in `HedvigNavHost.kt`.

- [ ] **Step 7: ktlint**

Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Full app assemble (final integration gate)**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 9: Commit**

```bash
git add app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppState.kt app/app/src/main/kotlin/com/hedvig/android/app/navigation/HedvigNavHost.kt app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigAppUi.kt app/app/src/main/kotlin/com/hedvig/android/app/ui/HedvigApp.kt
git commit -m "$(cat <<'EOF'
refactor(nav): split HedvigAppState/BackstackController responsibilities

Removes HedvigAppState's pure-navigation forwarders (currentDestination,
currentTopLevelGraph, navigateToTopLevelGraph/LoggedIn/LoggedOut) and
threads BackstackController + windowSizeClass directly into HedvigNavHost,
leaving HedvigAppState with only feature-knowledge state plus the
controller bridge.
EOF
)"
```

---

## Self-Review

**Spec coverage:**
- (a) HedvigAppState/BackstackController split → Task 7. ✓
- (b) three marker conversions in navigation-common → Task 1 (declare) + Task 2 (cross-sell) + Task 3 (chat) + Task 4 (logout). ✓
- (c) DeliberateLogoutOrigin logout reinstatement in `setLoggedOut` → Task 5 (TDD). ✓
- (d) CurrentDestinationHolder replacement (option A: generic holder, marker check at consumer) → Task 6. ✓

**Type consistency:** marker names (`CrossSellEligibleDestination`, `SuppressesChatPushNotification`, `DeliberateLogoutOrigin`) are used identically across Tasks 1–7. Holder API: `CurrentDestinationHolder.currentDestination: StateFlow<HedvigNavKey?>` + `update(HedvigNavKey?)` — read as `.currentDestination.value` (Task 2/6) and written via `.update(...)` (Task 6). Controller methods used by callers: `selectTopLevel`, `setLoggedIn`, `setLoggedOut`, `currentTopLevel`, `currentDestination`, `isLoggedIn`, `navigateUp`, `loneDeepLinkChrome`, `allLiveContentKeys`, `handleBack`, `entries` — all exist on `BackstackController`. ✓

**Placeholder scan:** every code step contains complete code; every command has an expected result. ✓

**Ordering safety:** Task 1 only adds interfaces (safe). Tasks 2–4 each add markers + delete the corresponding registry atomically (build green after each). Task 5 is TDD and self-contained. Task 6 swaps storage without touching the split. Task 7 removes forwarders last, after every other consumer is settled. Each task ends with a green build + ktlint + commit.

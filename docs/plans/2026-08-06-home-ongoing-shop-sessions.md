# Home "Your quotes" from Ongoing Shop Sessions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Drive the home screen's "Your quotes" section from the member's real backend `ongoingShopSessions` (resumable web shopping sessions), remove the placeholder cross-sell that used to fill it, and gate the whole section behind the `disable_resuming_ongoing_shop_sessions` kill switch.

**Architecture:** The backend field `currentMember.ongoingShopSessions { display { ... } }` already exists in the Octopus schema (federated from `storefront` PR #1272, merged to `master`). We query it in `feature-home`, map it to a project-owned `OngoingShopSession` model, expose it on the home UI state, and render a card per session in the section previously called "Offers". The recommended cross-sell that used to fill that section is removed from it (it still feeds the cross-sell bottom sheet, unchanged). The kill switch is read in the use case: when on, we emit an empty session list, and the section (which hides when empty) disappears.

**Tech Stack:** Kotlin, Jetpack Compose, Apollo GraphQL (Octopus), Molecule/MVI, Metro DI, Unleash feature flags, JUnit + Turbine + assertk + TestParameterInjector + Apollo test builders.

## Global Constraints

- **DI is Metro, not Koin.** Do not write `module { }` or `get()`. `GetHomeDataUseCaseImpl` is `@Inject`; no DI wiring changes are needed for this feature.
- **Never expose Apollo/`octopus.*` types in public API.** Map query results to project-owned types inside the `internal` use-case impl only. `OngoingShopSession` is the project-owned type.
- **Strings are managed by Lokalise.** Never add to any `strings.xml`. For new UI text, hardcode the English string and add a `// TODO: Add "<EN>" / "<SV>" to Lokalise` comment. Reuse existing keys where they already exist (the section title `HOME_QUOTES_SECTION_TITLE` already exists).
- **Feature-flag polarity + defaults.** `disable_resuming_ongoing_shop_sessions` is a kill switch. The enum name mirrors the key (`DISABLE_...`); `UnleashFeatureFlagProvider` returns the raw `isEnabled(key)`; the consumer inverts. Never-fetched default is `false` (switch off → feature available), which is acceptable here (not app-gating, and the section is empty until data loads anyway) — so **no bootstrap entry**. See `app/featureflags/feature-flags/FEATURE_FLAG_DEFAULTS.md`.
- **Logging** goes through `logcat` only.
- **Formatting:** run `./gradlew ktlintFormat` before each commit.
- **`navigateUp` rule** is irrelevant here (no new navigation). The resume button opens a web URL via the existing `openUrl` lambda.

---

## File Structure

- `app/featureflags/feature-flags/src/commonMain/.../flags/Feature.kt` — add the enum value.
- `app/featureflags/feature-flags/src/androidMain/.../flags/FeatureUnleashKey.kt` — add the raw key mapping (exhaustive `when`, compiler-enforced).
- `app/feature/feature-home/src/main/graphql/QueryHome.graphql` — add the `ongoingShopSessions` selection.
- `app/feature/feature-home/build.gradle.kts` — add `implementation(projects.coreUiData)` (needed for `UiMoney`).
- `app/feature/feature-home/src/main/kotlin/.../home/data/GetHomeDataUseCase.kt` — new `OngoingShopSession` model, `HomeData.ongoingShopSessions` field, mapping, and kill-switch gating.
- `app/feature/feature-home/src/main/kotlin/.../home/data/GetHomeDataUseCaseDemo.kt` — sample session data for demo mode.
- `app/feature/feature-home/src/main/kotlin/.../home/ui/HomePresenter.kt` — expose `ongoingShopSessions` on `HomeUiState.Success`; later remove the now-dead `offersCrossSell`.
- `app/feature/feature-home/src/main/kotlin/.../home/ui/HomeDestination.kt` — render the section from sessions; rename `Offers` → `Quotes`; update the preview.
- `app/feature/feature-home/src/test/kotlin/.../home/data/GetHomeUseCaseTest.kt` — new mapping + gating tests; patch existing map-based `FakeFeatureManager` sites.
- `app/feature/feature-home/src/test/kotlin/.../home/ui/HomePresenterTest.kt` — new propagation test; update the 3 `offersCrossSell` references.

**Gradle project paths** (modules are named by leaf directory): `:feature-home`, `:feature-flags`, `:core-ui-data`.
**Common commands:** unit tests `./gradlew :feature-home:testDebugUnitTest`; single test `--tests "com.hedvig.android.feature.home.home.data.GetHomeUseCaseTest"`; apollo codegen `./gradlew :feature-home:generateApolloSources`; format `./gradlew ktlintFormat`.

---

## Task 1: Add the `DISABLE_RESUMING_ONGOING_SHOP_SESSIONS` feature flag

**Files:**
- Modify: `app/featureflags/feature-flags/src/commonMain/kotlin/com/hedvig/android/featureflags/flags/Feature.kt`
- Modify: `app/featureflags/feature-flags/src/androidMain/kotlin/com/hedvig/android/featureflags/flags/FeatureUnleashKey.kt`

**Interfaces:**
- Produces: `Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS` (enum value) and its Unleash key `"disable_resuming_ongoing_shop_sessions"`, consumed by Task 3.

**Notes:** There is no dedicated unit test for the enum→key mapping; the `when` in `FeatureUnleashKey.kt` is exhaustive with no `else`, so a missing mapping is a **compile error**. Behavioral verification of the flag happens in Task 3's gating test. This task's deliverable is verified by compilation.

- [ ] **Step 1: Add the enum value**

In `Feature.kt`, add a new entry to the `enum class Feature` (place it next to the other `DISABLE_*` kill switches):

```kotlin
  DISABLE_RESUMING_ONGOING_SHOP_SESSIONS(
    "Kill switch for the home screen 'Your quotes' section, which lets a member resume an ongoing " +
      "shopping session they started on the web. When the toggle is on, the section is hidden.",
  ),
```

- [ ] **Step 2: Add the Unleash key mapping**

In `FeatureUnleashKey.kt`, add a branch to the `when` (keep alphabetical/grouped with the other `DISABLE_*` entries):

```kotlin
    Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS -> "disable_resuming_ongoing_shop_sessions"
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :feature-flags:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL (an exhaustive-`when` error here would mean the mapping is missing).

- [ ] **Step 4: Format and commit**

```bash
./gradlew ktlintFormat
git add app/featureflags/feature-flags/src
git commit -m "featureflags: add disable_resuming_ongoing_shop_sessions kill switch"
```

---

## Task 2: Query `ongoingShopSessions` in the Home query

**Files:**
- Modify: `app/feature/feature-home/src/main/graphql/QueryHome.graphql`

**Interfaces:**
- Produces: `HomeQuery.Data.CurrentMember.ongoingShopSessions`, each with `.id` and `.display` (`.display.title`, `.display.subtitle`, `.display.monthlyNet?.{amount,currencyCode}`, `.display.resumeUrl`, `.display.pillowImage?.src`). Consumed by Task 3.

**Notes:** No new query variable is added, so the generated `HomeQuery(...)` constructor arity is unchanged and existing tests that call `HomeQuery(true, false)` / `HomeQuery(true, true)` keep compiling. Gating is done in the use case (Task 3), not via an `@include` variable, to keep the blast radius small.

- [ ] **Step 1: Add the selection**

In `QueryHome.graphql`, inside the `currentMember { ... }` block (e.g. right after the `crossSellV2(...) { ... }` block), add:

```graphql
    ongoingShopSessions {
      id
      display {
        title
        subtitle
        monthlyNet {
          amount
          currencyCode
        }
        resumeUrl
        pillowImage {
          src
        }
      }
    }
```

- [ ] **Step 2: Regenerate Apollo sources and verify**

Run: `./gradlew :feature-home:generateApolloSources`
Expected: BUILD SUCCESSFUL, and `HomeQuery.Data.CurrentMember.OngoingShopSession` (with a nested `Display`) is generated. If the build complains the field is unknown, the local Octopus schema is stale — run `./gradlew downloadOctopusApolloSchemaFromIntrospection` and retry.

- [ ] **Step 3: Commit**

```bash
git add app/feature/feature-home/src/main/graphql/QueryHome.graphql
git commit -m "feature-home: query currentMember.ongoingShopSessions"
```

---

## Task 3: Map sessions into `HomeData`, gated by the kill switch

**Files:**
- Modify: `app/feature/feature-home/build.gradle.kts`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/data/GetHomeDataUseCase.kt`
- Test: `app/feature/feature-home/src/test/kotlin/com/hedvig/android/feature/home/home/data/GetHomeUseCaseTest.kt`

**Interfaces:**
- Consumes: `HomeQuery.Data.CurrentMember.ongoingShopSessions` (Task 2); `Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS` (Task 1); `com.hedvig.android.core.uidata.UiMoney` + `com.hedvig.android.core.uidata.UiCurrencyCode`.
- Produces:
  ```kotlin
  data class OngoingShopSession(
    val id: String,
    val title: String,
    val subtitle: String?,
    val monthlyNet: UiMoney?,
    val resumeUrl: String,
    val pillowImageUrl: String?,
  )
  ```
  and `HomeData.ongoingShopSessions: List<OngoingShopSession>` (default `emptyList()`), consumed by Tasks 4/5/7.

- [ ] **Step 1: Add the `core-ui-data` dependency**

In `app/feature/feature-home/build.gradle.kts`, add to the `dependencies { }` block (alphabetically near other `projects.core*` entries):

```kotlin
  implementation(projects.coreUiData)
```

- [ ] **Step 2: Write the failing mapping test**

In `GetHomeUseCaseTest.kt`, add the imports at the top:

```kotlin
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.feature.home.home.data.HomeData
import octopus.type.CurrencyCode
import octopus.type.buildMoney
import octopus.type.buildShopSession
import octopus.type.buildShopSessionDisplay
import assertk.assertions.containsExactly
```
(Skip any import already present — `HomeData` and `containsExactly` are likely already imported.)

Then add this test (uses `FakeFeatureManager(mapOf(...))`, so it must list every flag the use case reads — including the new one, set to `false` = not disabled):

```kotlin
  @Test
  fun `ongoing shop sessions are mapped into HomeData when the kill switch is off`() = runTest {
    val featureManager = FakeFeatureManager(
      mapOf(
        Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX to false,
        Feature.ENABLE_CLAIM_INTENT_RESUME to false,
        Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to false,
      ),
    )
    val getHomeDataUseCase = testUseCaseWithoutReminders(featureManager)

    apolloClient.registerTestResponse(
      HomeQuery(true, false),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          ongoingShopSessions = listOf(
            buildShopSession {
              id = "session-1"
              display = buildShopSessionDisplay {
                title = "Home + Accident"
                subtitle = "Studio apartment, Stockholm"
                monthlyNet = buildMoney {
                  amount = 199.0
                  currencyCode = CurrencyCode.SEK
                }
                resumeUrl = "https://hedvig.com/resume/session-1"
                pillowImage = null
              }
            },
          )
        }
      },
    )
    apolloClient.registerTestResponse(UnreadMessageCountQuery(), UnreadMessageCountQuery.Data(OctopusFakeResolver))

    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::ongoingShopSessions)
      .containsExactly(
        OngoingShopSession(
          id = "session-1",
          title = "Home + Accident",
          subtitle = "Studio apartment, Stockholm",
          monthlyNet = UiMoney(199.0, UiCurrencyCode.SEK),
          resumeUrl = "https://hedvig.com/resume/session-1",
          pillowImageUrl = null,
        ),
      )
  }
```

Also add the import for the model:
```kotlin
import com.hedvig.android.feature.home.home.data.OngoingShopSession
```

- [ ] **Step 3: Run it and confirm it fails to compile / fails**

Run: `./gradlew :feature-home:testDebugUnitTest --tests "com.hedvig.android.feature.home.home.data.GetHomeUseCaseTest"`
Expected: FAIL — `OngoingShopSession` and `HomeData.ongoingShopSessions` don't exist yet (compile error), or the assertion fails.

- [ ] **Step 4: Add the `OngoingShopSession` model and the `HomeData` field**

In `GetHomeDataUseCase.kt`, add the model near the other data-layer models (e.g. just below the `HomeData` data class, or beside `RecommendedCrossSell` usages):

```kotlin
data class OngoingShopSession(
  val id: String,
  val title: String,
  val subtitle: String?,
  val monthlyNet: UiMoney?,
  val resumeUrl: String,
  val pillowImageUrl: String?,
)
```

Add the field to `data class HomeData(...)` with a default so existing construction sites (tests, demo) stay terse:

```kotlin
  val ongoingShopSessions: List<OngoingShopSession> = emptyList(),
```

Add imports to the file:
```kotlin
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
```

- [ ] **Step 5: Read the kill switch and map the sessions**

In `GetHomeDataUseCaseImpl.invoke(...)`, change the outer flag read so both flags are read, then gate the mapping.

Replace the current outer:
```kotlin
    return featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_INTENT_RESUME)
      .flatMapLatest { resumeClaimEnabled ->
        combine(
```
with:
```kotlin
    return combine(
      featureManager.isFeatureEnabled(Feature.ENABLE_CLAIM_INTENT_RESUME),
      featureManager.isFeatureEnabled(Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS),
      ::Pair,
    ).flatMapLatest { (resumeClaimEnabled, disableShopSessions) ->
        combine(
```
(The inner `combine(...)` over the 6 data flows is unchanged — do not add a 7th flow to it.)

Inside the `either { }` block (next to where `crossSellsData` / `recommendedCrossSell` are built), add:
```kotlin
            val ongoingShopSessions = if (disableShopSessions) {
              emptyList()
            } else {
              homeQueryData.currentMember.ongoingShopSessions.map { session ->
                OngoingShopSession(
                  id = session.id,
                  title = session.display.title,
                  subtitle = session.display.subtitle,
                  monthlyNet = session.display.monthlyNet?.let {
                    UiMoney(it.amount, UiCurrencyCode.fromCurrencyCode(it.currencyCode))
                  },
                  resumeUrl = session.display.resumeUrl,
                  pillowImageUrl = session.display.pillowImage?.src,
                )
              }
            }
```

Pass it into the `HomeData(...)` constructor at the end of the `either { }` block:
```kotlin
              ongoingShopSessions = ongoingShopSessions,
```

- [ ] **Step 6: Add the gating (kill-switch-on) test**

In `GetHomeUseCaseTest.kt`, add:

```kotlin
  @Test
  fun `ongoing shop sessions are dropped when the kill switch is on`() = runTest {
    val featureManager = FakeFeatureManager(
      mapOf(
        Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX to false,
        Feature.ENABLE_CLAIM_INTENT_RESUME to false,
        Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to true,
      ),
    )
    val getHomeDataUseCase = testUseCaseWithoutReminders(featureManager)

    apolloClient.registerTestResponse(
      HomeQuery(true, false),
      HomeQuery.Data(OctopusFakeResolver) {
        currentMember = buildMember {
          ongoingShopSessions = listOf(
            buildShopSession {
              id = "session-1"
              display = buildShopSessionDisplay {
                title = "Home + Accident"
                resumeUrl = "https://hedvig.com/resume/session-1"
              }
            },
          )
        }
      },
    )
    apolloClient.registerTestResponse(UnreadMessageCountQuery(), UnreadMessageCountQuery.Data(OctopusFakeResolver))

    val result = getHomeDataUseCase.invoke(true).first()

    assertThat(result)
      .isNotNull()
      .isRight()
      .prop(HomeData::ongoingShopSessions)
      .isEmpty()
  }
```

- [ ] **Step 7: Patch existing map-based `FakeFeatureManager` sites**

Because the use case now reads a second flag, any test that builds `FakeFeatureManager(mapOf(...))` must include the new flag, or `isFeatureEnabled` will block on the turbine and the test will hang. Add the entry `Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to false,` to **each** of the 4 map literals in `GetHomeUseCaseTest.kt`. They are the blocks starting near lines **457, 530, 590, 749**, each currently containing:
```kotlin
        Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX to ...,
        Feature.ENABLE_CLAIM_INTENT_RESUME to false,
```
Add the new line right after the `ENABLE_CLAIM_INTENT_RESUME` entry in all four:
```kotlin
        Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to false,
```
The `FakeFeatureManager(true)` sites (all-true) need no change — they already resolve every flag.

- [ ] **Step 8: Run the module's use-case tests and confirm green**

Run: `./gradlew :feature-home:testDebugUnitTest --tests "com.hedvig.android.feature.home.home.data.GetHomeUseCaseTest"`
Expected: PASS (both new tests pass; no existing test hangs or fails).

- [ ] **Step 9: Format and commit**

```bash
./gradlew ktlintFormat
git add app/feature/feature-home/build.gradle.kts app/feature/feature-home/src/main app/feature/feature-home/src/test
git commit -m "feature-home: map ongoing shop sessions into HomeData behind the kill switch"
```

---

## Task 4: Expose `ongoingShopSessions` on the home UI state

**Files:**
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenter.kt`
- Test: `app/feature/feature-home/src/test/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenterTest.kt`

**Interfaces:**
- Consumes: `HomeData.ongoingShopSessions` (Task 3).
- Produces: `HomeUiState.Success.ongoingShopSessions: List<OngoingShopSession>` (default `emptyList()`), consumed by Task 5.

- [ ] **Step 1: Write the failing propagation test**

In `HomePresenterTest.kt`, add a test that pushes a `HomeData` carrying one session and asserts it reaches `HomeUiState.Success`. Model the presenter construction on the existing tests (7 constructor args). Add imports:
```kotlin
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.feature.home.home.data.OngoingShopSession
import assertk.assertions.containsExactly
```
Test:
```kotlin
  @Test
  fun `ongoing shop sessions propagate to the success ui state`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )

    val session = OngoingShopSession(
      id = "session-1",
      title = "Home + Accident",
      subtitle = null,
      monthlyNet = UiMoney(199.0, UiCurrencyCode.SEK),
      resumeUrl = "https://hedvig.com/resume/session-1",
      pillowImageUrl = null,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
      getHomeDataUseCase.responseTurbine.add(
        someIrrelevantHomeDataInstance.copy(ongoingShopSessions = listOf(session)).right(),
      )
      val success = awaitItem()
      assertThat(success)
        .isInstanceOf<HomeUiState.Success>()
        .prop(HomeUiState.Success::ongoingShopSessions)
        .containsExactly(session)
    }
  }
```
If `someIrrelevantHomeDataInstance` is not defined in this file, build a minimal `HomeData(...)` inline instead (see the existing `a successful response, properly propagates the info to the UI State` test for the field list) and set `ongoingShopSessions = listOf(session)`.

- [ ] **Step 2: Run it and confirm it fails**

Run: `./gradlew :feature-home:testDebugUnitTest --tests "com.hedvig.android.feature.home.home.ui.HomePresenterTest"`
Expected: FAIL — `HomeUiState.Success.ongoingShopSessions` does not exist yet.

- [ ] **Step 3: Add the field to `HomeUiState.Success`**

In `HomePresenter.kt`, in `data class Success(...)`, add (near `crossSellsPartition`):
```kotlin
    val ongoingShopSessions: List<OngoingShopSession> = emptyList(),
```
Add the import:
```kotlin
import com.hedvig.android.feature.home.home.data.OngoingShopSession
```

- [ ] **Step 4: Thread it through `SuccessData`**

In `private data class SuccessData(...)`, add:
```kotlin
  val ongoingShopSessions: List<OngoingShopSession>,
```
In `SuccessData.fromLastState(...)`, add:
```kotlin
        ongoingShopSessions = lastState.ongoingShopSessions,
```
In `SuccessData.fromHomeData(...)` return, add:
```kotlin
        ongoingShopSessions = homeData.ongoingShopSessions,
```
In the `HomeUiState.Success(...)` construction (where `crossSellsPartition = successData.crossSellsPartition` is set), add:
```kotlin
          ongoingShopSessions = successData.ongoingShopSessions,
```

- [ ] **Step 5: Run the presenter tests and confirm green**

Run: `./gradlew :feature-home:testDebugUnitTest --tests "com.hedvig.android.feature.home.home.ui.HomePresenterTest"`
Expected: PASS.

- [ ] **Step 6: Format and commit**

```bash
./gradlew ktlintFormat
git add app/feature/feature-home/src
git commit -m "feature-home: expose ongoingShopSessions on the home ui state"
```

---

## Task 5: Render the "Your quotes" section from sessions

**Files:**
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`

**Interfaces:**
- Consumes: `HomeUiState.Success.ongoingShopSessions` (Task 4); the existing `openUrl: (String) -> Unit` param already threaded into `HomeDestination` (opens a web URL — the correct handler for `resumeUrl`, which needs no auth).
- Produces: a `QuotesSection` composable; the `HomeSection.Quotes` entry.

**Notes:** This task switches what the section *renders* but does not yet remove the now-unused `crossSellsPartition.offersCrossSell` field — that cleanup is Task 6, kept separate so it can be reviewed independently. The section is a pure UI render of state; there is no presenter/unit test for Compose here (consistent with the module — the section's data is covered by Tasks 3–4). Verification is compilation + the `@Preview`.

- [ ] **Step 1: Rename the section enum `Offers` → `Quotes`**

In `HomeDestination.kt`, in `private enum class HomeSection`, rename `Offers` to `Quotes`. Update the reference in `homeSectionOrder` (`HomeSection.Offers` → `HomeSection.Quotes`).

- [ ] **Step 2: Point the visibility guard at the sessions**

Find the `HomeSection.Offers -> { uiState.crossSellsPartition.offersCrossSell != null }` branch (currently ~line 664) and change it to:
```kotlin
        HomeSection.Quotes -> {
          uiState.ongoingShopSessions.isNotEmpty()
        }
```

- [ ] **Step 3: Point the render branch at the sessions**

Find the `HomeSection.Offers -> uiState.crossSellsPartition.offersCrossSell?.let { ... OffersSection(...) }` branch (currently ~line 891) and replace it with:
```kotlin
            HomeSection.Quotes -> uiState.ongoingShopSessions.takeIf { it.isNotEmpty() }?.let { sessions ->
              QuotesSection(
                sessions = sessions,
                onResumeClick = openUrl,
                imageLoader = imageLoader,
                horizontalInsets = horizontalInsets,
              )
            }
```

- [ ] **Step 4: Replace `OffersSection` with `QuotesSection`**

Replace the entire `private fun OffersSection(...)` composable (currently ~lines 1101–1171) with:
```kotlin
@Composable
private fun QuotesSection(
  sessions: List<OngoingShopSession>,
  onResumeClick: (String) -> Unit,
  imageLoader: ImageLoader,
  horizontalInsets: PaddingValues,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(horizontalInsets),
  ) {
    HedvigText(
      text = stringResource(Res.string.HOME_QUOTES_SECTION_TITLE),
      style = HedvigTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    for (session in sessions) {
      HedvigCard(
        onClick = { onResumeClick(session.resumeUrl) },
        color = HedvigTheme.colorScheme.fillNegative,
        borderColor = HedvigTheme.colorScheme.borderPrimary,
        modifier = Modifier
          .fillMaxWidth()
          .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
      ) {
        Column(Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (session.pillowImageUrl != null) {
              AsyncImage(
                model = session.pillowImageUrl,
                contentDescription = null,
                imageLoader = imageLoader,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(48.dp),
              )
              Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
              HedvigText(text = session.title, style = HedvigTheme.typography.bodySmall)
              val secondary = session.subtitle ?: session.monthlyNet?.toString()
              if (secondary != null) {
                HedvigText(
                  text = secondary,
                  style = HedvigTheme.typography.label,
                  color = HedvigTheme.colorScheme.textSecondary,
                )
              }
            }
          }
          Spacer(Modifier.height(12.dp))
          // TODO: Add "Resume" / "Fortsätt" to Lokalise
          HedvigButton(
            text = "Resume",
            onClick = { onResumeClick(session.resumeUrl) },
            buttonStyle = Secondary,
            buttonSize = ButtonSize.Medium,
            enabled = true,
            shape = HedvigTheme.shapes.cornerFull,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
```
Add the import for the model:
```kotlin
import com.hedvig.android.feature.home.home.data.OngoingShopSession
```
(All other symbols — `HedvigCard`, `HedvigButton`, `AsyncImage`, `Secondary`, `ButtonSize`, `hedvigDropShadow`, etc. — are already imported for the old `OffersSection`.)

- [ ] **Step 5: Update the `@Preview` to exercise the new section**

In the preview's `HomeUiState.Success(...)` (the block currently containing `crossSellsPartition = CrossSellsPartition(offersCrossSell = RecommendedCrossSell(...), discoverCrossSells = emptyList())`, ~line 1479), add a sessions argument so the section renders in the preview:
```kotlin
          ongoingShopSessions = listOf(
            OngoingShopSession(
              id = "preview-1",
              title = "Home + Accident",
              subtitle = "Studio apartment, Stockholm",
              monthlyNet = null,
              resumeUrl = "",
              pillowImageUrl = null,
            ),
          ),
```
Leave the `crossSellsPartition = CrossSellsPartition(offersCrossSell = ...)` argument as-is for now (it is removed in Task 6).

- [ ] **Step 6: Verify it compiles**

Run: `./gradlew :feature-home:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Format and commit**

```bash
./gradlew ktlintFormat
git add app/feature/feature-home/src/main
git commit -m "feature-home: render the Your quotes section from ongoing shop sessions"
```

---

## Task 6: Remove the placeholder cross-sell from the quotes section

**Files:**
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenter.kt`
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/ui/HomeDestination.kt`
- Test: `app/feature/feature-home/src/test/kotlin/com/hedvig/android/feature/home/home/ui/HomePresenterTest.kt`

**Interfaces:**
- Removes `CrossSellsPartition.offersCrossSell`. `CrossSellsPartition` keeps only `discoverCrossSells: List<CrossSell>`, still consumed by the `DiscoverInsurances` section. The recommended cross-sell remains in `CrossSellSheetData` and still feeds the cross-sell bottom sheet (`crossSellsAction`) — do not touch that.

**Notes:** This is the "commit to approach #1" cleanup. It is separate from Task 5 so a reviewer can accept the new UI first.

- [ ] **Step 1: Drop the field from `CrossSellsPartition` and update its doc**

In `HomePresenter.kt`, remove `val offersCrossSell: RecommendedCrossSell? = null,` from `data class CrossSellsPartition(...)`, leaving:
```kotlin
internal data class CrossSellsPartition(
  val discoverCrossSells: List<CrossSell> = emptyList(),
)
```
Update the KDoc above it so it describes the current reality (it currently mentions "Offers"):
```kotlin
/**
 * The home screen surfaces cross-sells in the discover carousel and the "Discover our insurances"
 * list. This is the single place that decides which cross-sells go where.
 */
```
In `partitionCrossSells(...)`, remove the `offersCrossSell = crossSells.recommendedCrossSell,` line and the `// WS0 placeholder ...` comment, leaving:
```kotlin
internal fun partitionCrossSells(crossSells: CrossSellSheetData): CrossSellsPartition {
  return CrossSellsPartition(
    discoverCrossSells = crossSells.otherCrossSells,
  )
}
```
If the `RecommendedCrossSell` import becomes unused in this file after this, remove it. (It is still used via `homeData.crossSells.recommendedCrossSell` for `crossSellsAction`, so it likely stays — let the compiler/ktlint tell you.)

- [ ] **Step 2: Remove the dead preview argument**

In `HomeDestination.kt`, in the preview's `CrossSellsPartition(...)`, remove the `offersCrossSell = RecommendedCrossSell(...)` argument (the whole `RecommendedCrossSell(...)` block), leaving:
```kotlin
          crossSellsPartition = CrossSellsPartition(
            discoverCrossSells = emptyList(),
          ),
```

- [ ] **Step 3: Update the 3 presenter-test references**

In `HomePresenterTest.kt`, remove the `offersCrossSell = testCrossSell` usages:
- Line ~220: `crossSellsPartition = CrossSellsPartition(offersCrossSell = testCrossSell),` → `crossSellsPartition = CrossSellsPartition(),`
- Lines ~561 and ~704: remove the `offersCrossSell = testCrossSell,` line from each `CrossSellsPartition(...)` literal (keep the `discoverCrossSells = ...` argument if present; if `offersCrossSell` was the only argument, leave `CrossSellsPartition()`).

Read each site before editing to preserve the surrounding `discoverCrossSells` argument.

- [ ] **Step 4: Compile and run the full module test suite**

Run: `./gradlew :feature-home:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all tests PASS. (Confirms nothing else read `offersCrossSell`.)

- [ ] **Step 5: Format and commit**

```bash
./gradlew ktlintFormat
git add app/feature/feature-home/src
git commit -m "feature-home: drop the placeholder cross-sell from the quotes section"
```

---

## Task 7: Show sessions in demo mode

**Files:**
- Modify: `app/feature/feature-home/src/main/kotlin/com/hedvig/android/feature/home/home/data/GetHomeDataUseCaseDemo.kt`

**Interfaces:**
- Consumes: `HomeData.ongoingShopSessions` + `OngoingShopSession` (Task 3).

**Notes:** Demo mode never reads feature flags, so the section shows whenever `ongoingShopSessions` is non-empty. Keep the existing `crossSells` block as-is (it still drives the cross-sell sheet in demo).

- [ ] **Step 1: Add sample session data**

In `GetHomeDataUseCaseDemo.kt`, add to the `HomeData(...)` constructor:
```kotlin
      ongoingShopSessions = listOf(
        OngoingShopSession(
          id = "demo-session-1",
          title = "Home + Accident",
          subtitle = "Studio apartment, Stockholm",
          monthlyNet = UiMoney(199.0, UiCurrencyCode.SEK),
          resumeUrl = "https://www.hedvig.com",
          pillowImageUrl = null,
        ),
      ),
```
Add imports:
```kotlin
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
```

- [ ] **Step 2: Compile the module**

Run: `./gradlew :feature-home:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Format and commit**

```bash
./gradlew ktlintFormat
git add app/feature/feature-home/src/main
git commit -m "feature-home: show a sample ongoing shop session in demo mode"
```

---

## Final verification

- [ ] Run the full module test suite: `./gradlew :feature-home:testDebugUnitTest` → all PASS.
- [ ] Confirm formatting: `./gradlew ktlintCheck` → no violations.
- [ ] (Optional, manual) Run the app in demo mode and confirm the "Your quotes" section shows the sample session with a working "Resume" button; toggle `disable_resuming_ongoing_shop_sessions` on in a real build and confirm the section disappears.

## Notes / decisions baked into this plan

- **Approach #1 (chosen):** the recommended cross-sell no longer appears in the home "Your quotes" section. It is *not* deleted from the data layer — it still feeds the cross-sell bottom sheet via `CrossSellSheetData.recommendedCrossSell`, untouched.
- **Kill-switch gating is in the use case** (drop to empty list when on), not via a GraphQL `@include` variable, to avoid changing the generated `HomeQuery` constructor arity and breaking every existing test call site. Cost: the field is fetched even when the switch is on (the rare/rollback case); this is negligible.
- **Resume URL** is opened with the existing `openUrl` lambda (already threaded into `HomeDestination`), which matches the backend contract ("open directly in the device browser, no auth required"). No new `:app` plumbing.
- **No bootstrap** for the flag: natural never-fetched default (section available, but empty until data loads) is safe and correct.
- **`monthlyGross`, `lastActivityAt`, `validTo`** are available on the backend `display` type but are not queried in v1 (YAGNI). Add them if design wants a struck-through price, a relative "2 days ago" label, or client-side expiry hiding.
- **"Resume" button copy** is hardcoded with a Lokalise TODO. If a suitable generic key already exists in `strings.xml` (e.g. a "Continue" string), prefer reusing it over the hardcode.

# Navigation NavKey Modernization

**Date:** 2026-06-02
**Branch:** `eng/agentic-refactor` (PR2 follow-up, stacked on the Nav2→Nav3 migration)
**Status:** Approved design

## Goal

Drop the Nav2-era idioms that survived the mechanical Nav2→Nav3 migration and adopt
Nav3's native `NavKey` vocabulary throughout the app. Concretely:

1. Rename our `Destination` marker to a serializable `HedvigNavKey`.
2. Remove the `Navigator` interface entirely; manipulate the real Nav3 back stack
   (`MutableList<HedvigNavKey>`) directly, with a small set of tested central extensions
   for the operations that carry subtle logic.
3. Flatten each feature's key file: every screen becomes a top-level key, all `internal`
   except the public entry-point key(s). Drop the `graph-root destination + internal sealed
   interface` split.

This is a vocabulary/structure modernization. It is **not** intended to change navigation
behavior — back-stack semantics must be preserved exactly.

## Decisions (locked)

- **Marker:** `interface Destination : NavKey` → `interface HedvigNavKey : NavKey`. Concrete
  keys stay `@Serializable`.
- **Key naming:** concrete keys take a `...Key` suffix (e.g. `TerminationDateKey`).
- **Public entry keys:** named `<FeatureName>Key` (e.g. `TerminateInsuranceKey`). No `Graph`
  suffix — `public` visibility is the entry-point signal. Features with multiple deep-link
  entry points keep multiple public keys.
- **Backstack operations:** central extension functions on `MutableList<HedvigNavKey>`.
  Trivial pushes are `backStack.add(key)` inline; subtle operations use a named helper.
- **Rename scope:** only nav-key *types* and their wrappers. Screen-entry composables keep
  their existing `XDestination()` names — renaming those is a possible mechanical follow-up,
  out of scope here.
- **Untouched:** the multi-tab save/restore holder (`HedvigTopLevelBackStacks`). `Navigator`
  never modeled tab semantics, so removing it does not affect that layer.

## Architecture

### 1. `navigation-common`: the key marker

```kotlin
// Destination.kt → HedvigNavKey.kt
interface HedvigNavKey : NavKey

interface NavKeyTypeAware {        // was DestinationNavTypeAware
  val typeList: List<KType>
}
```

`NavKeyTypeAware` keeps `typeList`; it is still required for custom-argument serialization of
complex keys.

### 2. `navigation-compose`: remove `Navigator`, add backstack extensions

Delete `Navigator.kt` (interface, `NavigatorImpl`, inline helpers). Add a single extensions
file (e.g. `HedvigNavBackStack.kt`) operating on the real Nav3 back stack
`MutableList<HedvigNavKey>`. The bodies port `NavigatorImpl`'s logic verbatim so behavior is
byte-for-byte preserved:

```kotlin
// Pops the top entry; returns false if at the root (nothing popped).
fun MutableList<HedvigNavKey>.popBackStack(): Boolean

// Equivalent to Nav2 navigateUp().
fun MutableList<HedvigNavKey>.navigateUp(): Boolean

// Pops up to (and optionally including) the most recent entry of T.
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.popUpTo(inclusive: Boolean)

// Pops up to the most recent T, then pushes [key]. Replaces navigate(dest, popUpTo, inclusive).
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.navigateAndPopUpTo(
  key: HedvigNavKey,
  inclusive: Boolean,
)

// Most recent entry of T, or null. Uses `is T`, not KClass.isInstance (KMP-clean).
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.findLastOrNull(): T?

// Removes every entry of T. Replaces Nav2 typedClearBackStack.
inline fun <reified T : HedvigNavKey> MutableList<HedvigNavKey>.removeAllOf()
```

Plain forward navigation (`navigator.navigate(x)`) becomes `backStack.add(x)` inline — no
helper needed.

### 3. Graph builders

Each `EntryProviderScope<...>.xGraph(...)` builder swaps its `navigator: Navigator` parameter
for `backStack: MutableList<HedvigNavKey>`. Call-site translation:

| Before                                            | After                                         |
|---------------------------------------------------|-----------------------------------------------|
| `navigator.navigate(key)`                         | `backStack.add(key)`                          |
| `navigator::navigateUp`                           | `backStack::navigateUp`                       |
| `navigator::popBackStack`                         | `backStack::popBackStack`                     |
| `navigator.navigate(key, T::class, inclusive)`    | `backStack.navigateAndPopUpTo<T>(key, inclusive)` |
| `navigator.popUpTo<T>(inclusive)`                 | `backStack.popUpTo<T>(inclusive)`             |
| `navigator.findLastOrNull<T>()`                   | `backStack.findLastOrNull<T>()`               |
| `navigator.clearBackStackOf<T>()`                 | `backStack.removeAllOf<T>()`                  |

Bound references to extension functions (`backStack::navigateUp`) are supported by Kotlin and
satisfy the `() -> Unit` / `() -> Boolean` lambda parameters the screen composables expect.

The app shell (`HedvigNavDisplay` / `HedvigNavHost`) stops constructing `Navigator(backStack)`
and passes the back-stack list directly to graph builders.

### 4. Flatten per-feature key files

Worked example — `feature-terminate-insurance`:

**Before:** public `TerminateInsuranceGraphDestination(insuranceId)` outside, plus
`internal sealed interface TerminateInsuranceDestination` wrapping 9 screens, each
`: TerminateInsuranceDestination, Destination`.

**After:** no sealed wrapper. Top-level keys:

```kotlin
// Public entry point (deep-link target + first screen: choose-insurance).
@Serializable
data class TerminateInsuranceKey(
  @SerialName("contractId") val insuranceId: String? = null,
) : HedvigNavKey

@Serializable
internal data class TerminationSurveyFirstStepKey(...) : HedvigNavKey {
  companion object : NavKeyTypeAware { override val typeList = listOf(...) }
}
// ...TerminationSurveySecondStepKey, TerminationDateKey, TerminationConfirmationKey,
//    InsuranceDeletionKey, TerminationSuccessKey, TerminationFailureKey,
//    UnknownScreenKey, DeflectSuggestionKey — all internal, top-level.
```

Non-key helper types (`TerminationGraphParameters`, `TerminationDateParameters`) are unchanged
(they are arguments, not keys). The same flattening applies to all ~25 feature graphs; features
with several public deep-link targets (e.g. profile: profile / contact-info / eurobonus) keep
each as a `public` key.

### 5. Ripple updates

- `*DeepLinks.kt` providers and the serializer registration (`DestinationSerializersModule`)
  reference `X.serializer()` — update to the new key type names.
- Public entry-key renames touch cross-module call sites (home tiles, deep links, cross-feature
  navigation). All mechanical.

## Data flow (unchanged)

External URI / notification → `deepLinkChannel` → buffered until logged in →
`DeepLinkFirstUriHandler` → `HedvigDeepLinkMatcher.match()` → push key onto the back-stack list.
In-flow navigation: screen callback → `backStack.add(key)` / extension → `HedvigNavDisplay`
recomposes. No behavioral change; only the types and the mutation API change.

## Testing

- Unit tests for the backstack extensions in `navigation-compose` (popUpTo inclusive/exclusive,
  navigateAndPopUpTo when target absent, findLastOrNull, removeAllOf, popBackStack at root).
  These lock in the ported `NavigatorImpl` semantics.
- Build gates: `:app:assembleDebug`, `:navigation-compose:compileKotlinIosSimulatorArm64`,
  `ktlintFormat`.
- Manual: the same on-device deep-link / back-stack pass already pending for PR2 covers the
  behavioral surface.

## Risks

- **Large diff.** The `HedvigNavKey` rename and per-feature flattening touch every key type and
  every reference across ~25 features. Mechanical, but voluminous — execute feature-by-feature.
- **Behavioral regressions** are contained because the extensions port existing logic verbatim;
  the new unit tests plus the on-device pass are the safety net.
- **KMP.** `navigation-common`/`navigation-compose` are commonMain. Extensions use reified
  `is T` checks (not `KClass.isInstance`), keeping them KMP-clean; verified by the iOS native
  compile gate.

## Out of scope

- Renaming screen-entry composables (`XDestination()` → `XScreen()`).
- Any change to `HedvigTopLevelBackStacks` tab save/restore.
- Reinstating the deferred recents-URL-sharing (`onProvideAssistContent`).

# New Home Screen Overhaul — Implementation Plan

**Branch:** `feature/new-home-screen` → merges into `develop` as one coherent cut.
**Module:** primarily `feature-home` (presentation layer); `:app` navigation wiring (new lambdas); `data-addons` consumption.
**Design source:** FigJam critique *"Critique 24/06/26 – New Home Screen"* (`E8j1i2FXoJf31t1ZdLe5yY`, node `2001-349`) for intent + open questions; detail design *App-P2-2026* (`SogcacjzOxkCC46XcZP8lQ`, node `20-4489`) for component-level truth. Four density variants (Mini/Medium/Max/Mega) — the same screen at increasing content. **This plan has been adversarially reviewed against source; verified facts are marked ✓.**

---

## 1. Summary

Rebuild the **presentation layer** of the home screen, in place, on this branch. The **data layer stays largely intact** — most content is already fetched, order stays client-side. New data work is small: one GraphQL field (`firstName`) and one data-shape change (addon banner list). The bespoke centering `HomeLayout` is retired for a top-aligned scrolling section list.

## 2. Scope & non-goals

**In scope:** retire `HomeLayout`; decompose `HomeScreenSuccess` into per-section composables; restyle every existing section; add new sections (all backed by existing or trivially-added data); render the currently-dead addon data; a targeted data-resilience fix.

**Explicit non-goals (out of this branch):**
- **Navigation IA / tab restructure** (5 → 3 tabs, Profile/Forever leaving nav) — separate later workstream; home is built on the current 5-tab nav.
- **"New message" inbox-preview card** — cut (won't be in final design).
- **A distinct content feed for the carousel** (campaigns/news/guides) — backend fast-follow; v1 backs the carousel with existing cross-sell data (D12).
- **Per-section impression/click analytics** — the app has no such mechanism; screen tracking is automatic + key-driven via `ScreenParameterExtractor`, and `HomeKey` is unchanged. ✓
- **KMP/iOS changes** — `feature-home` is Android-only; iOS has its own independent Home. ✓
- **Server-driven layout, a screenshot-test framework, literal iOS "Liquid Glass".**

## 3. Locked decisions

| # | Decision | Notes |
|---|---|---|
| D1 | Replace `HomeLayout.kt` with a top-aligned **section list**; **`LazyColumn` preferred** (long feed). WS0 settles the container, restating the invariants it affects: the toolbar overlay, the ~64dp top spacer / window-insets, and the pull-to-refresh overlay (contentPadding vs verticalScroll). | Centering algorithm is gone. |
| D2 | Keep the single 7-way `combine()` in `GetHomeDataUseCase`. **Targeted resilience only.** | — |
| D3 | Fold the two **auxiliary** signals (`anyActiveConversations`, the 5 s `unreadMessageCount` poll) to safe defaults so they can't blank the screen; core `HomeQuery` still gates. ✓ `travelBannerInfo` is already `getOrNull()`-safe. | Seam: `GetHomeDataUseCase.kt` ~L98 (gate), L150/L152 (fold). |
| D4 | **Android-native interpretation**: reuse nearest DS; wrap missing ones behind a home-local composable + `// TODO: swap to <DS component>`. | — |
| D5 | *Section order — see D16.* | (kept for reference) |
| D6 | **To-do list = show all reminders, no truncation, no new dismissal.** | Pure restyle. |
| D7 | *Quick-action carousel/tiles — see D14.* | (kept for reference) |
| D8 | **Addons section = full addon list.** `HomeData.travelBannerInfo: AddonBannerInfo?` becomes a **`List<AddonBannerInfo>` (rename → `addonBannerInfos`)**; ✓ `GetAddonBannerInfoUseCase` already returns the full list — `feature-home` truncates it with `firstOrNull()`. Render **`FeatureAddonBanner` (design-system-hedvig) directly** — this is **new-UI, not reuse** (the Insurances-tab renderer is a *private* composable in a feature we can't depend on ✓). Needs a **new `navigateToAddonPurchaseFlow` (`AddonPurchaseKey`) lambda**. Covers car + travel addons (source = `INSURANCES_TAB`). | Type change ripples: `HomeData`, presenter mapping, demo impl, previews, tests. |
| D9 | **"Discover our insurances" and "Addons" duplicate** Insurances-tab content. Accepted. | #10 reuses the shared `CrossSellsSection` (✓ legitimately reusable). |
| D10 | **Tests = extend `HomePresenterTest` (state mapping) + `@Preview`.** The **resilience invariant** belongs in **`GetHomeUseCaseTest`** (real `GetHomeDataUseCaseImpl`), not `HomePresenterTest` (which injects a fake and can't observe the fold). ✓ | No screenshot framework. |
| D11 | Land as **one coherent cut**; branch buildable throughout. **Any `HomeData` change updates `GetHomeDataUseCaseDemo` in the same commit** (it's a real runtime-selected impl via `SwitchingGetHomeDataUseCase`). ✓ | In-place, no flag. |
| D12 | **Campaign/Discover carousel ships in v1, backed by existing cross-sell data** (`otherCrossSells`), rendered as a **pillow-icon carousel** (accepted lower fidelity — the data has only 48dp pillow icons, no banner imagery; discount text exists only on the single `recommendedCrossSell`). ✓ A real banner/content feed is a backend fast-follow. | — |
| D13 | **Liquid-glass = rounded tonal/solid surface** (DS squircle shapes), **no live blur**; wrap + `// TODO`. | Applies to the restyled toolbar icons too. |
| D14 | **Quick-action carousel (#3) = 4 static chips: Make a claim · Help & support · Contact us · Forever** (✓ real labels, detail design node `1:10786`). **Tiles (#9) = Help & support · Change address · Travel certificate** — "Help & support" intentionally in both. | Routes in §4. |
| D15 | **Toolbar = keep the existing three icons (FirstVet · Cross-sell · Chat), restyled to tonal glass.** Cross-sell icon **retains** its bottom sheet + red-dot badge + once-per-day tooltip; FirstVet and Chat (+unread badge) keep today's behaviour. **(Reverses the earlier remove-cross-sell call.)** | Pure restyle — no presenter-state removal. |
| D16 | **Section order = a single declarative section list.** Default = "Mega" order; design-owned, trivially reorderable. | — |
| D17 | **Greeting keeps the 5 contract-status variants** (Active/Pending/Switching/Terminated + the active-in-future Hedvig-logo case), restyled, **plus `firstName`**. No collapse to a single static greeting. ✓ `Member.firstName: String!` exists, absent from `QueryHome` today. | Threads through `HomeText` mapping. |

## 4. Target sections (top → bottom, "Mega" default order)

| # | Section | Data | Reuse / new | DS note |
|---|---|---|---|---|
| 1 | **Top-bar icons** — FirstVet · Cross-sell (+sheet/badge/tooltip) · Chat (+unread badge) | HAVE | restyle existing 3 icons (D15) | tonal rounded icon button (D13) |
| 2 | **Greeting** "Hi `<name>`…" — keeps 5 status variants + active-in-future logo | **`+firstName`** | restyle + thread name (D17) | `HedvigText` |
| 3 | **Quick-action carousel** (Make a claim · Help & support · Contact us · Forever) | HAVE | new UI | chip → wrap `HedvigButton` (tonal) + TODO |
| 4 | **Info Card** (announcement) | HAVE (`veryImportantMessages`; Hide = `MarkMessageAsSeen`, **in-memory only** ✓ — non-persistent, as today) | restyle | `HedvigNotificationCard` |
| 5 | **Claim card(s)** (+ loader dots) | HAVE | reuse `ClaimStatusCards` | — |
| 6 | **To-do list** (rows; show all) | HAVE (`memberReminders`) | restyle rows | row → wrap nearest list-row + TODO |
| 7 | **Offers** (recommended cross-sell card) | HAVE (`recommendedCrossSell`) | reuse cross-sells UI | product card+badge → `HedvigCard`+`HighlightLabel` |
| 8 | **Campaign/Discover carousel** (pillow-icon, D12) | HAVE (`otherCrossSells`) | new UI | `HorizontalPager` + pillow card |
| 9 | **Quick-actions tiles** — Help & support · Change address · Travel certificate | HAVE (`showHelpCenter` flag; nav lambdas) | new UI | tile → wrap `HedvigCard`/`Surface` + TODO |
| 10 | **Discover our insurances** | HAVE (`otherCrossSells`) | reuse `CrossSellsSection` ✓ | — |
| 11 | **Addons** (full list, D8) | HAVE→`List` (D8) | **new-UI**: render `FeatureAddonBanner` (DS) + new `navigateToAddonPurchaseFlow` | `FeatureAddonBanner` |
| — | **Pull-to-refresh · Loading · Error · active-in-future notification card** | HAVE | **retained** (do not drop when `HomeLayout`/`HomeScreenSuccess` are rewritten) | keep `PreviewHomeScreenWithError` |

**Navigation lambdas:**
- **Already wired (reuse):** Make a claim → `StartClaimBottomSheet`; Help & support → `navigateToHelpCenter`; Change address → `navigateToContactInfo`; **Contact us → `onNavigateToInbox`** (✓ already exists — *not* a new lambda; decide inbox vs `onNavigateToNewConversation`); cross-sell/FirstVet/chat toolbar actions.
- **Genuinely new (each co-lands its `homeEntries(...)` + `:app HedvigEntryProvider` signature change atomically — D11):** `navigateToForever` (→ `ForeverKey`; it's a `TopLevelTabRoot`, so use the **tab-switch** API, not a bare push ✓), `navigateToTravelCertificate`, `navigateToAddonPurchaseFlow` (→ `AddonPurchaseKey`).

## 5. Architecture changes (the three seams)

1. **Layout container** — delete `HomeLayout.kt`; render an ordered, declarative list (D16) of `HomeXSection` composables (container per D1 + its invariants). Per-section visibility stays modelled as today (nullability / list-emptiness), extended for new sections. Retain pull-to-refresh, Loading, Error, and the active-in-future card.
2. **Presenter / data mapping** — extend `SuccessData` → `HomeUiState.Success` with `firstName`, the `List<AddonBannerInfo>`, and a **centralised cross-sell → section partitioning** (which cross-sells go to Offers #7 vs carousel #8 vs Discover #10 — one place, built in WS0). No cross-sell tooltip/badge removal (D15 reversed).
3. **Data resilience** — fold `anyActiveConversations` / `unreadMessageCount` to defaults; keep `HomeQuery.bind()` gating; combine not decomposed (D2/D3).

## 6. Build order

| Phase | Workstream | Effort | Depends on |
|---|---|---|---|
| **1** | **WS0 — Foundation.** Delete `HomeLayout`; section-list container (D1 + invariants) + declarative section-order list (D16) + per-section visibility model + **centralised cross-sell→section mapping**; targeted resilience fix; rewire existing sections. **Update `GetHomeDataUseCaseDemo` for every `HomeData` change.** One cohesive PR. | M | — |
| **2** | **WS1 — Restyle on existing data:** toolbar (restyle 3 icons, D15) · greeting (keep status variants + `firstName`, D17) · Info Card · claim cards · Offers · Discover insurances · **Addons** (`FeatureAddonBanner` + new `navigateToAddonPurchaseFlow`, D8). + **WS3 — `firstName` GraphQL** (`QueryHome` + demo value). | M | WS0 |
| **3** | **WS2 — New-UI sections:** To-do rows · quick-action carousel (chips; reuse `onNavigateToInbox`, new `navigateToForever`) · quick-action tiles (new `navigateToTravelCertificate`) · campaign/discover **pillow-icon** carousel (D12). | M | WS0 |
| **4** | **Coherent cut + cleanup.** Verify `HomeLayout` gone, all sections via the declarative list, demo impl builds, presenter tests + `GetHomeUseCaseTest` resilience test + previews green, `ktlintFormat` + `lint` + `:feature-home:test`. Confirm `ExhaustiveBackStackSerializationTest` still passes (no new keys — verify). | S | WS0–WS2 |
| — | **Tests — woven throughout** (each PR ships its presenter-test/`@Preview` deltas; resilience invariant in `GetHomeUseCaseTest`). | — | each WS |

**Why this order:** WS0 owns the three central files (`GetHomeDataUseCase`/demo, `HomePresenter`, `HomeDestination`) and the shared mapping, so later PRs don't re-edit them. Each new `:app` lambda co-lands its `homeEntries` signature change atomically (so the branch never breaks). Restyle-on-existing-data sections (WS1) maximise low-risk progress; new-UI sections (WS2) follow.

## 7. Open items (non-blocking)

| Item | Owner | Note |
|---|---|---|
| Which cross-sells surface in Offers (#7) vs carousel (#8) vs Discover (#10) | Design | Centralised mapping (WS0) makes it a one-place change |
| Final section priority/order | Design/Product | D16 declarative list; default Mega |
| Contact-us chip target (inbox vs new conversation) | Eng/Design | Default `onNavigateToInbox`; both already plumbed |
| Carousel fidelity (pillow icons now) | Design/Backend | Revisit when a real banner/content feed exists (D12) |
| Distinct content feed (campaigns/news/guides) | Backend | Fast-follow |

## 8. Risks & mitigations

- **Branch buildability (WS0 + demo).** WS0 edits `HomeData`/`HomePresenter`/`HomeDestination` **and the demo impl** together. *Mitigation:* WS0 is one PR that rewires existing sections + updates demo; every later `HomeData` change updates demo in the same commit.
- **Resilience regression.** *Mitigation:* assert in `GetHomeUseCaseTest` (real impl) that an auxiliary-signal failure keeps the `Either` `Right` with safe defaults.
- **Cross-sell repetition** across #7/#8/#10. *Mitigation:* centralised mapping (WS0) + design differentiates which items go where.
- **Insurances-tab duplication** (Discover/Addons) looking like a bug. *Mitigation:* accepted (D9); flag to QA.
- **Carousel lower fidelity** (pillow icons, no banners/discounts on `otherCrossSells`). *Mitigation:* accepted for v1 (D12); revisit with real content.
- **DS-gap drift.** *Mitigation:* one shared home-local components file + consistent `// TODO: swap to <DS>` markers.
- **Lambda signature churn.** Each new lambda touches `homeEntries` + `:app`. *Mitigation:* co-land atomically (D11); ✓ no new module dependency needed (`ForeverKey`/`AddonPurchaseKey`/travel-cert already importable in `:app`).

## 9. Testing strategy

`HomePresenterTest`: extend state-mapping coverage (new-section visibility, `firstName`, `List<AddonBannerInfo>` mapping, status-variant greeting). `GetHomeUseCaseTest`: the resilience invariant (auxiliary failure ⇒ still `Right`/`Success`). `@Preview` per new `HomeXSection` (keep the error preview). Pre-merge: `:feature-home:test`, `ktlintFormat`, `lint`; confirm `ExhaustiveBackStackSerializationTest` + `BackstackTest` pass.

## 10. Strings, accessibility, demo

- **Strings (Lokalise):** reuse existing keys where present (claim button, get-help, open-chat, referrals, quick-actions/travel-certificate, offers headings, welcome titles). For net-new text (the named greeting with `firstName`, any new "To do" header), **hardcode English + `// TODO: Add "<EN>" / "<SV>" to Lokalise`** per CLAUDE.md — never touch `strings.xml`. Decide: greeting via a `%1$s` placeholder (new key) vs client-side `"Hi " + firstName`.
- **Accessibility:** each new chip/tile gets a `contentDescription` or `mergeDescendants`; carousel exposes page semantics; decide whether the greeting headline stays `hideFromAccessibility` or becomes a heading.
- **Demo mode:** `GetHomeDataUseCaseDemo` must populate `firstName` and a sample `addonBannerInfos` list so the new sections aren't empty in demo (D11). ✓

## 11. Design-system gap inventory

**Reuse as-is:** `HedvigButton`, `HedvigIconButton`, `HedvigCard`, `HedvigNotificationCard`, `HedvigText`, `HorizontalPager`, `LazyVerticalGrid`, `HighlightLabel`, `MemberReminderCards`, `ClaimStatusCards`, `CrossSellsSection`, **`FeatureAddonBanner`**.

**Missing → wrap nearest + `// TODO`:**
- Rounded / tonal "liquid-glass" icon button (toolbar) & chip → `HedvigIconButton`/`HedvigButton` in a DS-squircle tonal `Surface` (no live blur, D13).
- Product card with sales badge → `HedvigCard` + `HighlightLabel`.
- Quick-action tile → `HedvigCard`/`Surface` with icon+text.
- Section header / drag-handle hint → `HedvigText` / `Divider` (acceptable hardcode for v1).

## 12. Implementation status (WS0–WS2 complete)

All workstreams are implemented on `feature/new-home-screen` and the branch is **build/test-verified** (`:feature-home` unit tests, `:app:compileDebugKotlin`, the nav `ExhaustiveBackStackSerializationTest`/`BackstackControllerTest`, ktlint, `:feature-home:lint`, and `:app:assembleDebug` all green). Commits are local; **not pushed / no PR opened**. **Pixel/visual QA is still required** (no screenshot framework — see checklist).

**Sections built (final Mega order):** greeting (+firstName) · quick-action carousel (Make a claim · Help & support · Contact us · Forever) · Info Card · claim cards · To-do · Offers (recommended cross-sell) · quick-action tiles (Help & support · Change address · Travel certificate) · Discover insurances · Addons. Toolbar (FirstVet · cross-sell · chat) restyled with a tonal-glass background. **The campaign carousel is deferred (see below).**

**Post-implementation product clarifications (confirmed, no code change needed):** the top-right **cross-sell icon stays "same as today"** — it still opens the cross-sell bottom sheet alongside the inline sections (our code already does this). **Help & support appearing in both the carousel and the tiles is acceptable** and the exact quick-action chip/button set is "not final" — a one-place edit when finalized.

**Judgment calls made autonomously (please review):**
- **Greeting:** renders a hardcoded `"Hi <name>"` line above the existing localized status text (no `_with_name` Lokalise string exists). `// TODO` left.
- **Toolbar glass:** minimal translucent fill behind the existing circular icons — *not* the full iOS frosted-glass; `// TODO` for design specs.
- **Quick-action tiles:** text-only cards — per-tile icons (Helipad/Reload/Travel) deferred (`// TODO`) since exact DS icon names weren't confirmed.
- **Campaign carousel:** **DEFERRED from v1.** Per product it's a curated/news-content feed (new products, selected content) — not cross-sells — and that backend feed doesn't exist yet. The cross-sell-backed placeholder was removed to avoid an Offers/Discover overlap; re-add as a content feed when the backend provides one.
- **Hardcoded strings (Lokalise `// TODO`s):** `"Hi <name>"`, `"Change address"`, the `"Discover our insurances"` and `"Addons"` headers. Other labels reuse existing keys (claim/help/inbox/referrals/travel-certificate/quick-actions title).
- **Demo mode:** `firstName="Demo"`; `addonBannerInfos=emptyList()` (Addons section not shown in demo).

**QA checklist for tomorrow:**
1. Section order/spacing match the FigJam across the Mini/Medium/Max/Mega densities.
2. Greeting "Hi <name>" reads correctly per contract status (Active/Pending/Switching/Terminated/active-in-future).
3. Toolbar glass + chip/tile styling vs the iOS design (expected to need refinement).
4. Offers (recommended) vs Discover (others) read as distinct. (Campaign carousel deferred — re-add later as a curated-content feed, not cross-sells.)
5. Tile icons + the hardcoded strings get finalized in Lokalise.
6. Run in demo mode + a real account to sanity-check empty/populated states.

**Detail pass (round 2, vs the App-P2-2026 `44:6378` reference):** Offers section now has a title above the card + a Small (40dp) CTA; quick-action tiles have the per-tile icons (Helipad/Reload/Travel) above the labels and equal heights (`IntrinsicSize.Max`); the greeting is a tall scroll-away item with the quick-action pills pinned as a `stickyHeader` below the toolbar/insets. **Still deferred / needs visual QA:** card **background/border styling** (the Figma leans on borders; ours uses fills — deferred per request); exact greeting vertical spacing + the sticky-pin appearance (incl. content scrolling under the pinned pills, and whether a hero gradient background is wanted); tile icon tint/sizing; the carousel pills' height vs the 48dp design.

**Detail pass (round 3) — hero + blur + sheet, DONE since round 2:** full-screen `blur_background` behind the screen; greeting + pills float transparently on it; from the first scrolling section down content sits on an opaque "bottom-sheet" surface with a rounded top + drag handle, and the **drag-handle lid is part of the sticky header** so it pins with the pills. Scrolling sections **clip** their content below the pinned header (`stickyHeaderBottomPx` via `onPlaced` + `drawWithContent`/`clipRect`) so nothing bleeds through the transparent pills. **Collapsing hero:** the greeting sits low in a tall hero (fixed `addedSpacePx` above it, not viewport-fill) and the hero shrinks by a **bounded** amount on scroll (`maxCollapsePx`, speed-up `1 + maxCollapse/distance`), so portrait/landscape both behave and the scroll doesn't race the finger. Sparse/empty state confirmed acceptable (sheet ends, blur shows below). Position tracking uses `onPlaced` (not `onGloballyPositioned`); viewport height captured by intercepting the outer Box's `.layout`.

## 13. Next-steps scoping (questions awaiting input — opened 2026-06-26)

Each item: **Q** = decision needed, **Default** = recommendation if no override, **Decision** = filled in as answered.

### A. Layout & responsiveness
1. **Wide/rail layout.** Content is a full-width `LazyColumn` on expanded-width windows (rail nav). **Q:** in scope for this cut or follow-up? If in scope — max content width (default ~600dp centered)? Pills + sheet also capped, or full-bleed while only cards cap? **Default:** follow-up; if done, ~600dp cards, blur full-bleed. **Decision:** _pending_

### B. Visual / design-system fidelity
2. **Card backgrounds/borders.** Figma uses borders; we use fills. **Q:** wait for the design-system to ship bordered cards and swap, or hand-roll in feature-home now? **Default:** wait for DS. **Decision:** _pending_
3. **"Discover our insurances" rows over the blur** (currently transparent). **Q:** give them a surface or intentionally show the blur through? **Default:** leave transparent. **Decision:** _pending_
4. **Toolbar "glass."** Minimal tonal fill today, not full frosted. **Q:** accept minimal for v1 or do the full treatment (need design specs)? **Default:** ship minimal. **Decision:** _pending_
5. **Component fine-tuning** (tile icon tint/size, chip pill height vs 48dp, sheet corner radius). **Q:** dedicated polish pass or close enough? **Default:** close enough; revisit with DS pass. **Decision:** _pending_

### C. Content / data decisions (need product/design)
6. **Cross-sell allocation** (Offers vs Discover). **Q:** is "recommended → Offers, others → Discover" correct, or explicit rules? **Default:** keep current split. **Decision:** _pending_
7. **Section order** (default "Mega"). **Q:** keep, or finalized order from design? **Default:** keep Mega. **Decision:** _pending_
8. **"Contact us" chip target** (currently inbox). **Q:** inbox or new conversation? **Default:** inbox. **Decision:** _pending_
9. **Final quick-action set** (chips + tiles, with Help & support in both). **Q:** locked? **Default:** locked as-is. **Decision:** _pending_

### D. Deferred features (blocked / larger)
10. **Campaign / curated-content carousel** (removed from v1, needs content-feed backend). **Q:** backend timeline? Leave a marked seam or fully out of mind? **Default:** out of mind; re-add when backend exists. **Decision:** _pending_
11. **5→3 tab restructure** (separate workstream). **Q:** coming soon (design home with it in mind) or genuinely later? **Default:** later. **Decision:** _pending_

### E. Strings & accessibility
12. **Lokalise strings** (`"Hi <name>"`, `"Your quotes"`, `"Change address"`, `"Discover our insurances"`, `"Addons"` hardcoded w/ TODOs). **Q:** merge blocker or fast-follow? Want a prepared key list (EN/SV)? **Default:** fast-follow; I prep the list. **Decision:** _pending_
13. **Accessibility pass** (semantics for pinned header, drag handle, tiles, toolbar icons). **Q:** before merge or follow-up? Scope = new components or full-screen audit? **Default:** follow-up, new components. **Decision:** _pending_

### F. Code-quality / robustness
14. **`reservedPx` floor** (`pinnedTopOffset + 132.dp` guess; breaks if greeting grows, e.g. two-line `ActiveInFuture`). **Q:** derive from measured greeting or keep constant? **Default:** derive it. **Decision:** _pending_
15. **Extract `CollapsingHero` + dedup helpers** (Insurances tab reportedly has same overlap). **Q:** extract now or leave inline until a second consumer appears? **Default:** leave inline (YAGNI). **Decision:** _pending_
16. **Nested-scroll collapsing toolbar** (big hero + true 1× scroll). **Q:** current bounded collapse final, or revisit later? **Default:** final for now. **Decision:** _pending_

### G. Shipping
17. **Visual QA** (loading/error/active-in-future/terminated/pending; demo + real; Mini/Medium/Max/Mega). **Q:** who runs it, when; is design sign-off part of the gate? **Decision:** _pending_
18. **Merge to `develop`** (nothing pushed yet). **Q:** what's the gate to open the PR (QA + strings + design sign-off, or merge sooner + fast-follow)? **Decision:** _pending_

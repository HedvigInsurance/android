# Feature flag cleanup — working notes

Scratch doc to resume the feature-flag audit/cleanup later. Not meant to be committed
long-term. Last updated 2026-05-30.

## Context

We added the `PUPPY_GUIDE` kill switch (`disable_puppy_guide`) and, while doing so,
reworked how flag defaults/bootstrap work. The reasoning is documented permanently in
`app/featureflags/feature-flags/FEATURE_FLAG_DEFAULTS.md` and pointed to
from the repo `CLAUDE.md` ("Working with Feature Flags"). This notes file is about the
*next* step: retiring stale flags to reduce clutter.

## Important caveat

Code only reveals each flag's **age** and **read sites** — NOT the actual flip history or
current rollout %. That lives in the Unleash dashboard. Every "make permanent"
recommendation below is conditional on confirming in Unleash that the flag is at 100% in
production before removing it.

Key distinction:
- **Rollout flags** (gradually turn a new feature on) are meant to die once at 100%. These
  are the clutter.
- **Operational kill switches** (`UPDATE_NECESSARY`, `DISABLE_CHAT`) are meant to live
  forever so we can react to incidents without a release. Age is irrelevant for these.

## Audit of all 12 flags

| Flag | Added | Read sites (excl. provider/tests) | Type | Recommendation |
|---|---|---|---|---|
| DISABLE_REDEEM_CAMPAIGN | 2025-03 | **none** | kill switch | **Delete — dead code** |
| MOVING_FLOW | 2022-05 | insurances, help-center | rollout | Make permanent (likely ON) |
| PAYMENT_SCREEN | 2023-02 | profile, insurances, help-center | rollout | Make permanent (likely ON) |
| EDIT_COINSURED | 2023-12 | insurances, help-center, reminders | rollout | Make permanent (likely ON) |
| HELP_CENTER | 2023-12 | home, profile | kill switch | Make permanent (now core — puppy guide lives in it) |
| TRAVEL_ADDON | 2024-12 | movingflow, addon-purchase, changetier, addons | rollout | Keep — still new, wide surface |
| ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES | 2025-03 | chat | rollout | Keep — recent |
| ENABLE_CLAIM_HISTORY | 2026-04 | home, profile, delete-account | rollout | Keep — brand new |
| PUPPY_GUIDE | 2026-05 | help-center | kill switch | Keep — just shipped |
| UPDATE_NECESSARY | 2022-05 | HedvigAppState | operational kill switch | **Keep forever** — app-version gate |
| DISABLE_CHAT | 2023-09 | home | operational kill switch | **Keep forever** — disable chat during incidents |
| TERMINATION_FLOW | 2023-02 | insurances, data-termination | kill switch | Keep / confirm — may be deliberate legal/ops switch |

## Action plan

### 1. Safe, unambiguous win — delete `DISABLE_REDEEM_CAMPAIGN` (dead code) — DONE 2026-05-30
Was defined in the enum, `unleashKey`, and the provider, but read **nowhere** in the app.
Removed from all three files.

### 2. Make permanent — after confirming 100% rollout in Unleash
`MOVING_FLOW`, `PAYMENT_SCREEN`, `EDIT_COINSURED`, `HELP_CENTER`. For each:
- Delete the enum value, `unleashKey` arm, and provider arm.
- At each read site, delete the flag branch and collapse to the enabled path (remove the
  `combine` / `flatMapLatest` arm that gated on the flag).
- Delete the related test cases / `FakeFeatureManager` entries.

Read sites to touch (from the audit grep):
- MOVING_FLOW: `GetInsuranceContractsUseCase`, `GetMemberActionsUseCase`.
- PAYMENT_SCREEN: `ProfileViewModel`, `GetMemberActionsUseCase`, (+ insurances test data).
- EDIT_COINSURED: `GetInsuranceContractsUseCase`, `GetInsuranceForEditCoInsuredUseCase`,
  `GetMemberActionsUseCase`, `GetNeedsCoInsuredInfoRemindersUseCase`.
- HELP_CENTER: `GetHomeDataUseCase`, `ProfileViewModel` (+ profile/home tests).

### 3. Keep — no action
Operational kill switches: `UPDATE_NECESSARY`, `DISABLE_CHAT`.
Too new / still rolling out: `TRAVEL_ADDON`, `ENABLE_VIDEO_PLAYER_IN_CHAT_MESSAGES`,
`ENABLE_CLAIM_HISTORY`, `PUPPY_GUIDE`.

### 4. Confirm intent — `TERMINATION_FLOW`
Old enough to retire, but `disable_termination_flow` may be a deliberate legal/operational
lever. Check before treating as stale.

## Where things live (for when resuming)
- Enum: `app/featureflags/feature-flags/src/commonMain/.../flags/Feature.kt`
- Key map: `app/featureflags/feature-flags/src/androidMain/.../flags/FeatureUnleashKey.kt`
- Resolution + negation: `app/featureflags/feature-flags/src/androidMain/.../flags/UnleashFeatureFlagProvider.kt`
- Bootstrap: `app/featureflags/feature-flags/src/androidMain/.../HedvigUnleashClient.kt`
- Verify a build with: `./gradlew :feature-flags:ktlintFormat :feature-flags:compileAndroidMain`
- iOS equivalent flags live in the separate repo at `../ugglan` (different flag names; e.g.
  `help_center` positive there vs `disable_help_center` here).

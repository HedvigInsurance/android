# Phase 3 audit — HedvigNavKey polymorphic registration completeness

## Exact counts
- Concrete HedvigNavKey subtypes DECLARED (inventory): 97
  - feature-contributed: 96
  - :app-local, not feature-contributed (RootGraph): 1
- Distinct subtypes REGISTERED via subclass(...) (prod, excl test keys): 96
- DECLARED but NOT registered (crash risk): 1 (RootGraph — false positive, dead code)
- REGISTERED but NOT declared (stale/typo): 0

Inventory: docs/superpowers/plans/navkey-inventory.txt (97 names + total line).

## Missing registrations
None that are a runtime crash risk. Only RootGraph is unregistered:
- File: app/app/src/main/kotlin/com/hedvig/android/app/navigation/RootGraph.kt (module :app)
- @Serializable @SerialName("root") object RootGraph : HedvigNavKey
- Zero usages in app/app/src/main outside its own file -> Nav2 holdover, never pushed on the Nav3
  back stack, so never serialized. No subclass() needed while unused.
- If it ever becomes a real key: add `subclass(RootGraph::class)` to an :app provider and bump
  expectedRegisteredSubtypeCount to 97. (Better: delete it as dead code — out of scope.)
All 96 feature keys registered exactly once.

## Serial-name collisions
None. Only one class-level @SerialName exists: @SerialName("root") on RootGraph. All other keys use
the default discriminator (FQ class name), unique per class. The @SerialName("contractId") x6 and
@SerialName("id") x2 are PROPERTY-level (deep-link param alignment), not class discriminators.

## Per-feature test gap
Design called for ~23 per-feature round-trip tests. Actual: 0. Pre-existing serialization tests:
- app/app/src/test/.../BackstackSerializationTest.kt (weak; own local 6-key module)
- app/navigation/navigation-common/src/commonTest/.../HedvigNavKeySavedStateTest.kt (2 fake keys)
Gap: ~23. Closed centrally by the new exhaustive test (stronger: exercises the real merged module).

## Test added
Path: app/app/src/test/kotlin/com/hedvig/android/app/navigation/ExhaustiveBackstackSerializationTest.kt
Class: ExhaustiveBackstackSerializationTest
- Reconstructs the EXACT production merged module by invoking all 23 feature
  provide*SerializersModule() default-interface methods (anonymous object : XProvider {}) folded with
  the production merge() helper.
- 66 of 97 keys are internal and unnameable from :app, so the exhaustive guard uses
  SerializersModuleCollector.dumpTo(...) to enumerate registered HedvigNavKey subtypes and asserts
  distinct count == expectedRegisteredSubtypeCount (96). A missing subclass() lowers the count and fails.
Tests:
  1. every concrete HedvigNavKey subtype is registered in the production module (count==96, no dup) [crash guard]
  2. no two registered subtypes share a serial name (discriminator uniqueness)
  3. every registered subtype resolves a working polymorphic serializer
  4. publicly constructible keys round-trip through the production module (18 public keys)
Keys NOT individually round-tripped (registration still covered by count guard): all 66 internal keys,
plus public keys needing domain value types: RemoveAddonsKey, ClaimChatKey variants, ClaimDetailsKey,
CoInsuredAddInfoKey, CoInsuredAddOrRemoveKey, EditCoInsuredTriageKey, StartTierFlowKey,
StartTierFlowChooseInsuranceKey, ChooseTierKey, AddonPurchaseKey, TravelAddonTriageKey,
InsuranceContractDetailKey, ShowCertificateKey.
Not run (orchestrator owns gradle). If it fails at 95, diff registered set vs navkey-inventory.txt.

## Notes
- :app is hedvig.android.application; JVM unit tests resolve androidMain providers of feature-claim-chat
  and feature-remove-addons; all 23 providers are public.
- kotlinx-serialization 1.10.0; dumpTo + SerializersModuleCollector available.

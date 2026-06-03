# Deep-link pattern → HedvigNavKey inventory

Branch: `eng/metro-nav3-pr2-nav2-to-nav3` (post Nav2→Nav3 migration).

## How patterns are produced

- Every URI pattern lives in `HedvigDeepLinkContainer` (interface + `HedvigDeepLinkContainerImpl`)
  at `app/navigation/navigation-core/.../HedvigDeepLinkContainer.kt`.
- Each pattern list is built per deep-link host: `baseDeepLinkDomains = deepLinkHosts.map { "https://$it" }`,
  so every logical pattern below expands to **3 concrete URIs** (one per host).
- `deepLinkHosts` (per flavor, `CommonHedvigBuildConstants.deepLinkHosts`):
  - Production: `link.hedvig.com`, `www.hedvig.com/deeplink`, `hedvig.page.link`
  - Staging:    `link.dev.hedvigit.com`, `dev.hedvigit.com/deeplink`, `hedvigtest.page.link`
  - Develop:    `link.dev.hedvigit.com`, `dev.hedvigit.com/deeplink`, `hedvigdevelop.page.link`
- Each feature module contributes a `DeepLinkMatcherProvider` (`@ContributesIntoSet(AppScope::class)`)
  in `app/feature/*/.../navigation/*DeepLinks.kt`. `:app` flattens all `matchers()` into a single
  `HedvigDeepLinkMatcher`.
- `uriDeepLinkMatchers(patterns, serializer)` wraps each pattern in a Nav3 `UriDeepLinkMatcher`.

## Inventory (logical pattern suffix → target HedvigNavKey)

`{...}` = required/optional argument extracted from the URI. Path shown without the host prefix.

| # | Pattern suffix | Container field | Target HedvigNavKey | Provider file |
|---|----------------|-----------------|---------------------|---------------|
| 1 | `/` (bare host) | `home` | `HomeKey` | HomeDeepLinks.kt |
| 2 | `/submit-claim` | `claimFlow` | `HomeKey` | HomeDeepLinks.kt |
| 3 | `/help-center` | `helpCenter` | `HelpCenterHomeKey` | HelpCenterDeepLinks.kt |
| 4 | `/help-center/topic?id={id}` | `helpCenterCommonTopic` | `HelpCenterTopicKey(id)` | HelpCenterDeepLinks.kt |
| 5 | `/help-center/question?id={id}` | `helpCenterQuestion` | `HelpCenterQuestionKey(id)` | HelpCenterDeepLinks.kt |
| 6 | `/puppy-guide` | `puppyGuide` | `PuppyGuideKey` | HelpCenterDeepLinks.kt |
| 7 | `/insurances` | `insurances` | `InsurancesKey` | InsurancesDeepLinks.kt |
| 8 | `/contract` | `contractWithoutContractId` | `InsurancesKey` | InsurancesDeepLinks.kt |
| 9 | `/contract?contractId={contractId}` | `contract` | `InsuranceContractDetailKey(contractId)` | InsurancesDeepLinks.kt |
| 10 | `/edit-coinsured` | `editCoInsuredWithoutContractId` | `EditCoInsuredTriageKey(contractId=null)` | EditCoInsuredDeepLinks.kt |
| 11 | `/edit-coinsured?contractId={contractId}` | `editCoInsured` | `EditCoInsuredTriageKey(contractId)` | EditCoInsuredDeepLinks.kt |
| 12 | `/edit-coowners` | `editCoOwners` | `EditCoOwnersTriageDeepLinkKey` | EditCoInsuredDeepLinks.kt |
| 13 | `/terminate-contract?contractId={contractId}` | `terminateInsurance` | `TerminateInsuranceKey(insuranceId=contractId)` | TerminateInsuranceDeepLinks.kt |
| 14 | `/forever` | `forever` | `ForeverKey` | ForeverDeepLinks.kt |
| 15 | `/profile` | `profile` | `ProfileKey` | ProfileDeepLinks.kt |
| 16 | `/contact-info` | `contactInfo` | `ContactInfoKey` | ProfileDeepLinks.kt |
| 17 | `/eurobonus` | `eurobonus` | `EurobonusKey` | ProfileDeepLinks.kt |
| 18 | `/connect-payment` | `connectPayment` | `TrustlyKey` | TrustlyDeepLinks.kt |
| 19 | `/direct-debit` | `directDebit` | `TrustlyKey` | TrustlyDeepLinks.kt |
| 20 | `/payments` | `payments` | `PaymentsKey` | PaymentsDeepLinks.kt |
| 21 | `/manual-charge` | `manualCharge` | `ManualChargeKey` | PaymentsDeepLinks.kt |
| 22 | `/payout` | `payout` | `PayoutAccountKey` | PayoutAccountDeepLinks.kt |
| 23 | `/delete-account` | `deleteAccount` | `DeleteAccountKey` | DeleteAccountDeepLinks.kt |
| 24 | `/chat` | `chat` | `InboxKey` | ChatDeepLinks.kt |
| 25 | `/inbox` | `inbox` | `InboxKey` | ChatDeepLinks.kt |
| 26 | `/conversation/{conversationId}` | `conversation` | `ChatKey(conversationId)` | ChatDeepLinks.kt |
| 27 | `/travel-addon` | `travelAddon` | `TravelAddonTriageKey(source=TRAVEL_DEEPLINK)` | AddonPurchaseDeepLinks.kt |
| 28 | `/travel-addon?contractId={contractId}` | `travelAddonWithContractId` | `TravelAddonTriageKey(source=TRAVEL_DEEPLINK, contractId)` | AddonPurchaseDeepLinks.kt |
| 29 | `/car-plus-addon` | `carAddon` | `TravelAddonTriageKey(source=CAR_ADDON_DEEPLINK)` | AddonPurchaseDeepLinks.kt |
| 30 | `/car-plus-addon?contractId={contractId}` | `carAddonWithContractId` | `TravelAddonTriageKey(source=CAR_ADDON_DEEPLINK, contractId)` | AddonPurchaseDeepLinks.kt |
| 31 | `/travelCertificate` | `travelCertificate` | `TravelCertificateKey` | TravelCertificateDeepLinks.kt |
| 32 | `/change-tier` | `changeTierWithoutContractId` | `StartTierFlowChooseInsuranceKey` | ChooseTierDeepLinks.kt |
| 33 | `/change-tier?contractId={contractId}` | `changeTierWithContractId` | `StartTierFlowKey(contractId)` | ChooseTierDeepLinks.kt |
| 34 | `/claim-details?claimId={claimId}` | `claimDetails` | `ClaimDetailsKey(claimId)` | ClaimDetailsDeepLinks.kt |
| 35 | `/insurance-evidence` | `insuranceEvidence` | `InsuranceEvidenceKey` | InsuranceEvidenceDeepLinks.kt |
| 36 | `/move-contract` | `moveContract` | `SelectContractForMovingKey` | MovingFlowDeepLinks.kt |
| 37 | `/pet-id` | `petIdWithoutContractId` | `AddChipIdTriageKey(contractId=null)` | ChipIdDeepLinks.kt |
| 38 | `/pet-id?contractId={contractId}` | `petIdWithContractId` | `AddChipIdTriageKey(contractId)` | ChipIdDeepLinks.kt |

**38 logical patterns** across **19 provider files** (incl. `MovingFlowDeepLinks.kt`, which is not
under a `*/navigation/` path but under `feature-movingflow/.../movingflow/`).
Each logical pattern × 3 hosts = the concrete URI set the matcher registers.

## Container fields with no matcher

None. Every field on `HedvigDeepLinkContainer` (including `moveContract`) is consumed by exactly one
provider. `buildDeepLink(suffix)` is a helper, not a pattern.

## Precedence notes (relevant to overlapping paths)

`HedvigDeepLinkMatcher.match()` keeps the result with the highest `UriMatchResult.compareTo`
(strict `> 0`, so ties keep the first-registered matcher). Order: exact-path > more path-args >
has-args > more total args.

- `/contract` vs `/contract?contractId=…`: both are `isExactPath` (no path args/wildcard). With a
  `contractId` query present, the query-arg matcher (`InsuranceContractDetailKey`) wins on
  "has arguments". Without `contractId`, the `InsuranceContractDetailKey` matcher fails to decode
  (its `contractId` field has no default), leaving only `InsurancesKey`. Same shape for
  `/edit-coinsured`, `/change-tier`, `/travel-addon`, `/car-plus-addon`, `/pet-id`.
- `terminateInsurance` only has the `?contractId=` form; `TerminateInsuranceKey.insuranceId`
  is nullable with a default, so a bare `/terminate-contract` would still decode (but is not a
  manifest-advertised distinct path).

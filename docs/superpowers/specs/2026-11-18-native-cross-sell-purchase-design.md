# Native cross-sell purchase: design

Date: 2026-11-18. Owners: Hugo Linder (backend and app).

## Intent

Today a member who taps a cross-sell in the app is sent to hedvig.com to buy it. Each handoff loses members: a
browser opens, the member re-identifies with a personnummer, and fills in a form asking for things Hedvig already
knows. The goal is **more conversion from cross-sells**: a member taps a cross-sell, reads a native product page,
buys it in the app and signs with BankID, without leaving.

The near-term milestone is a **demo of every in-scope product, end to end, on staging, in about two days**. After
the demo, products are rolled out to members one by one (no A/B test).

## Goals

- One native product page per cross-sell product, whose button starts a native purchase flow.
- One generic, server-driven purchase flow in the app: form, offers, summary, BankID signing, success.
- Every Swedish product sold on hedvig.com purchasable through it on staging: accident, trygghetsförsäkring
  (payment protection), dog, cat, car, apartment (rental, BRF, student), villa (house), fritidshus (vacation home).
  Members do not necessarily have home insurance: many hold only a pet or car contract, so home products are
  real cross-sells.
- Forms prefilled from what Hedvig already knows about the member, so they are shorter than the web's.
- Adding a product, or changing a product's form, needs no app release once the field types it uses exist.

## Non-goals

- An A/B test against the web handoff. Products are simply rolled out.
- Changing an existing home insurance to a different home (apartment → villa). That is the moving flow, which
  already exists in the app.
- Markets other than Sweden.
- iOS UI. The API is designed so iOS can build on it later.
- Polished rollout tooling beyond the gating below.

## Gating and rollout *(Claude's call)*

- **Backend, per product:** storefront has no server-side Unleash (its experiments are assigned in the app), so the
  existing global `inAppPurchaseEnabled` boolean is replaced by a per-environment config list
  `inAppPurchaseProducts` of enabled `InAppProduct`s. A product not in the list gets `NOT_AVAILABLE` and
  `CrossSell.inAppProduct = null`. `productName.available()` is not touched, so web sales are unaffected. Rolling
  out a product = adding it to the production list and deploying; staging lists every product.
- **App:** the product page stays behind the existing `SHOW_PRODUCT_PAGES` Unleash flag. The native form is only
  reachable from the product page. Page flag off → today's behaviour (web), regardless of `inAppProduct`.

## Anti-goals (failure even if it works)

- Product-specific logic in the app (`if (product == CAR)`). The app renders field types; the backend decides
  fields, defaults and validation.
- A product whose page still opens the web after the demo claims it is native.
- Members who don't have the flag seeing anything change: when the backend says a cross-sell has no
  `inAppProduct`, the app behaves exactly as today (product page or web).
- Asking a member for data we already have.

## Current state (what exists)

| Piece | Where | State |
|---|---|---|
| Generic purchase API (`inAppPurchaseForm`, `inAppPurchaseQuote`, `inAppPurchaseSign`), per-product `InAppProductHandler`, `CrossSell.inAppProduct` | `storefront` branch `feat/in-app-purchase` (Mariia) | 8 commits, 51 behind master. Handlers: `ACCIDENT`, `PAYMENT_PROTECTION`. Field types: `NUMBER`, `DATE`, `SINGLE_SELECT`. Has tests. |
| Product page data (`crossSellProductInfo`: hero, intro, USPs, story carousel, from the product's Storyblok web page) | `storefront` branch `feat/cross-sell-options` | 3 ahead, 21 behind master |
| App purchase flow (form → offers → product info → summary → BankID QR/autostart → success) | `android` PR #3305, branch `feat/native-purchase-flow`, module `feature-in-app-purchase` (+ `-navigation`) | 9 commits, 280 behind develop. Contains mocks and "publish to staging" workflow commits. |
| App product page (`CrossSellProductKey`, options sheet, carousel with WebP animation) | `android` PR #3362, branch `feat/product-pages`, module `feature-product-in-app` | 28 behind develop. Button opens the web store URL. Has tests. |
| Per-product native forms incl. car reg, breed picker, extra-buildings editor, home picker | `android` branches `feat/in-app-*-purchase`, `feat/extra-buildings-in-house-purchase` (Hugo, May) | ~2300 behind develop, Koin + old nav. Source for UI widgets only. |
| Web form definitions (field lists per product) | `racoon/apps/store/src/features/priceCalculator/priceTemplates/SE_*.ts` | Reference for which data each product needs. |

The backend's `PricingFormData` (storefront `shop/priceintent/PricingFormData.kt`) already carries every input the
in-scope products need (`street`, `zipCode`, `livingSpace`, `ancillaryArea`, `yearOfConstruction`,
`numberOfBathrooms`, `isSubleted`, `extraBuildings`, `hasWaterConnected`, `multipleOwners`, `registrationNumber`,
`mileage`, `name`, `birthDate`, `breeds`, `gender`, `isNeutered`, `isPreviousDogOwner`, `hasOutsideAccess`,
`numberCoInsured`, `employmentType`). Quoting goes through the same shop-session/price-intent pipeline as the web, so
underwriting and pricing are unchanged.

## Approaches considered

- **A. Fully server-driven (chosen).** Each product is a backend `InAppProductHandler` returning typed fields; the
  app renders field types generically. Adding/changing a product is backend-only once its field types exist; iOS
  can reuse it; prefill lives server-side. Cost: richer field types on both sides.
- **B. Hybrid.** Generic flow for simple products, hand-built native forms posting to per-product mutations for car,
  pet and house. Faster for complex products, but two paths and an app release per form change.
- **C. Port the May branches as-is.** Product logic in the app, calling the web's price-intent API directly.
  Rejected: duplicates what the handler pattern already solves.

Hugo picked A because it serves conversion best: forms are short via server-side prefill and can be tuned without
an app release.

## Design

### Flow

```
Cross-sell tapped (Home, Insurances, cross-sell sheet, chat card, onboarding)
  └─ product page (feature-product-in-app)          ← CrossSellProductKey
       button:
         crossSell.inAppProduct != null → native form (feature-in-app-purchase)
         else                           → web store URL (today's behaviour)
  └─ form  → inAppPurchaseQuote → offers (pick coverage level / deductible)
  └─ summary → inAppPurchaseSign → BankID (autostart, QR fallback) → poll signing
  └─ success → popBackstack to where the cross-sell was tapped
```

Generic cross-sells (`HOME`, `PET`) keep the options sheet from #3362, which lists the concrete products
(e.g. dog / cat; villa / fritidshus) and opens that product's page.

### One product page

#3362's page is the single product page. #3305's separate product-info screen is removed; its FAQ moves onto the
product page below the carousel, and coverage levels are shown where the member picks one, on the offers step.
*(Claude's call.)* The page's data comes from
`crossSellProductInfo` (Storyblok web page) plus `inAppPurchaseForm.product` (name, FAQ, variants), or the backend
folds the latter into `crossSellProductInfo`; the builder picks whichever is a smaller change.

### Backend (storefront)

Base: rebase `feat/in-app-purchase` and `feat/cross-sell-options` onto master, together.

New `InAppProduct` values and handlers (field ids are the `PricingFormData` names):

| Product | Fields (prefill source) |
|---|---|
| `DOG` | name (TEXT), birth date (DATE), breeds (SEARCHABLE_SELECT, multi), gender (SINGLE_SELECT), neutered (SINGLE_SELECT yes/no), previous dog owner (SINGLE_SELECT yes/no), start date |
| `CAT` | name, birth date, breeds (SEARCHABLE_SELECT, multi), gender, neutered, outside access (yes/no), start date |
| `CAR` | registration number (TEXT), annual mileage (SINGLE_SELECT, web's options), start date |
| `APARTMENT_RENT`, `APARTMENT_BRF`, `APARTMENT_STUDENT` | street, postal code (TEXT), living space (NUMBER), household size (NUMBER), start date. Student: the web's student constraints (age, size, household) enforced by validation/underwriting, not by the app. |
| `HOUSE` | street, postal code (TEXT), living space, ancillary area, year built, bathrooms (NUMBER), household size (NUMBER), subleted (yes/no), extra buildings (EXTRA_BUILDINGS), start date |
| `VACATION_HOME` | street, postal code (TEXT, not prefilled: different address), living space, year built, bathrooms, water connected, multiple owners, subleted (yes/no), extra buildings, start date |

Existing: `ACCIDENT` (household size, prefilled), `PAYMENT_PROTECTION` (employment type).

**Prefill** comes from the member's own contracts when they exist: household size from any home contract. Address
is not prefilled for home products, since a member buying one has no home contract. *(Claude's call; prefilling
from the population register is a later improvement.)*

**Eligibility per product:**

| Products | Rule |
|---|---|
| Dog, cat, car | Always eligible: members may insure a second dog, cat or car. `ALREADY_INSURED` no longer applies. |
| Apartment (all three), villa | No extra rule. The cross-sell service already never offers home products to a member who has a home contract, so the in-app flow is only reached by members without one. |
| Fritidshus | Always eligible (a second home at a different address). |
| Accident, trygghetsförsäkring | Eligible when the member has no contract for that product (as today). |

New field types:

- `TEXT`: free text with optional `pattern`/max length hint; validation errors come back as `fieldErrors`.
- `SEARCHABLE_SELECT`: like `SINGLE_SELECT` but long option lists, filterable, `maxSelections` (breeds allow
  mixes). Options delivered inline in the form response. *(Claude's call: breed lists are a few hundred entries;
  inline keeps the API one round trip.)*
- `EXTRA_BUILDINGS`: list of {building type (from options), area m², has water}. Value sent as JSON per entry.
- Yes/no questions use `SINGLE_SELECT` with two options, not a new BOOLEAN type. *(Claude's call.)*

Car registration lookup and underwriting stay inside the price-intent confirm; failures surface as `fieldErrors` on
the registration field, or `userError`.

`CrossSell.inAppProduct` is set for every cross-sell whose product has a handler and is enabled, so the app needs
no product list.

### App (android)

Base: rebase #3305 and #3362 onto develop and combine them (one branch, or #3362 first then #3305 on top). Drop the
staging-publish workflow commits and the mocks.

- `feature-in-app-purchase`: form screen renders a list of fields by type. Add renderers for `TEXT`,
  `SEARCHABLE_SELECT`, `EXTRA_BUILDINGS`; port the breed picker and extra-buildings editor UI from the May
  branches into these generic renderers (no product names in them).
- `feature-product-in-app`: button routes on `inAppProduct` as above. Navigation into the purchase flow goes
  through the `feature-in-app-purchase-navigation` key; the product key carries the `crossSellId` only, the flow
  resolves `inAppProduct` in the data layer (no type discriminator nav args).
- Success pops back to where the cross-sell was tapped (arrange the back stack at navigation time; `popBackstack`,
  never `navigateUp`, for in-content buttons).
- `cross_sell_clicked` analytics: keep firing on product-page button; add a purchase-completed event if trivially
  available. *(Claude's call: nice for measuring conversion, not demo-critical.)*

### Demo

Staging backend, develop app build (`com.hedvig.dev.app`), product toggles and `SHOW_PRODUCT_PAGES` on for the demo
members. Real quotes from staging pricing, BankID test signing. Two demo members:

- one with only a pet contract, to show the home products (apartment, villa) plus car, accident and
  trygghetsförsäkring, and buying a second pet;
- one with an apartment contract, to show household prefill on accident and that home products are not offered.

### Priority if time runs short

Products are cut before tests: whatever is demoed is properly tested. Order of building, and the reverse of
cutting:

1. Accident, trygghetsförsäkring (handlers exist; proves the whole flow)
2. Dog, cat
3. Car
4. Apartment (rental, BRF, student)
5. Villa, fritidshus (extra buildings is the largest new field type)

### Testing

- Backend: one handler test per product (fields, prefill, `toPricingFormData`), validator tests for each new field
  type, a quote-and-sign test per new product in the style of `InAppPurchaseTest`.
- App: presenter tests for the form (validation errors mapped to fields, multi-select, extra buildings add/remove),
  previews of each renderer rendered with the `render-previews` skill, `ExhaustiveBackStackSerializationTest` passes.

## Left to the builder

- Exact Storyblok/I18n keys for new field titles (hardcode English + Lokalise TODO in the app where needed; the
  backend uses `I18n.get` with new keys).
- Where breed options come from in storefront (whatever the web's breed field uses).
- Mileage options and pattern for registration number: copy the web's.
- Whether product-page FAQ comes from `crossSellProductInfo` or `inAppPurchaseForm.product`.
- Order of PRs, provided each is reviewable and the app compiles against the staging schema.

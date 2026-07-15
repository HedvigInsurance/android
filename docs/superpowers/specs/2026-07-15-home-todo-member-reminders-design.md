# Home "To do" member-reminder list

Date: 2026-07-15
Branch: feature/new-home-screen

## Problem

The new home design (Figma `App P2 2026`, node `429-7853`, "To do" section) renders
action-required member reminders as a compact grouped list of tappable rows. In the current
app, member reminders render as full `HedvigNotificationCard`s in a horizontal pager/carousel,
the same component on both Home and Profile.

In the new design language the notification-card look is reserved for VeryImportantMessages.
Member reminders on home must therefore use a different rendering: a "To do" grouped card of
rows. This change is scoped to the **home screen only**; Profile keeps its existing card
carousel.

## Scope decisions (agreed)

- **Home only.** Profile continues to call the existing `MemberReminderCards` carousel. The
  shared `member-reminders-ui` card composables are left untouched.
- **Action-required reminders only.** The "To do" list renders the six reminder variants that
  represent a required action with a single destination. Purely informational reminders are
  excluded from the home "To do" list:
  - `UpcomingRenewal` — excluded. **This is a behavior change**: renewal prompts currently
    render on home as Info cards and will no longer appear there.
  - `EnableNotifications` — excluded. No behavior change on home (home already passes
    `notificationPermissionState = null`, so it renders nothing today).

## Reminder → row mapping

The six action-required variants and how each row renders and behaves:

| `MemberReminder` variant | Icon | Title (English, needs Lokalise) | Row tap → existing lambda |
|---|---|---|---|
| `PaymentReminder.TerminationDueToMissedPayments` | `HedvigIcons.WarningOutline` | "Your payment is overdue" | `onNavigateToNewConversation` |
| `PaymentReminder.ConnectPayment` | `HedvigIcons.Card` | "Missing payment method" | `navigateToConnectPayment` |
| `PaymentReminder.ConnectPayout` | `HedvigIcons.Card` | "Missing payout method" | `navigateToConnectPayout` |
| `CoInsuredInfo` | `HedvigIcons.ProfileOutline` | "Add co-insured" | `navigateToAddMissingInfo(contractId, coInsuredType)` |
| `ContactInfoUpdateNeeded` | `HedvigIcons.InfoOutline` | "Update contact details" | `navigateToContactInfo` |
| `MissingChipId` | `HedvigIcons.ID` | "Missing pet chip-ID" | `navigateToChipId` |

Every row uses the same subtitle: "Requires action".

Icons and the `ContactInfoUpdateNeeded` copy are pending the user's visual review. The old
`ContactInfoUpdateNeeded` card renders `NotificationPriority.Info` (which uses
`HedvigIcons.InfoFilled`); the outline variant `InfoOutline` is chosen here to match the outline
style of the other rows.

## Visual spec (from Figma)

One grouped card containing the rows:

- Card corner radius: `CornerRadiusXl` (16dp).
- Card surface: an off-white fill (Figma `fills/opaque/negative`, #FAFAFA). Use the semantic
  card-surface token; final token pending review (see Open questions).
- Each row: `horizontal padding 12dp`, `vertical padding 16dp`, `16dp` gap between icon and text.
  - Leading 24dp icon.
  - Two-line text: title (`colorScheme.textPrimary`, 14sp / 18 line height) over
    "Requires action" (`colorScheme.signalRedText`, 14sp / 18 line height).
  - Trailing `HedvigIcons.ChevronRight`, 24dp.
- Divider between rows: `Modifier.horizontalDivider(DividerPosition.Top, show = index != 0)`,
  matching existing usages (`TravelCertificateHistory`, `ComparisonDestination`). Default divider
  color.
- Whole row is `clickable`, invoking that variant's action. No per-row button, no carousel.
- Section header text "To do" above the card, rendered by home's existing section-title pattern
  (consistent with "Quick actions", "Your quotes"). Needs a Lokalise string.

## New strings (hardcode + `// TODO Lokalise`)

None of these exist in Lokalise yet (the current member-reminder Lokalise copy is long body text,
not these short titles):

- Section header: "To do"
- Subtitle: "Requires action"
- Row titles: "Your payment is overdue", "Missing payment method", "Missing payout method",
  "Add co-insured", "Update contact details", "Missing pet chip-ID"

## Implementation shape

- **New composable** `MemberReminderToDoList` in `member-reminders-ui`
  (`app/member-reminders/member-reminders-ui/.../ui/`), alongside `MemberReminderCards`. It renders
  the grouped card of rows only (not the section header). Signature reuses the existing navigation
  lambdas, minus the ones only used by excluded types (`openUrl`, notification-permission params):

  ```kotlin
  @Composable
  fun MemberReminderToDoList(
    memberReminders: List<MemberReminder>,
    navigateToConnectPayment: () -> Unit,
    navigateToConnectPayout: () -> Unit,
    navigateToAddMissingInfo: (String, CoInsuredFlowType) -> Unit,
    onNavigateToNewConversation: () -> Unit,
    navigateToContactInfo: () -> Unit,
    navigateToChipId: () -> Unit,
    modifier: Modifier = Modifier,
  )
  ```

- **Filtering helper** so home can decide whether to show the section at all. A function that
  takes the full `List<MemberReminder>` and returns only the action-required variants in a stable
  order (preserving the existing `onlyApplicableReminders` order minus excluded types). Home checks
  `isNotEmpty()` before rendering the "To do" header + `MemberReminderToDoList`.

- **Home call site** (`HomeDestination.kt`): replace the `MemberReminderCardsWithoutNotification(...)`
  call in the member-reminders section with the "To do" header + `MemberReminderToDoList(...)`,
  gated on the filtered list being non-empty. VeryImportantMessages continue to use
  `HedvigNotificationCard`. Profile's call site is unchanged.

- **Previews** for the new component (single row, all six rows) following the existing
  `@Preview` + `HedvigTheme` + `Surface` pattern in `MemberReminderCards.kt`.

## Resolved decisions

1. **Card surface token:** the Figma `fills/opaque/negative` (#FAFAFA) already exists in
   `ColorScheme` as `fillNegative` (the same token other home cards use). Use
   `HedvigTheme.colorScheme.fillNegative`; no new token needed, no hardcoded hex.
2. **Divider color:** keep the existing default (`Modifier.horizontalDivider(DividerPosition.Top,
   show = index != 0)` with no color override), matching current usages.
3. **Row order:** follow the Figma order, with payout immediately after payin, and the variants
   not depicted in Figma appended at the end:
   1. `TerminationDueToMissedPayments` ("Your payment is overdue")
   2. `ConnectPayment` ("Missing payment method")
   3. `ConnectPayout` ("Missing payout method") — right after payin
   4. `MissingChipId` ("Missing pet chip-ID")
   5. `CoInsuredInfo` ("Add co-insured")
   6. `ContactInfoUpdateNeeded` ("Update contact details") — not in Figma, appended last

## Remaining for user review (non-blocking)

- Final icon choices (all six) and the `ContactInfoUpdateNeeded` icon + title copy.

## Out of scope

- Profile screen rendering.
- Section placement/ordering within the broader new-home-screen redesign.
- Backend / `GetMemberRemindersUseCase` changes (no data-layer change needed).

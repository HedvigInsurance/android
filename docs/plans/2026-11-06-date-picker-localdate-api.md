# Date Picker `LocalDate` API

**Goal:** Feature code never handles date picker epoch millis. The design system converts between `LocalDate` and the picker's UTC-midnight millis in one place, so a caller cannot read a picked date in the wrong time zone.

**Status:** Not started. Follow-up to the time zone fixes in `feat/smaller-ui-fixes` (commit `ea1923e924`).

## Why

Material 3's `DatePickerState` represents a calendar day as epoch millis at **UTC midnight**. This is fixed and not configurable: `selectedDateMillis`, `initialSelectedDateMillis`, `initialDisplayedMonthMillis` and `SelectableDates.isSelectableDate(utcTimeMillis)` all use it.

Our wrapper passes those millis straight through to feature code, so every caller has to convert with `TimeZone.UTC` itself. Reading them with `TimeZone.currentSystemDefault()` instead gives the right day in Sweden (UTC+1/+2) and the previous day on any phone set to a zone behind UTC (the Americas, the Azores, Hawaii, …). Two callers had exactly this bug:

- `AddCoInsuredBottomSheetContent.kt`: the saved co-insured birth date.
- `TravelCertificateDateInputViewModel.kt`: `isSelectableDate`, which greyed out the wrong days.

Nothing catches it: in Stockholm, both versions give the same result.

## Current API

`design-system-api/.../api/HedvigDatePickerState.kt`:

```kotlin
interface HedvigDatePickerState {
  val locale: CalendarLocale
  var selectedDateMillis: Long?
  var displayedMonthMillis: Long
  var displayMode: HedvigDisplayMode
  val yearRange: IntRange
  val selectableDates: HedvigSelectableDates
}

interface HedvigSelectableDates {
  fun isSelectableDate(utcTimeMillis: Long) = true
  fun isSelectableYear(year: Int) = true
}
```

Plus `HedvigDatePickerImmutableState` (`design-system-hedvig/.../datepicker/DatePicker.kt`), which takes `selectedDateMillis`, `displayedMonthMillis`, `minDateInMillis`, `maxDateInMillis`, and the `HedvigDatePicker` overload whose `onSelectedDateChanged` callback gives `Long?`.

## Proposed API

Expose `LocalDate` and convert at the boundary, inside the design system only:

```kotlin
interface HedvigDatePickerState {
  val locale: CalendarLocale
  var selectedDate: LocalDate?
  var displayedMonth: LocalDate          // any day in the month to show
  var displayMode: HedvigDisplayMode
  val yearRange: IntRange
  val selectableDates: HedvigSelectableDates
}

interface HedvigSelectableDates {
  fun isSelectableDate(date: LocalDate) = true
  fun isSelectableYear(year: Int) = true
}
```

- `HedvigDatePickerImmutableState`: `selectedDate: LocalDate?`, `displayedMonth: LocalDate?`, `minDate: LocalDate`, `maxDate: LocalDate`.
- `HedvigDatePicker(onSelectedDateChanged: (LocalDate?) -> Unit)`.
- The two conversions live as private helpers next to the Material 3 adapter in `design-system-internals/.../DatePicker.kt`:
  - `LocalDate.toPickerMillis() = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()`
  - `Long.toPickerDate() = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date`
- Nothing outside `design-system-internals` mentions `TimeZone.UTC` for picker values again.

"What is today" stays the caller's job and keeps using the device zone (`clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date`), so a rule like "no future birth dates" becomes a plain `date <= today` comparison.

## Callers to migrate

| Feature | Files | Today |
|---|---|---|
| Moving flow | `EnterNewAddressDestination.kt` | immutable state, converts both ways with UTC |
| Travel certificate | `TravelCertificateDateInput.kt`, `TravelCertificateDateInputViewModel.kt` | `isSelectableDate` + `travelDate()` convert with UTC |
| Edit co-insured | `AddCoInsuredBottomSheetContent.kt` | builds millis from `birthDate`; `isSelectableDate` compares millis with `clock.now()` |
| Termination | `TerminationDateViewModel.kt`, `TerminationDateDestination.kt` | immutable state built from millis |
| Claim chat | `ClaimChatUiComponents.kt`, `FormStep.kt`, `ClaimIntent.kt`, `ClaimIntentExt.kt` | via design system `DatePickerUiState` |
| Design system | `HedvigDatePicker.kt` (`DatePickerWithDialog`, `DatePickerUiState`) | converts min/max/initial with UTC |

The co-insured `isSelectableDate` currently compares UTC midnight against the current instant, so for a user ahead of UTC "today" is unselectable for an hour or two after midnight. With the new API it becomes `date <= today` and that edge goes away.

## Steps

1. Add the `LocalDate` members to `HedvigDatePickerState` / `HedvigSelectableDates` / `HedvigDatePickerImmutableState` alongside the millis ones, implemented via the two helpers. Mark the millis members `@Deprecated` with a `ReplaceWith`.
2. Migrate callers one feature at a time (one commit each), deleting every `TimeZone.UTC` / `fromEpochMilliseconds` / `atStartOfDayIn` on picker values from feature code.
3. Remove the deprecated millis members.
4. `./gradlew ktlintFormat` and compile every affected module, including `:feature-claim-chat` (KMP; also check the iOS targets, since `design-system-api` is exported through `:umbrella`).

## Verification

- Unit test the two helpers in `design-system-internals`: round-trip a range of dates including leap days and DST-change dates, with the JVM default time zone set to `America/New_York`, `Europe/Stockholm` and `Pacific/Kiritimati` (UTC+14).
- On a device with the time zone set to `America/New_York`: pick a date on each migrated screen and confirm the same day is shown, saved (co-insured birth date, travel start date) and sent.
- Repeat in `Europe/Stockholm` to confirm nothing changed for Swedish users.
- Existing tests in each migrated module still pass (`./gradlew :feature-<name>:test`).

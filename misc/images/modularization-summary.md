# Module summary

Written by `./gradlew :generateProjectDependencyGraph`. Counts cover the modules the graph shows.

| Kind | Modules |
|---|---|
| KMP, iOS targets | 45 |
| KMP, no iOS | 33 |
| Android library | 30 |
| JVM library | 22 |
| Application | 1 |
| Other | 0 |
| In the iOS umbrella closure (all modules) | 47 |

## Non-KMP modules and what depends on them

Non-KMP modules ordered by the number of drawn modules that depend on them directly.

- `:core-demo-mode` (JVM library) <- `:data-addons`, `:data-paying-member`, `:datadog-demo-tracking`, `:feature-addon-purchase`, `:feature-chat`, `:feature-claim-chat`, `:feature-claim-details`, `:feature-cross-sell-sheet`, `:feature-edit-coinsured`, `:feature-home`, `:feature-insurances`, `:feature-login`, `:feature-payments`, `:feature-profile`, `:feature-travel-certificate`, `:forever-ui`, `:member-reminders-public`, `:notification-badge-data-public`, `:notification-firebase`, `:tracking-firebase`
- `:auth-core-public` (JVM library) <- `:datadog-android`, `:feature-delete-account`, `:feature-flags`, `:feature-impersonation`, `:feature-login`, `:feature-onboarding`, `:feature-payments`, `:feature-profile`, `:language-data`, `:notification-firebase`, `:tracking-firebase`
- `:auth-event-core` (JVM library) <- `:apollo-auth-listeners`, `:auth-core-public`, `:core-app-review`, `:core-demo-mode`, `:feature-flags`, `:language-data`, `:logging-device-model`, `:notification-firebase`
- `:data-addons` (JVM library) <- `:cross-sells`, `:feature-addon-purchase`, `:feature-cross-sell-sheet`, `:feature-home`, `:feature-insurances`, `:feature-travel-certificate`
- `:data-settings-datastore-public` (JVM library) <- `:feature-onboarding`, `:feature-payments`, `:feature-profile`, `:member-reminders-public`, `:tracking-firebase`, `:ui-analytics-consent`
- `:data-cross-sell-after-flow` (JVM library) <- `:data-changetier`, `:data-cross-sell-after-claim-closed`, `:feature-addon-purchase`, `:feature-cross-sell-sheet`, `:feature-movingflow`
- `:data-paying-member` (JVM library) <- `:feature-home`, `:feature-payin-account`, `:feature-payments`, `:feature-payout-account`, `:member-reminders-public`
- `:notification-permission` (Android library) <- `:feature-claim-chat`, `:feature-home`, `:feature-payments`, `:feature-profile`, `:member-reminders-ui`
- `:member-reminders-public` (JVM library) <- `:feature-home`, `:feature-payments`, `:feature-profile`, `:member-reminders-ui`
- `:theme` (JVM library) <- `:data-settings-datastore-public`, `:feature-onboarding`, `:feature-payments`, `:feature-profile`
- `:data-chat` (JVM library) <- `:data-changetier`, `:database-core`, `:feature-chat`
- `:member-reminders-ui` (Android library) <- `:feature-home`, `:feature-payments`, `:feature-profile`
- `:navigation-activity` (JVM library) <- `:feature-chat`, `:feature-home`, `:feature-travel-certificate`
- `:compose-webview` (Android library) <- `:feature-connect-payment-trustly`, `:feature-profile`
- `:data-changetier` (JVM library) <- `:feature-choose-tier`, `:feature-terminate-insurance`
- `:data-cross-sell-after-claim-closed` (JVM library) <- `:feature-claim-details`, `:feature-home`
- `:data-display-items` (JVM library) <- `:feature-claim-details`, `:feature-insurances`
- `:forever-ui` (Android library) <- `:feature-forever`, `:feature-onboarding`
- `:notification-badge-data-public` (JVM library) <- `:feature-home`, `:feature-insurances`
- `:tracking-datadog` (Android library) <- `:datadog-android`, `:design-showcase`
- `:core-app-review` (JVM library) <- `:feature-edit-coinsured`
- `:core-icons` (Android library) <- `:feature-profile`
- `:core-rive` (Android library) <- `:feature-claim-chat`
- `:database-core` (JVM library) <- `:database-android`
- `:language-data` (JVM library) <- `:feature-payments`
- `:notification-core` (Android library) <- `:notification-firebase`

package com.hedvig.android.data.settings.datastore

/**
 * Consent for product analytics (Firebase Analytics only; Datadog performance/bug analytics is
 * intentionally not covered by this and stays always-on).
 * [NOT_DECIDED] means the member has never been asked or never answered: events are buffered
 * in-app, not forwarded, until an explicit decision is made.
 */
enum class AnalyticsConsent {
  NOT_DECIDED,
  GRANTED,
  DENIED,
}

package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature

internal const val PRODUCTION_CLIENT_KEY = "*:production.21d6af57ae16320fde3a3caf024162db19cc33bf600ab7439c865c20"
internal const val DEVELOPMENT_CLIENT_KEY = "*:development.f2455340ac9d599b5816fa879d079f21dd0eb03e4315130deb5377b6"
internal const val UNLEASH_URL = "https://eu.app.unleash-hosted.com/eubb1047/api/frontend"
internal const val APP_NAME = "android"
internal const val MEMBER_ID_PROPERTY = "memberId"

/**
 * Values used until the client holds real toggle state, for the flags whose natural absent-toggle
 * default is wrong. These are kill switches that must read as on, keeping their feature hidden,
 * until a fetch or the stored state says otherwise. An app-gating flag must never get an entry
 * here: defaulting one into its blocking state locks out members who are offline on first launch.
 */
internal val neverFetchedDefaults: Map<Feature, Boolean> = mapOf(
  Feature.DISABLE_ANALYTICS to true,
  Feature.DISABLE_PUPPY_GUIDE to true,
  Feature.DISABLE_TERMINATION_REDIRECTION to true,
  Feature.DISABLE_RESUMING_ONGOING_SHOP_SESSIONS to true,
  Feature.HIDE_CANCEL_INSURANCE_FROM_HOME_SCREEN to true,
)

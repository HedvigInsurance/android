package com.hedvig.android.app.navigation

import com.hedvig.android.navigation.common.AnalyticsNamed
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass

/**
 * Analytics screen name for a nav key: its fully qualified class name with the shared feature package prefix
 * removed, so `SubmitSuccessKey` in feature-choose-tier reports as
 * `change.tier.navigation.SubmitSuccessKey`.
 *
 * **To get from a name back to the code, prepend `com.hedvig.android.feature.`.** That round trip is the
 * whole reason the name keeps its package: a screen name read off a Firebase report can be grepped straight
 * to the key that produced it, with no naming convention to decode first. A key declared outside that prefix
 * simply reports its full class name, which stays just as unique and just as greppable.
 *
 * A key implementing [AnalyticsNamed] reports its pinned name instead, with the same prefix removed, so
 * both kinds of name read the same way in a report.
 *
 * The prefix is the only thing removed. It is identical on every row, so it costs width without carrying
 * information, and dropping it leaves headroom under Firebase's 100 character cap on a parameter value: the
 * longest name is 91 characters fully qualified and 64 once stripped. `ScreenNameTest` pins the bound.
 *
 * Names must also stay unique: a `screen_view` is keyed by its name, so two keys sharing one name are
 * reported as a single screen and their metrics merge. Two names truncated to the same 100 characters would
 * collide the same way, which is the other reason to keep that headroom. A fully qualified class name is
 * unique by construction, and `ScreenNameTest` asserts it holds across every key on the classpath.
 */
internal fun HedvigNavKey.screenName(): String = when (this) {
  is AnalyticsNamed -> analyticsName.removePrefix(FEATURE_PACKAGE_PREFIX)
  else -> screenNameFor(this::class)
}

internal fun screenNameFor(keyClass: KClass<*>): String {
  val qualifiedName = keyClass.qualifiedName ?: return keyClass.simpleName ?: keyClass.java.name
  return qualifiedName.removePrefix(FEATURE_PACKAGE_PREFIX)
}

internal const val FEATURE_PACKAGE_PREFIX = "com.hedvig.android.feature."

package com.hedvig.android.app.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass

/**
 * Analytics screen name for a nav key: its fully qualified class name with the shared Hedvig feature package
 * prefix removed, so `SubmitSuccessKey` in feature-choose-tier reports as
 * `change.tier.navigation.SubmitSuccessKey`.
 *
 * **To get from a name back to the code, prepend `com.hedvig.android.feature.`** (or `com.hedvig.feature.`,
 * which a handful of modules use instead). That round trip is the whole reason the name keeps its package:
 * a screen name read off a Firebase report can be grepped straight to the key that produced it, with no
 * naming convention to decode first.
 *
 * The prefix is the only thing removed. GA4 truncates parameter values at 100 characters and the longest key
 * name is already 91, so dropping the 26 or 27 shared leading characters is what keeps every name safely
 * inside the limit. `ScreenNameTest` pins that bound.
 *
 * Names must also stay unique: a `screen_view` is keyed by its name, so two keys sharing one name are
 * reported as a single screen and their metrics merge. A fully qualified class name is unique by
 * construction, and `ScreenNameTest` asserts it holds across every key on the classpath.
 */
internal fun HedvigNavKey.screenName(): String = screenNameFor(this::class)

internal fun screenNameFor(keyClass: KClass<*>): String {
  val qualifiedName = keyClass.qualifiedName ?: return keyClass.simpleName ?: keyClass.java.name
  return qualifiedName
    .removePrefix("com.hedvig.android.feature.")
    .removePrefix("com.hedvig.feature.")
}

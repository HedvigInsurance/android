package com.hedvig.android.app.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass

/**
 * Analytics screen name for a nav key: the owning feature followed by the key's own name, e.g.
 * `ChangeTier.SubmitSuccess`. The feature qualifier is required because key names are not unique across features
 * (change-tier and addon-purchase each own a `SubmitSuccessKey`), and an unqualified name silently reports two
 * different screens as one.
 *
 * The qualifier is dropped when it would only repeat the key's own name, and repeated package segments collapse, so
 * feature-forever's `ForeverKey` reports as `Forever` rather than `ForeverForever.Forever`.
 *
 * `ScreenNameTest` asserts exhaustively that every key on the classpath maps to a distinct name.
 */
internal fun HedvigNavKey.screenName(): String = screenNameFor(this::class)

internal fun screenNameFor(keyClass: KClass<*>): String {
  val name = (keyClass.simpleName ?: keyClass.java.name).removeSuffix("Key")
  val qualifiedName = keyClass.qualifiedName ?: return name
  val feature = qualifiedName
    .removePrefix("com.hedvig.android.feature.")
    .removePrefix("com.hedvig.feature.")
    .split('.')
    .dropLast(1)
    .filterNot { it in packageSegmentsWithoutFeatureMeaning }
    .distinct()
    .joinToString("") { segment -> segment.replaceFirstChar { it.uppercaseChar() } }
  return if (feature.isEmpty() || feature == name) name else "$feature.$name"
}

private val packageSegmentsWithoutFeatureMeaning = setOf("navigation", "nav", "ui")

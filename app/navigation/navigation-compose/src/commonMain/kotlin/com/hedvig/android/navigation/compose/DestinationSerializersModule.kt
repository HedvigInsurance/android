package com.hedvig.android.navigation.compose

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

/**
 * Nav3 saves/restores the back stack across process death by serializing each [com.hedvig.android.navigation.common.HedvigNavKey]
 * key. Because the sealed hierarchies live across ~25 feature modules and we do not rely on JVM
 * reflection, each feature must register its subtypes polymorphically:
 *
 * ```
 * SerializersModule {
 *   polymorphic(HedvigNavKey::class) {
 *     subclass(MyFeatureDestination.Graph::class)
 *     subclass(MyFeatureDestination.Detail::class)
 *   }
 * }
 * ```
 *
 * Features contribute their module via Metro `@ContributesIntoSet`; `:app` injects the resulting
 * `Set<SerializersModule>`, folds it with [merge], and feeds the result into the
 * `SavedStateConfiguration` passed to `rememberNavBackStack`.
 */
val HedvigBaseSerializersModule: SerializersModule = SerializersModule {}

/** Folds every feature-contributed [SerializersModule] onto [HedvigBaseSerializersModule]. */
fun Iterable<SerializersModule>.merge(): SerializersModule =
  fold(HedvigBaseSerializersModule) { accumulator, module -> accumulator + module }

package com.hedvig.android.core.common.di

import kotlin.reflect.KClass

/**
 * Marks a ViewModel for DI-boilerplate generation by the `:viewmodel-processor` KSP processor. The
 * developer still writes the one Metro constructor annotation (`@Inject` or `@AssistedInject`, plus
 * `@Assisted` on assisted params) — KSP cannot stamp those onto an existing class — and this marker.
 * The processor then generates the map contribution (no-arg) or the assisted factory into a sibling
 * file, so no VM hand-writes `@ViewModelKey` / `@ContributesIntoMap` / a nested factory.
 *
 * [scope] is named explicitly at every call site, mirroring the other Metro DI annotations (e.g.
 * `@ContributesBinding(AppScope::class)`). Almost every VM is [ActivityRetainedScope]; the rare
 * [AppScope] case is a VM resolved by its own standalone Activity.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class HedvigViewModel(val scope: KClass<*>)

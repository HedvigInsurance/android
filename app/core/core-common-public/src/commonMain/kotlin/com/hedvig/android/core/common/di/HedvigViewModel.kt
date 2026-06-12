package com.hedvig.android.core.common.di

import kotlin.reflect.KClass

/**
 * Marks a ViewModel for DI-boilerplate generation by the `:viewmodel-processor` KSP processor. The
 * developer still writes the one Metro constructor annotation (`@Inject` or `@AssistedInject`, plus
 * `@Assisted` on assisted params) — KSP cannot stamp those onto an existing class — and this marker.
 * The processor then generates the map contribution (no-arg) or the assisted factory into a sibling
 * file, so no VM hand-writes `@ViewModelKey` / `@ContributesIntoMap` / a nested factory.
 *
 * [scope] defaults to [ActivityRetainedScope], centralizing the scope decision so individual VMs never
 * name a scope. The rare [AppScope] case (a VM resolved by its own standalone Activity) passes it
 * explicitly: `@HedvigViewModel(AppScope::class)`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class HedvigViewModel(val scope: KClass<*> = ActivityRetainedScope::class)

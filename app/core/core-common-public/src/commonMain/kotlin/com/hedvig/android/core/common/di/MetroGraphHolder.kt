package com.hedvig.android.core.common.di

/**
 * Holds the platform's root Metro dependency graph as [Any] so that framework-instantiated entry
 * points living in modules that cannot see the concrete graph type (e.g. variant-restricted feature
 * modules, or iOS-only code) can reach it. Callers cast it to a `@ContributesTo(AppScope::class)`
 * entry-point interface they declare themselves.
 */
object MetroGraphHolder {
  lateinit var graph: Any
}

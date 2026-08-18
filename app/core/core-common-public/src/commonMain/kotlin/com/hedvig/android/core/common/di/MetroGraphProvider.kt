package com.hedvig.android.core.common.di

/**
 * Implemented by the platform's `Application` (or equivalent root) to expose the root Metro dependency
 * graph as [Any], so that framework-instantiated entry points living in modules that cannot see the
 * concrete graph type (e.g. variant-restricted feature modules, or iOS-only code) can reach it.
 * Callers cast [metroGraph] to a `@ContributesTo(AppScope::class)` entry-point interface they declare
 * themselves.
 */
interface MetroGraphProvider {
  val metroGraph: Any
}

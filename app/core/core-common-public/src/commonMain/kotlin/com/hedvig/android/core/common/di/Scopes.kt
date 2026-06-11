package com.hedvig.android.core.common.di

abstract class AppScope private constructor()

/**
 * One instance per *Activity* (a Metro graph extension over [AppScope]) that survives configuration
 * changes but dies with the Activity. It exists so every screen owned by one Activity talks to that
 * Activity's own back stack: anything that depends on the [com.hedvig.android.navigation.compose.Backstack]
 * (the ViewModels, the session reconciler) is scoped here rather than to [AppScope], so two
 * `MainActivity` instances in the same process get two independent back stacks instead of silently
 * sharing one app-singleton.
 */
abstract class ActivityRetainedScope private constructor()

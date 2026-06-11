package com.hedvig.android.app.di

import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.app.navigation.SessionReconciler
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.compose.Backstack
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.MetroViewModelMultibindings

/**
 * A per-Activity graph extension over [AppScope]. One instance is created per `MainActivity` (built by
 * [com.hedvig.android.app.navigation.NavRetainedViewModel], so it survives a config change and dies
 * with the Activity) and is seeded with that Activity's own [BackstackController].
 *
 * Everything that must talk to *this* Activity's back stack is scoped here rather than to [AppScope]:
 * - the [Backstack] binding, bound off the per-Activity controller passed into the factory;
 * - [SessionReconciler], re-scoped to [ActivityRetainedScope];
 * - every ViewModel that injects [Backstack] (`@ContributesIntoMap(ActivityRetainedScope::class)`),
 *   collected here via the [MetroViewModelMultibindings] maps.
 *
 * The maps it exposes aggregate the parent [AppScope] contributions too, so a single merged
 * [dev.zacsweers.metrox.viewmodel.MetroViewModelFactory] built from them resolves every ViewModel.
 */
@GraphExtension(ActivityRetainedScope::class)
internal interface ActivityRetainedGraph : MetroViewModelMultibindings {
  val sessionReconciler: SessionReconciler

  @Provides
  fun bindBackstack(controller: BackstackController): Backstack = controller

  @ContributesTo(AppScope::class)
  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides backstackController: BackstackController,
    ): ActivityRetainedGraph
  }
}

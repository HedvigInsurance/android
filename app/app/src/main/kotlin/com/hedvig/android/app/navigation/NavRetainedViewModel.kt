package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.hedvig.android.app.di.ActivityRetainedGraph
import com.hedvig.android.app.di.AppGraph
import com.hedvig.android.app.di.MergedMetroViewModelFactory
import dev.zacsweers.metro.asContribution
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

/**
 * The per-Activity host for navigation state. Being a retained [ViewModel] it survives a configuration
 * change (the Activity's `ViewModelStore` non-config instance) but is cleared when the Activity is
 * really finished — exactly the lifetime we want for one Activity's back stack.
 *
 * It owns this Activity's [BackstackController] (built here, outside composition, so onCreate wiring —
 * splash keep-condition, restore/seed/persist, task hooks — can touch it before the first frame) and
 * spins up a per-Activity [ActivityRetainedGraph] seeded with that controller. Everything that must
 * talk to this Activity's stack — the [sessionReconciler] and every back-stack ViewModel resolved by
 * [viewModelFactory] — comes from that extension, so two `MainActivity` instances never share state.
 */
internal class NavRetainedViewModel(appGraph: AppGraph) : ViewModel() {
  val backstackController: BackstackController = BackstackController(
    entries = mutableStateListOf(),
    parkedRuns = mutableStateMapOf(),
    pendingDeepLinkState = mutableStateOf(null),
    pendingDeepLinkStashedAtState = mutableStateOf(null),
    stashedSessionState = mutableStateOf(null),
  )

  private val activityGraph: ActivityRetainedGraph =
    appGraph.asContribution<ActivityRetainedGraph.Factory>().create(backstackController)

  val sessionReconciler: SessionReconciler = activityGraph.sessionReconciler

  val viewModelFactory: MetroViewModelFactory = MergedMetroViewModelFactory(appGraph, activityGraph)
}

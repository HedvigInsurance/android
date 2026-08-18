package com.hedvig.android.app.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelMultibindings
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

/**
 * Resolves ViewModels from both the app graph and a per-Activity [ActivityRetainedGraph]. The
 * activity-scoped maps win on key collisions, so a ViewModel that injects the per-Activity
 * [com.hedvig.android.navigation.compose.Backstack] is built against *this* Activity's controller
 * while every plain app-scoped ViewModel still resolves. Installed via `LocalMetroViewModelFactory`
 * so the whole composition resolves through one factory.
 */
internal class MergedMetroViewModelFactory(
  appGraph: MetroViewModelMultibindings,
  activityGraph: MetroViewModelMultibindings,
) : MetroViewModelFactory() {
  override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel> =
    appGraph.viewModelProviders + activityGraph.viewModelProviders

  override val assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory> =
    appGraph.assistedFactoryProviders + activityGraph.assistedFactoryProviders

  override val manualAssistedFactoryProviders:
    Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory> =
    appGraph.manualAssistedFactoryProviders + activityGraph.manualAssistedFactoryProviders
}

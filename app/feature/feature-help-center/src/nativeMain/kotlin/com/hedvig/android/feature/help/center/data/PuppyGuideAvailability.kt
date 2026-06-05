package com.hedvig.android.feature.help.center.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.design.system.hedvig.IosDiHolder
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * iOS-facing surface for the Puppy Guide help-center entry. Its signature only mentions public
 * iOS types ([PuppyGuidePresentation], [PuppyGuideAvailabilityCancellable]), which keeps the
 * underlying [GetPuppyGuideUseCase] and its data models `internal` to this module.
 */
interface PuppyGuideAvailabilityObserver {
  fun observe(onResult: (PuppyGuidePresentation?) -> Unit): PuppyGuideAvailabilityCancellable
}

@ContributesBinding(AppScope::class)
@Inject
internal class PuppyGuideAvailabilityObserverImpl(
  private val getPuppyGuideUseCase: GetPuppyGuideUseCase,
) : PuppyGuideAvailabilityObserver {
  override fun observe(onResult: (PuppyGuidePresentation?) -> Unit): PuppyGuideAvailabilityCancellable {
    val scope = CoroutineScope(Dispatchers.Main)
    val job: Job = scope.launch {
      getPuppyGuideUseCase.invoke().collectLatest { either ->
        val puppyGuide = either.getOrNull()
        val presentation = when {
          puppyGuide == null || puppyGuide.stories.isEmpty() -> null
          puppyGuide.isForYoungDog == true -> PuppyGuidePresentation.FullCard
          else -> PuppyGuidePresentation.QuickAction
        }
        onResult(presentation)
      }
    }
    return PuppyGuideAvailabilityCancellable { job.cancel() }
  }
}

@ContributesTo(AppScope::class)
interface PuppyGuideEntryPoint {
  val puppyGuideAvailabilityObserver: PuppyGuideAvailabilityObserver
}

@Suppress("unused") // Used from iOS
fun observePuppyGuideAvailability(onResult: (PuppyGuidePresentation?) -> Unit): PuppyGuideAvailabilityCancellable {
  return (IosDiHolder.graph as PuppyGuideEntryPoint).puppyGuideAvailabilityObserver.observe(onResult)
}

sealed class PuppyGuidePresentation {
  object FullCard : PuppyGuidePresentation()

  object QuickAction : PuppyGuidePresentation()
}

class PuppyGuideAvailabilityCancellable internal constructor(private val onCancel: () -> Unit) {
  fun cancel() = onCancel()
}

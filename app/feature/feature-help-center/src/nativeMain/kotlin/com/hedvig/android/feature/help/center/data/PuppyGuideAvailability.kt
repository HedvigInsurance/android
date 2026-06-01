package com.hedvig.android.feature.help.center.data

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.design.system.hedvig.IosDiHolder
import dev.zacsweers.metro.ContributesTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ContributesTo(AppScope::class)
interface PuppyGuideEntryPoint {
  val getPuppyGuideUseCase: GetPuppyGuideUseCase
}

@Suppress("unused") // Used from iOS
fun observePuppyGuideAvailability(onResult: (PuppyGuidePresentation?) -> Unit): PuppyGuideAvailabilityCancellable {
  val useCase = (IosDiHolder.graph as PuppyGuideEntryPoint).getPuppyGuideUseCase
  val scope = CoroutineScope(Dispatchers.Main)
  val job: Job = scope.launch {
    useCase.invoke().collectLatest { either ->
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

sealed class PuppyGuidePresentation {
  object FullCard : PuppyGuidePresentation()

  object QuickAction : PuppyGuidePresentation()
}

class PuppyGuideAvailabilityCancellable internal constructor(private val onCancel: () -> Unit) {
  fun cancel() = onCancel()
}

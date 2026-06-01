package com.hedvig.android.feature.help.center.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

@Suppress("unused") // Used from iOS
fun observePuppyGuideAvailability(onResult: (PuppyGuidePresentation?) -> Unit): PuppyGuideAvailabilityCancellable {
  val useCase = KoinPlatform.getKoin().get<GetPuppyGuideUseCase>()
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

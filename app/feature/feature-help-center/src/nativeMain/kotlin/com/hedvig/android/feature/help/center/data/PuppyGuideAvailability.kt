package com.hedvig.android.feature.help.center.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

@Suppress("unused") // Used from iOS
fun observePuppyGuideAvailability(onResult: (Boolean) -> Unit): PuppyGuideAvailabilityCancellable {
  val useCase = KoinPlatform.getKoin().get<GetPuppyGuideUseCase>()
  val scope = CoroutineScope(Dispatchers.Main)
  val job: Job = scope.launch {
    useCase.invoke().collectLatest { either ->
      onResult(either.getOrNull()?.isNotEmpty() == true)
    }
  }
  return PuppyGuideAvailabilityCancellable { job.cancel() }
}

class PuppyGuideAvailabilityCancellable internal constructor(private val onCancel: () -> Unit) {
  fun cancel() = onCancel()
}

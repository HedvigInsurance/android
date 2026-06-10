package com.hedvig.android.app.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.hedvig.android.core.common.di.ChildWorkerFactory
import dev.zacsweers.metro.Inject
import kotlin.reflect.KClass

@Inject
class MetroWorkerFactory(
  private val factories: Map<KClass<out ListenableWorker>, ChildWorkerFactory>,
) : WorkerFactory() {
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters,
  ): ListenableWorker? {
    val factory = factories.entries.firstOrNull { it.key.java.name == workerClassName }?.value
    return factory?.create(appContext, workerParameters)
  }
}

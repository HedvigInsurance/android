package com.hedvig.android.core.common.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

/**
 * Assisted factory contract for [ListenableWorker]s built through Metro. Each worker contributes a
 * `@AssistedFactory` implementation of this into the multibound worker map keyed by [WorkerKey].
 */
interface ChildWorkerFactory {
  fun create(context: Context, params: WorkerParameters): ListenableWorker
}

@MapKey
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

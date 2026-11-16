package com.hedvig.android.core.demomode

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Reads [IosDemoMode] when collected and again after each [setDemoMode]. The iOS app changes the flag without telling
 * Kotlin, so a collector sees such a change only on its next collection.
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class NativeDemoManager(
  private val iosDemoMode: IosDemoMode,
) : DemoManager {
  private val writes = MutableStateFlow(0)

  override fun isDemoMode(): Flow<Boolean> = writes
    .map { withContext(Dispatchers.Main) { iosDemoMode.isDemoMode() } }
    .distinctUntilChanged()

  override suspend fun setDemoMode(demoMode: Boolean) {
    withContext(Dispatchers.Main) { iosDemoMode.setDemoMode(demoMode) }
    writes.value++
  }
}

package com.hedvig.android.core.demomode

import kotlinx.coroutines.flow.Flow

interface DemoManager {
  fun isDemoMode(): Flow<Boolean>

  suspend fun setDemoMode(demoMode: Boolean)
}

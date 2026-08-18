package com.hedvig.android.core.demomode

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DemoAuthEventListener(
  private val demoManager: DemoManager,
) : AuthEventListener {
  override suspend fun loggedOut() {
    demoManager.setDemoMode(false)
  }
}

package com.hedvig.android.tracking.firebase

import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.IoDispatcher
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.tracking.EventTrackingClient
import com.hedvig.android.initializable.Initializable
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class EventTrackingInitializer(
  private val eventTrackingClient: EventTrackingClient,
  private val demoManager: DemoManager,
  private val memberIdService: MemberIdService,
  private val applicationScope: ApplicationScope,
  @IoDispatcher private val coroutineContext: CoroutineContext,
) : Initializable {
  override fun initialize() {
    applicationScope.launch(coroutineContext) {
      demoManager.isDemoMode().distinctUntilChanged().collect { isDemoMode ->
        eventTrackingClient.setCollectionEnabled(!isDemoMode)
      }
    }
    applicationScope.launch(coroutineContext) {
      memberIdService.getMemberId().collect { memberId ->
        eventTrackingClient.setUserId(memberId)
      }
    }
  }
}

package com.hedvig.android.notification.badge.data.payment

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class SwitchingMissedPaymentNotificationService(
  private val demoManager: DemoManager,
  private val demoImpl: DemoMissedPaymentNotificationService,
  private val prodImpl: MissedPaymentNotificationServiceImpl,
) : MissedPaymentNotificationService {
  override fun showRedDotNotification() = flow {
    emitAll(pick().showRedDotNotification())
  }

  private suspend fun pick(): MissedPaymentNotificationService =
    if (demoManager.isDemoMode().first()) demoImpl else prodImpl
}

interface MissedPaymentNotificationService {
  fun showRedDotNotification(): Flow<Boolean>
}

@Inject
internal class DemoMissedPaymentNotificationService : MissedPaymentNotificationService {
  var showNotification = false

  override fun showRedDotNotification(): Flow<Boolean> {
    return flowOf(showNotification)
  }
}

@Inject
@SingleIn(AppScope::class)
internal class MissedPaymentNotificationServiceImpl(
  private val getIfMissedPaymentUseCase: GetIfMissedPaymentUseCase,
) : MissedPaymentNotificationService {
  override fun showRedDotNotification(): Flow<Boolean> {
    return getIfMissedPaymentUseCase.invoke()
  }
}

package com.hedvig.android.notification.badge.data.payment

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<MissedPaymentNotificationService>())
internal class SwitchingMissedPaymentNotificationService(
  override val demoManager: DemoManager,
  override val demoImpl: DemoMissedPaymentNotificationService,
  override val prodImpl: MissedPaymentNotificationServiceImpl,
) : MissedPaymentNotificationService, DemoSwitcher<MissedPaymentNotificationService>() {
  override fun showRedDotNotification() = pickFlow { it.showRedDotNotification() }
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

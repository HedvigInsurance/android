package com.hedvig.android.notification.badge.data.payment

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MissedPaymentNotificationServiceProvider(
  override val demoManager: DemoManager,
  override val demoImpl: MissedPaymentNotificationService,
  override val prodImpl: MissedPaymentNotificationService,
) : ProdOrDemoProvider<MissedPaymentNotificationService>

interface MissedPaymentNotificationService {
  fun showRedDotNotification(): Flow<Boolean>
}

@Inject
class DemoMissedPaymentNotificationService : MissedPaymentNotificationService {
  var showNotification = false

  override fun showRedDotNotification(): Flow<Boolean> {
    return flowOf(showNotification)
  }
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class MissedPaymentNotificationServiceImpl(
  private val getIfMissedPaymentUseCase: GetIfMissedPaymentUseCase,
) : MissedPaymentNotificationService {
  override fun showRedDotNotification(): Flow<Boolean> {
    return getIfMissedPaymentUseCase.invoke()
  }
}

package com.hedvig.android.notification.badge.data.payment

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
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

internal class DemoMissedPaymentNotificationService : MissedPaymentNotificationService {
  var showNotification = false

  override fun showRedDotNotification(): Flow<Boolean> {
    return flowOf(showNotification)
  }
}

internal class MissedPaymentNotificationServiceImpl(
  private val getMissedPaymentIdUseCase: GetMissedPaymentIdUseCase,
) : MissedPaymentNotificationService {
  override fun showRedDotNotification(): Flow<Boolean> {
    return getMissedPaymentIdUseCase.invoke()
  }
}

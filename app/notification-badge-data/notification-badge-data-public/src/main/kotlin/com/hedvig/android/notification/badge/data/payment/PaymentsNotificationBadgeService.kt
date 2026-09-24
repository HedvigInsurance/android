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

/** The dot on the Payments tab. When both apply, the pre-charge notice wins over the missed payment. */
enum class PaymentsNotificationBadge {
  PreChargeNotice,
  MissedPayment,
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<PaymentsNotificationBadgeService>())
internal class SwitchingPaymentsNotificationBadgeService(
  override val demoManager: DemoManager,
  override val demoImpl: DemoPaymentsNotificationBadgeService,
  override val prodImpl: PaymentsNotificationBadgeServiceImpl,
) : PaymentsNotificationBadgeService, DemoSwitcher<PaymentsNotificationBadgeService>() {
  override fun badge() = pickFlow { it.badge() }
}

interface PaymentsNotificationBadgeService {
  /** Emits null when the tab should show no dot. */
  fun badge(): Flow<PaymentsNotificationBadge?>
}

@Inject
internal class DemoPaymentsNotificationBadgeService : PaymentsNotificationBadgeService {
  override fun badge(): Flow<PaymentsNotificationBadge?> {
    return flowOf(null)
  }
}

@Inject
@SingleIn(AppScope::class)
internal class PaymentsNotificationBadgeServiceImpl(
  private val getPaymentsNotificationBadgeUseCase: GetPaymentsNotificationBadgeUseCase,
) : PaymentsNotificationBadgeService {
  override fun badge(): Flow<PaymentsNotificationBadge?> {
    return getPaymentsNotificationBadgeUseCase.invoke()
  }
}

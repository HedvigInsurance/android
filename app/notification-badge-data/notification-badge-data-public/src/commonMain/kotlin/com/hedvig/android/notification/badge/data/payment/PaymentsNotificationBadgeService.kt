package com.hedvig.android.notification.badge.data.payment

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.DemoSwitcher
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

/** The dot on the Payments tab. When both apply, the missed payment wins over the charge notice. */
enum class PaymentsNotificationBadge {
  /** An upcoming charge, or an upcoming retry of a failed one, that the member has not seen on the Payments tab yet. */
  ChargeNotice,
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

  override suspend fun markChargeNoticesAsSeen() = pick().markChargeNoticesAsSeen()
}

interface PaymentsNotificationBadgeService {
  /** Emits null when the tab should show no dot. */
  fun badge(): Flow<PaymentsNotificationBadge?>

  /** Clears the [PaymentsNotificationBadge.ChargeNotice] dot until the backend reports a new charge notice. */
  suspend fun markChargeNoticesAsSeen()
}

@Inject
internal class DemoPaymentsNotificationBadgeService : PaymentsNotificationBadgeService {
  override fun badge(): Flow<PaymentsNotificationBadge?> {
    return flowOf(null)
  }

  override suspend fun markChargeNoticesAsSeen() {}
}

@Inject
@SingleIn(AppScope::class)
internal class PaymentsNotificationBadgeServiceImpl(
  private val getPaymentsNotificationBadgeUseCase: GetPaymentsNotificationBadgeUseCase,
  private val notificationBadgeStorage: NotificationBadgeStorage,
) : PaymentsNotificationBadgeService {
  private val chargeNoticeBadge = NotificationBadge.PaymentsChargeNotice

  override fun badge(): Flow<PaymentsNotificationBadge?> {
    return combine(
      getPaymentsNotificationBadgeUseCase.invoke(),
      notificationBadgeStorage.getValue(chargeNoticeBadge),
    ) { badgeData, seenChargeNoticeIds ->
      when {
        badgeData == null -> null
        badgeData.hasMissedPayment -> PaymentsNotificationBadge.MissedPayment
        (badgeData.chargeNoticeIds - seenChargeNoticeIds).isNotEmpty() -> PaymentsNotificationBadge.ChargeNotice
        else -> null
      }
    }
  }

  override suspend fun markChargeNoticesAsSeen() {
    val chargeNoticeIds = getPaymentsNotificationBadgeUseCase.invoke().first()?.chargeNoticeIds ?: return
    notificationBadgeStorage.setValue(chargeNoticeBadge, chargeNoticeIds)
  }
}

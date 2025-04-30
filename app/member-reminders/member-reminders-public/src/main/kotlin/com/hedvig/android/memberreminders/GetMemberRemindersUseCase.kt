package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.NonEmptyList
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

interface GetMemberRemindersUseCase {
  fun invoke(): Flow<MemberReminders>
}

internal class GetMemberRemindersUseCaseImpl(
  private val enableNotificationsReminderSnoozeManager: EnableNotificationsReminderSnoozeManager,
  private val getConnectPaymentReminderUseCase: GetConnectPaymentReminderUseCase,
  private val getUpcomingRenewalRemindersUseCase: GetUpcomingRenewalRemindersUseCase,
  private val getNeedsCoInsuredInfoRemindersUseCase: GetNeedsCoInsuredInfoRemindersUseCase,
) : GetMemberRemindersUseCase {
  override fun invoke(): Flow<MemberReminders> {
    return combine(
      enableNotificationsReminderSnoozeManager.timeToShowNotificationReminder().map { showReminder ->
        if (showReminder) {
          MemberReminder.EnableNotifications()
        } else {
          null
        }
      },
      flow {
        emit(
          getConnectPaymentReminderUseCase.invoke().getOrNull()?.let { paymentReminder ->
            when (paymentReminder) {
              PaymentReminder.ShowConnectPaymentReminder -> MemberReminder.PaymentReminder.ConnectPayment()
              is PaymentReminder.ShowMissingPaymentsReminder -> {
                MemberReminder.PaymentReminder.TerminationDueToMissedPayments(
                  terminationDate = paymentReminder.terminationDate,
                )
              }
            }
          },
        )
      },
      flow {
        val upcomingRenewals = getUpcomingRenewalRemindersUseCase.invoke().getOrNull()
        emit(upcomingRenewals)
      },
      getNeedsCoInsuredInfoRemindersUseCase.invoke(),
    ) {
      enableNotifications: MemberReminder.EnableNotifications?,
      connectPayment: MemberReminder.PaymentReminder?,
      upcomingRenewalReminders: NonEmptyList<MemberReminder.UpcomingRenewal>?,
      coInsuredInfoResult: Either<CoInsuredInfoReminderError, NonEmptyList<MemberReminder.CoInsuredInfo>>,
      ->
      MemberReminders(
        connectPayment = connectPayment,
        upcomingRenewals = upcomingRenewalReminders,
        enableNotifications = enableNotifications,
        coInsuredInfo = coInsuredInfoResult.getOrNull(),
      )
    }
  }
}

data class MemberReminders(
  val connectPayment: MemberReminder.PaymentReminder? = null,
  val upcomingRenewals: List<MemberReminder.UpcomingRenewal>? = null,
  val enableNotifications: MemberReminder.EnableNotifications? = null,
  val coInsuredInfo: List<MemberReminder.CoInsuredInfo>? = null,
) {
  /**
   * In some cases a reminder may be present but may not be applicable in our current app state.
   *
   * If [alreadyHasNotificationPermission] is true, then the notification permission reminder should not be shown.
   */
  fun onlyApplicableReminders(alreadyHasNotificationPermission: Boolean): List<MemberReminder> {
    return buildList {
      connectPayment?.let {
        add(connectPayment)
      }
      coInsuredInfo?.let {
        addAll(coInsuredInfo)
      }
      if (!alreadyHasNotificationPermission) {
        enableNotifications?.let {
          add(enableNotifications)
        }
      }
      upcomingRenewals?.let {
        addAll(it)
      }
    }
  }
}

sealed interface MemberReminder {
  val id: String

  sealed interface PaymentReminder : MemberReminder {
    data class TerminationDueToMissedPayments(
      override val id: String = UUID.randomUUID().toString(),
      val terminationDate: LocalDate,
    ) : PaymentReminder

    data class ConnectPayment(
      override val id: String = UUID.randomUUID().toString(),
    ) : PaymentReminder
  }

  data class UpcomingRenewal(
    val contractDisplayName: String,
    val renewalDate: LocalDate,
    val draftCertificateUrl: String?,
    override val id: String = UUID.randomUUID().toString(),
  ) : MemberReminder

  data class EnableNotifications(
    override val id: String = UUID.randomUUID().toString(),
  ) : MemberReminder

  data class CoInsuredInfo(
    val contractId: String,
    override val id: String = UUID.randomUUID().toString(),
  ) : MemberReminder
}

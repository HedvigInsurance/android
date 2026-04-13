package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.merge
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.memberreminders.MemberReminder.ContactInfoUpdateNeeded
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
  private val getContactInfoUpdateIsNeededUseCase: GetContactInfoUpdateIsNeededUseCase,
  private val getMissingChipIdReminderUseCase: GetMissingChipIdReminderUseCase,
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
              PaymentReminder.ShowConnectPaymentReminder -> {
                MemberReminder.PaymentReminder.ConnectPayment()
              }

              is PaymentReminder.ShowMissingPaymentsReminder -> {
                MemberReminder.PaymentReminder.TerminationDueToMissedPayments(
                  terminationDate = paymentReminder.terminationDate,
                )
              }
            }
          },
        )
      },
      getUpcomingRenewalRemindersUseCase.invoke().map { it.mapLeft { null }.merge() },
      getNeedsCoInsuredInfoRemindersUseCase.invoke(),
      getContactInfoUpdateIsNeededUseCase.invoke(),
      getMissingChipIdReminderUseCase.invoke(),
    ) { values ->
      val enableNotifications = values[0] as MemberReminder.EnableNotifications?
      val connectPayment = values[1] as MemberReminder.PaymentReminder?
      val upcomingRenewalReminders = values[2] as? NonEmptyList<MemberReminder.UpcomingRenewal>?
      val coInsuredInfoResult = values[3] as? Either<CoInsuredInfoReminderError, NonEmptyList<MemberReminder.CoInsuredInfo>>
      val contactInfoReminder = values[4] as? Either<ErrorMessage, ContactInfoUpdateNeeded?>
      val missingChipIdReminder = values[5] as? Either<ErrorMessage, MemberReminder.MissingChipId?>

      MemberReminders(
        connectPayment = connectPayment,
        upcomingRenewals = upcomingRenewalReminders,
        enableNotifications = enableNotifications,
        coInsuredInfo = coInsuredInfoResult?.getOrNull(),
        updateContactInfo = contactInfoReminder?.getOrNull(),
        missingChipId = missingChipIdReminder?.getOrNull(),
      )
    }
  }
}

data class MemberReminders(
  val connectPayment: MemberReminder.PaymentReminder? = null,
  val upcomingRenewals: List<MemberReminder.UpcomingRenewal>? = null,
  val enableNotifications: MemberReminder.EnableNotifications? = null,
  val coInsuredInfo: List<MemberReminder.CoInsuredInfo>? = null,
  val updateContactInfo: ContactInfoUpdateNeeded? = null,
  val missingChipId: MemberReminder.MissingChipId? = null,
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
      missingChipId?.let {
        add(it)
      }
      if (!alreadyHasNotificationPermission) {
        enableNotifications?.let {
          add(enableNotifications)
        }
      }
      upcomingRenewals?.let {
        addAll(it)
      }
      updateContactInfo?.let {
        add(it)
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
    val coInsuredType: CoInsuredFlowType,
    override val id: String = UUID.randomUUID().toString(),
  ) : MemberReminder

  data object ContactInfoUpdateNeeded : MemberReminder {
    override val id: String = UUID.randomUUID().toString()
  }

  data class MissingChipId(
    override val id: String = UUID.randomUUID().toString(),
  ) : MemberReminder
}

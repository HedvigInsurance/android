package com.hedvig.android.memberquickactions

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.ui.emergency.FirstVetSection

interface GetMemberActionsUseCase {
  suspend fun invoke(): Either<ErrorMessage, MemberAction>
}

data class MemberAction(
  val isCancelInsuranceEnabled: Boolean,
  val isConnectPaymentEnabled: Boolean,
  val isEditCoInsuredEnabled: Boolean,
  val isEditCoOwnersEnabled: Boolean,
  val isMovingEnabled: Boolean,
  val isTravelCertificateEnabled: Boolean,
  val isTierChangeEnabled: Boolean,
  val sickAbroadAction: MemberActionWithDetails.SickAbroadAction?,
  val firstVetAction: MemberActionWithDetails.FirstVetAction?,
)

sealed interface MemberActionWithDetails {
  data class SickAbroadAction(
    val deflectData: DeflectData,
  ) : MemberActionWithDetails

  data class FirstVetAction(
    val sections: List<FirstVetSection>,
  ) : MemberActionWithDetails
}

internal data class DeflectPartner(
  val id: String,
  val imageUrl: String?,
  val phoneNumber: String?,
  val url: String?,
  val preferredImageHeight: Int?,
)

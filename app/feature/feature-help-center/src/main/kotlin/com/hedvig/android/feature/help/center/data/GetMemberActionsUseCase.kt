package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.coroutines.flow.first
import octopus.MemberActionsQuery

internal interface GetMemberActionsUseCase {
  suspend fun invoke(): Either<ErrorMessage, MemberAction>
}

internal class GetMemberActionsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetMemberActionsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberAction> {
    return either {
      parZip(
        { featureManager.isFeatureEnabled(Feature.EDIT_COINSURED).first() },
        { featureManager.isFeatureEnabled(Feature.MOVING_FLOW).first() },
        { featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN).first() },
        {
          apolloClient.query(MemberActionsQuery()).safeExecute().toEither(::ErrorMessage)
            .onLeft { logcat { "Cannot load memberActions" } }
            .bind().currentMember.memberActions
        },
      ) { isCoInsuredFeatureOn, isMovingFeatureOn, isConnectPaymentFeatureOn, memberActions ->
        MemberAction(
          isCancelInsuranceEnabled = memberActions?.isCancelInsuranceEnabled ?: false,
          isConnectPaymentEnabled = isConnectPaymentFeatureOn && memberActions?.isConnectPaymentEnabled ?: false,
          isEditCoInsuredEnabled = isCoInsuredFeatureOn && memberActions?.isEditCoInsuredEnabled ?: false,
          isMovingEnabled = isMovingFeatureOn && memberActions?.isMovingEnabled ?: false,
          isTravelCertificateEnabled = memberActions?.isTravelCertificateEnabled ?: false,
          sickAbroadAction = memberActions?.sickAbroadAction.toSickAbroadAction(),
          firstVetAction = memberActions?.firstVetAction?.toVetAction(),
        )
      }
    }
  }
}

internal data class MemberAction(
  val isCancelInsuranceEnabled: Boolean,
  val isConnectPaymentEnabled: Boolean,
  val isEditCoInsuredEnabled: Boolean,
  val isMovingEnabled: Boolean,
  val isTravelCertificateEnabled: Boolean,
  val sickAbroadAction: MemberActionWithDetails.SickAbroadAction?,
  val firstVetAction: MemberActionWithDetails.FirstVetAction?,
)

internal sealed interface MemberActionWithDetails {
  data class SickAbroadAction(
    val partners: List<DeflectPartner>?,
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
)

private fun MemberActionsQuery.Data.CurrentMember.MemberActions.FirstVetAction.toVetAction():
  MemberActionWithDetails.FirstVetAction {
  val sections = this.sections.map {
    FirstVetSection(
      buttonTitle = it.buttonTitle,
      description = it.description,
      title = it.title,
      url = it.url,
    )
  }
  return MemberActionWithDetails.FirstVetAction(
    sections,
  )
}

private fun MemberActionsQuery.Data.CurrentMember.MemberActions.SickAbroadAction?.toSickAbroadAction():
  MemberActionWithDetails.SickAbroadAction {
  val partners = this?.partners?.map {
    DeflectPartner(
      id = it.id,
      imageUrl = it.imageUrl,
      phoneNumber = it.phoneNumber,
      url = it.url,
    )
  }
  return MemberActionWithDetails.SickAbroadAction(partners)
}

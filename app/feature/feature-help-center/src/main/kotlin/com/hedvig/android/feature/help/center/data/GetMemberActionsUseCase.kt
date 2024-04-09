package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
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
      val result = apolloClient.query(MemberActionsQuery())
        .safeExecute().toEither(::ErrorMessage).onLeft {
          logcat { "Cannot load memberActions" }
        }.bind()
      val isCoInsuredFeatureOn = featureManager.isFeatureEnabled(Feature.EDIT_COINSURED).first()
      val isMovingFeatureOn = featureManager.isFeatureEnabled(Feature.MOVING_FLOW).first()
      val isConnectPaymentFeatureOn = featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN).first()
      MemberAction(
        isCancelInsuranceEnabled = result.currentMember.memberActions?.isCancelInsuranceEnabled ?: false,
        isConnectPaymentEnabled = isConnectPaymentFeatureOn && result.currentMember.memberActions?.isConnectPaymentEnabled ?: false,
        isEditCoInsuredEnabled = isCoInsuredFeatureOn && result.currentMember.memberActions?.isEditCoInsuredEnabled ?: false,
        isMovingEnabled = isMovingFeatureOn && result.currentMember.memberActions?.isMovingEnabled ?: false,
        isTravelCertificateEnabled = result.currentMember.memberActions?.isTravelCertificateEnabled ?: false,
        sickAbroadAction = result.currentMember.memberActions?.sickAbroadAction.toSickAbroadAction(),
        firstVetAction = result.currentMember.memberActions?.firstVetAction?.toVetAction(),
      )
    }
  }
}

internal data class MemberAction(
  val isCancelInsuranceEnabled: Boolean?,
  val isConnectPaymentEnabled: Boolean?,
  val isEditCoInsuredEnabled: Boolean?,
  val isMovingEnabled: Boolean?,
  val isTravelCertificateEnabled: Boolean?,
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

@Serializable
data class DeflectPartner(
  val id: String,
  val imageUrl: String?,
  val phoneNumber: String?,
  val url: String?,
)

@Serializable
data class FirstVetSection(
  val buttonTitle: String?,
  val description: String?,
  val title: String?,
  val url: String?,
)

private fun MemberActionsQuery.Data.CurrentMember.MemberActions.FirstVetAction.toVetAction(): MemberActionWithDetails.FirstVetAction {
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

private fun MemberActionsQuery.Data.CurrentMember.MemberActions.SickAbroadAction?.toSickAbroadAction(): MemberActionWithDetails.SickAbroadAction {
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

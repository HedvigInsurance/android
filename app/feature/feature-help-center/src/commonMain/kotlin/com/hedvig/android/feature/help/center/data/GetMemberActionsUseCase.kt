package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.coroutines.flow.first
import octopus.MemberActionsQuery

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
          apolloClient
            .query(MemberActionsQuery())
            .safeExecute(::ErrorMessage)
            .onLeft { logcat { "Cannot load memberActions: $it" } }
            .bind().currentMember.memberActions
        },
      ) {
        isCoInsuredFeatureOn,
        isMovingFeatureOn,
        isConnectPaymentFeatureOn,
        memberActions,
        ->
        MemberAction(
          isCancelInsuranceEnabled = memberActions?.isCancelInsuranceEnabled ?: false,
          isConnectPaymentEnabled =
            isConnectPaymentFeatureOn && memberActions?.isConnectPaymentEnabled ?: false,
          isEditCoInsuredEnabled = isCoInsuredFeatureOn && memberActions?.isEditCoInsuredEnabled ?: false,
          isEditCoOwnersEnabled = isCoInsuredFeatureOn && memberActions?.isEditCoOwnersEnabled ?: false,
          isMovingEnabled = isMovingFeatureOn && memberActions?.isMovingEnabled ?: false,
          isTravelCertificateEnabled = memberActions?.isTravelCertificateEnabled ?: false,
          sickAbroadAction = memberActions?.sickAbroadDeflect.toSickAbroadAction(),
          firstVetAction = memberActions?.firstVetAction?.toVetAction(),
          isTierChangeEnabled = memberActions?.isChangeTierEnabled ?: false,
        )
      }
    }
  }
}

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

private fun MemberActionsQuery.Data.CurrentMember.MemberActions.SickAbroadDeflect?.toSickAbroadAction():
  MemberActionWithDetails.SickAbroadAction? {
  if (this == null) return null
  val partners = if (partners.isNotEmpty()) {
    DeflectData.DeflectPartnerContainer.ExtendedPartnerContainer(
      partners = partners.map { partner ->
        DeflectData.DeflectPartnerContainer.ExtendedPartner(
          id = partner.id,
          imageUrl = partner.imageUrl,
          phoneNumber = partner.phoneNumber,
          title = partner.title,
          description = partner.description,
          info = partner.info,
          url = partner.url,
          urlButtonTitle = partner.urlButtonTitle,
        )
      },
    )
  } else if (simplePartners.isNotEmpty()) {
    DeflectData.DeflectPartnerContainer.SimplePartnerContainer(
      partners = simplePartners.map { partner ->
        DeflectData.DeflectPartnerContainer.SimplePartner(
          url = partner.url,
          urlButtonTitle = partner.urlButtonTitle,
        )
      },
    )
  } else {
    logcat { "DeflectionFragment: both partners and simplePartners came empty" }
    null
  }

  val deflectData = DeflectData(
    title = title,
    infoText = infoText,
    warningText = warningText,
    partnersContainer = partners,
    partnersInfo = partnersInfo?.let {
      DeflectData.InfoBlock(it.title, it.description)
    },
    content = content.let {
      DeflectData.InfoBlock(it.title, it.description)
    },
    faq = faq.map { faqItem ->
      DeflectData.InfoBlock(faqItem.title, faqItem.description)
    },
    buttonText = buttonTitle,
  )
  return MemberActionWithDetails.SickAbroadAction(deflectData)
}

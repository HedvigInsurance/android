package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.logcat
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.ui.emergency.FirstVetSection
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.MemberActionsQuery

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetMemberActionsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMemberActionsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, MemberAction> {
    return either {
      val memberActions = apolloClient
        .query(MemberActionsQuery())
        .safeExecute(::ErrorMessage)
        .onLeft { logcat { "Cannot load memberActions: $it" } }
        .bind().currentMember.memberActions
      MemberAction(
        isCancelInsuranceEnabled = memberActions?.isCancelInsuranceEnabled ?: false,
        isConnectPaymentEnabled = memberActions?.isConnectPaymentEnabled ?: false,
        isEditCoInsuredEnabled = memberActions?.isEditCoInsuredEnabled ?: false,
        isEditCoOwnersEnabled = memberActions?.isEditCoOwnersEnabled ?: false,
        isMovingEnabled = memberActions?.isMovingEnabled ?: false,
        isTravelCertificateEnabled = memberActions?.isTravelCertificateEnabled ?: false,
        sickAbroadAction = memberActions?.sickAbroadDeflect.toSickAbroadAction(),
        firstVetAction = memberActions?.firstVetAction?.toVetAction(),
        isTierChangeEnabled = memberActions?.isChangeTierEnabled ?: false,
      )
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

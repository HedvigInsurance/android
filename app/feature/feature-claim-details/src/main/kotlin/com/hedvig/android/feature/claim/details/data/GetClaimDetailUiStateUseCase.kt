package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.audio.player.data.SignedAudioUrl
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.ClaimQuery
import octopus.fragment.ClaimFragment
import octopus.fragment.PartnerClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus
import octopus.type.InsuranceDocumentType

internal class GetClaimDetailUiStateUseCase(
  private val apolloClient: ApolloClient,
  private val crossSellAfterClaimClosedRepository: CrossSellAfterClaimClosedRepository,
) {
  fun invoke(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val queryFlow = queryFlow(claimId)
        emitAll(queryFlow)
        delay(POLL_INTERVAL)
      }
    }
  }

  private fun queryFlow(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return apolloClient
      .query(ClaimQuery(claimId))
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow { Error.NetworkError }
      .map { response ->
        either {
          val claim = response.bind().claim
          val partnerClaim = response.bind().partnerClaim //todo
          if (claim != null) {
            if (claim.showClaimClosedFlow) {
              crossSellAfterClaimClosedRepository.acknowledgeClaimClosedStatus(claim)
            }
            ClaimDetailUiState.Content.ClaimContent.fromClaim(
              claim, claim.conversation?.id,
              claim.conversation?.unreadMessageCount,
            )
          } else if (partnerClaim != null) {
//            if (partnerClaim.showClaimClosedFlow) {
//              crossSellAfterClaimClosedRepository.acknowledgeClaimClosedStatus(partnerClaim)
//            }
            ClaimDetailUiState.Content.PartnerClaimContent.fromPartnerClaim(partnerClaim)
          } else raise(Error.NoClaimFound)
        }
      }
  }

  private fun ClaimDetailUiState.Content.PartnerClaimContent.Companion.fromPartnerClaim(
    partnerClaim: PartnerClaimFragment,
  ): ClaimDetailUiState.Content.PartnerClaimContent {
    val termsAndConditionsUrl = partnerClaim.productVariant?.documents?.firstOrNull {
      it.type == InsuranceDocumentType.TERMS_AND_CONDITIONS
    }?.url
    return ClaimDetailUiState.Content.PartnerClaimContent(
      claimId = partnerClaim.id,
      claimStatus = partnerClaim.status.toStatus(),
      submittedAt = partnerClaim.submittedAt,
      termsConditionsUrl = termsAndConditionsUrl,
      appealInstructionsUrl = null,
      displayItems = partnerClaim.displayItems.map { item ->
        DisplayItem.fromStrings(item.displayTitle, item.displayValue)
      },
      handlerEmail = partnerClaim.handlerEmail,
      savedFileUri = null,
      downloadError = null,
      isLoadingPdf = null,
      claimStatusCardUiState = ClaimStatusCardUiState.fromPartnerClaim(partnerClaim),
      regNumber = partnerClaim.exposureDisplayName,
    )
  }

  private fun ClaimStatus?.toStatus(): ClaimDetailUiState.Content.ClaimStatus {
    return when (this) {
      ClaimStatus.CREATED -> ClaimDetailUiState.Content.ClaimStatus.CREATED
      ClaimStatus.IN_PROGRESS -> ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS
      ClaimStatus.CLOSED -> ClaimDetailUiState.Content.ClaimStatus.CLOSED
      ClaimStatus.REOPENED -> ClaimDetailUiState.Content.ClaimStatus.REOPENED
      ClaimStatus.UNKNOWN__,
      null,
        -> ClaimDetailUiState.Content.ClaimStatus.UNKNOWN
    }
  }

  private fun ClaimDetailUiState.Content.ClaimContent.Companion.fromClaim(
    claim: ClaimFragment,
    conversationId: String?,
    conversationUnreadMessageCount: Int?,
  ): ClaimDetailUiState.Content.ClaimContent {
    val audioUrl = claim.audioUrl
    val memberFreeText = claim.memberFreeText

    val claimType: String? = claim.claimType
    val submittedAt = claim.submittedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val insuranceDisplayName = claim.productVariant?.displayName
    val termsConditionsUrl =
      claim.productVariant
        ?.documents
        ?.firstOrNull { it.type == InsuranceDocumentType.TERMS_AND_CONDITIONS }
        ?.url

    return ClaimDetailUiState.Content.ClaimContent(
      claimId = claim.id,
      conversationId = conversationId,
      hasUnreadMessages = (conversationUnreadMessageCount ?: 0) > 0,
      submittedContent = when {
        audioUrl != null -> {
          ClaimDetailUiState.Content.ClaimContent.SubmittedContent.Audio(
            SignedAudioUrl.fromSignedAudioUrlString(
              audioUrl,
            ),
          )
        }

        memberFreeText != null -> ClaimDetailUiState.Content.ClaimContent.SubmittedContent.FreeText(memberFreeText)
        else -> null
      },
      files = claim.files.map {
        UiFile(
          id = it.id,
          name = it.name,
          mimeType = it.mimeType,
          url = it.url,
          localPath = null,
        )
      },
      claimStatusCardUiState = ClaimStatusCardUiState.fromClaimStatusCardsQuery(claim),
      claimStatus = claim.status.toStatus(),
      claimOutcome = when (claim.outcome) {
        ClaimOutcome.PAID -> ClaimDetailUiState.Content.ClaimOutcome.PAID
        ClaimOutcome.NOT_COMPENSATED -> ClaimDetailUiState.Content.ClaimOutcome.NOT_COMPENSATED
        ClaimOutcome.NOT_COVERED -> ClaimDetailUiState.Content.ClaimOutcome.NOT_COVERED
        ClaimOutcome.UNKNOWN__,
        null,
          -> ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN

        ClaimOutcome.UNRESPONSIVE -> ClaimDetailUiState.Content.ClaimOutcome.UNRESPONSIVE
      },
      uploadUri = claim.targetFileUploadUri,
      isUploadingFile = false,
      uploadError = null,
      claimType = claimType,
      insuranceDisplayName = insuranceDisplayName,
      submittedAt = submittedAt,
      termsConditionsUrl = termsConditionsUrl,
      savedFileUri = null,
      downloadError = null,
      isLoadingPdf = null,
      appealInstructionsUrl = claim.appealInstructionsUrl,
      isUploadingFilesEnabled = claim.isUploadingFilesEnabled,
      infoText = claim.infoText,
      displayItems = claim.displayItems.map {
        DisplayItem.fromStrings(it.displayTitle, it.displayValue)
      },
    )
  }

  companion object {
    private val POLL_INTERVAL = 10.seconds
  }
}

sealed interface Error {
  data object NetworkError : Error

  data object NoClaimFound : Error
}

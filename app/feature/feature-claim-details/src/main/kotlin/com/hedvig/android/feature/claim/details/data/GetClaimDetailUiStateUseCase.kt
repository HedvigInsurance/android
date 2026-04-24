package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
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
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import octopus.ClaimQuery
import octopus.PartnerClaimDetailQuery
import octopus.fragment.ClaimFragment
import octopus.fragment.PartnerClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus
import octopus.type.InsuranceDocumentType

internal class GetClaimDetailUiStateUseCase(
  private val apolloClient: ApolloClient,
  private val crossSellAfterClaimClosedRepository: CrossSellAfterClaimClosedRepository,
) {
  fun invoke(claimId: String, isPartnerClaim: Boolean = false): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        if (isPartnerClaim) {
          emitAll(partnerQueryFlow(claimId))
        } else {
          emitAll(queryFlow(claimId))
        }
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
          ensureNotNull(claim) { Error.NoClaimFound }
          if (claim.showClaimClosedFlow) {
            crossSellAfterClaimClosedRepository.acknowledgeClaimClosedStatus(claim)
          }
          ClaimDetailUiState.Content.fromClaim(claim, claim.conversation?.id, claim.conversation?.unreadMessageCount)
        }
      }
  }

  private fun partnerQueryFlow(claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return apolloClient
      .query(PartnerClaimDetailQuery(claimId))
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeFlow { Error.NetworkError }
      .map { response ->
        either {
          val claim = response.bind().partnerClaim
          ensureNotNull(claim) { Error.NoClaimFound }
          fromPartnerClaim(claim)
        }
      }
  }

  private fun fromPartnerClaim(claim: PartnerClaimFragment): ClaimDetailUiState.Content {
    val termsConditionsUrl = claim.productVariant?.documents
      ?.firstOrNull { it.type == InsuranceDocumentType.TERMS_AND_CONDITIONS }?.url
    val submittedAt = claim.submittedAt
      ?.atStartOfDayIn(TimeZone.UTC)
      ?.toLocalDateTime(TimeZone.UTC)
      ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return ClaimDetailUiState.Content(
      claimId = claim.id,
      conversationId = null,
      hasUnreadMessages = false,
      submittedContent = null,
      files = emptyList(),
      claimStatusCardUiState = ClaimStatusCardUiState.fromPartnerClaim(claim),
      claimStatus = when (claim.status) {
        ClaimStatus.CREATED -> ClaimDetailUiState.Content.ClaimStatus.CREATED
        ClaimStatus.IN_PROGRESS -> ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS
        ClaimStatus.CLOSED -> ClaimDetailUiState.Content.ClaimStatus.CLOSED
        ClaimStatus.REOPENED -> ClaimDetailUiState.Content.ClaimStatus.REOPENED
        ClaimStatus.UNKNOWN__, null -> ClaimDetailUiState.Content.ClaimStatus.UNKNOWN
      },
      claimOutcome = ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN,
      uploadUri = "",
      isUploadingFile = false,
      uploadError = null,
      claimType = claim.claimType,
      insuranceDisplayName = claim.exposureDisplayName ?: claim.productVariant?.displayName,
      submittedAt = submittedAt,
      termsConditionsUrl = termsConditionsUrl,
      savedFileUri = null,
      downloadError = null,
      isLoadingPdf = null,
      appealInstructionsUrl = null,
      isUploadingFilesEnabled = false,
      infoText = null,
      displayItems = claim.displayItems.map {
        DisplayItem.fromStrings(it.displayTitle, it.displayValue)
      },
    )
  }

  private fun ClaimDetailUiState.Content.Companion.fromClaim(
    claim: ClaimFragment,
    conversationId: String?,
    conversationUnreadMessageCount: Int?,
  ): ClaimDetailUiState.Content {
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

    return ClaimDetailUiState.Content(
      claimId = claim.id,
      conversationId = conversationId,
      hasUnreadMessages = (conversationUnreadMessageCount ?: 0) > 0,
      submittedContent = when {
        audioUrl != null -> {
          ClaimDetailUiState.Content.SubmittedContent.Audio(SignedAudioUrl.fromSignedAudioUrlString(audioUrl))
        }

        memberFreeText != null -> {
          ClaimDetailUiState.Content.SubmittedContent.FreeText(memberFreeText)
        }

        else -> {
          null
        }
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
      claimStatus = when (claim.status) {
        ClaimStatus.CREATED -> ClaimDetailUiState.Content.ClaimStatus.CREATED

        ClaimStatus.IN_PROGRESS -> ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS

        ClaimStatus.CLOSED -> ClaimDetailUiState.Content.ClaimStatus.CLOSED

        ClaimStatus.REOPENED -> ClaimDetailUiState.Content.ClaimStatus.REOPENED

        ClaimStatus.UNKNOWN__,
        null,
        -> ClaimDetailUiState.Content.ClaimStatus.UNKNOWN
      },
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

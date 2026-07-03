package com.hedvig.android.feature.home.home.data

import androidx.compose.runtime.Immutable
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.context.bind
import arrow.core.raise.either
import arrow.core.raise.nullable
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.crosssells.BundleProgress
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.addons.data.GetAddonBannerInfoUseCase
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.ui.emergency.FirstVetSection
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.HomeQuery
import octopus.UnreadMessageCountQuery
import octopus.fragment.HomeCrossSellFragment

internal interface GetHomeDataUseCase {
  fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>>
}

@Inject
internal class GetHomeDataUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val featureManager: FeatureManager,
  private val clock: Clock,
  private val timeZone: TimeZone,
  private val getAddonBannerInfoUseCase: GetAddonBannerInfoUseCase,
  private val hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
) : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>> {
    return combine(
      apolloClient.query(HomeQuery(true))
        .fetchPolicy(if (forceNetworkFetch) FetchPolicy.NetworkOnly else FetchPolicy.CacheAndNetwork)
        .safeFlow(),
      flow {
        while (currentCoroutineContext().isActive) {
          emitAll(
            apolloClient.query(UnreadMessageCountQuery())
              .fetchPolicy(FetchPolicy.CacheAndNetwork)
              .safeFlow(),
          )
          delay(5.seconds)
        }
      },
      getMemberRemindersUseCase.invoke(),
      flow {
        emitAll(getAddonBannerInfoUseCase.invoke(AddonBannerSource.INSURANCES_TAB))
      },
      featureManager.isFeatureEnabled(Feature.ENABLE_NEW_CONVERSATION_FROM_INBOX),
      hasAnyActiveConversationUseCase.invoke(alwaysHitTheNetwork = true),
    ) {
      homeQueryDataResult,
      unreadMessageCountResult,
      memberReminders,
      travelBannerInfo,
      inboxAlwaysAvailable,
      anyActiveConversations,
      ->
      either {
        val homeQueryData: HomeQuery.Data = homeQueryDataResult.bind()
        val contractStatus = homeQueryData.currentMember.toContractStatus()
        val veryImportantMessages = homeQueryData.currentMember.importantMessages.map {
          HomeData.VeryImportantMessage(
            id = it.id,
            message = it.message,
            linkInfo = it.linkInfo?.let { linkInfo ->
              if (linkInfo.url.isEmpty()) {
                logcat(LogPriority.ERROR) { "Backend should never return a present linkInfo with an empty url string" }
                null
              } else {
                val buttonText = linkInfo.buttonText.takeIf { it.isNotEmpty() }
                if (buttonText == null) {
                  logcat(LogPriority.ERROR) { "Backend should never return a present buttonText with an empty string" }
                }
                HomeData.VeryImportantMessage.LinkInfo(
                  buttonText = buttonText,
                  link = linkInfo.url,
                )
              }
            },
          )
        }
        val crossSellsData = homeQueryData.currentMember.crossSellV2

        val recommendedCrossSell = crossSellsData.recommendedCrossSell?.let {
          val bundleProgress = if (it.numberOfEligibleContracts > 0 && it.discountPercent != null) {
            BundleProgress(it.numberOfEligibleContracts, it.discountPercent)
          } else {
            null
          }
          RecommendedCrossSell(
            crossSell = it.crossSell.toCrossSell(),
            bannerText = it.bannerText,
            buttonText = it.buttonText,
            discountText = it.discountText,
            buttonDescription = it.buttonDescription,
            bundleProgress = bundleProgress,
            backgroundPillowImages = it.backgroundPillowImages?.let { images ->
              images.leftImage.src to images.rightImage.src
            },
          )
        }
        val otherCrossSellsData = crossSellsData.otherCrossSells.map {
          it.toCrossSell()
        }
        val crossSells = CrossSellSheetData(
          recommendedCrossSell = recommendedCrossSell,
          otherCrossSells = otherCrossSellsData,
        )
        val showChatIcon = shouldShowChatButton(
          isInboxEnabledFromKillSwitch = inboxAlwaysAvailable,
          hasActiveConversations = anyActiveConversations.bind(),
        )
        val unreadMessageCountData = unreadMessageCountResult.bind()
        val hasUnseenChatMessages = unreadMessageCountData
          .currentMember
          .conversations
          .map { it.unreadMessageCount }
          .plus(unreadMessageCountData.currentMember.legacyConversation?.unreadMessageCount)
          .any { it != null && it > 0 }
        val firstVetActions = homeQueryData.currentMember.memberActions
          ?.firstVetAction?.sections?.map { section ->
            FirstVetSection(
              section.buttonTitle,
              section.description,
              section.title,
              section.url,
            )
          } ?: emptyList()
        val travelBannerInfo = travelBannerInfo.getOrNull()
        HomeData(
          contractStatus = contractStatus,
          claimStatusCardsData = homeQueryData.claimStatusCards(),
          veryImportantMessages = veryImportantMessages,
          memberReminders = memberReminders,
          hasUnseenChatMessages = hasUnseenChatMessages,
          showHelpCenter = true,
          firstVetSections = firstVetActions,
          crossSells = crossSells,
          travelBannerInfo = travelBannerInfo?.firstOrNull(),
          showChatIcon = showChatIcon,
          resumableClaimId = homeQueryData.currentMember.resumableClaimIntent?.id,
        )
      }.onLeft { error: ApolloOperationError ->
        logcat(operationError = error) { "GetHomeDataUseCase failed with $error" }
      }
    }
  }

  private fun shouldShowChatButton(isInboxEnabledFromKillSwitch: Boolean, hasActiveConversations: Boolean): Boolean {
    if (isInboxEnabledFromKillSwitch) return true
    return hasActiveConversations
  }

  private fun HomeQuery.Data.CurrentMember.toContractStatus(): HomeData.ContractStatus {
    val activeInTheFutureDate = activeInTheFutureDate()
    if (activeInTheFutureDate != null) {
      return HomeData.ContractStatus.ActiveInFuture(activeInTheFutureDate)
    }
    val hasActiveContracts = activeContracts.isNotEmpty()
    if (hasActiveContracts) {
      return HomeData.ContractStatus.Active
    }
    if (isAutomaticallySwitching()) {
      return HomeData.ContractStatus.Switching
    }
    if (areAllNonTerminatedContractsPending()) {
      return HomeData.ContractStatus.Pending
    }
    val allAreTerminated = activeContracts.isEmpty() && pendingContracts.isEmpty() && terminatedContracts.isNotEmpty()
    if (allAreTerminated) {
      return HomeData.ContractStatus.Terminated
    }
    return HomeData.ContractStatus.Unknown.also {
      logcat(LogPriority.WARN) {
        "HomeQuery.Data.CurrentMember:$this. Resulted in an unknown contract. It should be mapped instead"
      }
    }
  }

  internal fun HomeCrossSellFragment.toCrossSell(): CrossSell {
    return with(this) {
      CrossSell(
        id = id,
        title = title,
        subtitle = description,
        storeUrl = storeUrl,
        pillowImage = ImageAsset(
          id = pillowImageLarge.id,
          src = pillowImageLarge.src,
          description = pillowImageLarge.alt,
        ),
      )
    }
  }

  private fun HomeQuery.Data.CurrentMember.isAutomaticallySwitching(): Boolean {
    val isSwitchable = this.pendingContracts.any { pendingContract ->
      pendingContract.externalInsuranceCancellationHandledByHedvig
    }
    return isSwitchable && areAllNonTerminatedContractsPending()
  }

  /**
   * @return [LocalDate] of the earliest upcoming active contract.
   * returns null when there are no active contracts, or the earliest starting contract has already started in the past
   */
  private fun HomeQuery.Data.CurrentMember.activeInTheFutureDate(): LocalDate? {
    return nullable {
      val earliestStartingActiveContract = activeContracts
        .sortedBy { it.masterInceptionDate }
        .firstOrNull()
        .bind()
      val masterInceptionDate = earliestStartingActiveContract.masterInceptionDate
      ensure(masterInceptionDate.atStartOfDayIn(timeZone) > clock.now())
      masterInceptionDate
    }
  }

  private fun HomeQuery.Data.CurrentMember.areAllNonTerminatedContractsPending(): Boolean {
    return activeContracts.isEmpty() && pendingContracts.isNotEmpty()
  }
}

private fun HomeQuery.Data.claimStatusCards(): HomeData.ClaimStatusCardsData? {
  val regularCards =
    this.currentMember.claims.orEmpty().map(ClaimStatusCardUiState::fromClaimStatusCardsQuery) +
      this.currentMember.claimsActive.orEmpty().map(ClaimStatusCardUiState::fromClaimStatusCardsQuery)
  val partnerCards = this.currentMember.partnerClaimsActive.map(ClaimStatusCardUiState::fromPartnerClaim)

  val allCards = (regularCards + partnerCards)
    .sortedWith(compareByDescending(nullsLast()) { it.submittedDate })
    .toNonEmptyListOrNull() ?: return null

  return HomeData.ClaimStatusCardsData(
    claimStatusCardsUiState = allCards,
  )
}

data class HomeData(
  val contractStatus: ContractStatus,
  val claimStatusCardsData: ClaimStatusCardsData?,
  val veryImportantMessages: List<VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val showChatIcon: Boolean,
  val hasUnseenChatMessages: Boolean,
  val showHelpCenter: Boolean,
  val firstVetSections: List<FirstVetSection>,
  val crossSells: CrossSellSheetData,
  val travelBannerInfo: AddonBannerInfo?,
  val resumableClaimId: String?,
) {
  @Immutable
  data class ClaimStatusCardsData(
    val claimStatusCardsUiState: NonEmptyList<ClaimStatusCardUiState>,
  )

  data class VeryImportantMessage(
    val id: String,
    val message: String,
    val linkInfo: LinkInfo?,
  ) {
    data class LinkInfo(
      val buttonText: String?,
      val link: String,
    )
  }

  sealed interface ContractStatus {
    data object Active : ContractStatus

    data object Terminated : ContractStatus

    data object Pending : ContractStatus

    data object Switching : ContractStatus

    data class ActiveInFuture(
      val futureInceptionDate: LocalDate,
    ) : ContractStatus

    data object Unknown : ContractStatus
  }

  companion object
}

/**
 * The reason this exists is because the standard combine function only allows up to 5 generic flows.
 */
fun <T1, T2, T3, T4, T5, T6, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
  @Suppress("UNCHECKED_CAST")
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6,
  )
}

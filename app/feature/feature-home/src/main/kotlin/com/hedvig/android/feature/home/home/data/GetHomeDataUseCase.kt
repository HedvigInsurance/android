package com.hedvig.android.feature.home.home.data

import androidx.compose.runtime.Immutable
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.raise.either
import arrow.core.raise.nullable
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCaseProvider
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
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
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import octopus.HomeQuery
import octopus.UnreadMessageCountQuery
import octopus.fragment.HomeCrossSellFragment

internal interface GetHomeDataUseCase {
  fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>>
}

internal class GetHomeDataUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
  private val getMemberRemindersUseCase: GetMemberRemindersUseCase,
  private val featureManager: FeatureManager,
  private val clock: Clock,
  private val timeZone: TimeZone,
  private val getTravelAddonBannerInfoUseCaseProvider: GetTravelAddonBannerInfoUseCaseProvider,
) : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>> {
    return combine(
      apolloClient.query(HomeQuery())
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
      hasAnyActiveConversationUseCase.invoke(alwaysHitTheNetwork = true),
      getMemberRemindersUseCase.invoke(),
      flow {
        emitAll(getTravelAddonBannerInfoUseCaseProvider.provide().invoke(TravelAddonBannerSource.INSURANCES_TAB))
      },
      featureManager.isFeatureEnabled(Feature.DISABLE_CHAT),
      featureManager.isFeatureEnabled(Feature.HELP_CENTER),
    ) {
      homeQueryDataResult,
      unreadMessageCountResult,
      isEligibleToShowTheChatIconResult,
      memberReminders,
      travelBannerInfo,
      isChatDisabled,
      isHelpCenterEnabled,
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
        val crossSellsData = homeQueryData.currentMember.crossSell
        val recommendedCrossSell = crossSellsData.recommendedCrossSell?.let {
          RecommendedCrossSell(
            crossSell = it.crossSell.toCrossSell(),
            bannerText = it.bannerText,
            buttonText = it.buttonText,
            discountText = it.discountText,
            buttonDescription = it.buttonDescription,
          )
        }
        val otherCrossSellsData = crossSellsData.otherCrossSells.map {
          it.toCrossSell()
        }
        val crossSells = CrossSellSheetData(
          recommendedCrossSell = recommendedCrossSell,
          otherCrossSells = otherCrossSellsData,
        )
        val showChatIcon = !shouldHideChatButton(
          isChatDisabledFromKillSwitch = isChatDisabled,
          isEligibleToShowTheChatIcon = isEligibleToShowTheChatIconResult.bind(),
          isHelpCenterEnabled = isHelpCenterEnabled,
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
          showChatIcon = showChatIcon,
          hasUnseenChatMessages = hasUnseenChatMessages,
          showHelpCenter = isHelpCenterEnabled,
          firstVetSections = firstVetActions,
          crossSells = crossSells,
          travelBannerInfo = travelBannerInfo,
        )
      }.onLeft { error: ApolloOperationError ->
        logcat(operationError = error) { "GetHomeDataUseCase failed with $error" }
      }
    }
  }

  private fun shouldHideChatButton(
    isChatDisabledFromKillSwitch: Boolean,
    isEligibleToShowTheChatIcon: Boolean,
    isHelpCenterEnabled: Boolean,
  ): Boolean {
    // If the feature flag is off, we should hide the chat button regardless of the other conditions
    if (isChatDisabledFromKillSwitch) return true
    // If the help center is disabled, we must always show the chat button, otherwise there is no way to get to the chat
    if (!isHelpCenterEnabled) return false
    return !isEligibleToShowTheChatIcon
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
      logcat(LogPriority.ERROR) {
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
  val claimStatusCards: NonEmptyList<HomeQuery.Data.CurrentMember.Claim> =
    this.currentMember.claims.toNonEmptyListOrNull() ?: return null
  return HomeData.ClaimStatusCardsData(claimStatusCards.map(ClaimStatusCardUiState::fromClaimStatusCardsQuery))
}

internal data class HomeData(
  val contractStatus: ContractStatus,
  val claimStatusCardsData: ClaimStatusCardsData?,
  val veryImportantMessages: List<VeryImportantMessage>,
  val memberReminders: MemberReminders,
  val showChatIcon: Boolean,
  val hasUnseenChatMessages: Boolean,
  val showHelpCenter: Boolean,
  val firstVetSections: List<FirstVetSection>,
  val crossSells: CrossSellSheetData,
  val travelBannerInfo: TravelAddonBannerInfo?,
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
public fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6, flow7) { args: Array<*> ->
  @Suppress("UNCHECKED_CAST")
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6,
    args[6] as T7,
  )
}

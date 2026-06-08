package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.NonEmptyList
import arrow.core.merge
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowChooseInsuranceKey
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.help.center.HelpCenterEvent.ClearSearchQuery
import com.hedvig.android.feature.help.center.HelpCenterEvent.NavigateToQuickAction
import com.hedvig.android.feature.help.center.HelpCenterEvent.OnDismissQuickActionDialog
import com.hedvig.android.feature.help.center.HelpCenterEvent.OnQuickActionSelected
import com.hedvig.android.feature.help.center.HelpCenterEvent.UpdateSearchResults
import com.hedvig.android.feature.help.center.HelpCenterUiState.ActiveSearchState.Empty
import com.hedvig.android.feature.help.center.HelpCenterUiState.ActiveSearchState.Success
import com.hedvig.android.feature.help.center.HelpCenterUiState.Search
import com.hedvig.android.feature.help.center.data.FAQItem
import com.hedvig.android.feature.help.center.data.FAQTopic
import com.hedvig.android.feature.help.center.data.GetHelpCenterFAQUseCase
import com.hedvig.android.feature.help.center.data.GetPuppyGuideUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.data.InnerHelpCenterDestination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoInsured
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoOwners
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkChangeAddress
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkChangeTier
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddInfo
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddOrRemove
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkConnectPayment
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkTermination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.navigation.EmergencyKey
import com.hedvig.android.feature.help.center.navigation.FirstVetKey
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal sealed interface HelpCenterEvent {
  data class OnQuickActionSelected(val quickAction: QuickAction) : HelpCenterEvent

  data object OnDismissQuickActionDialog : HelpCenterEvent

  data class UpdateSearchResults(
    val searchQuery: String,
    val results: HelpCenterUiState.HelpSearchResults?,
  ) : HelpCenterEvent

  data object ClearSearchQuery : HelpCenterEvent

  data class NavigateToQuickAction(val destination: QuickLinkDestination) : HelpCenterEvent

  data object ReloadFAQAndQuickLinks : HelpCenterEvent
}

internal data class HelpCenterUiState(
  val topics: List<FAQTopic>,
  val questions: List<FAQItem>,
  val quickLinksUiState: QuickLinkUiState,
  val selectedQuickAction: QuickAction?,
  val search: Search?,
  val showNavigateToInboxButton: Boolean,
  val puppyGuide: PuppyGuidePresentation?,
) {
  data class QuickLink(val quickAction: QuickAction)

  sealed interface QuickLinkUiState {
    data object Loading : QuickLinkUiState

    data object NoQuickLinks : QuickLinkUiState

    data class QuickLinks(val quickLinks: NonEmptyList<QuickLink>) : QuickLinkUiState
  }

  sealed interface PuppyGuidePresentation {
    data object FullCard : PuppyGuidePresentation

    data object QuickAction : PuppyGuidePresentation
  }

  data class Search(
    val searchQuery: String?,
    val activeSearchState: ActiveSearchState,
  )

  sealed interface ActiveSearchState {
    data object Empty : ActiveSearchState

    data class Success(val results: HelpSearchResults) : ActiveSearchState
  }

  data class HelpSearchResults(
    val filteredQuickLinks: NonEmptyList<QuickLink>?,
    val filteredQuestions: NonEmptyList<FAQItem>?,
  )
}

internal class HelpCenterPresenter(
  private val getQuickLinksUseCase: GetQuickLinksUseCase,
  private val hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
  private val getHelpCenterFAQUseCase: GetHelpCenterFAQUseCase,
  private val getPuppyGuideUseCase: GetPuppyGuideUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<HelpCenterEvent, HelpCenterUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterEvent>.present(lastState: HelpCenterUiState): HelpCenterUiState {
    var selectedQuickAction by remember { mutableStateOf<QuickAction?>(null) }
    var quickLinksUiState by remember { mutableStateOf(lastState.quickLinksUiState) }
    var currentState by remember {
      mutableStateOf(lastState)
    }
    var loadIteration by remember { mutableIntStateOf(0) }
    val hasAnyActiveConversation by remember(hasAnyActiveConversationUseCase) {
      hasAnyActiveConversationUseCase.invoke().map { it.mapLeft { false }.merge() }
    }.collectAsState(false)

    CollectEvents { event ->
      when (event) {
        is OnQuickActionSelected -> {
          selectedQuickAction = event.quickAction
        }

        is OnDismissQuickActionDialog -> {
          selectedQuickAction = null
        }

        ClearSearchQuery -> {
          currentState = currentState.copy(search = null)
        }

        is UpdateSearchResults -> {
          currentState = if (event.results == null) {
            currentState.copy(
              search = Search(
                event.searchQuery,
                Empty,
              ),
            )
          } else {
            currentState.copy(
              search = Search(
                event.searchQuery,
                Success(event.results),
              ),
            )
          }
        }

        is NavigateToQuickAction -> {
          selectedQuickAction = null
          val key: HedvigNavKey = when (val destination = event.destination) {
            is InnerHelpCenterDestination.FirstVet -> {
              FirstVetKey(destination.sections)
            }

            is InnerHelpCenterDestination.QuickLinkSickAbroad -> {
              EmergencyKey(destination.deflectData)
            }

            QuickLinkChangeAddress -> {
              SelectContractForMovingKey
            }

            is QuickLinkCoInsuredAddInfo -> {
              CoInsuredAddInfoKey(destination.contractId, CoInsuredFlowType.CoInsured)
            }

            is QuickLinkCoInsuredAddOrRemove -> {
              CoInsuredAddOrRemoveKey(destination.contractId, CoInsuredFlowType.CoInsured)
            }

            is QuickLinkCoOwnerAddInfo -> {
              CoInsuredAddInfoKey(destination.contractId, CoInsuredFlowType.CoOwners)
            }

            is QuickLinkCoOwnerAddOrRemove -> {
              CoInsuredAddOrRemoveKey(destination.contractId, CoInsuredFlowType.CoOwners)
            }

            QuickLinkConnectPayment -> {
              TrustlyKey
            }

            QuickLinkTermination -> {
              TerminateInsuranceKey(null)
            }

            QuickLinkTravelCertificate -> {
              TravelCertificateKey
            }

            QuickLinkChangeTier -> {
              StartTierFlowChooseInsuranceKey
            }

            ChooseInsuranceForEditCoInsured -> {
              EditCoInsuredTriageKey()
            }

            ChooseInsuranceForEditCoOwners -> {
              EditCoInsuredTriageKey(type = CoInsuredFlowType.CoOwners)
            }
          }
          backstack.add(key)
        }

        HelpCenterEvent.ReloadFAQAndQuickLinks -> {
          loadIteration++
        }
      }
    }

    LaunchedEffect(loadIteration) {
      if (quickLinksUiState !is HelpCenterUiState.QuickLinkUiState.QuickLinks) {
        quickLinksUiState = HelpCenterUiState.QuickLinkUiState.Loading
      }
      combine(
        flow = flow { emit(getQuickLinksUseCase.invoke()) },
        flow2 = flow { emit(getHelpCenterFAQUseCase.invoke()) },
        flow3 = getPuppyGuideUseCase.invoke(),
      ) { quickLinks, faq, puppyGuideResult ->
        quickLinksUiState = quickLinks.fold(
          ifLeft = {
            HelpCenterUiState.QuickLinkUiState.NoQuickLinks
          },
          ifRight = { quickActionList ->
            val list = quickActionList.map { quickAction ->
              HelpCenterUiState.QuickLink(quickAction)
            }.toNonEmptyListOrNull()
            if (list == null) {
              HelpCenterUiState.QuickLinkUiState.NoQuickLinks
            } else {
              HelpCenterUiState.QuickLinkUiState.QuickLinks(list)
            }
          },
        )
        val topics = faq.getOrNull()?.topics ?: listOf()
        val questions = faq.getOrNull()?.commonFAQ ?: listOf()
        val puppyGuide = puppyGuideResult.getOrNull()
        val puppyGuidePresentation = when {
          puppyGuide == null || puppyGuide.stories.isEmpty() -> null
          puppyGuide.isForYoungDog == true -> HelpCenterUiState.PuppyGuidePresentation.FullCard
          else -> HelpCenterUiState.PuppyGuidePresentation.QuickAction
        }
        currentState = currentState.copy(
          topics = topics,
          questions = questions,
          quickLinksUiState = quickLinksUiState,
          selectedQuickAction = selectedQuickAction,
          showNavigateToInboxButton = hasAnyActiveConversation,
          puppyGuide = puppyGuidePresentation,
        )
      }.collect()
    }
    return currentState.copy(
      quickLinksUiState = quickLinksUiState,
      selectedQuickAction = selectedQuickAction,
      showNavigateToInboxButton = hasAnyActiveConversation,
    )
  }
}

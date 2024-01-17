package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.commonclaim.CommonClaim
import com.hedvig.android.feature.help.center.data.GetCommonClaimsUseCase
import com.hedvig.android.feature.help.center.data.GetQuickLinksUseCase
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.model.commonQuestions
import com.hedvig.android.feature.help.center.model.commonTopics
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal sealed interface HelpCenterEvent {
  data class OnQuickActionSelected(val quickAction: QuickAction) : HelpCenterEvent
  data object OnDismissQuickActionDialog : HelpCenterEvent
}

internal data class HelpCenterUiState(
  val topics: ImmutableList<Topic>,
  val questions: ImmutableList<Question>,
  val quickLinks: ImmutableList<QuickAction>,
  val commonClaims: ImmutableList<CommonClaim>,
  val selectedQuickAction: QuickAction?,
)

internal class HelpCenterPresenter(
  private val getCommonClaimsUseCase: GetCommonClaimsUseCase,
  private val getQuickLinksUseCase: GetQuickLinksUseCase,
) : MoleculePresenter<HelpCenterEvent, HelpCenterUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterEvent>.present(lastState: HelpCenterUiState): HelpCenterUiState {
    var quickLinks by remember { mutableStateOf(persistentListOf<QuickAction>()) }
    var commonClaims by remember { mutableStateOf(persistentListOf<CommonClaim>()) }
    var selectedQuickAction by remember { mutableStateOf<QuickAction?>(null) }

    CollectEvents { event ->
      selectedQuickAction = when (event) {
        is HelpCenterEvent.OnQuickActionSelected -> event.quickAction
        is HelpCenterEvent.OnDismissQuickActionDialog -> null
      }
    }

    LaunchedEffect(Unit) {
      getQuickLinksUseCase.invoke().fold(
        ifLeft = { quickLinks = persistentListOf() },
        ifRight = { quickLinks = it },
      )

      getCommonClaimsUseCase.invoke().fold(
        ifLeft = { commonClaims = persistentListOf() },
        ifRight = { commonClaims = it },
      )
    }

    return HelpCenterUiState(
      topics = commonTopics,
      questions = commonQuestions,
      quickLinks = quickLinks,
      commonClaims = commonClaims,
      selectedQuickAction = selectedQuickAction,
    )
  }
}

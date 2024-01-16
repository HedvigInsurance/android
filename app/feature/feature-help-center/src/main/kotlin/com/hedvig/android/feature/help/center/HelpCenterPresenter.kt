package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.help.center.model.Question
import com.hedvig.android.feature.help.center.model.QuickLink
import com.hedvig.android.feature.help.center.model.Topic
import com.hedvig.android.feature.help.center.model.commonQuestions
import com.hedvig.android.feature.help.center.model.commonTopics
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

internal sealed interface HelpCenterEvent {

}

internal data class HelpCenterUiState(
  val topics: ImmutableList<Topic>,
  val questions: ImmutableList<Question>,
  val quickLinks: ImmutableList<QuickLink>,
)

internal class HelpCenterPresenter(
  private val featureManager: FeatureManager,
) : MoleculePresenter<HelpCenterEvent, HelpCenterUiState> {
  @Composable
  override fun MoleculePresenterScope<HelpCenterEvent>.present(lastState: HelpCenterUiState): HelpCenterUiState {

    var quickLinks by remember { mutableStateOf(persistentListOf<QuickLink>()) }

    LaunchedEffect(Unit) {
      quickLinks = buildList {
        if (featureManager.isFeatureEnabled(Feature.EDIT_COINSURED)) {
          add(QuickLink.EditCoInsured)
        }

        if (featureManager.isFeatureEnabled(Feature.MOVING_FLOW)) {
          add(QuickLink.UpdateAddress)
        }

        if (featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN)) {
          add(QuickLink.ChangeBank)
        }
      }.toPersistentList()
    }

    return HelpCenterUiState(
      topics = commonTopics,
      questions = commonQuestions,
      quickLinks = quickLinks,
    )
  }
}

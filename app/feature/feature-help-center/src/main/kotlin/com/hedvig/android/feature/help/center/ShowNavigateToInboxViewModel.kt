package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import arrow.core.merge
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.map

internal class ShowNavigateToInboxViewModel(
  hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
) : MoleculeViewModel<Unit, Boolean>(
    false,
    ShowNavigateToInboxPresenter(hasAnyActiveConversationUseCase),
  )

internal class ShowNavigateToInboxPresenter(
  private val hasAnyActiveConversationUseCase: HasAnyActiveConversationUseCase,
) : MoleculePresenter<Unit, Boolean> {
  @Composable
  override fun MoleculePresenterScope<Unit>.present(lastState: Boolean): Boolean {
    return remember(hasAnyActiveConversationUseCase) {
      hasAnyActiveConversationUseCase.invoke().map { it.mapLeft { false }.merge() }
    }.collectAsState(lastState).value
  }
}

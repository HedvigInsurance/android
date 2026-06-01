package com.hedvig.android.feature.help.center

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import arrow.core.merge
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.map

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
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

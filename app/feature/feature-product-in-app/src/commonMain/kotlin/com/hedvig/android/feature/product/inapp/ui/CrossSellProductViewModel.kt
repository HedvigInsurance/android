package com.hedvig.android.feature.product.inapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.product.inapp.data.CrossSellProductInfo
import com.hedvig.android.feature.product.inapp.data.GetCrossSellProductInfoUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class CrossSellProductViewModel(
  @Assisted crossSellId: String,
  getCrossSellProductInfoUseCase: GetCrossSellProductInfoUseCase,
) : MoleculeViewModel<CrossSellProductEvent, CrossSellProductUiState>(
    initialState = CrossSellProductUiState.Loading,
    presenter = CrossSellProductPresenter(
      crossSellId = crossSellId,
      getCrossSellProductInfoUseCase = getCrossSellProductInfoUseCase,
    ),
  )

internal class CrossSellProductPresenter(
  private val crossSellId: String,
  private val getCrossSellProductInfoUseCase: GetCrossSellProductInfoUseCase,
) : MoleculePresenter<CrossSellProductEvent, CrossSellProductUiState> {
  @Composable
  override fun MoleculePresenterScope<CrossSellProductEvent>.present(
    lastState: CrossSellProductUiState,
  ): CrossSellProductUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        CrossSellProductEvent.Retry -> loadIteration++
      }
    }

    LaunchedEffect(loadIteration) {
      if (currentState is CrossSellProductUiState.Content) return@LaunchedEffect
      currentState = CrossSellProductUiState.Loading
      currentState = getCrossSellProductInfoUseCase.invoke(crossSellId).fold(
        ifLeft = { CrossSellProductUiState.Failure },
        ifRight = { CrossSellProductUiState.Content(it) },
      )
    }

    return currentState
  }
}

internal sealed interface CrossSellProductUiState {
  data object Loading : CrossSellProductUiState

  data object Failure : CrossSellProductUiState

  data class Content(val productInfo: CrossSellProductInfo) : CrossSellProductUiState
}

internal sealed interface CrossSellProductEvent {
  data object Retry : CrossSellProductEvent
}

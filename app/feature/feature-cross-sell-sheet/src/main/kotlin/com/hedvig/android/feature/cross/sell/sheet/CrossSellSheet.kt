package com.hedvig.android.feature.cross.sell.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.Content
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.DontShow
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.Error
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.Loading
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

@Composable
fun CrossSellSheet(
  isInScreenEligibleForCrossSells: Boolean,
  onCrossSellClick: (String) -> Unit,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  val viewModel: CrossSellSheetViewModel = koinViewModel()
  if (isInScreenEligibleForCrossSells) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberHedvigBottomSheetState<CrossSellSheetData>()
    LaunchedEffect(uiState) {
      @Suppress("NAME_SHADOWING")
      when (val uiState = uiState) {
        DontShow,
        is Error,
        Loading,
        -> sheetState.dismiss()

        is Content -> sheetState.show(uiState.crossSellSheetData)
      }
    }
    LaunchedEffect(sheetState, viewModel) {
      snapshotFlow { sheetState.isVisible }
        .distinctUntilChanged()
        .collect { isVisible ->
          if (isVisible) {
            viewModel.emit(CrossSellSheetEvent.CrossSellSheetShown)
          }
        }
    }
    com.hedvig.android.crosssells.CrossSellSheet(
      state = sheetState,
      onCrossSellClick = onCrossSellClick,
      onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
    )
  }
}

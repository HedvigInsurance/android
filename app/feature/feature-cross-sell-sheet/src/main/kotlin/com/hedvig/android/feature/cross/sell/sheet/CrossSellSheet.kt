package com.hedvig.android.feature.cross.sell.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.withStateAtLeast
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.Content
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.DontShow
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.Error
import com.hedvig.android.feature.cross.sell.sheet.CrossSellSheetState.Loading
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

@Composable
fun CrossSellSheet(isInScreenEligibleForCrossSells: Boolean, onCrossSellClick: (String) -> Unit) {
  val viewModel: CrossSellSheetViewModel = koinViewModel()
  if (isInScreenEligibleForCrossSells) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberHedvigBottomSheetState<CrossSellSheetData>()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(uiState, lifecycle) {
      @Suppress("NAME_SHADOWING")
      when (val uiState = uiState) {
        is Content -> {
          lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) {
            logcat { "CrossSellSheet showing after a succesful flow" }
            sheetState.show(uiState.crossSellSheetData)
          }
        }

        DontShow,
        is Error,
        Loading,
        -> {
        }
      }
    }
    @Suppress("NAME_SHADOWING")
    when (uiState) {
      Loading,
      DontShow,
      is Error,
      -> {
      }

      is Content -> {
        LaunchedEffect(sheetState, viewModel) {
          snapshotFlow { sheetState.isVisible }
            .distinctUntilChanged()
            .collect { isVisible ->
              if (isVisible) {
                viewModel.emit(CrossSellSheetEvent.CrossSellSheetShown)
              }
            }
        }
      }
    }
    com.hedvig.android.crosssells.CrossSellBottomSheet(
      state = sheetState,
      onCrossSellClick = onCrossSellClick,
    )
  }
}

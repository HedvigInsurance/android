package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredDestination
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.editCoInsuredGraph(navigateUp: () -> Unit) {
  composable<EditCoInsuredDestination> { backStackEntry ->
    EditCoInsuredDestination(
      koinViewModel { parametersOf(contractId) },
      contractId,
      allowEdit,
      navigateUp,
    )
  }
}

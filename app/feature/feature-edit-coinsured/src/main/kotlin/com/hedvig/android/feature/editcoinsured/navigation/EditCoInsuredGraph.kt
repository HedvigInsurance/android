package com.hedvig.android.feature.editcoinsured.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredDestination
import com.kiwi.navigationcompose.typed.composable

fun NavGraphBuilder.editCoInsuredGraph(
  navigateUp: () -> Unit,
) {
  composable<EditCoInsuredDestination> { backStackEntry ->
    EditCoInsuredDestination(
      contractId,
      navigateUp,
    )
  }
}

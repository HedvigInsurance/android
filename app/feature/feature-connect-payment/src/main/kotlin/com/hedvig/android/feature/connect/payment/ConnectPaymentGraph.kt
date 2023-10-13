package com.hedvig.android.feature.connect.payment

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable

fun NavGraphBuilder.connectPaymentGraph() {
  composable<AppDestination.ConnectPaymentTrustly>() {
  }
  composable<AppDestination.ConnectPaymentAdyen>() {
  }
}

package com.hedvig.android.feature.connect.payment.adyen

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable

fun NavGraphBuilder.connectAdyenPaymentGraph() {
  composable<AppDestination.ConnectPaymentAdyen>() {
  }
}

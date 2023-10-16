package com.hedvig.android.feature.connect.payment

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable

fun NavGraphBuilder.connectTrustlyPaymentGraph() {
  composable<AppDestination.ConnectPaymentTrustly>() {
  }
}

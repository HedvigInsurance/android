package com.hedvig.android.feature.payments2.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface PaymentsDestinations2 : Destination {
  @Serializable
  data object Overview : PaymentsDestinations2
}

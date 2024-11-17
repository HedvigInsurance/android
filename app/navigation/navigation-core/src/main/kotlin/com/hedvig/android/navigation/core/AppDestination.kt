package com.hedvig.android.navigation.core

import androidx.annotation.Keep
import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

sealed interface AppDestination {
  @Serializable
  data class ClaimDetails(
    val claimId: String,
  ) : AppDestination, Destination
}

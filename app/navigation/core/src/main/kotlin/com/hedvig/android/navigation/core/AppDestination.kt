package com.hedvig.android.navigation.core

import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface AppDestination : Destination {
  sealed interface TopLevelDestination : AppDestination {
    @Serializable
    object Home : TopLevelDestination

    @Serializable
    object Insurance : TopLevelDestination

    @Serializable
    object Profile : TopLevelDestination

    @Serializable
    object Referrals : TopLevelDestination
  }

  @Serializable
  object ChangeAddress : AppDestination

  @Serializable
  object GenerateTravelCertificate : AppDestination

  @Serializable
  object Eurobonus : AppDestination

  @Serializable
  object BusinessModel : AppDestination

  // region claims
  @Serializable
  object ClaimsTriaging : AppDestination

  @Serializable
  object LegacyClaimsTriaging : AppDestination

  @Serializable
  data class ClaimsFlow(
    val entryPointId: EntryPointId?,
    val entryPointOptionId: EntryPointOptionId?,
  ) : AppDestination
  //endregion
}

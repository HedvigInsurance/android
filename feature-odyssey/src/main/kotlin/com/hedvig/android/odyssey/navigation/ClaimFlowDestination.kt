package com.hedvig.android.odyssey.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface Destinations : Destination {
  @Serializable
  object ClaimFlow : Destinations
}

internal sealed interface ClaimFlowDestination : Destination {
  @Serializable
  object StartStep : ClaimFlowDestination

  @Serializable
  object AudioRecording : ClaimFlowDestination //  -> AudioRecorderScreen(

  @Serializable
  object ClaimSummary : ClaimFlowDestination //  -> EditClaimScreen(

  @Serializable
  object DateOfOccurrence : ClaimFlowDestination //  -> DateOfOccurrence(viewModel)

  @Serializable
  object DateOfOccurrencePlusLocation : ClaimFlowDestination //  -> DateOfOccurrenceAndLocationScreen(

  @Serializable
  object Location : ClaimFlowDestination //  -> Location(viewModel)

  @Serializable
  object PhoneNumber : ClaimFlowDestination //  -> PhoneNumber(

  @Serializable
  object SingleItem : ClaimFlowDestination //  -> SingleItemScreen(

  @Serializable
  object SingleItemPayout : ClaimFlowDestination

  @Serializable
  object ManualHandling : ClaimFlowDestination

  @Serializable
  object UnknownScreen : ClaimFlowDestination
}

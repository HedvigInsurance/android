package com.hedvig.android.odyssey

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.hedvig.android.odyssey.ui.AudioRecorderScreen
import com.hedvig.android.odyssey.ui.DateOfOccurence
import com.hedvig.android.odyssey.ui.DateOfOccurrenceAndLocation
import com.hedvig.android.odyssey.ui.HonestyPledge
import com.hedvig.android.odyssey.ui.Location
import com.hedvig.android.odyssey.ui.PayoutSummary
import com.hedvig.android.odyssey.ui.PhoneNumber
import com.hedvig.android.odyssey.ui.RequestPushNotifications
import com.hedvig.android.odyssey.ui.SelfCheckout
import com.hedvig.android.odyssey.ui.SingleItem
import com.hedvig.android.odyssey.ui.Success
import com.hedvig.common.remote.file.File
import java.time.LocalDate

class AutomationClaimScreen(
  val path: String,
  val isApplicable: (inputs: List<Input>, resolutions: Set<Resolution>) -> Boolean,
  val content: @Composable (NavBackStackEntry) -> Unit,
)

fun createAutomationClaimScreens(viewModel: ClaimsFlowViewModel): List<AutomationClaimScreen> = listOf(
  AutomationClaimScreen(
    path = "honesty-pledge",
    isApplicable = { _, _ -> true },
    content = { HonestyPledge(viewModel) },
  ),
  AutomationClaimScreen(
    path = "REQUEST_PUSH_NOTIFICATIONS",
    isApplicable = { _, _ -> false },
    content = { RequestPushNotifications(viewModel) },
  ),
  AutomationClaimScreen(
    path = "PHONE_NUMBER",
    isApplicable = { _, _ -> false },
    content = { PhoneNumber(viewModel) },
  ),
  AutomationClaimScreen(
    path = "DATE_OF_OCCURRENCE_PLUS_LOCATION",
    isApplicable = { inputs, _ ->
      inputs.filterIsInstance<Input.DateOfOccurrence>().isNotEmpty()
        && inputs.filterIsInstance<Input.Location>().isNotEmpty()
    },
    content = { DateOfOccurrenceAndLocation(viewModel) },
  ),
  AutomationClaimScreen(
    path = "DATE_OF_OCCURRENCE",
    isApplicable = { inputs, _ -> inputs.filterIsInstance<Input.DateOfOccurrence>().isNotEmpty() },
    content = { DateOfOccurence(viewModel) },
  ),
  AutomationClaimScreen(
    path = "LOCATION",
    isApplicable = { inputs, _ -> inputs.filterIsInstance<Input.Location>().isNotEmpty() },
    content = { Location(viewModel) },
  ),
  AutomationClaimScreen(
    "SINGLE_ITEM",
    isApplicable = { inputs, _ -> inputs.filterIsInstance<Input.SingleItem>().isNotEmpty() },
    content = { SingleItem(viewModel) },
  ),
  AutomationClaimScreen(
    path = "AUDIO_RECORDING",
    isApplicable = { inputs, _ ->
      inputs.filterIsInstance<Input.AudioRecording>().isNotEmpty()
    },
    content = { AudioRecorderScreen(viewModel) },
  ),
  AutomationClaimScreen(
    path = "SELF_CHECKOUT",
    isApplicable = { _, resolutions -> resolutions.filterIsInstance<Resolution.SingleItemPayout>().isNotEmpty() },
    content = { PayoutSummary(viewModel) },
  ),
  AutomationClaimScreen(
    path = "SUCCESS",
    isApplicable = { _, _ -> true },
    content = { Success(viewModel) },
  ),
)

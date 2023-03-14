package com.hedvig.android.odyssey.step.claimsummary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.android.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrenceAndLocation
import com.hedvig.android.odyssey.ui.SingleItem
import com.hedvig.odyssey.remote.money.MonetaryAmount
import hedvig.resources.R
import java.time.LocalDate

@Composable
internal fun ClaimSummaryDestination(
  imageLoader: ImageLoader,
) {
  ClaimSummaryScreen(imageLoader)
}

@Composable
private fun ClaimSummaryScreen(
  imageLoader: ImageLoader,
) {
  EditClaimScreen(
    state = ClaimState(), // viewState.claimState,
    imageLoader = imageLoader, // imageLoader,
    locationOptions = emptyList(), // input.dateOfOccurrencePlusLocation.locationOptions,
    modelOptions = emptyList(), // input.singleItem.modelOptions,
    problemIds = emptyList(), // input.singleItem.problemIds,
    onDateOfOccurrence = {}, // viewModel::onDateOfOccurrence,
    onLocation = { }, // viewModel::onLocation,
    onDateOfPurchase = {}, // viewModel::onDateOfPurchase,
    onTypeOfDamage = {}, // viewModel::onTypeOfDamage,
    onModelOption = {}, // viewModel::onModelOption,
    onPurchasePrice = {}, // viewModel::onPurchasePrice,
    onSave = {}, // viewModel::onNext,
  )
}

@Composable
private fun EditClaimScreen(
  state: ClaimState,
  problemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem>,
  modelOptions: List<AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption>,
  imageLoader: ImageLoader,
  onDateOfOccurrence: (LocalDate) -> Unit,
  onLocation: (AutomationClaimDTO2.ClaimLocation) -> Unit,
  locationOptions: List<AutomationClaimDTO2.ClaimLocation>,
  onDateOfPurchase: (LocalDate) -> Unit,
  onTypeOfDamage: (AutomationClaimInputDTO2.SingleItem.ClaimProblem) -> Unit,
  onModelOption: (AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption) -> Unit,
  onPurchasePrice: (MonetaryAmount?) -> Unit,
  onSave: () -> Unit,
) {
  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {
      Text(stringResource(R.string.claims_incident_screen_header), color = MaterialTheme.colorScheme.secondary)
      Spacer(Modifier.padding(top = 8.dp))
      DateOfOccurrenceAndLocation(
        state = state,
        imageLoader = imageLoader,
        onDateOfOccurrence = onDateOfOccurrence,
        locationOptions = locationOptions,
        onLocation = onLocation,
      )
      Spacer(Modifier.padding(top = 34.dp))
      Text(stringResource(R.string.claims_incident_screen_header), color = MaterialTheme.colorScheme.secondary)
      Spacer(Modifier.padding(top = 8.dp))
      SingleItem(
        state = state,
        problemIds = problemIds,
        modelOptions = modelOptions,
        imageLoader = imageLoader,
        onDateOfPurchase = onDateOfPurchase,
        onTypeOfDamage = onTypeOfDamage,
        onModelOption = onModelOption,
        onPurchasePrice = onPurchasePrice,
      )
    }
    LargeContainedTextButton(
      onClick = onSave,
      text = stringResource(R.string.general_continue_button),
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

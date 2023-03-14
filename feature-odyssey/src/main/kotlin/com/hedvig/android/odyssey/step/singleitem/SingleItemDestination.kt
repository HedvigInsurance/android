package com.hedvig.android.odyssey.step.singleitem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.android.odyssey.ui.SingleItem
import com.hedvig.odyssey.remote.money.MonetaryAmount
import hedvig.resources.R
import java.time.LocalDate

@Composable
internal fun SingleItemDestination(
  imageLoader: ImageLoader,
) {
  SingleItemScreen( // todo
    state = ClaimState(),
    problemIds = emptyList(),
    modelOptions = emptyList(),
    imageLoader = imageLoader,
    onDateOfPurchase = {},
    onTypeOfDamage = {},
    onModelOption = {},
    onPurchasePrice = {},
    onSave = {},
  )
}

@Composable
private fun SingleItemScreen(
  state: ClaimState,
  problemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem>,
  modelOptions: List<AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption>,
  imageLoader: ImageLoader,
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
    LargeContainedTextButton(
      onClick = onSave,
      text = stringResource(R.string.general_continue_button),
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

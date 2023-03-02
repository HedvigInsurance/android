package com.hedvig.android.odyssey.input.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.FormRowButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.odyssey.remote.money.MonetaryAmount
import hedvig.resources.R
import java.time.LocalDate

@Composable
fun SingleItem(
  state: ClaimState,
  input: Input.SingleItem,
  imageLoader: ImageLoader,
  onDateOfPurchase: (LocalDate) -> Unit,
  onTypeOfDamage: (AutomationClaimInputDTO2.SingleItem.ClaimProblem) -> Unit,
  onModelOption: (AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption) -> Unit,
  onPurchasePrice: (MonetaryAmount?) -> Unit,
  onNext: () -> Unit,
) {
  var openDamagePickerDialog by remember { mutableStateOf(false) }
  var openModelPickerDialog by remember { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }

  val now = LocalDate.now()
  val pickerDialog = DatePickerDialog(
    LocalContext.current,
    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
      onDateOfPurchase(LocalDate.of(year, month, dayOfMonth))
    },
    now.year,
    now.monthValue,
    now.dayOfMonth,
  )

  if (openDamagePickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.claims_item_screen_type_of_damage_button),
      optionsList = input.problemIds,
      onSelected = onTypeOfDamage,
      getDisplayText = { damageType: AutomationClaimInputDTO2.SingleItem.ClaimProblem -> damageType.getText() },
      getImageUrl = { null },
      getId = { it.name },
      imageLoader = imageLoader,
    ) { openDamagePickerDialog = false }
  }

  if (openModelPickerDialog) {
    SingleSelectDialog(
      title = stringResource(R.string.claims_item_screen_model_button),
      optionsList = input.modelOptions.sortedBy { it.modelName },
      onSelected = onModelOption,
      getDisplayText = { modelOption: AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption ->
        modelOption.modelName
      },
      getImageUrl = { itemModelOption -> itemModelOption.modelImageUrl },
      getId = { itemModelOption -> itemModelOption.modelId },
      imageLoader = imageLoader,
    ) { openModelPickerDialog = false }
  }

  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Spacer(modifier = Modifier.padding(top = 20.dp))

    Column {
      val selectedModel = state.item.selectedModelOption.let { selectedId ->
        input.modelOptions.find { it.modelId == selectedId?.modelId }
      }

      FormRowButton(
        mainText = stringResource(R.string.claims_item_screen_model_button),
        secondaryText = selectedModel?.modelName ?: "-",
      ) {
        openModelPickerDialog = true
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = stringResource(R.string.claims_item_screen_date_of_purchase_button),
        secondaryText = state.item.purchaseDate?.toString() ?: "-",
      ) {
        pickerDialog.show()
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      LargeContainedButton(
        onClick = { focusRequester.requestFocus() },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
        ),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = stringResource(R.string.claims_item_screen_purchase_price_button), maxLines = 1)
          MonetaryAmountInput(
            onInput = onPurchasePrice,
            currency = "SEK",
            maximumFractionDigits = 0,
            focusRequester = focusRequester,
          )
        }
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = stringResource(R.string.claims_item_screen_type_of_damage_button),
        secondaryText = state.item.problemIds.joinToString { it.getText() },
      ) {
        openDamagePickerDialog = true
      }
    }

    LargeContainedTextButton(
      onClick = onNext,
      text = stringResource(R.string.general_continue_button),
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

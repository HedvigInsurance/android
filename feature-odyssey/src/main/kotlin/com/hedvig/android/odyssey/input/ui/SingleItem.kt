package com.hedvig.android.odyssey.input.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.FormRowButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.odyssey.remote.money.MonetaryAmount
import java.time.LocalDate

@Composable
fun SingleItem(
  state: ClaimState,
  input: Input.SingleItem,
  imageLoader: ImageLoader,
  onDateOfPurchase: (LocalDate) -> Unit,
  onTypeOfDamage: (AutomationClaimInputDTO2.SingleItem.ClaimProblem) -> Unit,
  onModelOption: (AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption) -> Unit,
  onPurchasePrice: (MonetaryAmount) -> Unit,
  onNext: () -> Unit,
) {
  val openDamagePickerDialog = remember { mutableStateOf(false) }
  val openModelPickerDialog = remember { mutableStateOf(false) }

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

  if (openDamagePickerDialog.value) {
    SingleSelectDialog(
      title = "Select type of damage",
      optionsList = input.problemIds,
      onSelected = onTypeOfDamage,
      getDisplayText = { damageType: AutomationClaimInputDTO2.SingleItem.ClaimProblem -> damageType.getText() },
      getImageUrl = { null },
      getId = { it.name },
      imageLoader = imageLoader,
    ) { openDamagePickerDialog.value = false }
  }

  if (openModelPickerDialog.value) {
    SingleSelectDialog(
      title = "Select phone model",
      optionsList = input.modelOptions.sortedBy { it.modelName },
      onSelected = onModelOption,
      getDisplayText = { modelOption: AutomationClaimInputDTO2.SingleItem.ItemOptions.ItemModelOption -> modelOption.modelName },
      getImageUrl = { itemModelOption -> itemModelOption.modelImageUrl },
      getId = { itemModelOption -> itemModelOption.modelId },
      imageLoader = imageLoader,
    ) { openModelPickerDialog.value = false }
  }

  val message = remember { mutableStateOf("") }
  val openDialog = remember { mutableStateOf(false) }
  val editMessage = remember { mutableStateOf("") }

  if (openDialog.value) {
    PriceInputDialog(
      title = "Enter purchase price",
      message = message,
      openDialog = openDialog,
      editMessage = editMessage,
      onPositiveButtonClicked = {
        onPurchasePrice(MonetaryAmount(it, "SEK"))
      },
      onDismissRequest = {
        openDialog.value = false
      },
    )
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
        mainText = "Phone model",
        secondaryText = selectedModel?.modelName ?: "-",
      ) {
        openModelPickerDialog.value = true
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Date of purchase",
        secondaryText = state.item.purchaseDate?.toString() ?: "-",
      ) {
        pickerDialog.show()
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Purchase price",
        secondaryText = state.item.purchasePrice?.amount ?: "Not specified",
      ) {
        openDialog.value = true
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Damage",
        secondaryText = state.item.problemIds.joinToString { it.getText() },
      ) {
        openDamagePickerDialog.value = true
      }
    }

    LargeContainedTextButton(
      onClick = onNext,
      text = "Next",
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

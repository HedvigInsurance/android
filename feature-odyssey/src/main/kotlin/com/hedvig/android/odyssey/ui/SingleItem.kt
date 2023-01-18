package com.hedvig.android.odyssey.ui

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.FormRowButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.money.MonetaryAmount
import com.hedvig.common.remote.money.format
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun SingleItem(
  state: ClaimState,
  input: Input.SingleItem,
  onDateOfPurchase: (LocalDate) -> Unit,
  onTypeOfDamage: (AutomationClaimInputDTO2.SingleItem.ClaimProblem) -> Unit,
  onPurchasePrice: (MonetaryAmount) -> Unit,
  onNext: suspend () -> Unit,
) {

  val coroutineScope = rememberCoroutineScope()
  val openDamagePickerDialog = remember { mutableStateOf(false) }

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
    ) { openDamagePickerDialog.value = false }
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
      FormRowButton(
        mainText = "Date of purchase",
        secondaryText = state.item.purchaseDate?.toString() ?: "-",
      ) {
        pickerDialog.show()
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Purchase price",
        secondaryText = state.item.purchasePrice?.format() ?: "Not specified",
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
      onClick = {
        coroutineScope.launch {
          onNext()
        }
      },
      text = "Next",
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

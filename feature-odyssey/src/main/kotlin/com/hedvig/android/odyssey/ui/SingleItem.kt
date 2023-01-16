package com.hedvig.android.odyssey.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.FormRowButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.odyssey.ClaimsFlowViewModel
import com.hedvig.android.odyssey.Input
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.money.MonetaryAmount
import com.hedvig.common.remote.money.format
import java.time.LocalDate

@Composable
fun SingleItem(viewModel: ClaimsFlowViewModel) {
  val viewState by viewModel.viewState.collectAsState()
  val itemState = viewState.claim?.state?.item

  val openDamagePickerDialog = remember { mutableStateOf(false) }

  val now = LocalDate.now()
  val pickerDialog = DatePickerDialog(
    LocalContext.current,
    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
      viewModel.onDateOfPurchase(LocalDate.of(year, month, dayOfMonth))
    },
    now.year,
    now.monthValue,
    now.dayOfMonth,
  )

  val damageTypeValues = viewState.claim?.inputs
    ?.filterIsInstance<Input.SingleItem>()
    ?.firstOrNull()
    ?.problemIds
    ?: emptyList()

  if (openDamagePickerDialog.value) {
    SingleSelectDialog(
      title = "Select type of damage",
      optionsList = damageTypeValues,
      onSelected = viewModel::onTypeOfDamage,
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
        viewModel.onPurchasePrice(MonetaryAmount(it, "SEK"))
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
        secondaryText = viewState.claim?.state?.item?.purchaseDate?.toString() ?: "-",
      ) {
        pickerDialog.show()
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Purchase price",
        secondaryText = itemState?.purchasePrice?.format() ?: "Not specified",
      ) {
        openDialog.value = true
      }

      Spacer(modifier = Modifier.padding(top = 12.dp))

      FormRowButton(
        mainText = "Damage",
        secondaryText = itemState?.problemIds?.firstOrNull()?.getText() ?: "-",
      ) {
        openDamagePickerDialog.value = true
      }
    }

    LargeContainedTextButton(
      onClick = viewModel::onNext,
      text = "Next",
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

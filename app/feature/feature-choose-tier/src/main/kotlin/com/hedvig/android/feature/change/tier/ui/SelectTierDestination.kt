package com.hedvig.android.feature.change.tier.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.tier.data.CustomizeContractData

@Composable
fun SelectTierScreen(
  continueEnabled: Boolean,
  chosenTierIndex: Int,
  chosenDeductibleIndex: Int,
  data: CustomizeContractData,
  navigateUp: () -> Unit,
  onCompareClick: () -> Unit,
  onContinueClick: () -> Unit,
) {
  var showTierDialog by remember { mutableStateOf(false) }
  var showDeductibleDialog by remember { mutableStateOf(false) }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = { navigateUp() },
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = null,
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    HedvigText(
      text = "Customise your insurance", // TODO: string here
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Column {
      HedvigText(
        style = HedvigTheme.typography.headlineMedium.copy(
          lineBreak = LineBreak.Heading,
          color = HedvigTheme.colorScheme.textSecondary,
        ),
        text = "Select your coverage and deductible", // TODO: string here
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(16.dp))
      CustomizationCard(
        data = data,
        chosenTierIndex = chosenTierIndex,
        onChooseTierClick = {
          showTierDialog = true
        },
        onChooseDeductibleClick = {
          showDeductibleDialog = true
        },
        chosenDeductible = chosenDeductibleIndex,
      )
      Spacer(Modifier.height(4.dp))
      HedvigTextButton(
        "Compare coverage levels", // todo: string here
        onClick = {
          onCompareClick()
        },
      )
      Spacer(Modifier.height(4.dp))
      HedvigButton(
        buttonSize = Large,
        text = "",
        enabled = continueEnabled,
        onClick = {
          onContinueClick()
        },
      )
    }
  }
}

@Composable
private fun CustomizationCard(
  data: CustomizeContractData,
  chosenTierIndex: Int,
  chosenDeductible: Int,
  onChooseDeductibleClick: (index: Int) -> Unit,
  onChooseTierClick: (index: Int) -> Unit,
) {
  Surface(
    shape = HedvigTheme.shapes.cornerXLarge,
  ) {
    Column {
      PillAndBasicInfo(
        contractGroup = data.contractGroup,
        displayName = data.displayName,
        displaySubtitle = data.displaySubtitle,
      )
      Spacer(Modifier.height(4.dp))
      DropdownWithDialog(
        style = Label(
          label = "Coverage level", // todo: string here!
          items = data.tierDropdownData,
        ),
        size = Small,
        hintText = "", // todo: check here
        onItemChosen = { chosenIndex ->
          onChooseTierClick(chosenIndex)
        },
        chosenItemIndex = chosenTierIndex,
        onSelectorClick = {
          // todo
        },
      )
      Spacer(Modifier.height(4.dp))
      DropdownWithDialog(
        style = Label(
          label = "Deductible", // todo: string here!
          items = data.deductibleDropdownData,
        ),
        size = Small,
        hintText = "", // todo: check here
        onItemChosen = { chosenIndex ->
          onChooseDeductibleClick(chosenIndex)
        },
        chosenItemIndex = chosenDeductible,
        onSelectorClick = {
          // todo
        },
      )
    }
  }
}

@Composable
private fun PillAndBasicInfo(contractGroup: ContractGroup, displayName: String, displaySubtitle: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Image(
      painter = painterResource(id = contractGroup.toPillow()),
      contentDescription = null,
      modifier = Modifier.size(48.dp),
    )
    Spacer(modifier = Modifier.width(16.dp))
    Column {
      HedvigText(
        text = displayName,
        style = HedvigTheme.typography.headlineSmall,
      )
      HedvigText(
        color = HedvigTheme.colorScheme.textSecondary,
        text = displaySubtitle,
        style = HedvigTheme.typography.bodySmall,
      )
    }
  }
}

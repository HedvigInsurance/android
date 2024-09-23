package com.hedvig.android.feature.change.tier.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.android.toPillow
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownSize.Small
import com.hedvig.android.design.system.hedvig.DropdownDefaults.DropdownStyle.Label
import com.hedvig.android.design.system.hedvig.DropdownDefaults.LockedState
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import com.hedvig.android.design.system.hedvig.DropdownWithDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.change.tier.data.CustomizeContractData
import hedvig.resources.R

@Composable
fun SelectTierScreen(
  chosenTierIndex: Int,
  chosenDeductibleIndex: Int,
  data: CustomizeContractData,
  newDisplayPremium: String,
  isCurrentChosen: Boolean,
  tierLockedState: LockedState,
  deductibleLockedState: LockedState,
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
      text = stringResource(R.string.TIER_FLOW_TITLE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )

    HedvigText(
      style = HedvigTheme.typography.headlineMedium.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondary,
      ),
      text = stringResource(R.string.TIER_FLOW_SUBTITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    CustomizationCard(
      modifier = Modifier.padding(horizontal = 16.dp),
      data = data,
      chosenTierIndex = chosenTierIndex,
      onChooseTierClick = {
        showTierDialog = true
      },
      onChooseDeductibleClick = {
        showDeductibleDialog = true
      },
      chosenDeductible = chosenDeductibleIndex,
      newDisplayPremium = newDisplayPremium,
      tierLockedState = tierLockedState,
      deductibleLockedState = deductibleLockedState,
      isCurrentChosen = isCurrentChosen,
    )
    Spacer(Modifier.height(4.dp))
    HedvigTextButton(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      text = stringResource(R.string.TIER_FLOW_COMPARE_BUTTON),
      onClick = {
        onCompareClick()
      },
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      buttonSize = Large,
      text = "Continue",
      enabled = !isCurrentChosen,
      onClick = {
        onContinueClick()
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CustomizationCard(
  data: CustomizeContractData,
  chosenTierIndex: Int,
  chosenDeductible: Int,
  newDisplayPremium: String,
  tierLockedState: LockedState,
  deductibleLockedState: LockedState,
  onChooseDeductibleClick: (index: Int) -> Unit,
  onChooseTierClick: (index: Int) -> Unit,
  isCurrentChosen: Boolean,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = HedvigTheme.shapes.cornerXLarge,
  ) {
    Column(Modifier.padding(16.dp)) {
      PillAndBasicInfo(
        contractGroup = data.contractGroup,
        displayName = data.displayName,
        displaySubtitle = data.displaySubtitle,
      )
      Spacer(Modifier.height(16.dp))
      DropdownWithDialog(
        lockedState = tierLockedState,
        style = Label(
          label = stringResource(R.string.TIER_FLOW_COVERAGE_LABEL),
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
        containerColor = HedvigTheme.colorScheme.fillNegative,
      )
      Spacer(Modifier.height(4.dp))
      DropdownWithDialog(
        lockedState = deductibleLockedState,
        style = Label(
          label = stringResource(R.string.TIER_FLOW_DEDUCTIBLE_LABEL),
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
        containerColor = HedvigTheme.colorScheme.fillNegative,
      )
      Spacer(Modifier.height(16.dp))
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(
            stringResource(R.string.TIER_FLOW_TOTAL),
            style = HedvigTheme.typography.bodySmall,
          )
        },
        spaceBetween = 8.dp,
        endSlot = {
          HedvigText(
            text = newDisplayPremium,
            textAlign = TextAlign.End,
            style = HedvigTheme.typography.bodySmall,
          )
        },
      )
      if (!isCurrentChosen) {
        HedvigText(
          modifier = Modifier.fillMaxWidth(),
          textAlign = Companion.End,
          text = stringResource(R.string.TIER_FLOW_PREVIOUS_PRICE, data.currentDisplayPremium),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
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

@HedvigPreview
@Composable
private fun CustomizationCardPreview() {
  HedvigTheme {
    CustomizationCard(
      data = dataForPreview,
      chosenTierIndex = 2,
      chosenDeductible = 1,
      onChooseTierClick = {},
      onChooseDeductibleClick = {},
      newDisplayPremium = "249 kr/mo",
      tierLockedState = LockedState.Locked({}),
      deductibleLockedState = LockedState.NotLocked,
      isCurrentChosen = false,
    )
  }
}

@HedvigMultiScreenPreview
@Composable
private fun SelectTierScreenPreview() {
  HedvigTheme {
    SelectTierScreen(
      data = dataForPreview,
      chosenTierIndex = 2,
      chosenDeductibleIndex = 1,
      newDisplayPremium = "249 kr/mo",
      tierLockedState = LockedState.Locked({}),
      deductibleLockedState = LockedState.NotLocked,
      isCurrentChosen = false,
      onCompareClick = {},
      onContinueClick = {},
      navigateUp = {},
    )
  }
}

private val dataForPreview = CustomizeContractData(
  contractGroup = ContractGroup.HOMEOWNER,
  displayName = "Home Homeowner",
  displaySubtitle = "Addressvägen 777",
  tierDropdownData = listOf(
    SimpleDropdownItem("Bas"),
    SimpleDropdownItem("Standard"),
    SimpleDropdownItem("Max"),
  ),
  deductibleDropdownData = listOf(
    SimpleDropdownItem("1750 kr"),
    SimpleDropdownItem("2500 kr"),
    SimpleDropdownItem("3500 kr"),
  ),
  currentDisplayPremium = "279 kr/mo",
)

package com.hedvig.app.feature.insurance.ui.detail.coverage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

@Composable
internal fun CoverageTab(
  viewModel: CoverageViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  CoverageTab(
    uiState = uiState,
    retryLoading = viewModel::reload,
    modifier = modifier,
  )
}

@Composable
private fun CoverageTab(
  uiState: CoverageUiState,
  retryLoading: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    CoverageUiState.Error -> {
      HedvigErrorSection(
        retry = retryLoading,
        modifier = modifier.fillMaxSize(),
      )
    }
    CoverageUiState.Loading -> {}
    is CoverageUiState.Success -> {
      val coroutineScope = rememberCoroutineScope()
      val sheetState = rememberModalBottomSheetState(true)
      var selectedInsurableLimit by remember { mutableStateOf<ContractCoverage.InsurableLimit?>(null) }
      val selectedInsurableLimitValue = selectedInsurableLimit
      if (selectedInsurableLimitValue != null) {
        ModalBottomSheet(
          onDismissRequest = { selectedInsurableLimit = null },
          // todo use "https://github.com/c5inco/smoother" for a top only squircle shape here
          sheetState = sheetState,
          tonalElevation = 0.dp,
        ) {
          Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
          ) {
            Text(stringResource(hedvig.resources.R.string.CONTRACT_COVERAGE_MORE_INFO))
            Spacer(Modifier.height(8.dp))
            Text(text = selectedInsurableLimitValue.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            HedvigTextButton(
              text = stringResource(hedvig.resources.R.string.general_close_button),
              onClick = {
                coroutineScope.launch {
                  sheetState.hide()
                }.invokeOnCompletion {
                  selectedInsurableLimit = null
                }
              },
            )
          }
        }
      }

      Column(modifier = modifier) {
        Spacer(Modifier.height(16.dp))
        InsurableLimitSection(
          insurableLimitItems = uiState.insurableLimitItems,
          onInsurableLimitClick = { insurableLimit: ContractCoverage.InsurableLimit ->
            selectedInsurableLimit = insurableLimit
          },
        )
        Spacer(Modifier.height(16.dp))
        if (uiState.perilItems.isNotEmpty()) {
          PerilSection(uiState.perilItems)
          Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.PerilSection(
  perilItems: ImmutableList<ContractCoverage.Peril>,
) {
  var expandedItemIndex by rememberSaveable { mutableStateOf(-1) }
  for ((index, perilItem) in perilItems.withIndex()) {
    ExpandableCoverageCard(
      isExpanded = expandedItemIndex == index,
      onClick = {
        if (expandedItemIndex == index) {
          expandedItemIndex = -1
        } else {
          expandedItemIndex = index
        }
      },
      color = perilItem.colorHexValue?.let { Color(it) },
      title = perilItem.title,
      expandedTitle = perilItem.description,
      expandedDescriptionList = perilItem.covered,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (index != perilItems.lastIndex) {
      Spacer(Modifier.height(4.dp))
    }
  }
}

@Composable
private fun ExpandableCoverageCard(
  isExpanded: Boolean,
  onClick: () -> Unit,
  color: Color?,
  title: String,
  expandedTitle: String,
  expandedDescriptionList: ImmutableList<String>,
  modifier: Modifier = Modifier,
) {
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = onClick,
    content = {
      Spacer(
        Modifier
          .size(24.dp)
          .wrapContentSize(Alignment.Center)
          .size(20.dp)
          .background(color ?: Color(0xFFB8D194), CircleShape),
      )
      Spacer(Modifier.width(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.weight(1f, true),
      )
    },
    expandedContent = {
      Column(
        Modifier.padding(start = 44.dp, end = 32.dp),
      ) {
        Spacer(Modifier.height(12.dp))
        Text(
          text = expandedTitle,
          style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        if (expandedDescriptionList.isNotEmpty()) {
          Spacer(Modifier.height(12.dp))
          for ((index, itemDescription) in expandedDescriptionList.withIndex()) {
            Row {
              Text(
                text = (index + 1).toString().padStart(length = 2, padChar = '0'),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(Modifier.width(12.dp))
              Text(
                text = itemDescription,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
              )
            }
            if (index != expandedDescriptionList.lastIndex) {
              Spacer(Modifier.height(12.dp))
            }
          }
        }
        Spacer(Modifier.height(12.dp))
      }
    },
    modifier = modifier,
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.InsurableLimitSection(
  insurableLimitItems: ImmutableList<ContractCoverage.InsurableLimit>,
  onInsurableLimitClick: (ContractCoverage.InsurableLimit) -> Unit,
) {
  insurableLimitItems.mapIndexed { index, insurableLimitItem ->
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 16.dp),
        ) {
          Text(insurableLimitItem.label)
        }
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.padding(vertical = 16.dp),
        ) {
          Text(
            text = insurableLimitItem.limit,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(Modifier.width(8.dp))
          Icon(
            imageVector = Icons.Hedvig.InfoFilled,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      },
      spaceBetween = 8.dp,
      modifier = Modifier
        .heightIn(min = 56.dp)
        .fillMaxWidth()
        .clickable {
          onInsurableLimitClick(insurableLimitItem)
        }
        .padding(horizontal = 16.dp),
    )
    if (index != insurableLimitItems.lastIndex) {
      Divider(
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCoverageTab() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      CoverageTab(
        uiState = CoverageUiState.Success(
          perilItems = previewPerils,
          insurableLimitItems = previewInsurableLimits,
        ),
        retryLoading = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitSection() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        InsurableLimitSection(
          previewInsurableLimits,
          {},
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPerilSection() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        PerilSection(previewPerils)
      }
    }
  }
}

private val previewPerils: PersistentList<ContractCoverage.Peril> = List(4) { index ->
  val hexValue = listOf(0xFFC45D4F, 0xFF125D4F, 0xFFC4FF4F, 0xFFC45D00)[index]
  ContractCoverage.Peril(
    id = index.toString(),
    title = "Eldsvåda",
    description = "description$index",
    covered = persistentListOf("Covered#$index"),
    colorHexValue = hexValue,
  )
}.toPersistentList()

private val previewInsurableLimits: PersistentList<ContractCoverage.InsurableLimit> = persistentListOf(
  ContractCoverage.InsurableLimit("Insured amount".repeat(2), "1 000 000 kr", ""),
  ContractCoverage.InsurableLimit("Deductible", "1 500 kr", ""),
  ContractCoverage.InsurableLimit("Travel insurance", "45 days", ""),
)

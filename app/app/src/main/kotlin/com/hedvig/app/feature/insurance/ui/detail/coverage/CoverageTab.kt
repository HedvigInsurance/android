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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.app.feature.perils.Peril
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * TODO in this screen:
 *  Make peril description text come from octopus probably, so that it shows the right information
 *  Take the color for perils also from octopus which would mean that it's non-null. Otherwise provide a sane default
 *  Put bottom sheet contents in the bigger screen, and fix its design
 */
@Composable
internal fun CoverageTab(
  viewModel: CoverageViewModel,
  onInsurableLimitClick: (ContractCoverage.InsurableLimit) -> Unit,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  CoverageFragmentScreen(
    uiState = uiState,
    onInsurableLimitClick = onInsurableLimitClick,
    retryLoading = viewModel::reload,
    modifier = modifier,
  )
}

@Composable
private fun CoverageFragmentScreen(
  uiState: CoverageUiState,
  onInsurableLimitClick: (ContractCoverage.InsurableLimit) -> Unit,
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
      Column(modifier = modifier) {
        Spacer(Modifier.height(16.dp))
        InsurableLimitSection(uiState.insurableLimitItems, onInsurableLimitClick = onInsurableLimitClick)
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
  perilItems: ImmutableList<Peril>,
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
      color = perilItem.colorCode?.let { Color(it) },
      title = perilItem.title,
      expandedTitle = perilItem.description,
      expandedDescriptionList = buildList {
        add(perilItem.info)
        addAll(perilItem.covered)
        addAll(perilItem.exception)
      }.toPersistentList(),
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
          .background(color ?: Color(0xFFC45D4F), CircleShape), // todo consider a different default color?
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
private fun PreviewCoverageFragmentScreen() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      CoverageFragmentScreen(
        uiState = CoverageUiState.Success(
          contractDisplayName = "Your insurance",
          perilItems = previewPerils,
          insurableLimitItems = previewInsurableLimits,
        ),
        onInsurableLimitClick = {},
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

private val previewPerils = listOf(
  Peril("Eldsvåda", "", null, null, 0xFFC45D4F, emptyList(), emptyList(), "Info"),
  Peril("Eldsvåda", "", null, null, 0xFFC45D4F, emptyList(), emptyList(), "Info"),
  Peril("Eldsvåda", "", null, null, 0xFFC45D4F, emptyList(), emptyList(), "Info"),
  Peril("Eldsvåda", "", null, null, 0xFFC45D4F, emptyList(), emptyList(), "Info"),
).toPersistentList()

private val previewInsurableLimits = listOf(
  ContractCoverage.InsurableLimit("Insured amount".repeat(2), "1 000 000 kr", ""),
  ContractCoverage.InsurableLimit("Deductible", "1 500 kr", ""),
  ContractCoverage.InsurableLimit("Travel insurance", "45 days", ""),
).toPersistentList()

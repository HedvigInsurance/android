package com.hedvig.android.feature.insurances.insurancedetail.coverage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.ProductVariantPeril
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
internal fun CoverageTab(
  insurableLimits: List<InsurableLimit>,
  perils: List<ProductVariantPeril>,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(true)
  var selectedInsurableLimit: InsurableLimit? by remember { mutableStateOf(null) }
  val selectedInsurableLimitValue = selectedInsurableLimit

  if (selectedInsurableLimitValue != null) {
    ModalBottomSheet(
      containerColor = MaterialTheme.colorScheme.background,
      onDismissRequest = { selectedInsurableLimit = null },
      shape = MaterialTheme.shapes.squircleLargeTop,
      sheetState = sheetState,
      tonalElevation = 0.dp,
      contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top) },
    ) {
      Column(
        Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp)
          .padding(bottom = 16.dp)
          .windowInsetsPadding(
            BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
          ),
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
      insurableLimits = insurableLimits,
      onInsurableLimitClick = { insurableLimit: InsurableLimit ->
        selectedInsurableLimit = insurableLimit
      },
    )
    Spacer(Modifier.height(16.dp))
    if (perils.isNotEmpty()) {
      PerilSection(perils)
      Spacer(Modifier.height(16.dp))
    }
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.PerilSection(perilItems: List<ProductVariantPeril>) {
  var expandedItemIndex by rememberSaveable { mutableIntStateOf(-1) }
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
      color = perilItem.colorCode?.color,
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

private val String.color
  get() = Color(android.graphics.Color.parseColor(this))

@Composable
private fun ExpandableCoverageCard(
  isExpanded: Boolean,
  onClick: () -> Unit,
  color: Color?,
  title: String,
  expandedTitle: String,
  expandedDescriptionList: List<String>,
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
          .size(16.dp)
          .background(color ?: Color(0xFFB8D194), CircleShape),
      )
      Spacer(Modifier.width(8.dp))
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
  insurableLimits: List<InsurableLimit>,
  onInsurableLimitClick: (InsurableLimit) -> Unit,
) {
  insurableLimits.mapIndexed { index, insurableLimitItem ->
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
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically),
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
    if (index != insurableLimits.lastIndex) {
      HorizontalDivider(
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
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      CoverageTab(
        previewInsurableLimits,
        previewPerils,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsurableLimitSection() {
  HedvigTheme {
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
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        PerilSection(previewPerils)
      }
    }
  }
}

private val previewPerils: List<ProductVariantPeril> = List(4) { index ->
  ProductVariantPeril(
    id = index.toString(),
    title = "Eldsvåda",
    description = "description$index",
    covered = listOf("Covered#$index"),
    colorCode = "0xFFC45D4F",
    info = "test",
    exceptions = listOf(),
  )
}

private val previewInsurableLimits: List<InsurableLimit> = listOf(
  InsurableLimit("Insured amount".repeat(2), "1 000 000 kr", "", InsurableLimit.InsurableLimitType.GOODS_FAMILY),
  InsurableLimit("Deductible", "1 500 kr", "", InsurableLimit.InsurableLimitType.DEDUCTIBLE),
  InsurableLimit("Travel insurance", "45 days", "", InsurableLimit.InsurableLimitType.BIKE),
)

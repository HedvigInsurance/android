package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTabRowMaxSixTabs
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Medium
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.MEDIUM
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TabDefaults
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.feature.change.tier.data.ComparisonRow
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Failure
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Loading
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonState.Success
import hedvig.resources.R

@Composable
internal fun ComparisonDestination(viewModel: ComparisonViewModel, navigateUp: () -> Unit) {
  val uiState: ComparisonState by viewModel.uiState.collectAsStateWithLifecycle()
  when (val state = uiState) {
    Loading -> {
      Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        HedvigCircularProgressIndicator()
      }
    }

    is Success -> ComparisonScreen(state, navigateUp)
    Failure -> {
      Box(Modifier.fillMaxSize()) {
        HedvigErrorSection(
          onButtonClick = {
            viewModel.emit(ComparisonEvent.Reload)
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun ComparisonScreen(uiState: Success, navigateUp: () -> Unit) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.TIER_FLOW_COMPARE_BUTTON),
  ) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    Spacer(Modifier.height(20.dp))
    val titles = uiState.comparisonData.columns.filterNotNull() // todo: could be dangerous?
    CoveragePagerSelector(
      selectedTabIndex = selectedTabIndex,
      selectTabIndex = { selectedTabIndex = it },
      tabTitles = titles,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    AnimatedContent(
      targetState = selectedTabIndex,
      transitionSpec = {
        val spec = tween<IntOffset>(durationMillis = 600, easing = FastOutSlowInEasing)
        if (targetState < initialState) {
          slideIntoContainer(SlideDirection.End, spec) togetherWith slideOutOfContainer(SlideDirection.End, spec)
        } else {
          slideIntoContainer(SlideDirection.Start, spec) togetherWith slideOutOfContainer(SlideDirection.Start, spec)
        }
      },
    ) { columnIndex ->
      Column(Modifier.padding(horizontal = 16.dp)) {
        uiState.comparisonData.rows.forEachIndexed { rowIndex, comparisonRow ->
          val isCovered = comparisonRow.cells[columnIndex].isCovered
          var isExpanded by rememberSaveable { mutableStateOf(false) }
          ExpandableComparisonRow(
            comparisonRow = comparisonRow,
            isExpanded = isExpanded,
            isCovered = isCovered,
            columnIndex = columnIndex,
            onClick = {
              if (isCovered) {
                isExpanded = !isExpanded
              }
            },
          )
          if (rowIndex != uiState.comparisonData.rows.lastIndex) {
            HorizontalDivider()
          }
        }
      }
    }
  }
}

@Composable
private fun ExpandableComparisonRow(
  comparisonRow: ComparisonRow,
  isExpanded: Boolean,
  isCovered: Boolean,
  columnIndex: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val contentColor = if (isCovered) {
    HedvigTheme.colorScheme.textPrimary
  } else {
    HedvigTheme.colorScheme.textDisabled
  }
  val secondaryContentColor = if (isCovered) {
    HedvigTheme.colorScheme.textSecondary
  } else {
    HedvigTheme.colorScheme.textDisabled
  }
  Surface(
    modifier = modifier
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(
          bounded = true,
          radius = 1000.dp,
        ),
        onClick = onClick,
      ),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            val halfRotation by animateFloatAsState(
              targetValue = if (isExpanded) 0f else -90f,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            )
            val fullRotation by animateFloatAsState(
              targetValue = if (isExpanded) 0f else -180f,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            )
            Box {
              val iconModifier = Modifier.size(24.dp)
              Icon(
                HedvigIcons.Minus,
                tint = contentColor,
                contentDescription = null,
                modifier = iconModifier.graphicsLayer {
                  rotationZ = halfRotation
                },
              )
              Icon(
                HedvigIcons.Minus,
                tint = contentColor,
                contentDescription = null,
                modifier = iconModifier.graphicsLayer {
                  rotationZ = fullRotation
                },
              )
            }
            Spacer(Modifier.width(8.dp))
            HedvigText(comparisonRow.title, color = contentColor)
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
          ) {
            val highLightColor = if (isCovered) HighlightColor.Blue(MEDIUM) else HighlightColor.Grey(MEDIUM)
            val cellText = comparisonRow.cells[columnIndex].coverageText
            if (cellText != null) {
              HighlightLabel(cellText, color = highLightColor, size = Medium)
            } else {
              Icon(
                imageVector = if (isCovered) HedvigIcons.Checkmark else HedvigIcons.Minus,
                null,
                tint = contentColor,
              )
            }
          }
        },
        spaceBetween = 12.dp,
      )
      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(clip = false, expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(clip = false, shrinkTowards = Alignment.Top),
      ) {
        Column {
          Spacer(Modifier.height(8.dp))
          HedvigText(comparisonRow.description, color = secondaryContentColor)
        }
      }
    }
  }
}

@Composable
private fun CoveragePagerSelector(
  selectedTabIndex: Int,
  selectTabIndex: (Int) -> Unit,
  tabTitles: List<String>,
  modifier: Modifier = Modifier,
) {
  HedvigTabRowMaxSixTabs(
    tabTitles = tabTitles,
    tabStyle = TabDefaults.TabStyle.Filled,
    selectedTabIndex = selectedTabIndex,
    onTabChosen = { selectTabIndex(it) },
    modifier = modifier,
  )
}

package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTabRowMaxSixTabs
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.PerilDefaults.PerilSize
import com.hedvig.android.design.system.hedvig.PerilList
import com.hedvig.android.design.system.hedvig.TabDefaults
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
  }
}

@Composable
private fun ComparisonScreen(uiState: Success, navigateUp: () -> Unit) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.TIER_FLOW_COMPARE_BUTTON), // todo: change copy??
  ) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    Spacer(Modifier.height(20.dp))
    val titles = uiState.quotes.map { it.tier.tierDisplayName ?: "-" }
    CoveragePagerSelector(
      selectedTabIndex = selectedTabIndex,
      selectTabIndex = { selectedTabIndex = it },
      tabTitles = titles,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    AnimatedContent(
      targetState = selectedTabIndex,
      transitionSpec = {
        val spec = tween<IntOffset>(durationMillis = 600, easing = FastOutSlowInEasing)
        if (initialState == 0) {
          slideIntoContainer(SlideDirection.Start, spec) togetherWith slideOutOfContainer(SlideDirection.Start, spec)
        } else {
          slideIntoContainer(SlideDirection.End, spec) togetherWith slideOutOfContainer(SlideDirection.End, spec)
        }
      },
    ) { index ->
      // todo: will use different API for this
      Column(Modifier.padding(horizontal = 16.dp)) {
        val quote = uiState.quotes[index]
        quote.productVariant.insurableLimits.forEachIndexed { i, insurableLimit ->
          HorizontalItemsWithMaximumSpaceTaken(
            modifier = Modifier.padding(vertical = 16.dp),
            startSlot = {
              HedvigText(insurableLimit.label)
            },
            endSlot = {
              Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                HedvigText(
                  insurableLimit.limit,
                  color = HedvigTheme.colorScheme.textSecondary,
                  textAlign = TextAlign.End,
                )
              }
            },
            spaceBetween = 8.dp,
          )
          if (i != quote.productVariant.insurableLimits.lastIndex) {
            HorizontalDivider()
          }
        }
        Spacer(Modifier.height(16.dp))
        PerilList(
          size = PerilSize.Small,
          perilItems = quote.productVariant.perils.map {
            PerilData(
              title = it.title,
              description = it.description,
              colorCode = it.colorCode,
              covered = it.covered,
            )
          },
        )
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

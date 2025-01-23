package com.hedvig.android.feature.addon.purchase.ui.travelinsuranceplusexplanation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Medium
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Blue
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.PerilData
import com.hedvig.android.design.system.hedvig.PerilDefaults.PerilSize.Small
import com.hedvig.android.design.system.hedvig.PerilList
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType.BACK
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.TravelInsurancePlusExplanation.TravelPerilData
import hedvig.resources.R

@Composable
internal fun TravelInsurancePlusExplanationDestination(travelPerilData: List<TravelPerilData>, navigateUp: () -> Unit) {
  TravelInsurancePlusExplanationScreen(
    perilData = remember(travelPerilData) {
      travelPerilData.map {
        PerilData(
          title = it.title,
          description = it.description,
          covered = it.covered,
          colorCode = it.colorCode,
        )
      }
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun TravelInsurancePlusExplanationScreen(perilData: List<PerilData>, navigateUp: () -> Unit) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      val topAppbarInsets =
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
      TopAppBar(
        title = "",
        actionType = BACK,
        windowInsets = topAppbarInsets,
        onActionClick = dropUnlessResumed(block = navigateUp),
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .consumeWindowInsets(topAppbarInsets.only(WindowInsetsSides.Top))
          .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
      ) {
        HedvigText(
          text = stringResource(R.string.ADDON_FLOW_TRAVEL_INFORMATION_TITLE),
          style = HedvigTheme.typography.bodyMedium,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        )
        HedvigText(
          text = stringResource(R.string.ADDON_FLOW_TRAVEL_INFORMATION_DESCRIPTION),
          color = HedvigTheme.colorScheme.textSecondary,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        )
        Spacer(Modifier.height(32.dp))
        HighlightLabel(
          labelText = stringResource(R.string.ADDON_LEARN_MORE_LABEL),
          size = Medium,
          color = Blue(LIGHT),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        PerilList(perilData, Small, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelInsurancePlusExplanationScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelInsurancePlusExplanationScreen(
        perilData = List(4) { index ->
          PerilData(
            title = "Title$index",
            description = "Description$index",
            covered = listOf("Covered#$index", "Also covered#$index"),
            colorCode = "#FFD0ECFB",
          )
        },
        navigateUp = {},
      )
    }
  }
}

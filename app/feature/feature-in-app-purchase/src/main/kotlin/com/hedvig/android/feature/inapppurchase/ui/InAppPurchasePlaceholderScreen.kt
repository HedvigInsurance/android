package com.hedvig.android.feature.inapppurchase.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText

/**
 * Stand-in for a step's content until its ViewModel exists. Each [actions] entry is a button.
 */
@Composable
internal fun InAppPurchasePlaceholderScreen(
  title: String,
  description: String,
  navigateUp: () -> Unit,
  actions: List<Pair<String, () -> Unit>>,
) {
  HedvigScaffold(navigateUp = navigateUp, topAppBarText = title) {
    HedvigText(description, Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.weight(1f))
    actions.forEach { (text, onClick) ->
      HedvigButton(
        text = text,
        onClick = onClick,
        enabled = true,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
    }
    Spacer(Modifier.height(8.dp))
  }
}

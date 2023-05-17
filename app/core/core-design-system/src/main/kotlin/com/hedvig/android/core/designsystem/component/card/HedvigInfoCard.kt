package com.hedvig.android.core.designsystem.component.card

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.squircle

@Composable
fun HedvigInfoCard(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  content: @Composable RowScope.() -> Unit,
) {
  HedvigCard(
    shape = MaterialTheme.shapes.squircle,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.infoContainer,
      contentColor = MaterialTheme.colorScheme.onInfoContainer,
    ),
    modifier = modifier,
  ) {
    Row(modifier = Modifier.padding(contentPadding)) {
      content()
    }
  }
}

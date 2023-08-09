package com.hedvig.android.feature.home.otherservices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.home.model.CommonClaim
import hedvig.resources.R

@Composable
internal fun OtherServicesBottomSheetContent(
  commonClaims: List<CommonClaim>,
  onClick: (CommonClaim) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
  ) {
    Text(
      text = "Other services", // TODO string res
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    )
    Spacer(modifier = Modifier.height(32.dp))

    commonClaims.forEach { commonClaim ->
      SelectableItem(
        text = commonClaim.title,
        onClick = { onClick(commonClaim) },
      )
      Spacer(modifier = Modifier.height(8.dp))
    }

    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      onClick = onDismiss,
    )
  }
}

@Composable
private fun SelectableItem(text: String, onClick: () -> Unit) {
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(72.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewOtherServicesBottomSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      OtherServicesBottomSheetContent(
        commonClaims = listOf(
          CommonClaim.GenerateTravelCertificate,
          CommonClaim.ChangeAddress,
        ),
        onClick = {},
        onDismiss = {},
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

package com.hedvig.android.core.ui.infocard

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.material3.containedButtonContainer
import com.hedvig.android.core.designsystem.material3.onContainedButtonContainer

@Composable
fun InfoCardTextButton(
  onClick: () -> Unit,
  text: String,
  modifier: Modifier = Modifier,
) {
  HedvigContainedSmallButton(
    text = text,
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.containedButtonContainer,
      contentColor = MaterialTheme.colorScheme.onContainedButtonContainer,
    ),
    textStyle = MaterialTheme.typography.bodyMedium,
    modifier = modifier,
    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 1.dp),
  )
}

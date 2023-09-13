package com.hedvig.android.feature.changeaddress.destination

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.success.HedvigSuccessSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun ChangeAddressResultDestination(
  movingDate: String?,
  popBackstack: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigSuccessSection(
      title = "Address updated",
      subTitle = "Your new home will be insured starting from $movingDate",
      modifier = Modifier.align(Alignment.Center),
    )
    HedvigTextButton(
      text = stringResource(id = hedvig.resources.R.string.general_close_button),
      onClick = popBackstack,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(vertical = 32.dp, horizontal = 16.dp),
    )
  }
}

@HedvigPreview
@Composable
internal fun PreviewChangeAddressResultDestination() {
  HedvigTheme {
    Surface {
      ChangeAddressResultDestination(movingDate = "2023.03.12") {}
    }
  }
}

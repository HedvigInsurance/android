package com.hedvig.app.ui.compose.composables.screens

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.composables.buttons.LargeOutlinedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.HedvigTypography

@Composable
fun GenericInfoScreen(
  modifier: Modifier = Modifier,
  title: String,
  description: String,
  @DrawableRes icon: Int,
  primaryButtonText: String = stringResource(R.string.continue_button),
  onPrimaryButtonClicked: (() -> Unit)?,
  secondaryButtonText: String = stringResource(R.string.home_tab_error_button_text),
  onSecondaryButtonClicked: (() -> Unit)?,
) {
  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colors.onPrimary,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Icon(
        painter = painterResource(id = icon),
        contentDescription = null,
        Modifier.padding(top = 80.dp).size(25.dp),
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = title,
        style = HedvigTypography.h5,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = description,
        style = HedvigTypography.body2,
      )

      Spacer(modifier = Modifier.height(32.dp))

      Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
      ) {
        if (onPrimaryButtonClicked != null) {
          LargeContainedButton(onClick = onPrimaryButtonClicked) {
            Text(text = primaryButtonText)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (onSecondaryButtonClicked != null) {
          LargeOutlinedButton(onClick = onSecondaryButtonClicked) {
            Text(text = secondaryButtonText)
          }
        }
      }
    }
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GenericInfoScreenPreview() {
  HedvigTheme {
    GenericInfoScreen(
      title = "Title",
      description = "description test test test",
      icon = R.drawable.ic_hedvig_h,
      primaryButtonText = "Continue",
      onPrimaryButtonClicked = {},
      secondaryButtonText = "Retry",
      onSecondaryButtonClicked = {},
    )
  }
}

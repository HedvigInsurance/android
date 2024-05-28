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
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.hedvigDateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun ChangeAddressResultDestination(movingDate: LocalDate?, popBackstack: () -> Unit) {
  Box(
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigSuccessSection(
      title = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_SUCCESS_TITLE),
      subTitle = stringResource(
        id = hedvig.resources.R.string.CHANGE_ADDRESS_SUCCESS_SUBTITLE,
        movingDate?.toJavaLocalDate()?.format(hedvigDateTimeFormatter(getLocale())) ?: "-",
      ),
      modifier = Modifier.align(Alignment.Center),
    )
    HedvigTextButton(
      text = stringResource(id = hedvig.resources.R.string.general_done_button),
      onClick = popBackstack,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(vertical = 32.dp, horizontal = 16.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewChangeAddressResultDestination() {
  HedvigTheme {
    Surface {
      ChangeAddressResultDestination(movingDate = LocalDate.fromEpochDays(3000)) {}
    }
  }
}

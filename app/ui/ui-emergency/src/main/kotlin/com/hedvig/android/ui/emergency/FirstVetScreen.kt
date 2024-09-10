package com.hedvig.android.ui.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Scaffold
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.ColoredFirstVet
import hedvig.resources.R
import kotlinx.serialization.Serializable

@Composable
fun FirstVetScreen(
  sections: List<FirstVetSection>,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  Scaffold(
    topAppBarText = stringResource(id = R.string.HC_QUICK_ACTIONS_FIRSTVET_TITLE),
    navigateUp = navigateUp,
  ) {
    Column(
      Modifier.verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(modifier = Modifier.height(8.dp))
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(horizontal = 16.dp),
      ) {
        for (section in sections) {
          Surface(
            shape = HedvigTheme.shapes.cornerXLarge,
            modifier = Modifier
              .fillMaxWidth(),
          ) {
            Column(Modifier.padding(16.dp)) {
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  HedvigIcons.ColoredFirstVet,
                  "",
                  Modifier.size(28.dp),
                  tint = Color.Unspecified,
                )
                HedvigText(text = section.title ?: "${sections.indexOf(section)}")
              }
              Spacer(modifier = Modifier.height(16.dp))
              HedvigText(
                text = section.description ?: "",
                color = HedvigTheme.colorScheme.textSecondary,
              )
              Spacer(modifier = Modifier.height(16.dp))
              HedvigButton(
                buttonSize = ButtonDefaults.ButtonSize.Medium,
                buttonStyle = ButtonDefaults.ButtonStyle.SecondaryAlt,
                enabled = true,
                text = section.buttonTitle
                  ?: stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_ONLINE_BOOKING_BUTTON),
                onClick = {
                  val url = section.url ?: "https://app.adjust.com/11u5tuxu"
                  context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                  )
                },
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.weight(1f))
      Spacer(modifier = Modifier.height(16.dp))
      HedvigTextButton(
        text = stringResource(R.string.general_close_button),
        onClick = navigateBack,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Serializable
data class FirstVetSection(
  val buttonTitle: String?,
  val description: String?,
  val title: String?,
  val url: String?,
)

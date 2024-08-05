package com.hedvig.android.ui.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.FirstVet
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
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
  HedvigScaffold(
    topAppBarText = stringResource(id = R.string.HC_QUICK_ACTIONS_FIRSTVET_TITLE),
    navigateUp = navigateUp,
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = modifier.padding(horizontal = 16.dp),
    ) {
      for (section in sections) {
        HedvigCard(Modifier.fillMaxWidth()) {
          Column(Modifier.padding(16.dp)) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Image(Icons.Hedvig.FirstVet, null, Modifier.size(28.dp))
              Text(text = section.title ?: "${sections.indexOf(section)}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = section.description ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            HedvigContainedSmallButton(
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

@Serializable
data class FirstVetSection(
  val buttonTitle: String?,
  val description: String?,
  val title: String?,
  val url: String?,
)

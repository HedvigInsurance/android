package com.hedvig.android.feature.help.center.commonclaim

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.FirstVet
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import hedvig.resources.R

@Composable
internal fun CommonClaimDestination(
  commonClaim: CommonClaim.Generic,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val context = LocalContext.current
  HedvigScaffold(
    topAppBarText = commonClaim.title,
    navigateUp = navigateUp,
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      for (bulletPoint in commonClaim.bulletPoints) {
        val isFirstVet = commonClaim.isFirstVet
        HedvigCard(Modifier.fillMaxWidth()) {
          Column(Modifier.padding(16.dp)) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              if (isFirstVet) {
                Image(Icons.Hedvig.FirstVet, null, Modifier.size(28.dp))
              }
              Text(text = bulletPoint.title)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = bulletPoint.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isFirstVet) {
              Spacer(modifier = Modifier.height(16.dp))
              HedvigContainedSmallButton(
                text = stringResource(R.string.SUBMIT_CLAIM_GLASS_DAMAGE_ONLINE_BOOKING_BUTTON),
                onClick = {
                  context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://app.adjust.com/11u5tuxu")),
                  )
                },
                modifier = Modifier.fillMaxWidth(),
              )
            }
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

@HedvigPreview
@Composable
private fun PreviewCommonClaimDestination(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isManyPets: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      CommonClaimDestination(
        CommonClaim.Generic(
          if (isManyPets) "30" else "29",
          "Title",
          listOf(
            CommonClaim.Generic.BulletPoint("Title", "Description"),
            CommonClaim.Generic.BulletPoint("Title#2", "Description#2"),
          ),
        ),
        {},
        {},
      )
    }
  }
}

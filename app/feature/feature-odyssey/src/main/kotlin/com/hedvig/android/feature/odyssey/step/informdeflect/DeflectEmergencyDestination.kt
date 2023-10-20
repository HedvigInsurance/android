package com.hedvig.android.feature.odyssey.step.informdeflect

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.rememberShapedColorPainter
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.infocard.VectorWarningCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DeflectEmergencyDestination(
  deflectEmergency: ClaimFlowDestination.DeflectEmergency,
  openChat: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  DeflectEmergencyScreen(
    partners = deflectEmergency.partners,
    openChat = openChat,
    closeClaimFlow = closeClaimFlow,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    imageLoader = imageLoader,
  )
}

@Composable
private fun DeflectEmergencyScreen(
  partners: ImmutableList<DeflectPartner>,
  openChat: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  val context = LocalContext.current
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) {
    Spacer(Modifier.height(8.dp))
    VectorWarningCard(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INFO_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    partners.forEachIndexed { index, partner ->
      if (index > 0) {
        Spacer(Modifier.height(8.dp))
      }
      HedvigCard(
        colors = CardDefaults.outlinedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      ) {
        Column(Modifier.padding(16.dp)) {
          AsyncImage(
            model = partner.imageUrl,
            contentDescription = null,
            imageLoader = imageLoader,
            placeholder = rememberShapedColorPainter(MaterialTheme.colorScheme.surface),
            modifier = Modifier
              .padding(16.dp)
              .fillMaxWidth()
              .height(80.dp),
          )
          Spacer(Modifier.height(8.dp))
          Text(
            text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_GLOBAL_ASSISTANCE_TITLE),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Text(
            text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_GLOBAL_ASSISTANCE_LABEL),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(lineBreak = LineBreak.Heading),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          )
          val phoneNumber = partner.phoneNumber
          if (phoneNumber != null) {
            Spacer(Modifier.height(24.dp))
            HedvigContainedButton(
              text = stringResource(R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_CALL_LABEL, phoneNumber),
              onClick = {
                try {
                  context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")))
                } catch (exception: Throwable) {
                  logcat(LogPriority.ERROR, exception) {
                    "Could not open dial activity in deflect emergency destination"
                  }
                }
              },
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_FOOTNOTE),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INSURANCE_COVER_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INSURANCE_COVER_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(32.dp))
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_TITLE),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_NEED_HELP_LABEL),
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    HedvigContainedSmallButton(
      text = stringResource(R.string.open_chat),
      onClick = openChat,
      modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun DeflectEmergencyScreenPreview() {
  DeflectEmergencyScreen(
    partners = persistentListOf(
      DeflectPartner(
        id = "1",
        imageUrl = "test",
        phoneNumber = "1234",
        url = "test",
      ),
    ),
    openChat = {},
    closeClaimFlow = {},
    windowSizeClass = WindowSizeClass.calculateForPreview(),
    navigateUp = {},
    imageLoader = rememberPreviewImageLoader(),
  )
}

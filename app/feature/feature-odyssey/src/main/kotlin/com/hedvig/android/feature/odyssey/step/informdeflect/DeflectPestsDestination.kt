package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.DeflectPartner
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun DeflectPestsDestination(
  parameter: ClaimFlowDestination.DeflectPests,
  openChat: () -> Unit,
  closeClaimFlow: () -> Unit,
  windowSizeClass: WindowSizeClass,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    VectorInfoCard(
      text = stringResource(id = R.string.SUBMIT_CLAIM_PESTS_INFO_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_PARTNER_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(16.dp))
    parameter.partners.forEach { partner ->
      HedvigCard(
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primary),
      ) {
        Column {
          Spacer(modifier = Modifier.height(24.dp))
          AsyncImage(
            model = partner.imageUrl,
            contentDescription = "Partner image",
            imageLoader = imageLoader,
            modifier = Modifier.padding(16.dp).fillMaxWidth().height(40.dp),
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = stringResource(id = R.string.SUBMIT_CLAIM_PESTS_CUSTOMER_SERVICE_LABEL),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 32.dp),
          )
          Spacer(modifier = Modifier.height(16.dp))
          HedvigContainedButton(
            text = stringResource(id = R.string.SUBMIT_CLAIM_PESTS_CUSTOMER_SERVICE_BUTTON),
            onClick = {
              val url = partner.url
              if (url != null) {
                openUrl(url)
              }
            },
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.onPrimary,
              contentColor = MaterialTheme.colorScheme.primary,
              disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f),
              disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            ),
          )
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_EMERGENCY_INSURANCE_COVER_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_PESTS_HOW_IT_WORKS_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(56.dp))
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_NEED_HELP_TITLE),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Text(
      text = stringResource(id = R.string.SUBMIT_CLAIM_NEED_HELP_LABEL),
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(24.dp))
    Box(
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      HedvigContainedSmallButton(
        text = stringResource(id = R.string.open_chat),
        onClick = openChat,
      )
    }
    Spacer(modifier = Modifier.height(56.dp))
  }
}

@HedvigPreview
@Composable
private fun DeflectPestsDestinationPreview() {
  DeflectPestsDestination(
    parameter = ClaimFlowDestination.DeflectPests(
      partners = listOf(
        DeflectPartner(
          id = "1",
          imageUrl = "test",
          phoneNumber = "1234",
          url = "test",
        ),
      ),
    ),
    openChat = {},
    closeClaimFlow = {},
    windowSizeClass = WindowSizeClass.calculateForPreview(),
    navigateUp = {},
    imageLoader = rememberPreviewImageLoader(),
    openUrl = {},
  )
}

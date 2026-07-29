package com.hedvig.android.feature.terminateinsurance.step.redirection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigMarkdownText
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.terminateinsurance.data.RedirectionImage
import com.hedvig.android.feature.terminateinsurance.data.RedirectionType
import com.hedvig.android.feature.terminateinsurance.data.SurveyOptionRedirection
import com.hedvig.android.feature.terminateinsurance.ui.TerminationScaffold

@Composable
internal fun TerminationRedirectionDestination(
  redirection: SurveyOptionRedirection,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  onStartRedirection: () -> Unit,
  onContinueCancelling: () -> Unit,
) {
  TerminationScaffold(
    navigateUp = navigateUp,
    closeTerminationFlow = closeTerminationFlow,
  ) { scaffoldTitle ->
    // TODO: Add "Before we continue" / "Innan vi fortsätter" to Lokalise
    FlowHeading(
      title = scaffoldTitle,
      description = "Before we continue",
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    if (redirection.image != null) {
      RedirectionImageBlock(
        image = redirection.image,
        imageLoader = imageLoader,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
    HedvigText(
      redirection.title,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(4.dp))
    HedvigMarkdownText(
      redirection.description,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f).heightIn(min = 16.dp))
    if (redirection.type == RedirectionType.UPDATE_ADDRESS) {
      HedvigButton(
        text = redirection.actionText,
        enabled = true,
        onClick = dropUnlessResumed { onStartRedirection() },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
    }
    // TODO: Add "Continue cancelling" / "Fortsätt att avsluta" to Lokalise
    HedvigTextButton(
      text = "Continue cancelling",
      buttonSize = Large,
      onClick = dropUnlessResumed { onContinueCancelling() },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun RedirectionImageBlock(image: RedirectionImage, imageLoader: ImageLoader, modifier: Modifier = Modifier) {
  Box(modifier) {
    AsyncImage(
      model = image.url,
      contentDescription = EmptyContentDescription,
      imageLoader = imageLoader,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .clip(RoundedCornerShape(16.dp)),
    )
    if (image.overlayText != null) {
      HighlightLabel(
        labelText = image.overlayText,
        size = HighlightLabelDefaults.HighLightSize.Small,
        color = HighlightLabelDefaults.HighlightColor.Green(HighlightLabelDefaults.HighlightShade.LIGHT),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(12.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationRedirectionDestinationWithImage() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationRedirectionDestination(
        redirection = SurveyOptionRedirection(
          title = "Bring Hedvig to your new home",
          description = "Move your insurance to your new home with Hedvig and get 15% off your home insurance the first year.",
          type = RedirectionType.UPDATE_ADDRESS,
          actionText = "See price for new home",
          image = RedirectionImage(url = "", overlayText = "15% off"),
        ),
        imageLoader = rememberPreviewImageLoader(),
        navigateUp = {},
        closeTerminationFlow = {},
        onStartRedirection = {},
        onContinueCancelling = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationRedirectionDestinationWithoutImage() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationRedirectionDestination(
        redirection = SurveyOptionRedirection(
          title = "Bring Hedvig to your new home",
          description = "Move your insurance to your new home with Hedvig and get 15% off your home insurance the first year.",
          type = RedirectionType.UPDATE_ADDRESS,
          actionText = "See price for new home",
          image = null,
        ),
        imageLoader = rememberPreviewImageLoader(),
        navigateUp = {},
        closeTerminationFlow = {},
        onStartRedirection = {},
        onContinueCancelling = {},
      )
    }
  }
}

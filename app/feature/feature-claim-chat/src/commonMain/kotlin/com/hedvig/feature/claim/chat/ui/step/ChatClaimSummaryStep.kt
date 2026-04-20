package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.SignedAudioUrl
import com.hedvig.feature.claim.chat.ui.common.FilesRow
import com.hedvig.feature.claim.chat.ui.common.RoundCornersPill
import hedvig.resources.CLAIM_CHAT_FILE_TITLE
import hedvig.resources.CLAIM_CHAT_RECORDING_TITLE
import hedvig.resources.EMBARK_SUBMIT_CLAIM
import hedvig.resources.Res
import hedvig.resources.claim_status_claim_details_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChatClaimSummaryBottomContent(
  onSubmit: () -> Unit,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    if (isCurrentStep) {
      HedvigButton(
        text = stringResource(Res.string.EMBARK_SUBMIT_CLAIM),
        enabled = !continueButtonLoading,
        isLoading = continueButtonLoading,
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
internal fun ChatClaimSummaryTopContent(
  recordingUrls: List<String>,
  fileUploads: List<UiFile>,
  freeTexts: List<String>,
  displayItems: List<Pair<String, String>>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigCard(
      color = HedvigTheme.colorScheme.fillNegative,
      modifier = Modifier
        .shadow(
          elevation = 4.dp,
          shape = HedvigTheme.shapes.cornerXLarge,
        )
        .border(
          width = 1.dp,
          color = HedvigTheme.colorScheme.borderPrimary,
          shape = HedvigTheme.shapes.cornerXLarge,
        ),
    ) {
      Column(Modifier.padding(vertical = 16.dp)) {
        if (displayItems.isNotEmpty()) {
          HedvigText(
            stringResource(Res.string.claim_status_claim_details_title),
            modifier = Modifier.padding(horizontal = 16.dp).semantics {
              heading()
            },
          )
          Spacer(Modifier.height(8.dp))
          CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
            Column(Modifier.padding(horizontal = 16.dp)) {
              for (displayItem in displayItems) {
                HorizontalItemsWithMaximumSpaceTaken(
                  spaceBetween = 8.dp,
                  startSlot = {
                    HedvigText(text = displayItem.first)
                  },
                  endSlot = {
                    HedvigText(
                      text = displayItem.second,
                      textAlign = TextAlign.End,
                    )
                  },
                  modifier = Modifier.semantics(true) {},
                )
              }
            }
          }
        }
        if (recordingUrls.isNotEmpty()) {
          Spacer(Modifier.height(24.dp))
          HedvigText(
            stringResource(Res.string.CLAIM_CHAT_RECORDING_TITLE),
            Modifier.padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))
          recordingUrls.forEachIndexed { index, string ->
            val audioPlayer = rememberAudioPlayer(
              PlayableAudioSource.RemoteUrl(
                SignedAudioUrl.fromSignedAudioUrlString(string),
              ),
            )
            HedvigAudioPlayer(audioPlayer = audioPlayer, Modifier.padding(horizontal = 16.dp))
            if (index != recordingUrls.lastIndex) {
              Spacer(Modifier.height(8.dp))
            }
          }
        }
        if (freeTexts.isNotEmpty()) {
          Column(
            Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            freeTexts.forEach { string ->
              Spacer(Modifier.height(24.dp))
              RoundCornersPill(
                modifier = Modifier.fillMaxWidth(),
                onClick = null,
                isSelected = false,
              ) {
                HedvigText(string)
              }
            }
          }
        }
        if (fileUploads.isNotEmpty()) {
          Spacer(Modifier.height(24.dp))
          HedvigText(
            stringResource(Res.string.CLAIM_CHAT_FILE_TITLE),
            Modifier.padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(8.dp))
          FilesRow(
            uiFiles = fileUploads,
            imageLoader = imageLoader,
            onNavigateToImageViewer = onNavigateToImageViewer,
            onRemoveFile = null,
            alignment = Alignment.Start,
            contentPadding = PaddingValues(horizontal = 16.dp),
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSummaryTopContent() {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
      ) {
        ChatClaimSummaryTopContent(
          recordingUrls = listOf("", ""),
          displayItems = listOf(
            "Locked" to "Yes",
            "Electric bike" to "Yes",
          ),
          fileUploads = listOf(),
          imageLoader = rememberPreviewImageLoader(),
          onNavigateToImageViewer = { _, _ -> },
          freeTexts = listOf("A quite short text short text short text short text"),
        )
      }
    }
  }
}

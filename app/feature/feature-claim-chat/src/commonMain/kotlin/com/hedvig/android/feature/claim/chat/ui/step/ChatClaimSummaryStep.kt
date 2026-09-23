package com.hedvig.android.feature.claim.chat.ui.step

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.hedvig.android.audio.player.data.PlayableAudioSource
import com.hedvig.android.audio.player.data.SignedAudioUrl
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigButtonGhostWithBorder
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.ui.common.FilesRow
import hedvig.resources.EMBARK_SUBMIT_CLAIM
import hedvig.resources.Res
import hedvig.resources.claim_status_claim_details_title
import hedvig.resources.claim_status_show_all_answers
import hedvig.resources.general_close_button
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

/**
 * The answers to show behind "Show all answers".
 *
 * [StepContent.Summary.answers] does not carry a recording the member made by voice; that arrives only in
 * [StepContent.Summary.audioRecordings]. The sheet is the recording's only surface on this screen, so when the
 * answers carry no recording of their own the legacy list supplies them, which keeps a voice answer reachable
 * on a claim whose other answers came through normally. Uploads follow the same rule.
 *
 * The answers win for a kind as soon as they hold one of it, rather than each url being matched against them.
 * The two lists are the same claim seen twice, and nothing in the payload promises that the same file is named
 * identically in both, so a url comparison would be free to decide two names are two recordings and play the
 * member their answer twice.
 *
 * [recordingTitle] and [fileTitle] stand in for the questions, which neither list carries.
 */
internal fun StepContent.Summary.answersToShow(
  recordingTitle: String,
  fileTitle: String,
): List<StepContent.Summary.Answer> {
  val answersCarryAudio = answers.any { it.value is StepContent.Summary.Answer.Value.Audio }
  val answersCarryFiles = answers.any { it.value is StepContent.Summary.Answer.Value.Files }
  val unansweredRecordings = if (answersCarryAudio) {
    emptyList()
  } else {
    audioRecordings.distinctBy { it.url.withoutSigning() }
  }
  val unlistedUploads = if (answersCarryFiles) {
    emptyList()
  } else {
    fileUploads.distinctBy { it.url.withoutSigning() }
  }
  return buildList {
    addAll(answers)
    for (audioRecording in unansweredRecordings) {
      add(
        StepContent.Summary.Answer(
          title = recordingTitle,
          value = StepContent.Summary.Answer.Value.Audio(audioRecording.url, null),
        ),
      )
    }
    if (unlistedUploads.isNotEmpty()) {
      // The sheet draws a set of files as a single row, so they share one answer.
      add(
        StepContent.Summary.Answer(
          title = fileTitle,
          value = StepContent.Summary.Answer.Value.Files(unlistedUploads),
        ),
      )
    }
  }
}

/** The url stripped of its signing query, which the backend varies between fetches of the same file. */
private fun String.withoutSigning(): String = substringBefore("?")

@Composable
internal fun ChatClaimSummaryTopContent(
  keyDetails: List<StepContent.Summary.Item>,
  answers: List<StepContent.Summary.Answer>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val answersSheetState = rememberHedvigBottomSheetState<List<StepContent.Summary.Answer>>()
  HedvigBottomSheet(answersSheetState) { sheetAnswers ->
    ClaimSummaryAnswersContent(
      answers = sheetAnswers,
      imageLoader = imageLoader,
      onNavigateToImageViewer = onNavigateToImageViewer,
      onClose = { answersSheetState.dismiss() },
    )
  }
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
        if (keyDetails.isNotEmpty()) {
          HedvigText(
            stringResource(Res.string.claim_status_claim_details_title),
            modifier = Modifier.padding(horizontal = 16.dp).semantics {
              heading()
            },
          )
          Spacer(Modifier.height(8.dp))
          CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
            Column(Modifier.padding(horizontal = 16.dp)) {
              for (keyDetail in keyDetails) {
                HorizontalItemsWithMaximumSpaceTaken(
                  spaceBetween = 8.dp,
                  startSlot = {
                    HedvigText(text = keyDetail.title)
                  },
                  endSlot = {
                    HedvigText(
                      text = keyDetail.value,
                      textAlign = TextAlign.End,
                    )
                  },
                  modifier = Modifier.semantics(true) {},
                )
              }
            }
          }
        }
        if (answers.isNotEmpty()) {
          Spacer(Modifier.height(16.dp))
          HedvigButtonGhostWithBorder(
            text = stringResource(Res.string.claim_status_show_all_answers),
            onClick = { answersSheetState.show(answers) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          )
        }
      }
    }
  }
}

@Composable
internal fun ClaimSummaryAnswersContent(
  answers: List<StepContent.Summary.Answer>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth()) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      stringResource(Res.string.claim_status_claim_details_title),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().semantics { heading() },
    )
    Spacer(Modifier.height(24.dp))
    answers.forEachIndexed { index, answer ->
      HedvigText(
        text = answer.title,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      Spacer(Modifier.height(4.dp))
      CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textPrimary) {
        AnswerValue(
          value = answer.value,
          imageLoader = imageLoader,
          onNavigateToImageViewer = onNavigateToImageViewer,
        )
      }
      if (index != answers.lastIndex) {
        Spacer(Modifier.height(24.dp))
      }
    }
    Spacer(Modifier.height(32.dp))
    HedvigButton(
      text = stringResource(Res.string.general_close_button),
      onClick = onClose,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun AnswerValue(
  value: StepContent.Summary.Answer.Value,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
) {
  when (value) {
    is StepContent.Summary.Answer.Value.Text -> {
      HedvigText(text = value.text)
    }

    is StepContent.Summary.Answer.Value.Audio -> {
      val audioPlayer = rememberAudioPlayer(
        PlayableAudioSource.RemoteUrl(
          SignedAudioUrl.fromSignedAudioUrlString(value.url),
        ),
      )
      HedvigAudioPlayer(audioPlayer = audioPlayer)
    }

    is StepContent.Summary.Answer.Value.Files -> {
      FilesRow(
        uiFiles = value.files.map {
          UiFile(
            name = it.fileName,
            localPath = null,
            url = it.url,
            mimeType = it.contentType,
            id = it.url,
          )
        },
        imageLoader = imageLoader,
        onNavigateToImageViewer = onNavigateToImageViewer,
        onRemoveFile = null,
        alignment = Alignment.Start,
      )
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
          keyDetails = listOf(
            StepContent.Summary.Item("Type of claim", "Theft"),
            StepContent.Summary.Item("Date", "2026-07-13"),
            StepContent.Summary.Item("Location", "Stockholm"),
          ),
          answers = previewAnswers(),
          imageLoader = rememberPreviewImageLoader(),
          onNavigateToImageViewer = { _, _ -> },
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSummaryAnswersContent() {
  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      ClaimSummaryAnswersContent(
        answers = previewAnswers(),
        imageLoader = rememberPreviewImageLoader(),
        onNavigateToImageViewer = { _, _ -> },
        onClose = {},
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}

internal fun previewAnswers(): List<StepContent.Summary.Answer> = listOf(
  StepContent.Summary.Answer(
    title = "Was the bike locked?",
    value = StepContent.Summary.Answer.Value.Text("No"),
  ),
  StepContent.Summary.Answer(
    title = "Where did it happen?",
    value = StepContent.Summary.Answer.Value.Text("Outside the central station in Stockholm"),
  ),
  StepContent.Summary.Answer(
    title = "Describe what happened",
    value = StepContent.Summary.Answer.Value.Audio(
      url = "",
      transcript = "I parked my bike and when I came back it was gone.",
    ),
  ),
)

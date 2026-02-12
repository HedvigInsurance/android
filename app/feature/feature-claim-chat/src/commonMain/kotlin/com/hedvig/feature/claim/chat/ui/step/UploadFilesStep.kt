package com.hedvig.feature.claim.chat.ui.step

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import coil3.ImageLoader
import com.eygraber.uri.Uri
import com.hedvig.android.compose.photo.capture.state.rememberGetMultipleContentsResultLauncher
import com.hedvig.android.compose.photo.capture.state.rememberPhotoCaptureState
import com.hedvig.android.compose.photo.capture.state.rememberPickMultipleVisualMediaResultLauncher
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.Camera
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Image
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.data.StepId
import com.hedvig.feature.claim.chat.ui.common.EditButton
import com.hedvig.feature.claim.chat.ui.common.FilesRow
import com.hedvig.feature.claim.chat.ui.common.SkippedLabel
import hedvig.resources.CLAIM_CHAT_FILE_UPLOAD_SEND_BUTTON
import hedvig.resources.Res
import hedvig.resources.claim_status_detail_add_files
import hedvig.resources.claim_status_detail_add_more_files
import hedvig.resources.claims_skip_button
import hedvig.resources.file_upload_choose_files
import hedvig.resources.file_upload_photo_library
import hedvig.resources.file_upload_take_photo
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UploadFilesStep(
  itemId: StepId,
  stepContent: StepContent.FileUpload,
  appPackageId: String,
  isCurrentStep: Boolean,
  canEdit: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  imageLoader: ImageLoader,
  onEvent: (ClaimChatEvent) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    if (isCurrentStep) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UploadFilesBubble(
          addLocalFile = { uri ->
            onEvent(
              ClaimChatEvent.AddFile(
                itemId,
                uri.toString(), // todo: check!
              ),
            )
          },
          onRemoveFile = { fileId ->
            onEvent(
              ClaimChatEvent.RemoveFile(
                itemId,
                fileId,
              ),
            )
          },
          appPackageId = appPackageId,
          localFiles = stepContent.localFiles,
          imageLoader = imageLoader,
          onNavigateToImageViewer = onNavigateToImageViewer,
        )
        if (stepContent.localFiles.isNotEmpty()) {
          HedvigButton(
            text = stringResource(Res.string.CLAIM_CHAT_FILE_UPLOAD_SEND_BUTTON),
            enabled = !continueButtonLoading,
            onClick = {
              onEvent(
                ClaimChatEvent.SubmitFile(
                  itemId,
                ),
              )
            },
            isLoading = continueButtonLoading,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        if (stepContent.isSkippable && stepContent.localFiles.isEmpty()) {
          HedvigButton(
            text = stringResource(Res.string.claims_skip_button),
            enabled = !skipButtonLoading,
            onClick = {
              onEvent(ClaimChatEvent.Skip(itemId))
            },
            isLoading = skipButtonLoading,
            modifier = Modifier.fillMaxWidth(),
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
          )
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (stepContent.localFiles.isNotEmpty()) {
          FilesRow(
            uiFiles = stepContent.localFiles,
            onRemoveFile = null,
            imageLoader = imageLoader,
            onNavigateToImageViewer = onNavigateToImageViewer,
            alignment = Alignment.End,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.layout { measurable, constraints ->
              val extraHorizontalSpace = 16.dp
              val measurable = measurable.measure(
                constraints.offset(horizontal = (extraHorizontalSpace * 2).roundToPx()),
              )
              layout(measurable.width, measurable.height) {
                measurable.place(0, 0)
              }
            },
          )
          if (canEdit) {
            Spacer(Modifier.height(8.dp))
          }
        } else {
          SkippedLabel()
        }
        EditButton(
          canEdit,
          onRegret = {
            onEvent(ClaimChatEvent.ShowConfirmEditDialog(itemId))
          },
        )
      }
    }
  }
}

@Composable
private fun UploadFilesBubble(
  addLocalFile: (uri: Uri) -> Unit,
  onRemoveFile: (fileId: String) -> Unit,
  appPackageId: String,
  localFiles: List<UiFile>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (
    imageUrl: String,
    cacheKey: String,
  ) -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  val fileTypeSelectBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  val photoCaptureState = rememberPhotoCaptureState(appPackageId = appPackageId) { uri ->
    addLocalFile(uri)
  }
  val photoPicker = rememberPickMultipleVisualMediaResultLauncher { resultingUriList ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }
  val filePicker = rememberGetMultipleContentsResultLauncher { resultingUriList ->
    for (resultingUri in resultingUriList) {
      addLocalFile(resultingUri)
    }
  }
  FilePickerBottomSheet(
    sheetState = fileTypeSelectBottomSheetState,
    onPickPhoto = {
      photoPicker.launch()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onPickFile = {
      filePicker.launch()
      fileTypeSelectBottomSheetState.dismiss()
    },
    onTakePhoto = {
      photoCaptureState.launchTakePhotoRequest()
      fileTypeSelectBottomSheetState.dismiss()
    },
  )
  UploadFilesBubbleContent(
    onAddFilesButtonClick = {
      focusManager.clearFocus()
      fileTypeSelectBottomSheetState.show()
    },
    onRemoveFile = onRemoveFile,
    localFiles = localFiles,
    imageLoader = imageLoader,
    onNavigateToImageViewer = onNavigateToImageViewer,
    modifier = modifier,
  )
}

@Composable
private fun UploadFilesBubbleContent(
  onRemoveFile: ((fileId: String) -> Unit)?,
  onAddFilesButtonClick: () -> Unit,
  localFiles: List<UiFile>,
  imageLoader: ImageLoader,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
    if (localFiles.isNotEmpty()) {
      FilesRow(
        uiFiles = localFiles,
        onRemoveFile = onRemoveFile,
        imageLoader = imageLoader,
        onNavigateToImageViewer = onNavigateToImageViewer,
        alignment = Alignment.Start,
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.layout { measurable, constraints ->
          val extraHorizontalSpace = 16.dp
          val measurable = measurable.measure(
            constraints.offset(horizontal = (extraHorizontalSpace * 2).roundToPx()),
          )
          layout(measurable.width, measurable.height) {
            measurable.place(0, 0)
          }
        },
      )
    }
    HedvigButton(
      buttonStyle = if (localFiles.isNotEmpty()) {
        ButtonDefaults.ButtonStyle.Secondary
      } else {
        ButtonDefaults.ButtonStyle.Primary
      },
      text = if (localFiles.isNotEmpty()) {
        stringResource(Res.string.claim_status_detail_add_more_files)
      } else {
        stringResource(Res.string.claim_status_detail_add_files)
      },
      onClick = onAddFilesButtonClick,
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun FilePickerBottomSheet(
  sheetState: HedvigBottomSheetState<Unit>,
  onPickPhoto: () -> Unit,
  onPickFile: () -> Unit,
  onTakePhoto: () -> Unit,
) {
  HedvigBottomSheet(
    sheetState,
    content = {
      FilePickerBottomSheetContent(
        onPickPhoto = onPickPhoto,
        onTakePhoto = onTakePhoto,
        onPickFile = onPickFile,
      )
    },
  )
}

@Composable
private fun FilePickerBottomSheetContent(
  onPickPhoto: () -> Unit,
  onTakePhoto: () -> Unit,
  onPickFile: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      HedvigButton(
        onClick = onTakePhoto,
        true,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      ) {
        Row {
          Icon(HedvigIcons.Camera, null)
          Spacer(Modifier.width(8.dp))
          HedvigText(stringResource(Res.string.file_upload_take_photo))
        }
      }
      HedvigButton(
        onClick = onPickPhoto,
        true,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      ) {
        Row {
          Icon(HedvigIcons.Image, null)
          Spacer(Modifier.width(8.dp))
          HedvigText(stringResource(Res.string.file_upload_photo_library))
        }
      }
      HedvigButton(
        onClick = onPickFile,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      ) {
        Row {
          Icon(HedvigIcons.Document, null)
          Spacer(Modifier.width(8.dp))
          HedvigText(stringResource(Res.string.file_upload_choose_files))
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@HedvigPreview
@Composable
private fun PreviewUploadFilesStep(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasFiles: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      UploadFilesStep(
        itemId = StepId(""),
        stepContent = StepContent.FileUpload(
          "",
          true,
          listOf(
            UiFile(
              name = "file",
              localPath = "path",
              mimeType = "image/jpg",
              url = null,
              id = "1",
            ),
          ).takeIf { hasFiles }.orEmpty(),
        ),
        appPackageId = "",
        isCurrentStep = true,
        canEdit = true,
        continueButtonLoading = false,
        skipButtonLoading = false,
        imageLoader = rememberPreviewImageLoader(),
        onEvent = {},
        onNavigateToImageViewer = { _, _ -> },
      )
    }
  }
}

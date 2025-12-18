package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.core.fileupload.UploadSuccess
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.launch

internal class AddFilesViewModel(
  uploadFileUseCase: UploadFileUseCase,
  fileService: FileService,
  targetUploadUrl: String,
  cacheManager: NetworkCacheManager,
  initialFilesUri: List<String>,
) : MoleculeViewModel<AddFilesEvent, FileUploadUiState>(
    initialState = FileUploadUiState(),
    presenter = AddFilesPresenter(
      uploadFiles = { url, uriStrings ->
        uploadFileUseCase.invoke(url, uriStrings.map { Uri.parse(it) })
      },
      getMimeType = { uriString -> fileService.getMimeType(Uri.parse(uriString)) },
      getFileName = { uriString -> fileService.getFileName(Uri.parse(uriString)) },
      targetUploadUrl = targetUploadUrl,
      clearCache = cacheManager::clearCache,
      initialFilesUri = initialFilesUri,
    ),
  )

internal class AddFilesPresenter(
  private val uploadFiles: suspend (url: String, uriStrings: List<String>) -> Either<ErrorMessage, UploadSuccess>,
  private val getMimeType: (String) -> String?,
  private val getFileName: (String) -> String?,
  private val targetUploadUrl: String,
  private val clearCache: suspend () -> Unit,
  private val initialFilesUri: List<String>,
) : MoleculePresenter<AddFilesEvent, FileUploadUiState> {
  @Composable
  override fun MoleculePresenterScope<AddFilesEvent>.present(lastState: FileUploadUiState): FileUploadUiState {
    var uiState by remember { mutableStateOf(lastState) }

    // Process initial files on first launch
    LaunchedEffect(Unit) {
      // State preservation - already have files loaded
      if (lastState.localFiles.isNotEmpty()) {
        return@LaunchedEffect
      }
      try {
        for (uriString in initialFilesUri) {
          if (uriString in uiState.localFiles.map { it.id }) {
            continue
          }
          val mimeType = getMimeType(uriString) ?: ""
          val name = getFileName(uriString) ?: uriString
          val localFile = UiFile(
            name = name,
            localPath = uriString,
            mimeType = mimeType,
            id = uriString,
            url = null,
          )
          uiState = uiState.copy(localFiles = uiState.localFiles + localFile)
        }
      } catch (e: Exception) {
        uiState = uiState.copy(errorMessage = e.message)
      }
    }

    CollectEvents { event ->
      when (event) {
        is AddFilesEvent.AddLocalFile -> {
          val uriString = event.uriString
          if (uriString in uiState.localFiles.map { it.id }) {
            return@CollectEvents
          }
          try {
            val mimeType = getMimeType(uriString) ?: ""
            val name = getFileName(uriString) ?: uriString
            val localFile = UiFile(
              name = name,
              localPath = uriString,
              mimeType = mimeType,
              id = uriString,
              url = null,
            )
            uiState = uiState.copy(localFiles = uiState.localFiles + localFile)
          } catch (e: Exception) {
            uiState = uiState.copy(errorMessage = e.message)
          }
        }

        AddFilesEvent.UploadFiles -> {
          if (uiState.isLoading) return@CollectEvents
          uiState = uiState.copy(isLoading = true)
          launch {
            either {
              val uriStrings = uiState.localFiles.mapNotNull { it.localPath }
              if (uriStrings.isNotEmpty()) {
                val result = uploadFiles(targetUploadUrl, uriStrings).bind()
                result.fileIds
              } else {
                emptyList()
              }
            }.fold(
              ifRight = { uploadedFileIds ->
                clearCache()
                uiState = uiState.copy(uploadedFileIds = uploadedFileIds, isLoading = false)
              },
              ifLeft = { errorMessage ->
                uiState = uiState.copy(errorMessage = errorMessage.message, isLoading = false)
              },
            )
          }
        }

        AddFilesEvent.DismissError -> {
          uiState = uiState.copy(errorMessage = null)
        }

        is AddFilesEvent.RemoveFile -> {
          uiState = uiState.copy(
            localFiles = uiState.localFiles.filterNot { it.id == event.fileId },
          )
        }
      }
    }

    return uiState
  }
}

internal sealed interface AddFilesEvent {
  data class AddLocalFile(val uriString: String) : AddFilesEvent

  data object UploadFiles : AddFilesEvent

  data object DismissError : AddFilesEvent

  data class RemoveFile(val fileId: String) : AddFilesEvent
}

internal data class FileUploadUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val localFiles: List<UiFile> = emptyList(),
  val uploadedFileIds: List<String> = emptyList(),
)

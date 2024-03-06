package com.hedvig.android.feature.odyssey.step.fileupload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.navigation.compose.typed.SerializableImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FileUploadViewModel(
  private val claimFlowRepository: ClaimFlowRepository,
  private val uploadFileUseCase: UploadFileUseCase,
  private val fileService: FileService,
  private val targetUploadUrl: String,
  files: SerializableImmutableList<UiFile>,
) : ViewModel() {
  private val _uiState = MutableStateFlow(FileUploadUiState(uploadedFiles = files))
  val uiState: StateFlow<FileUploadUiState> = _uiState.asStateFlow()

  fun onContinue() {
    if (uiState.value.hasFiles) {
      uploadFiles()
    } else {
      continueWithoutFiles()
    }
  }

  private fun uploadFiles() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val uris = uiState.value.localFiles.map { Uri.parse(it.localPath) }
        val uploadedFileIds = uiState.value.uploadedFiles.map { it.id }
        val allIds = if (uris.isNotEmpty()) {
          val result = uploadFileUseCase.invoke(url = targetUploadUrl, uris = uris).bind()
          result.fileIds + uploadedFileIds
        } else {
          uploadedFileIds
        }
        claimFlowRepository.submitFiles(allIds).bind()
      }.fold(
        ifRight = { nextStep ->
          _uiState.update { it.copy(nextStep = nextStep, isLoading = false) }
        },
        ifLeft = { errorMessage ->
          _uiState.update { it.copy(errorMessage = errorMessage.message, isLoading = false) }
        },
      )
    }
  }

  private fun continueWithoutFiles() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val nextStep = claimFlowRepository.submitFiles(emptyList()).bind()
        _uiState.update { it.copy(nextStep = nextStep, isLoading = false) }
      }.onLeft {
        _uiState.update { it.copy(errorMessage = it.errorMessage, isLoading = false) }
      }
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update {
      it.copy(nextStep = null)
    }
  }

  fun addLocalFile(uri: Uri) {
    if (uri.toString() in _uiState.value.localFiles.map { it.id }) {
      return
    }
    _uiState.update {
      try {
        val mimeType = fileService.getMimeType(uri)
        val name = fileService.getFileName(uri) ?: uri.toString()
        val localFile = UiFile(
          name = name,
          localPath = uri.toString(),
          mimeType = mimeType,
          id = uri.toString(),
          url = null,
          thumbnailUrl = null,
        )
        it.copy(localFiles = it.localFiles + localFile)
      } catch (e: Exception) {
        it.copy(errorMessage = e.message)
      }
    }
  }

  fun dismissError() {
    _uiState.update {
      it.copy(errorMessage = null)
    }
  }

  fun onRemoveFile(fileId: String) {
    _uiState.update {
      it.copy(
        localFiles = it.localFiles.filterNot { it.id == fileId },
        uploadedFiles = it.uploadedFiles.filterNot { it.id == fileId },
      )
    }
  }
}

internal data class FileUploadUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val localFiles: List<UiFile> = emptyList(),
  val uploadedFiles: List<UiFile> = emptyList(),
  val nextStep: ClaimFlowStep? = null,
) {
  val hasFiles = localFiles.isNotEmpty() || uploadedFiles.isNotEmpty()
}

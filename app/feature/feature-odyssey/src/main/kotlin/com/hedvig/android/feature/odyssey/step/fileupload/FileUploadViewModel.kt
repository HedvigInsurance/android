package com.hedvig.android.feature.odyssey.step.fileupload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocalFile
import com.hedvig.android.data.claimflow.UploadedFile
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
  files: SerializableImmutableList<UploadedFile>,
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
        val uris = uiState.value.localFiles.map { Uri.parse(it.path) }
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
    _uiState.update {
      val mimeType = fileService.getMimeType(uri)
      val localFile = LocalFile(
        path = uri.toString(),
        mimeType = mimeType,
        id = uri.toString(),
      )
      it.copy(localFiles = it.localFiles + localFile)
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
  val localFiles: List<LocalFile> = emptyList(),
  val uploadedFiles: List<UploadedFile> = emptyList(),
  val nextStep: ClaimFlowStep? = null,
) {
  val hasFiles = localFiles.isNotEmpty() || uploadedFiles.isNotEmpty()
}

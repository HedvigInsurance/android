package com.hedvig.android.feature.claim.details.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.eygraber.uri.Uri
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.fileupload.ClaimsServiceUploadFileUseCase
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.uidata.UiFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class AddFilesViewModel(
  private val claimsServiceUploadFileUseCase: ClaimsServiceUploadFileUseCase,
  private val fileService: FileService,
  private val targetUploadUrl: String,
  private val cacheManager: NetworkCacheManager,
  initialFilesUri: List<String>,
) : ViewModel() {
  private val _uiState = MutableStateFlow(FileUploadUiState())
  val uiState: StateFlow<FileUploadUiState> = _uiState.asStateFlow()

  init {
    try {
      for (uri in initialFilesUri) {
        addLocalFile(Uri.parse(uri))
      }
    } catch (e: Exception) {
      _uiState.update { it.copy(errorMessage = e.message) }
    }
  }

  fun uploadFiles() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val uris = uiState.value.localFiles.mapNotNull { it.localPath?.let { path -> Uri.parse(path) } }
        if (uris.isNotEmpty()) {
          val result = claimsServiceUploadFileUseCase.invoke(url = targetUploadUrl, uris = uris).bind()
          result.fileIds
        } else {
          emptyList()
        }
      }.fold(
        ifRight = { uploadedFileIds ->
          cacheManager.clearCache()
          _uiState.update { it.copy(uploadedFileIds = uploadedFileIds, isLoading = false) }
        },
        ifLeft = { errorMessage ->
          _uiState.update { it.copy(errorMessage = errorMessage.message, isLoading = false) }
        },
      )
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
      )
    }
  }
}

internal data class FileUploadUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val localFiles: List<UiFile> = emptyList(),
  val uploadedFileIds: List<String> = emptyList(),
)

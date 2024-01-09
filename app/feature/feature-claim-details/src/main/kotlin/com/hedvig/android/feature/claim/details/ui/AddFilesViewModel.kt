package com.hedvig.android.feature.claim.details.ui

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.fileupload.FileService
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.core.uidata.UiFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class AddFilesViewModel(
  private val uploadFileUseCase: UploadFileUseCase,
  private val fileService: FileService,
  private val targetUploadUrl: String,
  private val cacheManager: NetworkCacheManager,
  initialFileUri: String,
) : ViewModel() {
  private val _uiState = MutableStateFlow(FileUploadUiState())
  val uiState: StateFlow<FileUploadUiState> = _uiState.asStateFlow()

  init {
    try {
      addLocalFile(Uri.parse(initialFileUri))
    } catch (e: Exception) {
      _uiState.update { it.copy(errorMessage = e.message) }
    }
  }

  fun uploadFiles() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val uris = uiState.value.localFiles.map { Uri.parse(it.path) }
        if (uris.isNotEmpty()) {
          val result = uploadFileUseCase.invoke(url = targetUploadUrl, uris = uris).bind()
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
    _uiState.update {
      val mimeType = fileService.getMimeType(uri)
      val localFile = UiFile(
        path = uri.toString(),
        mimeType = mimeType,
        id = uri.toString(),
        name = uri.toFile().name,
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


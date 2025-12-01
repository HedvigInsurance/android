package com.hedvig.feature.claim.chat.di

import android.content.Context
import com.hedvig.feature.claim.chat.data.AndroidAudioRecordingManager
import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import com.hedvig.feature.claim.chat.data.file.AndroidFileService
import com.hedvig.feature.claim.chat.data.file.FileService
import kotlin.time.Clock
import org.koin.core.module.Module
import org.koin.dsl.module

actual val claimChatPlatformModule: Module = module {
  single<FileService> {
    AndroidFileService(
      coreFileService = get<com.hedvig.android.core.fileupload.FileService>(),
      contentResolver = get<Context>().contentResolver,
    )
  }
  single<AudioRecordingManager> { AndroidAudioRecordingManager(Clock.System) }
}

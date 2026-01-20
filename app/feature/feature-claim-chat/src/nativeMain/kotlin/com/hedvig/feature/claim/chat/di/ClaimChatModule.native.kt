package com.hedvig.feature.claim.chat.di

import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import com.hedvig.feature.claim.chat.data.NativeAudioRecordingManager
import com.hedvig.feature.claim.chat.data.file.FileService
import com.hedvig.feature.claim.chat.data.file.NativeFileService
import kotlin.time.Clock
import org.koin.core.module.Module
import org.koin.dsl.module

actual val claimChatPlatformModule: Module = module {
  single<FileService> {
    // TODO: Implement iOS FileService
    NativeFileService()
  }
  single<AudioRecordingManager> {
    NativeAudioRecordingManager(Clock.System)
  }
}

package com.hedvig.feature.claim.chat.di

import com.hedvig.feature.claim.chat.data.AndroidAudioRecordingManager
import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import kotlin.time.Clock
import org.koin.core.module.Module
import org.koin.dsl.module

actual val claimChatPlatformModule: Module = module {
  single<AudioRecordingManager> { AndroidAudioRecordingManager(Clock.System) }
}

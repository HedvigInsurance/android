package com.hedvig.feature.claim.chat.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.feature.claim.chat.data.AudioRecordingManager
import com.hedvig.feature.claim.chat.data.NativeAudioRecordingManager
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock

@ContributesTo(AppScope::class)
interface ClaimChatNativeMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideAudioRecordingManager(): AudioRecordingManager = NativeAudioRecordingManager(Clock.System)
}

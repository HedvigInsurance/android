package com.hedvig.feature.claim.chat.di

import android.content.Context
import com.hedvig.feature.claim.chat.data.file.AndroidFileService
import com.hedvig.feature.claim.chat.data.file.FileService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val claimChatPlatformModule: Module = module {
  single<FileService> {
    AndroidFileService(get<Context>().contentResolver)
  }
}

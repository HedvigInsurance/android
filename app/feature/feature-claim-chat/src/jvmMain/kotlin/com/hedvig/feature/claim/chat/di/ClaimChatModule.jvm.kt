package com.hedvig.feature.claim.chat.di

import com.hedvig.feature.claim.chat.data.file.FileService
import com.hedvig.feature.claim.chat.data.file.JvmFileService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val claimChatPlatformModule: Module = module {
  single<FileService> {
    JvmFileService()
  }
}

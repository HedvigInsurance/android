package com.hedvig.feature.claim.chat.di

import com.hedvig.feature.claim.chat.ClaimChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimChatModule = module {
  viewModel<ClaimChatViewModel> { ClaimChatViewModel(get()) }
}

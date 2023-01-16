package com.hedvig.android.odyssey.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val submitClaimModule = module {
  viewModel { com.hedvig.android.odyssey.AudioRecorderViewModel(get()) }
}

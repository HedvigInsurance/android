package com.hedvig.android.odyssey.di

import com.hedvig.android.odyssey.input.ui.audiorecorder.AudioRecorderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val submitClaimModule = module {
  viewModel { AudioRecorderViewModel(get()) }
}

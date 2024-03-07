package com.hedvig.android.core.di

import com.hedvig.android.core.appreview.ReviewDialogViewModel
import com.hedvig.android.core.appreview.SelfServiceCompletedEventDataStore
import com.hedvig.android.core.appreview.SelfServiceCompletedEventStore
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val coreAppReviewModule = module {
  single<SelfServiceCompletedEventStore> {
    SelfServiceCompletedEventDataStore(get())
  }

  viewModel<ReviewDialogViewModel> {
    ReviewDialogViewModel(get())
  }
}

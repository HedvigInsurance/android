package com.hedvig.android.core.appreview.di

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.core.appreview.SelfServiceCompletedAuthEventListener
import com.hedvig.android.core.appreview.SelfServiceCompletedEventDataStore
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManagerImpl
import com.hedvig.android.core.appreview.SelfServiceCompletedEventStore
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCase
import com.hedvig.android.core.appreview.WaitUntilAppReviewDialogShouldBeOpenedUseCaseImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val coreAppReviewModule = module {
  single<SelfServiceCompletedEventStore> {
    SelfServiceCompletedEventDataStore(get())
  }
  single<SelfServiceCompletedEventManager> {
    SelfServiceCompletedEventManagerImpl(get<SelfServiceCompletedEventStore>())
  }
  single<WaitUntilAppReviewDialogShouldBeOpenedUseCase> {
    WaitUntilAppReviewDialogShouldBeOpenedUseCaseImpl(get<SelfServiceCompletedEventStore>())
  }
  single<SelfServiceCompletedAuthEventListener> {
    SelfServiceCompletedAuthEventListener(get<SelfServiceCompletedEventManager>())
  } bind AuthEventListener::class
}

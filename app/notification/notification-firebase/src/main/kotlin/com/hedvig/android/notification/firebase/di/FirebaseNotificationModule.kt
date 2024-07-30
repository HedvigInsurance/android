package com.hedvig.android.notification.firebase.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.WorkerParameters
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.notification.firebase.DatastoreFCMTokenStorage
import com.hedvig.android.notification.firebase.FCMTokenAuthEventListener
import com.hedvig.android.notification.firebase.FCMTokenManager
import com.hedvig.android.notification.firebase.FCMTokenStorage
import com.hedvig.android.notification.firebase.FCMTokenUploadWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val firebaseNotificationModule = module {
  single<FCMTokenStorage> { DatastoreFCMTokenStorage(get<DataStore<Preferences>>()) }
  single<FCMTokenManager> {
    FCMTokenManager(
      applicationContext = get<Context>(),
      fcmTokenStorage = get<FCMTokenStorage>(),
    )
  }
  single<FCMTokenAuthEventListener> {
    FCMTokenAuthEventListener(get<FCMTokenManager>())
  } bind AuthEventListener::class
  worker<FCMTokenUploadWorker>(named<FCMTokenUploadWorker>()) {
    FCMTokenUploadWorker(
      context = get<Context>(),
      params = get<WorkerParameters>(),
      apolloClient = get<ApolloClient>(),
      fcmTokenStorage = get<FCMTokenStorage>(),
      authTokenService = get(),
    )
  }
}

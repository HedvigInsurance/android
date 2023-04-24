package com.hedvig.android.notification.firebase.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.WorkerParameters
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.notification.firebase.DatastoreFCMTokenStorage
import com.hedvig.android.notification.firebase.FCMTokenAuthEventListener
import com.hedvig.android.notification.firebase.FCMTokenManager
import com.hedvig.android.notification.firebase.FCMTokenStorage
import com.hedvig.android.notification.firebase.FCMTokenUploadWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.bind
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
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
  worker<FCMTokenUploadWorker> {
    FCMTokenUploadWorker(
      context = get<Context>(),
      params = get<WorkerParameters>(),
      apolloClient = get<ApolloClient>(giraffeClient),
      fcmTokenStorage = get<FCMTokenStorage>(),
      authTokenService = get<AuthTokenService>(),
    )
  }
}

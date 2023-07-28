package com.hedvig.android.appinformation.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.appinformation.EnableNotificationsReminderManager
import com.hedvig.android.appinformation.EnableNotificationsReminderManagerImpl
import com.hedvig.android.appinformation.GetConnectPaymentReminderUseCase
import com.hedvig.android.appinformation.GetConnectPaymentReminderUseCaseImpl
import com.hedvig.android.appinformation.GetUpcomingRenewalRemindersUseCase
import com.hedvig.android.appinformation.GetUpcomingRenewalRemindersUseCaseImpl
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import kotlinx.datetime.Clock
import org.koin.dsl.module

val appInformationModule = module {
  single<EnableNotificationsReminderManager> {
    EnableNotificationsReminderManagerImpl(get<DataStore<Preferences>>(), get<Clock>())
  }
  single<GetConnectPaymentReminderUseCase> {
    GetConnectPaymentReminderUseCaseImpl(get<ApolloClient>(giraffeClient), get<FeatureManager>())
  }
  single<GetUpcomingRenewalRemindersUseCase> {
    GetUpcomingRenewalRemindersUseCaseImpl(get<ApolloClient>(giraffeClient), get<Clock>())
  }
}

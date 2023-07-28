package com.hedvig.android.memberreminders.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManagerImpl
import com.hedvig.android.memberreminders.GetConnectPaymentReminderUseCase
import com.hedvig.android.memberreminders.GetConnectPaymentReminderUseCaseImpl
import com.hedvig.android.memberreminders.GetUpcomingRenewalRemindersUseCase
import com.hedvig.android.memberreminders.GetUpcomingRenewalRemindersUseCaseImpl
import kotlinx.datetime.Clock
import org.koin.dsl.module

val memberRemindersModule = module {
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

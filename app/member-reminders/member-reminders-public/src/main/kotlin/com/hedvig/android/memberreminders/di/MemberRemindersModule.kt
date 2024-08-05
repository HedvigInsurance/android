package com.hedvig.android.memberreminders.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseProvider
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManager
import com.hedvig.android.memberreminders.EnableNotificationsReminderManagerImpl
import com.hedvig.android.memberreminders.GetConnectPaymentReminderUseCase
import com.hedvig.android.memberreminders.GetConnectPaymentReminderUseCaseImpl
import com.hedvig.android.memberreminders.GetMemberRemindersUseCase
import com.hedvig.android.memberreminders.GetMemberRemindersUseCaseImpl
import com.hedvig.android.memberreminders.GetNeedsCoInsuredInfoRemindersUseCase
import com.hedvig.android.memberreminders.GetNeedsCoInsuredInfoRemindersUseCaseImpl
import com.hedvig.android.memberreminders.GetUpcomingRenewalRemindersUseCase
import com.hedvig.android.memberreminders.GetUpcomingRenewalRemindersUseCaseImpl
import kotlinx.datetime.Clock
import org.koin.dsl.module

val memberRemindersModule = module {
  single<EnableNotificationsReminderManager> {
    EnableNotificationsReminderManagerImpl(get<DataStore<Preferences>>(), get<Clock>(), get<HedvigBuildConstants>())
  }
  single<GetConnectPaymentReminderUseCase> {
    GetConnectPaymentReminderUseCaseImpl(get<ApolloClient>(), get<GetOnlyHasNonPayingContractsUseCaseProvider>())
  }
  single<GetUpcomingRenewalRemindersUseCase> {
    GetUpcomingRenewalRemindersUseCaseImpl(get<ApolloClient>(), get<Clock>())
  }
  single<GetNeedsCoInsuredInfoRemindersUseCase> {
    GetNeedsCoInsuredInfoRemindersUseCaseImpl(
      get<ApolloClient>(),
      get<FeatureManager>(),
    )
  }
  single<GetMemberRemindersUseCase> {
    GetMemberRemindersUseCaseImpl(
      get<EnableNotificationsReminderManager>(),
      get<GetConnectPaymentReminderUseCase>(),
      get<GetUpcomingRenewalRemindersUseCase>(),
      get<GetNeedsCoInsuredInfoRemindersUseCase>(),
    )
  }
}

package com.hedvig.android.appinformation.di

import com.hedvig.android.appinformation.EnableNotificationsReminderManager
import com.hedvig.android.appinformation.EnableNotificationsReminderManagerImpl
import org.koin.dsl.module

val appInformationModule = module {
  single<EnableNotificationsReminderManager> { EnableNotificationsReminderManagerImpl(get(), get()) }
}

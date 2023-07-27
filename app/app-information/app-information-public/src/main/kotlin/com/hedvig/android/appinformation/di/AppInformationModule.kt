package com.hedvig.android.appinformation.di

import com.hedvig.android.appinformation.EnableNotificationsInfoManager
import com.hedvig.android.appinformation.EnableNotificationsInfoManagerImpl
import org.koin.dsl.module

val appInformationModule = module {
  single<EnableNotificationsInfoManager> { EnableNotificationsInfoManagerImpl(get(), get()) }
}

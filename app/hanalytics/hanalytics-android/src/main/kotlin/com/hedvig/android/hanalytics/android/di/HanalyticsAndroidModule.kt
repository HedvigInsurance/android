package com.hedvig.android.hanalytics.android.di

import android.content.Context
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.hanalytics.AndroidHAnalyticsService
import com.hedvig.android.hanalytics.HAnalyticsService
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import okhttp3.OkHttpClient
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val hAnalyticsAndroidModule = module {
  single<HAnalyticsService> {
    val hedvingBuildConstants = get<HedvigBuildConstants>()
    AndroidHAnalyticsService(
      context = get<Context>(),
      okHttpClient = get<OkHttpClient>(),
      deviceIdDataStore = get<DeviceIdDataStore>(),
      hAnalyticsBaseUrl = hedvingBuildConstants.urlHanalytics,
      appVersionName = hedvingBuildConstants.appVersionName,
      appVersionCode = hedvingBuildConstants.appVersionCode,
      appId = hedvingBuildConstants.appId,
    )
  }
  single<ApplicationLifecycleTracker> { ApplicationLifecycleTracker() }
}

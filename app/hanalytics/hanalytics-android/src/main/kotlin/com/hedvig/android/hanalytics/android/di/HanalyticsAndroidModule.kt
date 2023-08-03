package com.hedvig.android.hanalytics.android.di

import com.hedvig.android.code.buildoconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.isProductionQualifier
import com.hedvig.android.hanalytics.AndroidHAnalyticsService
import com.hedvig.android.hanalytics.HAnalyticsService
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val appVersionCodeQualifier: StringQualifier = qualifier("appVersionCodeQualifier")
val appIdQualifier: StringQualifier = qualifier("appIdQualifier")

@Suppress("RemoveExplicitTypeArguments")
val hAnalyticsAndroidModule = module {
  single<HAnalyticsService> {
    val hedvingBuildConstants = get<HedvigBuildConstants>()
    AndroidHAnalyticsService(
      context = get(),
      okHttpClient = get(),
      deviceIdDataStore = get(),
      hAnalyticsBaseUrl = hedvingBuildConstants.urlHanalytics,
      appVersionName = hedvingBuildConstants.appVersionName,
      appVersionCode = get(appVersionCodeQualifier),
      appId = get(appIdQualifier),
    )
  }
  single<ApplicationLifecycleTracker> { ApplicationLifecycleTracker(get(), get(isProductionQualifier)) }
}

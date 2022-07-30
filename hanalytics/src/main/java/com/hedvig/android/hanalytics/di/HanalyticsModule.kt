@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.android.hanalytics.di

import com.hedvig.android.core.common.di.isDebugQualifier
import com.hedvig.android.hanalytics.HAnalyticsExperimentManager
import com.hedvig.android.hanalytics.HAnalyticsExperimentManagerImpl
import com.hedvig.android.hanalytics.HAnalyticsImpl
import com.hedvig.android.hanalytics.HAnalyticsService
import com.hedvig.android.hanalytics.HAnalyticsServiceImpl
import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.android.hanalytics.NetworkHAnalyticsSink
import com.hedvig.android.hanalytics.SendHAnalyticsEventUseCase
import com.hedvig.android.hanalytics.SendHAnalyticsEventUseCaseImpl
import com.hedvig.android.hanalytics.featureflags.ClearHAnalyticsExperimentsCacheUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.FeatureManagerImpl
import com.hedvig.android.hanalytics.featureflags.flags.DevFeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.flags.HAnalyticsFeatureFlagProvider
import com.hedvig.android.hanalytics.featureflags.loginmethod.DevLoginMethodProvider
import com.hedvig.android.hanalytics.featureflags.loginmethod.HAnalyticsLoginMethodProvider
import com.hedvig.android.hanalytics.featureflags.paymenttype.DevPaymentTypeProvider
import com.hedvig.android.hanalytics.featureflags.paymenttype.HAnalyticsPaymentTypeProvider
import com.hedvig.android.hanalytics.tracking.ApplicationLifecycleTracker
import com.hedvig.hanalytics.HAnalytics
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.qualifier
import org.koin.dsl.bind
import org.koin.dsl.module

val hAnalyticsUrlQualifier: StringQualifier = qualifier("hAnalyticsUrlQualifier")
val appVersionNameQualifier: StringQualifier = qualifier("appVersionNameQualifier")
val appVersionCodeQualifier: StringQualifier = qualifier("appVersionCodeQualifier")
val appIdQualifier: StringQualifier = qualifier("appIdQualifier")

val hAnalyticsModule = module {
  single<HAnalytics> { HAnalyticsImpl(get(), get()) }
  single<SendHAnalyticsEventUseCase> {
    // Workaround for https://github.com/InsertKoinIO/koin/issues/1146
    val allAnalyticsSinks = getAll<HAnalyticsSink>().distinct()
    SendHAnalyticsEventUseCaseImpl(allAnalyticsSinks)
  }
  single<HAnalyticsExperimentManager> { HAnalyticsExperimentManagerImpl(get(), get()) }
  single<NetworkHAnalyticsSink> { NetworkHAnalyticsSink(get()) } bind HAnalyticsSink::class
  single<HAnalyticsService> {
    HAnalyticsServiceImpl(
      context = get(),
      okHttpClient = get(),
      deviceIdDataStore = get(),
      hAnalyticsBaseUrl = get(hAnalyticsUrlQualifier),
      appVersionName = get(appVersionNameQualifier),
      appVersionCode = get(appVersionCodeQualifier),
      appId = get(appIdQualifier),
    )
  }
  single<ClearHAnalyticsExperimentsCacheUseCase> { ClearHAnalyticsExperimentsCacheUseCase(get()) }
}

val featureManagerModule = module {
  single<FeatureManager> {
    if (get(isDebugQualifier)) {
      FeatureManagerImpl(
        DevFeatureFlagProvider(get()),
        DevLoginMethodProvider(get()),
        DevPaymentTypeProvider(get()),
        get<ClearHAnalyticsExperimentsCacheUseCase>(),
      )
    } else {
      FeatureManagerImpl(
        HAnalyticsFeatureFlagProvider(get()),
        HAnalyticsLoginMethodProvider(get()),
        HAnalyticsPaymentTypeProvider(get()),
        get<ClearHAnalyticsExperimentsCacheUseCase>(),
      )
    }
  }
}

val trackerModule = module {
  single<ApplicationLifecycleTracker> { ApplicationLifecycleTracker(get()) }
}

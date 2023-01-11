package com.hedvig.android.hanalytics.di

import com.hedvig.android.core.common.di.LogInfoType
import com.hedvig.android.core.common.di.logInfoQualifier
import com.hedvig.android.hanalytics.HAnalyticsExperimentManager
import com.hedvig.android.hanalytics.HAnalyticsExperimentManagerImpl
import com.hedvig.android.hanalytics.HAnalyticsImpl
import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.android.hanalytics.SendHAnalyticsEventUseCase
import com.hedvig.android.hanalytics.SendHAnalyticsEventUseCaseImpl
import com.hedvig.android.hanalytics.sink.LoggingHAnalyticsSink
import com.hedvig.android.hanalytics.sink.NetworkHAnalyticsSink
import com.hedvig.hanalytics.HAnalytics
import org.koin.dsl.bind
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val hAnalyticsModule = module {
  single<HAnalytics> {
    HAnalyticsImpl(
      sendHAnalyticsEventUseCase = get(),
      hAnalyticsExperimentManager = get(),
      logInfo = get<LogInfoType>(logInfoQualifier),
    )
  }
  single<SendHAnalyticsEventUseCase> {
    // Workaround for https://github.com/InsertKoinIO/koin/issues/1146
    val allAnalyticsSinks = getAll<HAnalyticsSink>().distinct()
    SendHAnalyticsEventUseCaseImpl(allAnalyticsSinks)
  }
  single<HAnalyticsExperimentManager> {
    HAnalyticsExperimentManagerImpl(
      sendHAnalyticsEventUseCase = get(),
      hAnalyticsService = get(),
      logInfo = get<LogInfoType>(logInfoQualifier),
    )
  }
  single<NetworkHAnalyticsSink> { NetworkHAnalyticsSink(get(), get()) } bind HAnalyticsSink::class
  single<LoggingHAnalyticsSink> { LoggingHAnalyticsSink() } bind HAnalyticsSink::class
}

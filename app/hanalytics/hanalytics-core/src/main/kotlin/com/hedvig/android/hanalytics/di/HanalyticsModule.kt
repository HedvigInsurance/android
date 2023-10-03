package com.hedvig.android.hanalytics.di

import com.hedvig.android.hanalytics.HAnalyticsExperimentClearUseCase
import com.hedvig.android.hanalytics.HAnalyticsExperimentClearUseCaseImpl
import com.hedvig.android.hanalytics.HAnalyticsExperimentManager
import com.hedvig.android.hanalytics.HAnalyticsExperimentManagerImpl
import com.hedvig.android.hanalytics.HAnalyticsExperimentStorage
import com.hedvig.android.hanalytics.HAnalyticsExperimentStorageImpl
import com.hedvig.android.hanalytics.HAnalyticsImpl
import com.hedvig.android.hanalytics.HAnalyticsService
import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.android.hanalytics.SendHAnalyticsEventUseCase
import com.hedvig.android.hanalytics.SendHAnalyticsEventUseCaseImpl
import com.hedvig.android.hanalytics.sink.LoggingHAnalyticsSink
import com.hedvig.hanalytics.HAnalytics
import org.koin.dsl.bind
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val hAnalyticsModule = module {
  single<HAnalytics> {
    HAnalyticsImpl(
      sendHAnalyticsEventUseCase = get<SendHAnalyticsEventUseCase>(),
      hAnalyticsExperimentManager = get<HAnalyticsExperimentManager>(),
    )
  }
  single<SendHAnalyticsEventUseCase> {
    // Workaround for https://github.com/InsertKoinIO/koin/issues/1146
    val allAnalyticsSinks = getAll<HAnalyticsSink>().distinct()
    SendHAnalyticsEventUseCaseImpl(allAnalyticsSinks)
  }
  single<HAnalyticsExperimentManager> {
    HAnalyticsExperimentManagerImpl(
      hAnalyticsExperimentStorage = get<HAnalyticsExperimentStorage>(),
      hAnalyticsService = get<HAnalyticsService>(),
    )
  }
  single<HAnalyticsExperimentStorage> { HAnalyticsExperimentStorageImpl() }
  single<HAnalyticsExperimentClearUseCase> { HAnalyticsExperimentClearUseCaseImpl(get<HAnalyticsExperimentStorage>()) }
  single<LoggingHAnalyticsSink> { LoggingHAnalyticsSink() } bind HAnalyticsSink::class
}

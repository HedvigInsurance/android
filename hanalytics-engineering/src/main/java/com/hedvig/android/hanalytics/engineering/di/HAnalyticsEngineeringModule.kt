package com.hedvig.android.hanalytics.engineering.di

import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.android.hanalytics.engineering.tracking.TrackingLogViewModel
import com.hedvig.android.hanalytics.engineering.tracking.sink.DebugLogTrackerSink
import com.hedvig.android.hanalytics.engineering.tracking.sink.EngineeringTrackerSink
import com.hedvig.android.hanalytics.engineering.tracking.sink.ShakeTrackerSink
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

class HAnalyticsEngineeringModuleImpl : HAnalyticsEngineeringModule {
  override fun getModule() = hAnalyticsEngineeringModule
}

@Suppress("RemoveExplicitTypeArguments")
private val hAnalyticsEngineeringModule = module {
  viewModel<TrackingLogViewModel> { TrackingLogViewModel(get()) }
  single<EngineeringTrackerSink> { EngineeringTrackerSink() } bind HAnalyticsSink::class
  single<DebugLogTrackerSink> { DebugLogTrackerSink() } bind HAnalyticsSink::class
  single<ShakeTrackerSink> { ShakeTrackerSink() } bind HAnalyticsSink::class
}

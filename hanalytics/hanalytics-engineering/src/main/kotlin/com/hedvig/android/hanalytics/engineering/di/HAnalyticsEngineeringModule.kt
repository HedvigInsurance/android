package com.hedvig.android.hanalytics.engineering.di

import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.android.hanalytics.engineering.tracking.TrackingLogViewModel
import com.hedvig.android.hanalytics.engineering.tracking.sink.EngineeringTrackerSink
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

@Suppress("unused") // Used on non-release builds
class HAnalyticsEngineeringModuleImpl : HAnalyticsEngineeringModule {
  override fun getModule() = hAnalyticsEngineeringModule
}

@Suppress("RemoveExplicitTypeArguments")
private val hAnalyticsEngineeringModule = module {
  viewModel<TrackingLogViewModel> { TrackingLogViewModel(get()) }
  single<EngineeringTrackerSink> { EngineeringTrackerSink() } bind HAnalyticsSink::class
}

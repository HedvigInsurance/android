package com.hedvig.app.feature.tracking

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val trackingLogModule = module {
    viewModel { TrackingLogViewModel(get()) }
    single { EngineeringTracker() } bind HAnalyticsSink::class
    single { DebugLogTrackerSink() } bind HAnalyticsSink::class
    single { ShakeTrackerSink() } bind HAnalyticsSink::class
}

package com.hedvig.app.feature.tracking

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val trackingLogModule = module {
    viewModel { TrackingLogViewModel(get()) }
    single { EngineeringTracker() } bind TrackerSink::class
    single { DebugLogTrackerSink() } bind TrackerSink::class
}

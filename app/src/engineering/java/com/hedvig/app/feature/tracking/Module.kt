package com.hedvig.app.feature.tracking

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.dsl.single

val trackingLogModule = module {
    viewModel<TrackingLogViewModel>()
    single<EngineeringTracker>() bind TrackerSink::class
}

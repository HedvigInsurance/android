@file:Suppress("RemoveExplicitTypeArguments")

package com.hedvig.app.feature.tracking

import com.hedvig.app.feature.hanalytics.HAnalyticsSink
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val trackingLogModule = module {
    viewModel { TrackingLogViewModel(get()) }
    single<EngineeringTrackerSink> { EngineeringTrackerSink() } bind HAnalyticsSink::class
    single<DebugLogTrackerSink> { DebugLogTrackerSink() } bind HAnalyticsSink::class
    single<ShakeTrackerSink> { ShakeTrackerSink() } bind HAnalyticsSink::class
}

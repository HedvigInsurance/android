package com.hedvig.android.hanalytics.engineering.tracking

import androidx.lifecycle.ViewModel
import com.hedvig.android.hanalytics.engineering.tracking.sink.EngineeringTrackerSink

internal class TrackingLogViewModel(
  private val engineeringTrackerSink: EngineeringTrackerSink,
) : ViewModel() {
  val tracks = engineeringTrackerSink.tracks

  fun clear() = engineeringTrackerSink.clear()
}

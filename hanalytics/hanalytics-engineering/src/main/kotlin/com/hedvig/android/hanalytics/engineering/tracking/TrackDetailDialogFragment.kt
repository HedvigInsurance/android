package com.hedvig.android.hanalytics.engineering.tracking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.hedvig.android.core.ui.appbar.TopAppBarWithClose
import com.hedvig.android.hanalytics.engineering.R
import java.time.format.DateTimeFormatter

internal class TrackDetailDialogFragment : DialogFragment() {
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    val event = requireArguments().getParcelable<TrackEvent>(EVENT)
      ?: error("Missing EVENT in ${this.javaClass.name}")
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        DialogScreen(
          eventName = event.name,
          timestamp = event.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
          properties = event.propertiesJsonString,
          onBackClick = { dismiss() },
        )
      }
    }
  }

  companion object {
    private const val TAG = "TrackDetailFragment"
    private const val EVENT = "EVENT"
    fun newInstance(event: TrackEvent) = TrackDetailDialogFragment().apply {
      arguments = bundleOf(EVENT to event)
    }

    fun TrackDetailDialogFragment.show(fragmentManager: FragmentManager) = show(fragmentManager, TAG)
  }
}

@Composable
private fun DialogScreen(
  eventName: String,
  timestamp: String,
  properties: String?,
  onBackClick: () -> Unit,
) {
  Column {
    TopAppBarWithClose(
      onClick = onBackClick,
      title = "Event Detail",
    )
    DialogContent(eventName, timestamp, properties, Modifier.padding(16.dp))
  }
}

@Composable
private fun DialogContent(
  eventName: String,
  timestamp: String,
  properties: String?,
  modifier: Modifier = Modifier,
) {
  SelectionContainer {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
      InfoItem(stringResource(R.string.event_detail_name), eventName)
      InfoItem(stringResource(R.string.event_detail_timestamp), timestamp)
      InfoItem(
        stringResource(R.string.event_detail_properties),
        properties ?: stringResource(R.string.event_detail_properties_none),
      )
    }
  }
}

@Composable
private fun InfoItem(topText: String, bottomText: String) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = topText,
      style = MaterialTheme.typography.body1,
    )
    Text(
      text = bottomText,
      style = MaterialTheme.typography.subtitle1,
    )
  }
}

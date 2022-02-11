package com.hedvig.app.feature.tracking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.google.accompanist.insets.ui.TopAppBar
import com.hedvig.app.R
import com.hedvig.app.feature.tracking.TrackDetailFragment.Companion.show
import com.hedvig.app.ui.compose.theme.HedvigTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TrackingLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = getViewModel<TrackingLogViewModel>()

        setContent {
            val tracks by viewModel.tracks.collectAsState()
            HedvigTheme {
                TrackingLogScreen(
                    onNavigateUp = ::finish,
                    onClickEvent = ::openEventDetail,
                    tracks = tracks,
                )
            }
        }
    }

    private fun openEventDetail(event: TrackEvent) {
        TrackDetailFragment
            .newInstance(event)
            .show(supportFragmentManager)
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TrackingLogActivity::class.java)
    }
}

@Composable
fun TrackingLogScreen(
    onNavigateUp: () -> Unit,
    onClickEvent: (TrackEvent) -> Unit,
    tracks: List<TrackEvent>,
) {
    var dropdownOpen by rememberSaveable { mutableStateOf(false) }
    var showNotification by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        showNotification = context.trackingPreferences.data.first()[SHOULD_SHOW_NOTIFICATION] ?: false
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recorded Tracks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go Back",
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(onClick = { dropdownOpen = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                            DropdownMenuItem(
                                onClick = {
                                    coroutineScope.launch {
                                        context.trackingPreferences.edit { prefs ->
                                            prefs[SHOULD_SHOW_NOTIFICATION] = !showNotification
                                        }
                                        context.startService(
                                            TrackingShortcutService.newInstance(
                                                context,
                                                show = !showNotification
                                            )
                                        )
                                        showNotification = !showNotification
                                    }
                                }
                            ) {
                                Checkbox(checked = showNotification, onCheckedChange = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.tracking_show_notification))
                            }
                            Divider()
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
            )
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(tracks) { event ->
                Column(
                    modifier = Modifier
                        .clickable { onClickEvent(event) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.subtitle1,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.event_list_item_line_two,
                            event.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            if (event.propertiesJsonString != "{}") {
                                "Yes"
                            } else {
                                "No"
                            },
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TrackingLogScreenPreview() {
    HedvigTheme {
        TrackingLogScreen(
            onNavigateUp = {},
            onClickEvent = {},
            tracks = listOf(
                TrackEvent("example_event", "{}", LocalDateTime.now()),
                TrackEvent("example_event", "{}", LocalDateTime.now()),
                TrackEvent("example_event", "{}", LocalDateTime.now()),
                TrackEvent("example_event", "{}", LocalDateTime.now()),
            )
        )
    }
}

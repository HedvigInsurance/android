package com.hedvig.app.util.featureflags

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.app.BaseActivity
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FeatureFlagActivity : BaseActivity() {

    private val dataStore: DataStore<Preferences> by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HedvigTheme {
                Scaffold(
                    topBar = {
                        TopAppBarWithBack(
                            onClick = ::onBackPressed,
                            title = "Feature Manager"
                        )
                    },
                    modifier = Modifier.systemBarsPadding(top = true),
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        items(Feature.values()) { feature -> FeatureItem(feature) }
                    }
                }
            }
        }
    }

    @Composable
    fun FeatureItem(feature: Feature) {
        var isFeatureChecked by rememberSaveable { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            isFeatureChecked = dataStore.data.first()[booleanPreferencesKey(feature.name)] ?: false
        }

        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .width(300.dp),
                    )
                    Text(
                        text = feature.explanation,
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.width(300.dp)
                    )
                }
                Checkbox(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    checked = isFeatureChecked,
                    onCheckedChange = {
                        coroutineScope.launch {
                            dataStore.edit {
                                it[booleanPreferencesKey(feature.name)] = !isFeatureChecked
                                isFeatureChecked = !isFeatureChecked
                            }
                        }
                    }
                )
            }
        }
    }
}

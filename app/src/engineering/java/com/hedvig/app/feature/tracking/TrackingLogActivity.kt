package com.hedvig.app.feature.tracking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.checkbox.MaterialCheckBox
import com.hedvig.app.R
import com.hedvig.app.databinding.TrackingLogActivityBinding
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackingLogActivity : AppCompatActivity(R.layout.tracking_log_activity) {
    private val binding by viewBinding(TrackingLogActivityBinding::bind)
    private val model: TrackingLogViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
            val showNotification = toolbar.menu.findItem(R.id.showNotification).actionView as MaterialCheckBox
            showNotification.setText(R.string.tracking_show_notification)

            lifecycleScope.launch {
                val shouldShowNotification = trackingPreferences.data.first()[SHOULD_SHOW_NOTIFICATION] ?: false
                showNotification.isChecked = shouldShowNotification
                showNotification
                    .setOnCheckedChangeListener { _, isChecked ->
                        lifecycleScope.launch {
                            trackingPreferences.edit { prefs ->
                                prefs[SHOULD_SHOW_NOTIFICATION] = isChecked
                            }
                            this@TrackingLogActivity.startService(
                                TrackingShortcutService.newInstance(
                                    this@TrackingLogActivity,
                                    show = isChecked
                                )
                            )
                        }
                    }
            }

            val adapter = TrackingLogAdapter(supportFragmentManager)
            recycler.adapter = adapter

            model
                .tracks
                .flowWithLifecycle(lifecycle)
                .onEach(adapter::submitList)
                .launchIn(lifecycleScope)
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TrackingLogActivity::class.java)
    }
}

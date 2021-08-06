package com.hedvig.app.feature.tracking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.R
import com.hedvig.app.databinding.TrackingLogActivityBinding
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackingLogActivity : AppCompatActivity(R.layout.tracking_log_activity) {
    private val binding by viewBinding(TrackingLogActivityBinding::bind)
    private val model: TrackingLogViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = TrackingLogAdapter(supportFragmentManager)
        binding.recycler.adapter = adapter

        model
            .tracks
            .flowWithLifecycle(lifecycle)
            .onEach(adapter::submitList)
            .launchIn(lifecycleScope)
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TrackingLogActivity::class.java)
    }
}

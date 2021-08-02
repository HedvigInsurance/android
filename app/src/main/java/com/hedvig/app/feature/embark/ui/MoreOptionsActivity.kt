package com.hedvig.app.feature.embark.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityMoreOptionsBinding
import com.hedvig.app.feature.onboarding.MemberIdViewModel
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreOptionsActivity : BaseActivity(R.layout.activity_more_options) {
    private val binding by viewBinding(ActivityMoreOptionsBinding::bind)
    private val model: MemberIdViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            toolbar.applyStatusBarInsets()

            setSupportActionBar(toolbar)
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            val adapter = MoreOptionsAdapter(model)
            recycler.adapter = adapter

            model
                .state
                .flowWithLifecycle(lifecycle)
                .onEach { state ->
                    when (state) {
                        MemberIdViewModel.State.Error -> {
                            adapter.submitList(
                                listOf(
                                    MoreOptionsModel.Header,
                                    MoreOptionsModel.UserId.Error,
                                    MoreOptionsModel.Version,
                                    MoreOptionsModel.Copyright
                                )
                            )
                        }
                        MemberIdViewModel.State.Loading -> {
                        }
                        is MemberIdViewModel.State.Success -> {
                            adapter.submitList(
                                listOf(
                                    MoreOptionsModel.Header,
                                    MoreOptionsModel.UserId.Success(state.id),
                                    MoreOptionsModel.Version,
                                    MoreOptionsModel.Copyright,
                                )
                            )
                        }
                    }
                }
                .launchIn(lifecycleScope)
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, MoreOptionsActivity::class.java)
    }
}

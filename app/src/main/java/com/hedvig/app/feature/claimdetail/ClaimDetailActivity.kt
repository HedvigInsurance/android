package com.hedvig.app.feature.claimdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClaimDetailActivity : BaseActivity() {
    private val viewModel: ClaimDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getParcelableExtra<ClaimDetailParameter>(PARAMETER)
            ?: throw IllegalArgumentException("Programmer error: Missing DATA in ${this.javaClass.name}")

        viewModel
            .events
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    ClaimDetailViewModel.Event.Chat -> startActivity(ChatActivity.newInstance(this))
                }
            }
            .launchIn(lifecycleScope)

        setContent {
            HedvigTheme {
                ClaimDetailScreen(
                    onUpClick = ::finish,
                    onChatClick = viewModel::onChatClick,
                    data = data.toClaimDetailData(this),
                )
            }
        }
    }

    companion object {
        private const val PARAMETER = "PARAMETER"
        fun newInstance(
            context: Context,
            parameter: ClaimDetailParameter,
        ) = Intent(context, ClaimDetailActivity::class.java).apply {
            putExtra(PARAMETER, parameter)
        }
    }
}

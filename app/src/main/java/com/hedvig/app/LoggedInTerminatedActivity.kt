package com.hedvig.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.databinding.ActivityLoggedInTerminatedBinding
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.terminated.TerminatedTracker
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class LoggedInTerminatedActivity : BaseActivity(R.layout.activity_logged_in_terminated) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val tracker: TerminatedTracker by inject()
    private val binding by viewBinding(ActivityLoggedInTerminatedBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.terminatedOpenChatButton.setHapticClickListener {
            tracker.openChat()
            lifecycleScope.launch {
                claimsViewModel.triggerFreeTextChat()
                val intent = Intent(this@LoggedInTerminatedActivity, ChatActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        fun newInstance(context: Context) =
            Intent(context, LoggedInTerminatedActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
    }
}

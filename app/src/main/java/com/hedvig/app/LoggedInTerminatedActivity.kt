package com.hedvig.app

import android.content.Intent
import android.os.Bundle
import com.hedvig.app.feature.chat.ui.ChatActivity
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.terminated.TerminatedTracker
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.logged_in_terminated_activity.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class LoggedInTerminatedActivity : BaseActivity() {
    private val claimsViewModel: ClaimsViewModel by viewModel()

    private val tracker: TerminatedTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logged_in_terminated_activity)

        terminatedOpenChatButton.setHapticClickListener {
            tracker.openChat()
            claimsViewModel.triggerFreeTextChat {
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
            }
        }
    }
}

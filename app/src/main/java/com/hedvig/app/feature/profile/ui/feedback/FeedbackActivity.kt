package com.hedvig.app.feature.profile.ui.feedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityFeedbackBinding
import com.hedvig.app.feature.ratings.openPlayStore
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags

class FeedbackActivity : BaseActivity(R.layout.activity_feedback) {
    private val binding by viewBinding(ActivityFeedbackBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)

            setupToolbar(R.id.toolbar, R.drawable.ic_back, true, root) {
                onBackPressed()
            }

            bugReportEmail.setOnClickListener {
                startActivity(Intent(Intent.ACTION_SENDTO).apply {
                    data =
                        Uri.parse("mailto:${getString(R.string.bug_report_mail)}?subject=Buggrapport")
                })
            }

            playLink.setOnClickListener {
                openPlayStore()
            }
        }
    }
}

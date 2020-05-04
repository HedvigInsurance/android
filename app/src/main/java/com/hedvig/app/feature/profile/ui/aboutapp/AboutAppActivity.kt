package com.hedvig.app.feature.profile.ui.aboutapp

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.useEdgeToEdge
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_about_app.*
import org.koin.android.viewmodel.ext.android.viewModel

class AboutAppActivity : BaseActivity(R.layout.activity_about_app) {

    private val profileViewModel: ProfileViewModel by viewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        root.useEdgeToEdge()
        root.setEdgeToEdgeSystemUiFlags(true)

        setupToolbar(R.id.toolbar, R.drawable.ic_close, true, root) {
            onBackPressed()
        }

        licenseAttributions.setOnClickListener {
            startActivity(Intent(this, LicensesActivity::class.java))
        }

        whatsNewViewModel.fetchNews(NEWS_BASE_VERSION)
        whatsNewViewModel.news.observe(this) { data ->
            data?.let {
                whatsNew.show()
                whatsNew.setOnClickListener {
                    WhatsNewDialog.newInstance(data.news)
                        .show(supportFragmentManager, WhatsNewDialog.TAG)
                }
            }
        }

        versionNumber.text =
            resources.getString(R.string.PROFILE_ABOUT_APP_VERSION, BuildConfig.VERSION_NAME)

        profileViewModel.data.observe(this, Observer { data ->
            data?.member?.id?.let { id ->
                memberId.text = resources.getString(R.string.PROFILE_ABOUT_APP_MEMBER_ID, id)
            }
        })
    }

    companion object {
        private const val NEWS_BASE_VERSION = "3.0.0"
    }
}

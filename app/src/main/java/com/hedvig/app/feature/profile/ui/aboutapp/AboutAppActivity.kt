package com.hedvig.app.feature.profile.ui.aboutapp

import androidx.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.fragment_about_app.*
import org.koin.android.viewmodel.ext.android.viewModel

class AboutAppActivity : BaseActivity() {

    private val profileViewModel: ProfileViewModel by viewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_about_app)

        setupLargeTitle(R.string.PROFILE_ABOUT_APP_TITLE, R.font.circular_bold, R.drawable.ic_back) {
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
                    WhatsNewDialog.newInstance(data.news).show(supportFragmentManager, WhatsNewDialog.TAG)
                }
            }
        }

        versionNumber.text = interpolateTextKey(
            resources.getString(R.string.PROFILE_ABOUT_APP_VERSION),
            "VERSION_NUMBER" to BuildConfig.VERSION_NAME
        )

        profileViewModel.data.observe(this, Observer { data ->
            data?.member?.id?.let { id ->
                memberId.text = interpolateTextKey(
                    resources.getString(R.string.PROFILE_ABOUT_APP_MEMBER_ID),
                    "MEMBER_ID" to id
                )
            }
        })
    }

    companion object {
        private const val NEWS_BASE_VERSION = "2.8.1"
    }
}

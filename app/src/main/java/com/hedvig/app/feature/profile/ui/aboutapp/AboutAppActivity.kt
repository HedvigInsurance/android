package com.hedvig.app.feature.profile.ui.aboutapp

import android.content.Intent
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityAboutAppBinding
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.extensions.setupToolbar
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.android.viewmodel.ext.android.viewModel

class AboutAppActivity : BaseActivity(R.layout.activity_about_app) {
    private val binding by viewBinding(ActivityAboutAppBinding::bind)
    private val profileViewModel: ProfileViewModel by viewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)

            setupToolbar(R.id.toolbar, R.drawable.ic_back, true, root) {
                onBackPressed()
            }

            licenseAttributions.setOnClickListener {
                startActivity(Intent(this@AboutAppActivity, LicensesActivity::class.java))
            }

            whatsNewViewModel.fetchNews(NEWS_BASE_VERSION)
            whatsNewViewModel.news.observe(this@AboutAppActivity) { data ->
                whatsNew.show()
                whatsNew.setOnClickListener {
                    WhatsNewDialog.newInstance(
                        data.news.mapIndexed { index, page ->
                            DismissiblePagerModel.TitlePage(
                                ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                                page.title,
                                page.paragraph,
                                getString(
                                    if (index == data.news.size - 1) {
                                        R.string.NEWS_DISMISS
                                    } else {
                                        R.string.NEWS_PROCEED
                                    }
                                )
                            )
                        }
                    )
                        .show(supportFragmentManager, WhatsNewDialog.TAG)
                }
            }

            versionNumber.text =
                resources.getString(R.string.PROFILE_ABOUT_APP_VERSION, BuildConfig.VERSION_NAME)

            profileViewModel.data.observe(this@AboutAppActivity) { data ->
                data.member.id?.let { id ->
                    memberId.text = resources.getString(R.string.PROFILE_ABOUT_APP_MEMBER_ID, id)
                }
            }
        }
    }

    companion object {
        private const val NEWS_BASE_VERSION = "3.0.0"
    }
}

package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.commonclaim.bulletpoint.BulletPointsAdapter
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.util.darkenColor
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.disable
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.lightenColor
import com.hedvig.app.util.mappedColor
import com.hedvig.app.util.svg.buildRequestBuilder
import kotlinx.android.synthetic.main.activity_common_claim.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.common_claim_first_message.*
import org.koin.android.ext.android.inject

class CommonClaimActivity : BaseActivity() {

    private val requestBuilder by lazy { buildRequestBuilder() }
    private val tracker: ClaimsTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_claim)

        val data = intent.getParcelableExtra<CommonClaimsData>(CLAIMS_DATA) ?: return

        val backgroundColor = if (isDarkThemeActive) {
            darkenColor(compatColor(data.color.mappedColor()), 0.3f)
        } else {
            lightenColor(compatColor(data.color.mappedColor()), 0.3f)
        }
        setupLargeTitle(data.title, R.font.circular_bold, R.drawable.ic_back, backgroundColor) {
            onBackPressed()
        }
        appBarLayout.setExpanded(false, false)

        requestBuilder
            .load(Uri.parse(BuildConfig.BASE_URL + data.iconUrls.iconByTheme(this)))
            .into(commonClaimFirstMessageIcon)

        commonClaimFirstMessageContainer.setBackgroundColor(backgroundColor)
        commonClaimFirstMessage.text = data.layoutTitle
        commonClaimCreateClaimButton.text = data.buttonText
        when (data.insuranceStatus) {
            InsuranceStatus.ACTIVE -> {
                commonClaimCreateClaimButton.enable()
                commonClaimCreateClaimButton.setHapticClickListener {
                    tracker.createClaimClick(data.title)
                    HonestyPledgeBottomSheet
                        .newInstance(data.title)
                        .show(supportFragmentManager, HonestyPledgeBottomSheet.TAG)
                }
            }
            else -> {
                commonClaimCreateClaimButton.disable()
            }
        }

        bulletPointsRecyclerView.adapter =
            BulletPointsAdapter(
                data.bulletPoints,
                BuildConfig.BASE_URL,
                requestBuilder
            )
    }

    companion object {
        private const val CLAIMS_DATA = "claims_data"

        fun newInstance(context: Context, data: CommonClaimsData) =
            Intent(context, CommonClaimActivity::class.java).apply {
                putExtra(CLAIMS_DATA, data)
            }
    }
}

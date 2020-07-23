package com.hedvig.app.feature.claims.ui.commonclaim

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.commonclaim.bulletpoint.BulletPointsAdapter
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.util.extensions.view.disable
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.setupToolbarScrollListener
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.svg.buildRequestBuilder
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_common_claim.*
import kotlinx.android.synthetic.main.common_claim_first_message.*
import org.koin.android.ext.android.inject

class CommonClaimActivity : BaseActivity(R.layout.activity_common_claim) {

    private val requestBuilder by lazy { buildRequestBuilder() }
    private val tracker: ClaimsTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.getParcelableExtra<CommonClaimsData>(CLAIMS_DATA) ?: return

        commonClaimTitle.text = data.title
        root.setEdgeToEdgeSystemUiFlags(true)
        root.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
        }
        toolbar.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        root.setupToolbarScrollListener(toolbar = toolbar)


        requestBuilder
            .load(Uri.parse(BuildConfig.BASE_URL + data.iconUrls.iconByTheme(this)))
            .into(commonClaimFirstMessageIcon)

        commonClaimFirstMessage.text = data.layoutTitle
        commonClaimCreateClaimButton.text = data.buttonText
        if (data.eligibleToClaim) {
            commonClaimCreateClaimButton.enable()
            commonClaimCreateClaimButton.setHapticClickListener {
                HonestyPledgeBottomSheet
                    .newInstance(data.title)
                    .show(supportFragmentManager, HonestyPledgeBottomSheet.TAG)
            }
        } else {
            commonClaimCreateClaimButton.disable()
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

package com.hedvig.app.feature.referrals.ui.activated

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ReferralsTracker
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_referrals_activated.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ReferralsActivatedActivity : BaseActivity(R.layout.activity_referrals_activated) {
    private val model: ReferralsActivatedViewModel by viewModel()
    private val tracker: ReferralsTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        close.measure(
            View.MeasureSpec.makeMeasureSpec(root.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(root.height, View.MeasureSpec.UNSPECIFIED)
        )

        scrollView.updatePadding(bottom = scrollView.paddingBottom + close.measuredHeight)
        root.setEdgeToEdgeSystemUiFlags(true)

        scrollView.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom,
                top = initialState.paddings.top + insets.systemWindowInsetTop
            )
        }

        close.doOnApplyWindowInsets { view, insets, initialState ->
            view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
        }

        close.setHapticClickListener {
            tracker.closeActivated()
            finish()
        }

        model.data.observe(this) { data ->
            data?.referralInformation?.campaign?.incentive?.asMonthlyCostDeduction?.amount?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                ?.let { incentive ->
                    body.show()
                    body.text =
                        getString(R.string.referrals_intro_screen_body, incentive.format(this))
                    body
                        .animate()
                        .alpha(1f)
                        .setDuration(CROSSFADE_DURATION)
                        .start()
                    bodyPlaceholder
                        .animate()
                        .alpha(0f)
                        .setDuration(CROSSFADE_DURATION)
                        .withEndAction { bodyPlaceholder.remove() }
                        .start()
                }
        }
    }

    companion object {
        private const val CROSSFADE_DURATION = 350L
        fun newInstance(context: Context) = Intent(context, ReferralsActivatedActivity::class.java)
    }
}

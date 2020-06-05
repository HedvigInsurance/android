package com.hedvig.app.feature.referrals.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_referrals_activated.*

class ReferralsActivatedActivity : BaseActivity(R.layout.activity_referrals_activated) {
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
            finish()
        }
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, ReferralsActivatedActivity::class.java)
    }
}

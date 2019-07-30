package com.hedvig.app.feature.claims.ui.commonclaim

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.commonclaim.bulletpoint.BulletPointsAdapter
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.disable
import com.hedvig.app.util.extensions.view.enable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.lightenColor
import com.hedvig.app.util.mappedColor
import kotlinx.android.synthetic.main.common_claim_first_message.*
import kotlinx.android.synthetic.main.fragment_common_claim.*

class CommonClaimFragment : BaseCommonClaimFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_common_claim, container, false)

    override fun bindData(insuranceStatus: InsuranceStatus, data: CommonClaimQuery.CommonClaim) {
        super.bindData(insuranceStatus, data)
        val layout = data.layout as? CommonClaimQuery.AsTitleAndBulletPoints ?: return
        val backgroundColor = lightenColor(requireContext().compatColor(layout.color.mappedColor()), 0.3f)
        setupLargeTitle(data.title, R.font.circular_bold, R.drawable.ic_back, backgroundColor) {
            navController.popBackStack()
        }

        commonClaimFirstMessageContainer.setBackgroundColor(backgroundColor)

        commonClaimFirstMessage.text = layout.title
        commonClaimCreateClaimButton.text = layout.buttonTitle
        when (insuranceStatus) {
            InsuranceStatus.ACTIVE -> {
                commonClaimCreateClaimButton.enable()
                commonClaimCreateClaimButton.setHapticClickListener {
                    tracker.createClaimClick(data.title)
                    HonestyPledgeBottomSheet
                        .newInstance(data.title)
                        .show(childFragmentManager, "honestyPledge")
                }
            }
            else -> {
                commonClaimCreateClaimButton.disable()
            }
        }


        bulletPointsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        bulletPointsRecyclerView.adapter =
            BulletPointsAdapter(
                layout.bulletPoints,
                BuildConfig.BASE_URL,
                requestBuilder
            )
    }
}

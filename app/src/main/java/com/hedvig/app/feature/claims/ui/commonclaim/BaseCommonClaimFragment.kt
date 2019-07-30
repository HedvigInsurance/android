package com.hedvig.app.feature.claims.ui.commonclaim

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.findNavController
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.graphql.CommonClaimQuery
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.claims.service.ClaimsTracker
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.svg.buildRequestBuilder
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.common_claim_first_message.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

abstract class BaseCommonClaimFragment : androidx.fragment.app.Fragment() {
    val tracker: ClaimsTracker by inject()
    val claimsViewModel: ClaimsViewModel by sharedViewModel()

    val requestBuilder: RequestBuilder<PictureDrawable> by lazy { buildRequestBuilder() }

    val navController by lazy { requireActivity().findNavController(R.id.loggedNavigationHost) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        claimsViewModel.selectedSubViewData.observe(this) { commonClaim ->
            claimsViewModel.data.value?.insurance?.status?.let { insuranceStatus ->
                commonClaim?.let { bindData(insuranceStatus, it) }
            }
        }
    }

    @CallSuper
    open fun bindData(insuranceStatus: InsuranceStatus, data: CommonClaimQuery.CommonClaim) {
        appBarLayout.setExpanded(false, false)
        requestBuilder.load(Uri.parse(BuildConfig.BASE_URL + data.icon.svgUrl)).into(commonClaimFirstMessageIcon)
    }
}



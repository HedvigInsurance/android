package com.hedvig.app.feature.welcome

import android.os.Bundle
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class WelcomeDialog : DismissiblePager() {

    override val proceedLabel = R.string.NEWS_PROCEED
    override val lastButtonText = R.string.NEWS_DISMISS
    override val animationStyle = R.style.WelcomeDialogAnimation
    override val titleLabel: Nothing? = null
    override val shouldShowLogo = true

    override val tracker: WelcomeTracker by inject()
    override val items: List<DismissiblePagerModel>
        get() = requireArguments().getParcelableArrayList<DismissiblePagerModel>(ITEMS).orEmpty()

    private lateinit var reviewManager: ReviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewManager = ReviewManagerFactory.create(requireContext())
    }

    override fun onDismiss() {
        startReviewRequest()
    }

    override fun onLastSwipe() {
        dismiss()
        startReviewRequest()
    }

    override fun onLastPageButton() {
        dismiss()
        startReviewRequest()
    }

    private fun startReviewRequest() {
        reviewManager
            .requestReviewFlow()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    startReviewFlow(it.result)
                } else {
                    Timber.e(it.exception, "Review request not successful")
                }
            }
            .addOnFailureListener {
                Timber.e(it, "Review request failed")
            }
    }

    private fun startReviewFlow(reviewInfo: ReviewInfo) {
        val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo)
        flow.addOnCompleteListener { _ ->
            // The flow has finished. The API does not indicate whether the user
            // reviewed or not, or even whether the review dialog was shown. Thus, no
            // matter the result, we continue our app flow.
        }
    }

    companion object {
        const val TAG = "WelcomeDialog"
        private const val ITEMS = "items"

        fun newInstance(items: List<DismissiblePagerModel>) = WelcomeDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(ITEMS, ArrayList(items + DismissiblePagerModel.SwipeOffScreen))
            }
        }
    }
}

package com.hedvig.app.feature.whatsnew

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.dismissablepager.DismissablePager
import com.hedvig.app.feature.dismissablepager.DismissablePagerPage
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.screenWidth
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.fragment_dismissable_pager.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class WhatsNewDialog : DismissablePager() {
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()

    override val proceedLabel = R.string.NEWS_PROCEED
    override val dismissLabel = R.string.NEWS_DISMISS
    override val animationStyle = R.style.WhatsNewDialogAnimation
    override val titleLabel = R.string.NEWS_TITLE

    override val tracker: WhatsNewTracker by inject()
    override val items: List<DismissablePagerPage> by lazy {
        arguments!!.getParcelableArrayList<DismissablePagerPage>(PAGES) // Enforced by newInstance()
    }

    override fun onDismiss() {
        whatsNewViewModel.hasSeenNews(BuildConfig.VERSION_NAME)
    }

    companion object {
        const val TAG = "whats_new_dialog"

        private const val PAGES = "pages"

        fun newInstance(pages: List<WhatsNewQuery.News>) = WhatsNewDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(
                    PAGES,
                    ArrayList(pages.map {
                        DismissablePagerPage(
                            it.illustration.svgUrl,
                            it.title,
                            it.paragraph
                        )
                    })
                )
            }
        }
    }
}

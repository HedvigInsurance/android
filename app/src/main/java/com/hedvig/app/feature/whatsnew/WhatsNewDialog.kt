package com.hedvig.app.feature.whatsnew

import android.app.Application
import android.os.Bundle
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.util.apollo.ThemedIconUrls
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class WhatsNewDialog : DismissiblePager() {
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()

    override val proceedLabel = R.string.NEWS_PROCEED
    override val lastButtonText = R.string.NEWS_DISMISS
    override val animationStyle = R.style.DialogSlideInSlideOut
    override val titleLabel = R.string.NEWS_TITLE
    override val shouldShowLogo = true

    override val tracker: WhatsNewTracker by inject()
    override val items: List<DismissiblePagerModel>
        get() = requireArguments().getParcelableArrayList<DismissiblePagerModel>(PAGES).orEmpty()

    override fun onDismiss() {
        whatsNewViewModel.hasSeenNews(BuildConfig.VERSION_NAME)
    }

    override fun onLastSwipe() {
        dismiss()
        whatsNewViewModel.hasSeenNews(BuildConfig.VERSION_NAME)
    }

    override fun onLastPageButton() {
        dismiss()
        whatsNewViewModel.hasSeenNews(BuildConfig.VERSION_NAME)
    }

    companion object {
        const val TAG = "whats_new_dialog"

        private const val PAGES = "pages"

        fun newInstance(pages: List<WhatsNewQuery.New>) = WhatsNewDialog().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(
                    PAGES,
                    ArrayList(pages.mapIndexed { index, page ->
                        if (index == items.size - 1) {
                            DismissiblePagerModel.TitlePage(
                                ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                                page.title,
                                page.paragraph,
                                getString(R.string.NEWS_DISMISS)
                            )
                        } else {
                            DismissiblePagerModel.TitlePage(
                                ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                                page.title,
                                page.paragraph,
                                getString(R.string.NEWS_PROCEED)
                            )
                        }
                    } + DismissiblePagerModel.SwipeOffScreen)
                )
            }
        }
    }
}

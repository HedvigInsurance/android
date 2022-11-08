package com.hedvig.app.feature.whatsnew

import android.os.Bundle
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhatsNewDialog : DismissiblePager() {
  private val whatsNewViewModel: WhatsNewViewModel by viewModel()

  override val proceedLabel = hedvig.resources.R.string.NEWS_PROCEED
  override val lastButtonText = hedvig.resources.R.string.NEWS_DISMISS
  override val animationStyle = R.style.DialogSlideInSlideOut
  override val titleLabel = hedvig.resources.R.string.NEWS_TITLE
  override val shouldShowLogo = true

  override val items: List<DismissiblePagerModel>
    get() = requireArguments().getParcelableArrayList<DismissiblePagerModel>(PAGES).orEmpty()

  override fun onDismiss() {
    whatsNewViewModel.hasSeenNews(BuildConfig.VERSION_NAME)
    super.onDismiss()
  }

  override fun onLastSwipe() {
    whatsNewViewModel.hasSeenNews(BuildConfig.VERSION_NAME)
    super.onLastSwipe()
  }

  override fun onLastPageButton() {
    whatsNewViewModel.hasSeenNews(BuildConfig.VERSION_NAME)
    dismiss()
  }

  companion object {
    const val TAG = "whats_new_dialog"

    private const val PAGES = "pages"

    fun newInstance(pages: List<DismissiblePagerModel>) = WhatsNewDialog().apply {
      arguments = Bundle().apply {
        putParcelableArrayList(
          PAGES,
          ArrayList(pages + DismissiblePagerModel.SwipeOffScreen),
        )
      }
    }
  }
}

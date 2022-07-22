package com.hedvig.app.feature.welcome

import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePager
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel

class WelcomeDialog : DismissiblePager() {

  override val proceedLabel = hedvig.resources.R.string.NEWS_PROCEED
  override val lastButtonText = hedvig.resources.R.string.NEWS_DISMISS
  override val animationStyle = R.style.WelcomeDialogAnimation
  override val titleLabel: Nothing? = null
  override val shouldShowLogo = true

  override val items: List<DismissiblePagerModel>
    get() = requireArguments().getParcelableArrayList<DismissiblePagerModel>(ITEMS).orEmpty()

  override fun onLastPageButton() {
    dismiss()
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

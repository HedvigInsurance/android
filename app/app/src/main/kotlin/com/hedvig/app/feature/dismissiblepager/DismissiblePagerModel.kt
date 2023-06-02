package com.hedvig.app.feature.dismissiblepager

import android.os.Parcelable
import com.hedvig.android.core.common.android.ThemedIconUrls
import kotlinx.parcelize.Parcelize

sealed class DismissiblePagerModel : Parcelable {
  @Parcelize
  data class TitlePage(
    val imageUrls: ThemedIconUrls,
    val title: String,
    val paragraph: String,
    val buttonText: String,
  ) : DismissiblePagerModel()

  @Parcelize
  data class NoTitlePage(
    val imageUrls: ThemedIconUrls,
    val paragraph: String,
    val buttonText: String,
  ) : DismissiblePagerModel()

  @Parcelize
  object SwipeOffScreen : DismissiblePagerModel()
}

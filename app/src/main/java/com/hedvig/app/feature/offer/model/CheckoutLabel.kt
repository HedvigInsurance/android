package com.hedvig.app.feature.offer.model

import android.content.Context
import androidx.annotation.StringRes
import com.hedvig.app.R

enum class CheckoutLabel(@StringRes val resId: Int) {
  SIGN_UP(hedvig.resources.R.string.OFFER_SIGN_BUTTON),
  CONTINUE(hedvig.resources.R.string.OFFER_CHECKOUT_BUTTON),
  APPROVE(hedvig.resources.R.string.OFFER_APPROVE_CHANGES),
  CONFIRM(hedvig.resources.R.string.OFFER_CONFIRM_PURCHASE),
  UNKNOWN(hedvig.resources.R.string.dummy_string);

  fun toString(context: Context) = context.getString(resId)

  fun localizationKey(context: Context): String = context.resources.getResourceEntryName(resId)
}

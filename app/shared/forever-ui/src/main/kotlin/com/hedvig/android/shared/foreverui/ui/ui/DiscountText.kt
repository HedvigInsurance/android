package com.hedvig.android.shared.foreverui.ui.ui

import com.hedvig.android.core.uidata.UiMoney

/**
 * A discount as money taken off the price, so a member with no discount sees "0 kr" rather than "-0 kr".
 */
internal fun UiMoney.toDiscountText(): String = if (amount > 0) "-$this" else toString()

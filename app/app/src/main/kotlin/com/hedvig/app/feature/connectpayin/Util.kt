package com.hedvig.app.feature.connectpayin

import android.content.Context
import com.hedvig.app.util.extensions.showAlert

fun showConfirmCloseDialog(
  context: Context,
  connectPayinType: ConnectPayinType,
  close: () -> Unit,
) = context.showAlert(
  title = hedvig.resources.R.string.pay_in_iframe_post_sign_skip_alert_title,
  message = when (connectPayinType) {
    ConnectPayinType.ADYEN -> hedvig.resources.R.string.pay_in_iframe_post_sign_skip_alert_body
    ConnectPayinType.TRUSTLY -> hedvig.resources.R.string.pay_in_iframe_in_app_skip_alert_direct_debit_body
  },
  positiveLabel = hedvig.resources.R.string.pay_in_iframe_post_sign_skip_alert_proceed_button,
  negativeLabel = hedvig.resources.R.string.pay_in_iframe_post_sign_skip_alert_dismiss_button,
  positiveAction = {
    close()
  },
)

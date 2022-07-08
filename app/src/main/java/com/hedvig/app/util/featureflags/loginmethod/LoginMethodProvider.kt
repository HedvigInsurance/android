package com.hedvig.app.util.featureflags.loginmethod

import com.hedvig.hanalytics.LoginMethod

interface LoginMethodProvider {
  suspend fun getLoginMethod(): LoginMethod?
}

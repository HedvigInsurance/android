package com.hedvig.android.hanalytics.featureflags.loginmethod

import com.hedvig.hanalytics.LoginMethod

interface LoginMethodProvider {
  suspend fun getLoginMethod(): LoginMethod
}

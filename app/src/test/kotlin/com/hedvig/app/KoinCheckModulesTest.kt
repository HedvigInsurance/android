package com.hedvig.app

import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class KoinCheckModulesTest : KoinTest {
  @OptIn(KoinExperimentalAPI::class)
  @Test
  fun checkAllModules() {
    applicationModule.verify()
  }
}

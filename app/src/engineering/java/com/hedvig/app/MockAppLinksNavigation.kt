package com.hedvig.app

object MockOnboarding: AppLink() {
    override val path: String = "embarkmock"
    override val query: Pair<String, String>? = null
}

object MockEmbark: AppLink() {
    override val path: String = "onboardingmock"
    override val query: Pair<String, String>? = null
}

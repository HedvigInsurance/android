package com.hedvig.app

class TestApplication : HedvigApplication() {
    override val graphqlUrl = "http://localhost:$PORT/"
    override val graphqlSubscriptionUrl = "http://localhost:$PORT/"
    override val isTestBuild = true

    companion object {
        const val PORT = 8080
    }
}

package com.hedvig.app

import android.app.Activity
import android.content.Intent
import android.net.Uri

abstract class AppLink {
    abstract val path: String
    abstract val query: Pair<String, String>?
}

class Onboarding(storyName: String) : AppLink() {
    override val path = "onboarding"
    override val query = Pair("storyName", storyName)
}

private const val DEFAULT_URL = "https://instantapptest.dev.hedvigit.com"

fun Activity.startActivityWithAppLink(appLink: AppLink) {
    val uri = getUri(appLink)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.addCategory(Intent.CATEGORY_BROWSABLE)
    startActivity(intent)
}

private fun getUri(appLink: AppLink): Uri {
    return appLink.query?.let {
        Uri.parse("$DEFAULT_URL/${appLink.path}?${it.first}=${it.second}")
    } ?: Uri.parse("$DEFAULT_URL/${appLink.path}")
}

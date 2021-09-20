package com.hedvig.app.service

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import kotlinx.coroutines.suspendCancellableCoroutine

sealed class DynamicLink {

    data class Referrals(
        val code: String,
        val incentive: String
    ) : DynamicLink()

    object DirectDebit : DynamicLink()
    object Forever : DynamicLink()
    object Insurance : DynamicLink()
    object None : DynamicLink()
    object Unknown : DynamicLink()
}

suspend fun getDynamicLinkFromFirebase(intent: Intent): DynamicLink = suspendCancellableCoroutine {
    FirebaseDynamicLinks.getInstance()
        .getDynamicLink(intent)
        .addOnSuccessListener { createDynamicLinkFromUri(it.link) }
        .addOnFailureListener { DynamicLink.None }
}

fun createDynamicLinkFromUri(uri: Uri?): DynamicLink {
    return if (uri != null) {
        when (uri.path) {
            "referrals" -> DynamicLink.Referrals(
                code = uri.getQueryParameter("code") ?: "",
                // Fixme "10" should not be hard coded
                incentive = "10"
            )
            "direct-debit" -> DynamicLink.DirectDebit
            "forever" -> DynamicLink.Forever
            "insurance" -> DynamicLink.Insurance
            null -> DynamicLink.None
            else -> DynamicLink.Unknown
        }
    } else {
        DynamicLink.None
    }
}

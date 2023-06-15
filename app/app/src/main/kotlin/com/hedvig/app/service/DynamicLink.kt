package com.hedvig.app.service

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

sealed class DynamicLink(
  val type: String,
) {

  data class Referrals(
    val code: String,
    val incentive: String,
  ) : DynamicLink("referrals")

  object DirectDebit : DynamicLink("direct-debit")

  object Forever : DynamicLink("forever")
  object Insurance : DynamicLink("insurances")
  object None : DynamicLink("none")
  object Unknown : DynamicLink("unknown")
}

suspend fun getDynamicLinkFromFirebase(intent: Intent): DynamicLink = suspendCancellableCoroutine { cont ->
  FirebaseDynamicLinks.getInstance()
    .getDynamicLink(intent)
    .addOnSuccessListener { linkData: PendingDynamicLinkData? ->
      // This can be null despite the "@RecentlyNonNull" annotation in addOnSuccessListener
      if (linkData != null) {
        val link = createDynamicLinkFromUri(linkData.link)
        cont.resume(link)
      } else {
        cont.resume(DynamicLink.None)
      }
    }
    .addOnFailureListener { cont.resume(DynamicLink.None) }
}

fun createDynamicLinkFromUri(uri: Uri?): DynamicLink {
  return if (uri != null) {
    when {
      uri.pathSegments.contains("referrals") -> DynamicLink.Referrals(
        code = uri.getQueryParameter("code") ?: "",
        // Fixme "10" should not be hard coded
        incentive = "10",
      )
      uri.pathSegments.contains("direct-debit") -> DynamicLink.DirectDebit
      uri.pathSegments.contains("forever") -> DynamicLink.Forever
      uri.pathSegments.contains("insurances") -> DynamicLink.Insurance
      uri.pathSegments.isEmpty() -> DynamicLink.None
      else -> DynamicLink.Unknown
    }
  } else {
    DynamicLink.None
  }
}

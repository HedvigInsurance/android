package com.hedvig.app.service

import android.content.Context
import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.hedvig.app.R
import io.reactivex.Single

class Referrals(val context: Context) {
    fun generateFirebaseLink(memberId: String, remoteConfigData: RemoteConfigData): Single<Uri> {
        return Single.create { subscriber ->
            FirebaseDynamicLinks
                .getInstance()
                .createDynamicLink()
                .setDomainUriPrefix(remoteConfigData.referralsDomain)
                .setLink(Uri.parse("https://www.hedvig.com/referrals?memberId=$memberId&incentive=${remoteConfigData.referralsIncentiveAmount}"))
                .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                .setIosParameters(DynamicLink.IosParameters.Builder(remoteConfigData.referralsIosBundleId).build())
                .setSocialMetaTagParameters(
                    DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(context.resources.getString(R.string.PROFILE_REFERRAL_LINK_SOCIAL_TITLE))
                        .setDescription(context.resources.getString(R.string.PROFILE_REFERRAL_LINK_SOCIAL_DESCRIPTION))
                        .setImageUrl(Uri.parse(context.resources.getString(R.string.PROFILE_REFERRAL_LINK_SOCIAL_IMAGE_URL)))
                        .build()
                )
                .buildShortDynamicLink()
                .addOnFailureListener { error ->
                    subscriber.onError(error)
                }
                .addOnSuccessListener { link ->
                    subscriber.onSuccess(link.shortLink)
                }
        }
    }
}


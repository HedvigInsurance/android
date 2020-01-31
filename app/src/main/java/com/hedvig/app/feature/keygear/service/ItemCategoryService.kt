package com.hedvig.app.feature.keygear.service

import android.content.Context
import android.net.Uri
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ItemCategoryService(
    private val context: Context
) {
    private val labeler = FirebaseVision
        .getInstance()
        .onDeviceImageLabeler

    suspend fun categorizeImage(uri: Uri): List<FirebaseVisionImageLabel> =
        withContext(Dispatchers.IO) {
            val image = FirebaseVisionImage.fromFilePath(context, uri)
            suspendCancellableCoroutine<List<FirebaseVisionImageLabel>> { cont ->
                labeler
                    .processImage(image)
                    .addOnSuccessListener { labels ->
                        cont.resume(labels)
                    }
                    .addOnFailureListener { e: Exception -> cont.resumeWithException(e) }
            }
        }
}

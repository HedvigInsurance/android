package com.hedvig.app.feature.keygear.service

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.ml.custom.FirebaseCustomLocalModel
import com.google.firebase.ml.custom.FirebaseModelDataType
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions
import com.google.firebase.ml.custom.FirebaseModelInterpreter
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemCategoryService(
    private val context: Context
) {
    private val labels =
        context.assets.open("labels_mobilenet_quant_v1_224.txt").bufferedReader().use { br ->
            br.lineSequence().toList()
        }
    private val localModel =
        FirebaseCustomLocalModel.Builder().setAssetFilePath("mobilenet_v1_1.0_224_quant.tflite")
            .build()

    private val interpreter =
        FirebaseModelInterpreter.getInstance(FirebaseModelInterpreterOptions.Builder(localModel).build())

    private val dataOptions = FirebaseModelInputOutputOptions.Builder()
        .setInputFormat(0, FirebaseModelDataType.BYTE, IntArray(0))

    // private val labeler = FirebaseVision
    //     .getInstance()
    //     .getOnDeviceImageLabeler(
    //         FirebaseVisionOnDeviceImageLabelerOptions.Builder(localModel)
    //             .setConfidenceThreshold(0f)
    //             .build()
    //     )

    // suspend fun categorizeImage(uri: Uri): List<FirebaseVisionImageLabel> =
    //     withContext(Dispatchers.IO) {
    //         val image = FirebaseVisionImage.fromFilePath(context, uri)
    //         suspendCancellableCoroutine<List<FirebaseVisionImageLabel>> { cont ->
    //             labeler
    //                 .processImage(image)
    //                 .addOnSuccessListener { labels ->
    //                     cont.resume(labels)
    //                 }
    //                 .addOnFailureListener { e: Exception -> cont.resumeWithException(e) }
    //         }
    //     }

    suspend fun categorizeImage(uri: Uri) = withContext(Dispatchers.IO) {
        val imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

    }
}

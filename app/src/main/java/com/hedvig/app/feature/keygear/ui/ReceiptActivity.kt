package com.hedvig.app.feature.keygear.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_receipt.*
import java.io.File
import java.io.FileOutputStream

class ReceiptActivity : BaseActivity(R.layout.activity_receipt) {

    private var tempPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val data = intent.getParcelableExtra<Receipt>("RECEIPT_DATA")

        val fileUrl = data.file.key

        Glide.with(this)
            .asBitmap()
            .load(fileUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    saveImage(resource, "receipt")
                }
            })

        close.setHapticClickListener {
            onBackPressed()
        }

        share.setHapticClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/jpeg"

                this.data = FileProvider.getUriForFile(
                    applicationContext,
                    getString(R.string.file_provider_authority),
                    File(tempPhotoPath)
                )

                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

                putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                        applicationContext,
                        getString(R.string.file_provider_authority),
                        File(tempPhotoPath)
                    )
                )
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }


        Glide.with(this)
            .load(fileUrl)
            .transform(CenterCrop())
            .into(receipt)
    }

    private fun saveImage(finalBitmap: Bitmap, image_name: String) {
        val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        val myDir = File(root)
        myDir.mkdirs()

        val fname = "Image-$image_name.jpg"
        val file = File(myDir, fname)

        tempPhotoPath = file.absolutePath

        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Parcelize
data class Receipt(
    val id: String,
    val file: S3File
) : Parcelable

@Parcelize
data class S3File(
    val bucket: String,
    val key: String
) : Parcelable

val mockReceipt = Receipt(
    "123",
    S3File(
        "123",
        "https://upload.wikimedia.org/wikipedia/commons/0/0b/ReceiptSwiss.jpg"
    )
)

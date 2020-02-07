package com.hedvig.app.feature.keygear.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.activity_receipt.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class ReceiptActivity : BaseActivity(R.layout.activity_receipt) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileUrl = intent.getStringExtra(RECEIPT_URL)

        close.setHapticClickListener {
            onBackPressed()
        }

        share.setHapticClickListener {

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
                        saveImage(resource)?.let { filePath ->

                            val sendIntent = Intent().apply {

                                action = Intent.ACTION_SEND
                                type = "image/jpg"

                                this.data = FileProvider.getUriForFile(
                                    applicationContext,
                                    getString(R.string.file_provider_authority),
                                    File(filePath)
                                )

                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                putExtra(
                                    Intent.EXTRA_STREAM,
                                    FileProvider.getUriForFile(
                                        applicationContext,
                                        getString(R.string.file_provider_authority),
                                        File(filePath)
                                    )
                                )
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        }
                    }
                })

        }
        Glide.with(this)
            .load(fileUrl)
            .transform(CenterCrop())
            .into(receipt)
    }

    private fun saveImage(finalBitmap: Bitmap): String? {
        val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        val myDir = File(root)
        myDir.mkdirs()

        val fName = "Image-receipt.jpg"
        val file = File(myDir, fName)

        if (file.exists()) file.delete()

        return try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            file.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error saving image")
            null
        }
    }

    companion object {
        private const val RECEIPT_URL = "RECEIPT_URL"

        fun newInstance(context: Context, receiptUrl: String) =
            Intent(context, ReceiptActivity::class.java).apply {
                putExtra(RECEIPT_URL, receiptUrl)
            }
    }
}
